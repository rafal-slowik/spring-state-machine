package com.slowikit.springstatemachine.services;

import com.slowikit.springstatemachine.domain.Payment;
import com.slowikit.springstatemachine.domain.PaymentEvent;
import com.slowikit.springstatemachine.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

/**
 * Created by rslowik on 05/05/2022.
 */
public interface PaymentService {
    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuthorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declinePaymentAuthorization(Long paymentId);
}
