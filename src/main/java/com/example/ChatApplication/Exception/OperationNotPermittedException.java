package com.example.ChatApplication.Exception;

public class OperationNotPermittedException extends RuntimeException {

    public OperationNotPermittedException(String message){
        super(message);
    }
}
