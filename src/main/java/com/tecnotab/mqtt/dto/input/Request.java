package com.tecnotab.mqtt.dto.input;

import lombok.Data;

@Data
public class Request {
    private String uid;
    private double lat;
    private double lng;
    private String city;
    private int reliability;
    private int relevance;
}
