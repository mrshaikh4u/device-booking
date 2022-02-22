package com.rs4u.devicebooking.pojos;

import lombok.*;

import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Device {
    private String name;
    private boolean availableInStock;
    private Map<String,String> techSpecs;
}
