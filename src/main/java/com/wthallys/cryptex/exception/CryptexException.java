package com.wthallys.cryptex.exception;

public class CryptexException extends Exception {

    public CryptexException(String message) {
        super(message);
    }

    public CryptexException(String message, Throwable cause) {
        super(message, cause);
    }
}
