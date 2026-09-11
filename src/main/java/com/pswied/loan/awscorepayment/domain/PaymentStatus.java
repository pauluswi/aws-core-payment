package com.pswied.loan.awscorepayment.domain;

public enum PaymentStatus {
    INITIATED,
    PENDING,
    AUTHORIZED,
    DECLINED,
    CAPTURED,
    SETTLED,
    REVERSED,
    FAILED
}
