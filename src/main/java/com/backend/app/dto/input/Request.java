package com.backend.app.dto.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Request {

    @NotNull(message = "uid cannot be null")
    @NotBlank(message = "uid cannot be empty")
    private String uid;

    @NotNull(message = "lat cannot be null")
    @NotBlank(message = "lat cannot be empty")
    private double lat;

    @NotNull(message = "lng cannot be null")
    @NotBlank(message = "lng cannot be empty")
    private double lng;

    @NotNull(message = "city cannot be null")
    @NotBlank(message = "city cannot be empty")
    private String city;

    @NotNull(message = "reliability cannot be null")
    @NotBlank(message = "reliability cannot be empty")
    private int reliability;

    @NotNull(message = "relevance cannot be null")
    @NotBlank(message = "relevance cannot be empty")
    private int relevance;
}
