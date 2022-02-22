package com.rs4u.devicebooking.pojos;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Booking {
    @EqualsAndHashCode.Exclude
    @Setter
    private int id;
    private String deviceName;
    private int count;
    private String owner;
    private LocalDateTime bookingTime;
}
