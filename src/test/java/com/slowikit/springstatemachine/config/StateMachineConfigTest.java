package com.slowikit.springstatemachine.config;

import com.slowikit.springstatemachine.domain.PaymentEvent;
import com.slowikit.springstatemachine.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Created by rslowik on 05/05/2022.
 */
@Slf4j
@SpringBootTest
class StateMachineConfigTest {

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Test
    void testNewSTateMachine() {
        StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());
        sm.startReactively().block();
        log.info(sm.getState().toString());
        sm.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTHORIZE).build())).blockLast();
        log.info(sm.getState().toString());
        sm.sendEvent(Mono.just(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED).build())).blockLast();
        log.info(sm.getState().toString());
        sm.stopReactively().block();
    }
}