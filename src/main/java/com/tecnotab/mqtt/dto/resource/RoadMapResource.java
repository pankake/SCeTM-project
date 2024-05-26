package com.tecnotab.mqtt.dto.resource;

import com.tecnotab.mqtt.dto.resource.alerts.RoadAlerts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadMapResource {
    private List<String> urls;
    private List<RoadAlerts> alerts;
}
