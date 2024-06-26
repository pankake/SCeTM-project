package com.backend.app.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoundRequest extends Request {

    @NotNull(message = "decibelLevel cannot be null")
    @NotBlank(message = "decibelLevel cannot be empty")
    private double decibelLevel;
}
