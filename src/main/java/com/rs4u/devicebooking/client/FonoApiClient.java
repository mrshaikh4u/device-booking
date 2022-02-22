package com.rs4u.devicebooking.client;

import java.util.Map;

public interface FonoApiClient {
    Map<String,String> fetchTechSpecs(String deviceName);
}
