package com.slowikit.springstatemachine.services;

import com.slowikit.springstatemachine.domain.PaymentEvent;
import com.slowikit.springstatemachine.domain.PaymentState;
import com.slowikit.springstatemachine.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.slowikit.springstatemachine.services.PaymentServiceImpl.PAYMENT_ID_HEADER;

/**
 * Created by rslowik on 05/05/2022.
 */
@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message, Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine, StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        Optional.ofNullable(message)
                .map(Message::getHeaders)
                .map(headers -> headers.get(PAYMENT_ID_HEADER, Long.class))
                .flatMap(paymentRepository::findById)
                .ifPresent(payment -> {
                    payment.setPaymentState(state.getId());
                    paymentRepository.save(payment);
                });
    }
}
