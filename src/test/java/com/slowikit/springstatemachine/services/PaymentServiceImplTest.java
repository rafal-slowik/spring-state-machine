package com.slowikit.springstatemachine.services;

import com.slowikit.springstatemachine.domain.Payment;
import com.slowikit.springstatemachine.domain.PaymentEvent;
import com.slowikit.springstatemachine.domain.PaymentState;
import com.slowikit.springstatemachine.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by rslowik on 05/05/2022.
 */
@Slf4j
@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("16.99")).build();
    }

    @Test
    void newPayment() {
    }

    @Test
    void preAuthorizePayment() {
        Payment savedPayment = paymentService.newPayment(payment);
        log.info("New Payment created: {}", savedPayment);

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuthorizePayment(savedPayment.getId());

        Optional<Payment> paymentToVerifyOptional = paymentRepository.findById(savedPayment.getId());

        Payment paymentToVerify = paymentToVerifyOptional.orElse(null);
        assertNotNull(paymentToVerify);

        assertTrue(
                EnumSet.of(PaymentState.PRE_AUTH, PaymentState.PRE_AUTH)
                        .contains(paymentToVerify.getPaymentState())
        );
    }

    @RepeatedTest(10)
    void authorizePayment() {
        payment.setPaymentState(PaymentState.PRE_AUTH);
        paymentRepository.save(payment);

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.authorizePayment(payment.getId());

        Optional<Payment> paymentToVerifyOptional = paymentRepository.findById(payment.getId());
        Payment paymentToVerify = paymentToVerifyOptional.orElse(null);
        assertNotNull(paymentToVerify);

        assertTrue(
                EnumSet.of(PaymentState.AUTH, PaymentState.AUTH)
                        .contains(paymentToVerify.getPaymentState()));
    }

    @Test
    void declinePaymentAuthorization() {
    }
}
