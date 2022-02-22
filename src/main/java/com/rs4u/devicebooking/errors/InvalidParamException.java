package com.rs4u.devicebooking.errors;

public class InvalidParamException extends RuntimeException{
    public InvalidParamException(String errorMessage){
        super(errorMessage);
    }
}
