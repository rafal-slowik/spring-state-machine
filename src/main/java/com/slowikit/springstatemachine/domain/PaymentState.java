package com.slowikit.springstatemachine.domain;

/**
 * Created by rslowik on 05/05/2022.
 */
public enum PaymentState {
    NEW,
    PRE_AUTH,
    PRE_AUTH_ERROR,
    AUTH,
    AUTH_ERROR
}
