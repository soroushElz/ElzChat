package com.example.ChatApplication.Exception;

public class IsSameUserException extends RuntimeException {

    public  IsSameUserException(String errorMessage){
        super(errorMessage);
    }

}
