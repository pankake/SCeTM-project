package com.backend.app.dto.resource.alerts;

import com.google.maps.model.LatLng;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class BrightAlerts {
    private String uid;
    private String alert;
    private LatLng location;
    private double lux;
    private int reliability;
    private int relevance;


    @Getter
    public enum AlertType {
        DIMLY_LIT_AREA("Zona poco luminosa"),
        MODERATELY_BRIGHT_AREA("Zona moderatamente luminosa"),
        VERY_BRIGHT_AREA("Zona molto luminosa");

        private final String message;

        AlertType(String message) {
            this.message = message;
        }

    }
}
