package com.rs4u.devicebooking.service;

import com.rs4u.devicebooking.pojos.Booking;
import com.rs4u.devicebooking.pojos.Device;

import java.util.List;

public interface BookingService {
    int reserveDevice(String deviceName, int count, String userName);
    void releaseDevice(int bookingID);
    List<Device> retrieveDevices();
    List<Booking> retrieveBookings();
    Booking retrieveBookingByID(int bookingID);
}
