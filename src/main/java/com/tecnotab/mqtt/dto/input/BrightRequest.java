package com.tecnotab.mqtt.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrightRequest extends Request {

    @NotNull(message = "luxLevel cannot be null")
    @NotBlank(message = "luxLevel cannot be empty")
    private double luxLevel;
}
