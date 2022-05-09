package com.slowikit.springstatemachine.config;

import com.slowikit.springstatemachine.domain.PaymentEvent;
import com.slowikit.springstatemachine.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.Set;

import static com.slowikit.springstatemachine.domain.PaymentEvent.PRE_AUTH_APPROVED;
import static com.slowikit.springstatemachine.domain.PaymentEvent.PRE_AUTH_DECLINED;
import static com.slowikit.springstatemachine.services.PaymentServiceImpl.PAYMENT_ID_HEADER;

/**
 * Created by rslowik on 05/05/2022.
 */
@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(Set.of(PaymentState.values()))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
        super.configure(states);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                .action(preAuthAction())
                .guard(paymentIdGuard())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PRE_AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE)
                .action(authAction())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED);
    }

    private Action<PaymentState, PaymentEvent> authAction() {
        return context -> {
            log.info("Auth was called!");
            if (new Random().nextInt(10) > 2) {
                log.info("Successfully Authorized!");
                context.getStateMachine()
                        .sendEvent(
                                Mono.just(
                                        MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                                                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                                                .build()
                                )
                        ).blockLast();
            } else {
                log.info("Authorization Declined!!!");
                context.getStateMachine()
                        .sendEvent(
                                Mono.just(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                                        .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                                        .build())
                        ).blockLast();
            }
        };
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("StateChanged from: {} to {}", from, to);
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Guard<PaymentState, PaymentEvent> paymentIdGuard() {
        return context -> context.getMessageHeader(PAYMENT_ID_HEADER) != null;
    }

    public Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            log.info("PreAuth was called!");
            if (new Random().nextInt(10) > 2) {
                log.info("Successfully PreAuthorized!");
                context.getStateMachine().sendEvent(
                        Mono.just(
                                MessageBuilder.withPayload(PRE_AUTH_APPROVED)
                                        .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER)).build()
                        )
                ).blockLast();
            } else {
                log.info("PreAuthorization Declined!!!");
                context.getStateMachine().sendEvent(
                        Mono.just(
                                MessageBuilder.withPayload(PRE_AUTH_DECLINED)
                                        .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER)).build()
                        )
                ).blockLast();
            }
        };
    }
}
