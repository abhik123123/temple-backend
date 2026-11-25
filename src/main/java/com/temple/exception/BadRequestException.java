package com.temple.exception;

/** A request that is well-formed but violates a business rule. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
