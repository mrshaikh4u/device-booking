package com.rs4u.devicebooking.service;

import com.rs4u.devicebooking.client.FonoAPiClientImpl;
import com.rs4u.devicebooking.client.FonoApiClient;
import com.rs4u.devicebooking.errors.BookingNotFoundException;
import com.rs4u.devicebooking.errors.DeviceNotAvailableException;
import com.rs4u.devicebooking.errors.InvalidParamException;
import com.rs4u.devicebooking.pojos.Booking;
import com.rs4u.devicebooking.pojos.Device;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookingServiceImpl implements BookingService {

    private volatile static BookingServiceImpl instance;
    private final Map<String, Integer> availableDevices;
    private final Map<Integer, Booking> currentBookings;
    private final StampedLock lock;
    private final FonoApiClient fonoApiClient;

    private BookingServiceImpl() {
        currentBookings = new HashMap<>();
        lock = new StampedLock();
        fonoApiClient = new FonoAPiClientImpl();
        availableDevices = new HashMap<>() {{
            put("Samsung Galaxy S9", 1000);
            put("Samsung Galaxy S8", 2);
            put("Motorola Nexus 6", 1);
            put("Oneplus 9", 1);
            put("Apple iPhone 13", 1);
            put("Apple iPhone 12", 1);
            put("Apple iPhone 11", 1);
            put("iPhone X", 1);
            put("Nokia 3310", 1);
        }};
    }

    public static BookingServiceImpl getInstance() {
        if (instance == null) {
            synchronized (BookingServiceImpl.class) {
                if (instance == null) {
                    instance = new BookingServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public int reserveDevice(String deviceName, int count, String userName) {
        validateInput(deviceName, count, userName);
        long stamp = lock.writeLock();
        Booking booking;
        try {
            Integer currentAvailableCnt = availableDevices.entrySet().stream()
                    .filter(entry -> deviceAvailable(deviceName, count, entry))
                    .findFirst()
                    .orElseThrow(() -> new DeviceNotAvailableException("ERROR!! Device not available")).getValue();
            booking = prepareBookingInst(deviceName, count, userName);
            currentBookings.put(booking.getId(), booking);
            availableDevices.put(deviceName, currentAvailableCnt - count);
        } finally {
            lock.unlockWrite(stamp);
        }
        return booking.getId();
    }

    @Override
    public void releaseDevice(int bookingID) {
        long stamp = lock.writeLock();
        try {
            Optional
                    .ofNullable(currentBookings.get(bookingID))
                    .ifPresentOrElse(booking -> {
                        availableDevices.put(booking.getDeviceName(), availableDevices.get(booking.getDeviceName()) + booking.getCount());
                        currentBookings.remove(bookingID);
                    }, () -> {
                        throw new BookingNotFoundException("booking with ID : " + bookingID + " not found");
                    });
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public List<Device> retrieveDevices() {
        long stamp = lock.readLock();
        try {
            return availableDevices.entrySet().stream()
                    .map(deviceToPOJOMapper()
                    ).collect(Collectors.toList());
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public List<Booking> retrieveBookings() {
        long stamp = lock.readLock();
        try {
            return new ArrayList<>(currentBookings.values());
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Booking retrieveBookingByID(int bookingID) {
        long stamp = lock.readLock();
        try {
            return Optional.ofNullable(currentBookings.get(bookingID))
                    .orElseThrow(() -> new BookingNotFoundException("booking with ID : " + bookingID + " not found"));
        } finally {
            lock.unlockRead(stamp);
        }
    }

    private void validateInput(String deviceName, int count, String userName) {
        if (count < 1)
            throw new InvalidParamException("Requested count can't be less than 1");
        if (StringUtils.isBlank(userName))
            throw new InvalidParamException("userName is required");
        if (StringUtils.isBlank(deviceName))
            throw new InvalidParamException("deviceName is required");
        if (!availableDevices.containsKey(deviceName)) {
            throw new DeviceNotAvailableException("Device " + deviceName + " not found");
        }
    }

    private boolean deviceAvailable(String deviceName, int count, Map.Entry<String, Integer> entry) {
        return entry.getKey().equals(deviceName) && entry.getValue() >= count;
    }

    private Booking prepareBookingInst(String deviceName, int count, String userName) {
        Booking booking = Booking.builder()
                .deviceName(deviceName)
                .count(count)
                .owner(userName)
                .bookingTime(LocalDateTime.now())
                .build();
        booking.setId(booking.hashCode());
        return booking;
    }

    private Function<Map.Entry<String, Integer>, Device> deviceToPOJOMapper() {
        return device ->
                Device.builder()
                        .name(device.getKey())
                        .availableInStock(device.getValue() > 0)
                        .techSpecs(fonoApiClient.fetchTechSpecs(device.getKey()))
                        .build();
    }

}
