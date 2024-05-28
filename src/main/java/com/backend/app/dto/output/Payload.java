package com.backend.app.dto.output;

public interface Payload {
    double getLat();
    double getLng();
    String getCity();
    String getUid();
    int getReliability();
    int getRelevance();
}
