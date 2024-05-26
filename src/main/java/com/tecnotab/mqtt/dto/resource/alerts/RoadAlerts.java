package com.tecnotab.mqtt.dto.resource.alerts;

import com.google.maps.model.LatLng;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class RoadAlerts {
    private String uid;
    private String alert;
    private LatLng location;
    private double magnitude;
    private int reliability;
    private int relevance;

    @Getter
    public enum AlertType {
        NOT_DAMAGED("Strada in buono stato"),
        PARTIALLY_DAMAGED("Strada parzialmente danneggiata"),
        HIGHLY_DAMAGED("Strada molto danneggiata");

        private final String message;

        AlertType(String message) {
            this.message = message;
        }

    }
}
