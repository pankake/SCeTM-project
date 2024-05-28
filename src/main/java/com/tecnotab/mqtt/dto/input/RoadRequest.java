package com.tecnotab.mqtt.dto.input;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RoadRequest extends Request {

	@NotNull(message = "accelerationX cannot be null")
	@NotBlank(message = "accelerationX cannot be empty")
	private double accelerationX;

	@NotNull(message = "accelerationY cannot be null")
	@NotBlank(message = "accelerationY cannot be empty")
	private double accelerationY;

	@NotNull(message = "accelerationZ cannot be null")
	@NotBlank(message = "accelerationZ cannot be empty")
	private double accelerationZ;
}
