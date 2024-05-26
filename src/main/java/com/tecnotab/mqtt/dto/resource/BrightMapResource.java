package com.tecnotab.mqtt.dto.resource;

import com.tecnotab.mqtt.dto.resource.alerts.BrightAlerts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrightMapResource {
    private String url;
    private List<BrightAlerts> alerts;
}
