package com.backend.app.dto.resource;

import com.backend.app.dto.resource.alerts.SoundAlerts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoundMapResource {
    private String url;
    private List<SoundAlerts> alerts;
}
