package com.tsystem.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("Forbidden");
    }

    public ForbiddenException(String m) {
        super(m);
    }
}

