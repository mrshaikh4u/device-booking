package com.rs4u.devicebooking.errors;

public class DeviceNotAvailableException extends RuntimeException{
    public DeviceNotAvailableException(String errorMessage){
        super(errorMessage);
    }
}
