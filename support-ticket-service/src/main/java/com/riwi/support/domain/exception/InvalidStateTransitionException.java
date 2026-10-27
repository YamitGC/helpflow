package com.riwi.support.domain.exception;

public class InvalidStateTransitionException extends BusinessRuleException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}

