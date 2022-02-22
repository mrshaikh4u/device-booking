package com.rs4u.devicebooking.errors;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String errorMessage){
        super(errorMessage);
    }
}
