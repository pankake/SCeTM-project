package com.tecnotab.mqtt.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrightRequest extends Request {
    private double luxLevel;
}
