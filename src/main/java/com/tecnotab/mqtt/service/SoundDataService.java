package com.tecnotab.mqtt.service;

import com.google.maps.errors.ApiException;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.LatLng;
import com.tecnotab.mqtt.dto.output.SoundResponse;
import com.tecnotab.mqtt.dto.resource.SoundMapResource;
import com.tecnotab.mqtt.dto.resource.StatsResource;
import com.tecnotab.mqtt.dto.resource.alerts.SoundAlerts;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.tecnotab.mqtt.dto.resource.alerts.SoundAlerts.AlertType.*;
import static com.tecnotab.mqtt.util.MapUtils.*;

@Service
public class SoundDataService {

    public final static double RADIUS_SCALE_FACTOR = 1.6;

    public SoundMapResource colorSoundNearDataPoints(List<SoundResponse> data) throws ApiException, InterruptedException, IOException {
        if (data == null || data.isEmpty()) {
            throw new InvalidRequestException("Nessun dato fornito");
        }

        SoundMapResource soundMapResource = new SoundMapResource();
        List<SoundAlerts> alerts = new ArrayList<>();
        soundMapResource.setAlerts(alerts);

        StringBuilder path = new StringBuilder();

        for (SoundResponse point : data) {
            LatLng coord = point.getPayload().toLatLng();
            double radius = determineRadiusAndAlertBasedOnDecibelLevel(point.getPayload().getDecibelLevel(),
                    soundMapResource, point);

            // Define the color for the circle
            String color = COLOR_PURPLE; // Hex color code for purple

            // Generate a single filled circle
            path.append("&path=fillcolor:0x").append(color).append("33|color:0x").append(color).append("FF|weight:1");

            List<LatLng> circlePoints = generateCircleAroundPoint(coord, radius);
            for (LatLng circlePoint : circlePoints) {
                path.append("|").append(circlePoint.lat).append(",").append(circlePoint.lng);
            }
        }

        // Calculate the center of the data points
        double avgLat = 0;
        double avgLng = 0;
        for (SoundResponse point : data) {
            avgLat += point.getPayload().getLat();
            avgLng += point.getPayload().getLng();
        }
        avgLat /= data.size();
        avgLng /= data.size();
        LatLng center = new LatLng(avgLat, avgLng);

        // Generate the static map URL
        String mapUrl = STATIC_MAP_URL+ "?center=" +
                center.lat + "," + center.lng +
                "&zoom=14" +
                "&size=" + RESOLUTION +
                "&scale=2" +
                path +
                "&key=" + GOOGLE_API_KEY;

        soundMapResource.setUrl(mapUrl);

        return soundMapResource;
    }

    private List<LatLng> generateCircleAroundPoint(LatLng center, double radius) {
        // Generate a list of LatLng points forming a circle around the center point
        List<LatLng> points = new ArrayList<>();
        int numPoints = 18; // Number of points to generate for the circle

        for (int i = 0; i < numPoints; i++) {
            double angle = Math.toRadians((360.0 / numPoints) * i);
            double latOffset = radius * Math.cos(angle) / 111320; // Approx conversion to degrees latitude
            double lngOffset = radius * Math.sin(angle) / (111320 * Math.cos(Math.toRadians(center.lat))); // Approx conversion to degrees longitude

            points.add(new LatLng(center.lat + latOffset, center.lng + lngOffset));
        }

        return points;
    }

    private double determineRadiusAndAlertBasedOnDecibelLevel(double decibelLevel,
                                                              SoundMapResource soundMapResource,
                                                              SoundResponse point) {

        if(decibelLevel < 55)
            soundMapResource.getAlerts().add(new SoundAlerts(point.getPayload().getUid(), LOW_NOISE_AREA.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), decibelLevel, point.getPayload().getReliability(), point.getPayload().getRelevance()));
        else if(decibelLevel < 70)
            soundMapResource.getAlerts().add(new SoundAlerts(point.getPayload().getUid(), MODERATELY_NOISY_AREA.getMessage(),
                    new LatLng(point.getPayload().getLat()
                            , point.getPayload().getLng()), decibelLevel, point.getPayload().getReliability(), point.getPayload().getRelevance()));
        else
            soundMapResource.getAlerts().add(new SoundAlerts(point.getPayload().getUid(), VERY_NOISY_AREA.getMessage(),
                    new LatLng(point.getPayload().getLat()
                            , point.getPayload().getLng()), decibelLevel, point.getPayload().getReliability(), point.getPayload().getRelevance()));

        return decibelLevel * RADIUS_SCALE_FACTOR;
    }

    public List<SoundResponse> findMinMaxSoundResponses(List<SoundResponse> soundResponses) {
        if (soundResponses == null || soundResponses.isEmpty()) {
            return new ArrayList<>();
        }

        SoundResponse minSoundResponse = soundResponses.get(0);
        SoundResponse maxSoundResponse = soundResponses.get(0);

        for (SoundResponse response : soundResponses) {
            if (response.getPayload().getDecibelLevel() < minSoundResponse.getPayload().getDecibelLevel()) {
                minSoundResponse = response;
            }
            if (response.getPayload().getDecibelLevel() > maxSoundResponse.getPayload().getDecibelLevel()) {
                maxSoundResponse = response;
            }
        }

        List<SoundResponse> result = new ArrayList<>();
        result.add(minSoundResponse);
        result.add(maxSoundResponse);

        return result;
    }

    public StatsResource analyzeDaily(List<SoundResponse> soundResponses, int day, int month, int year) {
        List<SoundResponse> filteredResponses = soundResponses.stream()
                .filter(response -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(response.getSampleTime());
                    return calendar.get(Calendar.DAY_OF_MONTH) == day &&
                            calendar.get(Calendar.MONTH) == month - 1 &&
                            calendar.get(Calendar.YEAR) == year;
                })
                .collect(Collectors.toList());

        return calculateStatistics(filteredResponses);
    }

    private StatsResource calculateStatistics(List<SoundResponse> soundResponses) {
        StatsResource statistics = new StatsResource();

        if (soundResponses.isEmpty()) {
            return statistics;
        }

        double min = soundResponses.stream()
                .mapToDouble(response -> response.getPayload().getDecibelLevel())
                .min()
                .orElse(Double.NaN);

        double max = soundResponses.stream()
                .mapToDouble(response -> response.getPayload().getDecibelLevel())
                .max()
                .orElse(Double.NaN);

        double average = soundResponses.stream()
                .mapToDouble(response -> response.getPayload().getDecibelLevel())
                .average()
                .orElse(Double.NaN);

        double roundedAvg = Math.round(average * 10.0) / 10.0;

        double variance = soundResponses.stream()
                .mapToDouble(response -> Math.pow(response.getPayload().getDecibelLevel() - average, 2))
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

    public StatsResource analyzeWeekly(List<SoundResponse> soundResponses, int week, int year) {
        List<SoundResponse> filteredResponses = soundResponses.stream()
                .filter(response -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(response.getSampleTime());
                    return calendar.get(Calendar.WEEK_OF_YEAR) == week &&
                            calendar.get(Calendar.YEAR) == year;
                })
                .collect(Collectors.toList());

        return calculateStatistics(filteredResponses);
    }

    public StatsResource analyzeSeasonally(List<SoundResponse> soundResponses, String season, int year) {
        List<SoundResponse> filteredResponses = soundResponses.stream()
                .filter(response -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(response.getSampleTime());
                    int month = calendar.get(Calendar.MONTH);
                    int responseYear = calendar.get(Calendar.YEAR);

                    return responseYear == year && isInSeason(month, season);
                })
                .collect(Collectors.toList());

        return calculateStatistics(filteredResponses);
    }

    private boolean isInSeason(int month, String season) {
        switch (season.toLowerCase()) {
            case "winter":
                return month == Calendar.DECEMBER || month == Calendar.JANUARY || month == Calendar.FEBRUARY;
            case "spring":
                return month == Calendar.MARCH || month == Calendar.APRIL || month == Calendar.MAY;
            case "summer":
                return month == Calendar.JUNE || month == Calendar.JULY || month == Calendar.AUGUST;
            case "autumn":
            case "fall":
                return month == Calendar.SEPTEMBER || month == Calendar.OCTOBER || month == Calendar.NOVEMBER;
            default:
                return false;
        }
    }
}
