package com.backend.app.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsResource {
    private double min;
    private double max;
    private double average;
    private double standardDeviation;
}
