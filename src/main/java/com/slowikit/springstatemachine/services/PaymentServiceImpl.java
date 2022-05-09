package com.slowikit.springstatemachine.services;

import com.slowikit.springstatemachine.domain.Payment;
import com.slowikit.springstatemachine.domain.PaymentEvent;
import com.slowikit.springstatemachine.domain.PaymentState;
import com.slowikit.springstatemachine.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import static com.slowikit.springstatemachine.domain.PaymentEvent.*;

/**
 * Created by rslowik on 05/05/2022.
 */
@Transactional
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        sendEvent(paymentId, sm, PRE_AUTHORIZE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        sendEvent(paymentId, sm, AUTHORIZE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declinePaymentAuthorization(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildStateMachine(paymentId);
        sendEvent(paymentId, sm, PRE_AUTH_DECLINED);
        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {
        Message<PaymentEvent> message = MessageBuilder.withPayload(event).setHeader(PAYMENT_ID_HEADER, paymentId).build();
        sm.sendEvent(Mono.just(message)).blockLast();
    }

    private StateMachine<PaymentState, PaymentEvent> buildStateMachine(Long id) {
        Payment payment = paymentRepository.getById(id);
        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(Long.toString(payment.getId()));
        sm.stopReactively().block();
        sm.getStateMachineAccessor().doWithAllRegions(
                sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(
                                    payment.getPaymentState(), null, null, null
                            )
                    ).block();
                }
        );
        sm.startReactively().block();
        return sm;
    }
}
