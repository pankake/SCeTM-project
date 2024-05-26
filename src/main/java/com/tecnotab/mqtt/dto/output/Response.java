package com.tecnotab.mqtt.dto.output;

public interface Response {
    long getSampleTime();
    Payload getPayload();
}
