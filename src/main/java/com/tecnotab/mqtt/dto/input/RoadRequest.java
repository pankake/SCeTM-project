package com.tecnotab.mqtt.dto.input;


import lombok.Data;

@Data
public class RoadRequest extends Request {
	private double accelerationX;
	private double accelerationY;
	private double accelerationZ;
}
