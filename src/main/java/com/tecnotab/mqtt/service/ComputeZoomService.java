package com.tecnotab.mqtt.service;

import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComputeZoomService {

    public int getDynamicZoom(List<LatLng> coordinates, double avgLat, double avgLng) {
        // Calcola il bounding box per determinare lo zoom dinamico
        double maxLat = coordinates.stream().mapToDouble(coord -> coord.lat).max().orElse(avgLat);
        double minLat = coordinates.stream().mapToDouble(coord -> coord.lat).min().orElse(avgLat);
        double maxLng = coordinates.stream().mapToDouble(coord -> coord.lng).max().orElse(avgLng);
        double minLng = coordinates.stream().mapToDouble(coord -> coord.lng).min().orElse(avgLng);

        double latDiff = maxLat - minLat;
        double lngDiff = maxLng - minLng;

        // Determina lo zoom basato sulla differenza di latitudine e longitudine
        int zoom = calculateZoom(latDiff, lngDiff);
        return zoom;
    }

    private int calculateZoom(double latDiff, double lngDiff) {
        double maxDiff = Math.max(latDiff, lngDiff);
        if (maxDiff < 0.01) {
            return 15;
        } else if (maxDiff < 0.05) {
            return 14;
        } else if (maxDiff < 0.1) {
            return 13;
        } else if (maxDiff < 0.5) {
            return 11;
        } else if (maxDiff < 1) {
            return 10;
        } else if (maxDiff < 5) {
            return 8;
        } else if (maxDiff < 10) {
            return 7;
        } else if (maxDiff < 50) {
            return 5;
        } else {
            return 3;
        }
    }
}
