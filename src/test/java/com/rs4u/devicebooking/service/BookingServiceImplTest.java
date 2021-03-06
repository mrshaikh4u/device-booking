package com.rs4u.devicebooking.service;

import com.rs4u.devicebooking.errors.BookingNotFoundException;
import com.rs4u.devicebooking.errors.DeviceNotAvailableException;
import com.rs4u.devicebooking.errors.InvalidParamException;
import com.rs4u.devicebooking.pojos.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceImplTest {

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        this.bookingService = BookingServiceImpl.getInstance();
        resetBookings();
    }

    private void resetBookings() {
        this.bookingService.retrieveBookings().
                forEach(booking -> this.bookingService.releaseDevice(booking.getId()));
    }

    @Test
    @DisplayName("Reserve a device and check the available count for the same , available count should be zero")
    void test1() {
        String deviceName = "Oneplus 9";
        bookingService.reserveDevice(deviceName, 1, "tom");
        Optional<Device> deviceOptional = bookingService.retrieveDevices().stream()
                .filter(device -> device.getName().equals(deviceName))
                .findFirst();
        if (deviceOptional.isPresent()) {
            assertFalse(deviceOptional.get().isAvailableInStock());
        } else {
            fail();
        }
    }

    @Test
    @DisplayName("Reserve a device and check the available count for the same , available count should be one")
    void test2() {
        String deviceName = "Samsung Galaxy S8";
        bookingService.reserveDevice(deviceName, 1, "tom");
        Optional<Device> deviceOptional = bookingService.retrieveDevices().stream()
                .filter(device -> device.getName().equals(deviceName))
                .findFirst();
        if (deviceOptional.isPresent()) {
            assertTrue(deviceOptional.get().isAvailableInStock());
        } else {
            fail();
        }
    }

    @Test
    @DisplayName("Reserve a device " +
            "make sure it goes out of stock" +
            "release it" +
            "check again and to sure it is available in stock again"
    )
    void test3() {
        String deviceName = "Apple iPhone 13";
        int bookingID = bookingService.reserveDevice(deviceName, 1, "tom");
        assertFalse(bookingService.retrieveDevices().stream()
                .filter(device -> device.getName().equals(deviceName))
                .findFirst().get().isAvailableInStock());
        bookingService.releaseDevice(bookingID);
        assertTrue(bookingService.retrieveDevices().stream()
                .filter(device -> device.getName().equals(deviceName))
                .findFirst().get().isAvailableInStock());
    }

    @Test
    @DisplayName("Try to reserve more count than available should throw DeviceNotAvailable Exception")
    void test4() {
        assertThrows(DeviceNotAvailableException.class, () -> bookingService.reserveDevice("Apple iPhone 13", 2, "tom"));
    }

    @Test
    @DisplayName("pass wrong booking id to release booking should throw BookingNotFound Exception")
    void test5() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.releaseDevice(101));
    }

    @Test
    @DisplayName("reserve two devices and check current bookings should return 2 bookings")
    void test6() {
        bookingService.reserveDevice("Apple iPhone 13", 1, "tom");
        bookingService.reserveDevice("Samsung Galaxy S8", 1, "harry");
        assertEquals(2, bookingService.retrieveBookings().size());
    }

    @Test
    @DisplayName("try to place reserve request for unknown device should throw DeviceNotAvailableException")
    void test7() {
        assertThrows(DeviceNotAvailableException.class, () -> bookingService.reserveDevice("abc", 1, "tom"));
    }

    @Test
    @DisplayName("check owner is populated correctly")
    void test8() {
        String userName = "tom";
        int bookingID = bookingService.reserveDevice("Apple iPhone 13", 1, userName);
        assertEquals(userName, bookingService.retrieveBookingByID(bookingID).getOwner());
    }

    @Test
    @DisplayName("check booked date is populated correctly")
    void test9() {
        String userName = "tom";
        int bookingID = bookingService.reserveDevice("Apple iPhone 13", 1, userName);
        assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), bookingService.retrieveBookingByID(bookingID).getBookingTime().truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("Book a device by a user and another user request same device should throw DeviceNotFoundError")
    void test10() {
        String userName1 = "tom";
        String userName2 = "hary";
        String deviceName = "Apple iPhone 13";
        bookingService.reserveDevice(deviceName, 1, userName1);
        assertThrows(DeviceNotAvailableException.class, () -> bookingService.reserveDevice(deviceName, 1, userName2));
    }

    @Test
    @DisplayName("Concurrently running 10 threads each booking different device should run smoothly")
    void test11() {
        int numberOfThreads = 1000;
        Runnable runnableOperation = this::performOperations;
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(runnableOperation).start();
        }
    }

    @Test
    @DisplayName("null device Name should throw InvalidParam exception")
    void test12() {
        assertThrows(InvalidParamException.class, () -> bookingService.reserveDevice(null, 1, "tom"));
    }

    @Test
    @DisplayName("invalid count should throw InvalidParam exception")
    void test13() {
        assertThrows(InvalidParamException.class, () -> bookingService.reserveDevice("abc", 0, "tom"));
    }

    @Test
    @DisplayName("invalid user name should throw InvalidParam exception")
    void test14() {
        assertThrows(InvalidParamException.class, () -> bookingService.reserveDevice("abc", 1, ""));
    }
    private void performOperations() {
        for (int i = 0; i < 10; i++) {
            bookingService.retrieveBookings();
        }
        bookingService.reserveDevice("Samsung Galaxy S9", 1, "tom");
    }




}
