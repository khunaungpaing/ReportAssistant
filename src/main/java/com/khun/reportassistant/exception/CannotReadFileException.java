package com.khun.reportassistant.exception;

import java.io.IOException;

public class CannotReadFileException extends IOException {
    public CannotReadFileException(String message) {
        super(message);
    }
}
