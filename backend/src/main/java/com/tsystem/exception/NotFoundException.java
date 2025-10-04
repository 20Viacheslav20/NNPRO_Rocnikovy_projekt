package com.tsystem.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Not found");
    }

    public NotFoundException(String m) {
        super(m);
    }
}
