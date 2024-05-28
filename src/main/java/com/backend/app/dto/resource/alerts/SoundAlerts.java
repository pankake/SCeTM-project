package com.backend.app.dto.resource.alerts;

import com.google.maps.model.LatLng;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class SoundAlerts {
    private String uid;
    private String alert;
    private LatLng location;
    private double decibel;
    private int reliability;
    private int relevance;


    @Getter
    public enum AlertType {
        LOW_NOISE_AREA("Zona poco rumorosa"),
        MODERATELY_NOISY_AREA("Zona moderatamente rumorosa"),
        VERY_NOISY_AREA("Zona molto rumorosa");

        private final String message;

        AlertType(String message) {
            this.message = message;
        }

    }
}
