package com.swifttrack.ProviderService.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class GetUUID {
    public String getUUID() {
        return UUID.randomUUID().toString();
    }
}
