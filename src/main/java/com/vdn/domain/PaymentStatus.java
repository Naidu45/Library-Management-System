package com.vdn.domain;

public enum PaymentStatus {

    PENDING,

    //Payment was successfully processed
    SUCCESS,

    //Payment failed due to insufficient funds, card decline, etc.
    FAILED,

    //Payment was cancelled by user
    CANCELLED,

    //Payment was refunded
    REFUND,

    PROCESSING,
}
