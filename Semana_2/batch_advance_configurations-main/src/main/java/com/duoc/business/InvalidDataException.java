package com.duoc.business;

public class InvalidDataException extends Exception { // Extiende Exception para una excepción verificada

    public InvalidDataException(String message) {
        super(message);
    }
    
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}