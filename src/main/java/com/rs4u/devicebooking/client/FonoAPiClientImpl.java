package com.rs4u.devicebooking.client;

import java.util.Map;

public class FonoAPiClientImpl implements FonoApiClient{
    @Override
    public Map<String, String> fetchTechSpecs(String deviceName) {
        return Map.of(
                "technology", "GSM / CDMA / HSPA / EVDO / LTE",
                "_2g_bands", "GSM 850 / 900 / 1800 / 1900",
                "_3g_bands", "HSDPA 850 / 900 / 1700(AWS) / 1900 / 2100",
                "_4g_bands", "1, 2, 3, 4, 5, 7, 8, 12, 13, 14, 17, 18, 19, 20, 25, 26, 28, 29, 30, 32, 34, 38, 39, 40, 41, 46, 66 - A2097"
        );
    }
}
