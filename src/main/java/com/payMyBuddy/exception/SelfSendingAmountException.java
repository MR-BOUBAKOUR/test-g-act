package com.payMyBuddy.exception;

public class SelfSendingAmountException extends RuntimeException {
    public SelfSendingAmountException(String message) {
        super(message);
    }
}