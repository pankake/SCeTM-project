package com.backend.app.service;

import com.backend.app.dto.output.BrightResponse;
import com.backend.app.dto.resource.BrightMapResource;
import com.backend.app.dto.resource.StatsResource;
import com.backend.app.dto.resource.alerts.BrightAlerts;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.backend.app.dto.resource.alerts.BrightAlerts.AlertType.*;
import static com.backend.app.util.MapUtils.*;

@Service
public class BrightDataService {
    public final static double RADIUS_SCALE_FACTOR = 0.15;

    public BrightMapResource colorBrightNearDataPoints(List<BrightResponse> data) throws ApiException {
        if (data == null || data.isEmpty()) {
            throw new InvalidRequestException("Nessun dato fornito");
        }

        BrightMapResource brightMapResource = new BrightMapResource();
        List<BrightAlerts> alerts = new ArrayList<>();
        brightMapResource.setAlerts(alerts);

        StringBuilder path = new StringBuilder();

        for (BrightResponse point : data) {
            LatLng coord = point.getPayload().toLatLng();
            double radius = determineRadiusAndAlertBasedOnLuxLevel(point.getPayload().getLux(),
                    brightMapResource, point);

            String color = COLOR_GOLD;
            
            path.append("&path=fillcolor:0x").append(color).append("33|color:0x").append(color).append("FF|weight:1");

            List<LatLng> circlePoints = generateCircleAroundPoint(coord, radius);
            for (LatLng circlePoint : circlePoints) {
                path.append("|").append(circlePoint.lat).append(",").append(circlePoint.lng);
            }
        }

        // calcola il centro della mappa come la media tra le latitudini e long. dei punti
        double avgLat = 0;
        double avgLng = 0;
        for (BrightResponse point : data) {
            avgLat += point.getPayload().getLat();
            avgLng += point.getPayload().getLng();
        }
        avgLat /= data.size();
        avgLng /= data.size();
        LatLng center = new LatLng(avgLat, avgLng);

        // genera l'url della mappa statica
        String mapUrl = STATIC_MAP_URL+ "?center=" +
                center.lat + "," + center.lng +
                "&zoom=14" +
                "&size=" + RESOLUTION +
                "&scale=2" +
                path +
                "&key=" + GOOGLE_API_KEY;

        brightMapResource.setUrl(mapUrl);

        return brightMapResource;
    }

    private List<LatLng> generateCircleAroundPoint(LatLng center, double radius) {
        // genera una lista di punti per creare un cerchio attorno al punto centrale
        List<LatLng> points = new ArrayList<>();
        int numPoints = 18; // numero di punti usati per generare il cerchio

        for (int i = 0; i < numPoints; i++) {
            double angle = Math.toRadians((360.0 / numPoints) * i);
            double latOffset = radius * Math.cos(angle) / 111320;
            double lngOffset = radius * Math.sin(angle) / (111320 * Math.cos(Math.toRadians(center.lat)));

            points.add(new LatLng(center.lat + latOffset, center.lng + lngOffset));
        }

        return points;
    }

    private double determineRadiusAndAlertBasedOnLuxLevel(double luxLevel,
                                                          BrightMapResource brightMapResource,
                                                          BrightResponse point) {

        if(luxLevel < 100)
            brightMapResource.getAlerts().add(new BrightAlerts(point.getPayload().getUid(), DIMLY_LIT_AREA.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), luxLevel, point.getPayload().getReliability(), point.getPayload().getRelevance()));
        else if(luxLevel < 500)
            brightMapResource.getAlerts().add(new BrightAlerts(point.getPayload().getUid(), MODERATELY_BRIGHT_AREA.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), luxLevel, point.getPayload().getReliability(), point.getPayload().getRelevance()));
        else
            brightMapResource.getAlerts().add(new BrightAlerts(point.getPayload().getUid(), VERY_BRIGHT_AREA.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), luxLevel, point.getPayload().getReliability(), point.getPayload().getRelevance()));

        return luxLevel < 100 ? luxLevel * RADIUS_SCALE_FACTOR * 2 : luxLevel * RADIUS_SCALE_FACTOR;
    }

    public List<BrightResponse> findMinMaxBrightResponses(List<BrightResponse> brightResponses) {
        if (brightResponses == null || brightResponses.isEmpty()) {
            return new ArrayList<>();
        }

        BrightResponse minBrightResponse = brightResponses.get(0);
        BrightResponse maxBrightResponse = brightResponses.get(0);

        for (BrightResponse response : brightResponses) {
            if (response.getPayload().getLux() < minBrightResponse.getPayload().getLux()) {
                minBrightResponse = response;
            }
            if (response.getPayload().getLux() > maxBrightResponse.getPayload().getLux()) {
                maxBrightResponse = response;
            }
        }

        List<BrightResponse> result = new ArrayList<>();
        result.add(minBrightResponse);
        result.add(maxBrightResponse);

        return result;
    }

    public StatsResource computeStatistics(List<BrightResponse> brightResponses) {
        StatsResource statistics = new StatsResource();

        if (brightResponses.isEmpty()) {
            return statistics;
        }

        double min = brightResponses.stream()
                .mapToDouble(response -> response.getPayload().getLux())
                .min()
                .orElse(Double.NaN);

        double max = brightResponses.stream()
                .mapToDouble(response -> response.getPayload().getLux())
                .max()
                .orElse(Double.NaN);

        double average = brightResponses.stream()
                .mapToDouble(response -> response.getPayload().getLux())
                .average()
                .orElse(Double.NaN);

        double roundedAvg = Math.round(average * 10.0) / 10.0;

        double variance = brightResponses.stream()
                .mapToDouble(response -> Math.pow(response.getPayload().getLux() - average, 2))
                .average()
                .orElse(Double.NaN);

        double standardDeviation = Math.sqrt(variance);
        double roundedStandardDeviation = Math.round(standardDeviation * 10.0) / 10.0;

        statistics.setMin(min);
        statistics.setMax(max);
        statistics.setAverage(roundedAvg);
        statistics.setStandardDeviation(roundedStandardDeviation);

        return statistics;
    }
}
