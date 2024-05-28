package com.backend.app.service;

import com.backend.app.dto.output.RoadResponse;
import com.backend.app.util.MapUtils;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.*;
import com.backend.app.dto.resource.RoadMapResource;
import com.backend.app.dto.resource.alerts.RoadAlerts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.backend.app.dto.resource.alerts.RoadAlerts.AlertType.*;

@Service
public class RoadDataService {

    @Autowired
    ComputeZoomService computeZoomService;

    private final GeoApiContext context;

    public RoadDataService() {
        // inizializzazione del context
        this.context = new GeoApiContext.Builder()
                .apiKey(MapUtils.GOOGLE_API_KEY)
                .build();
    }

    public RoadMapResource drawMapWithColoredRoutes(List<LatLng> coordinates, List<RoadResponse> pointsToCheck, double tolerance) throws ApiException, InterruptedException, IOException {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new InvalidRequestException("Nessuna coordinata fornita");
        }

        // Calcola il centro della mappa
        double avgLat = 0;
        double avgLng = 0;
        for (LatLng coord : coordinates) {
            avgLat += coord.lat;
            avgLng += coord.lng;
        }
        avgLat /= coordinates.size();
        avgLng /= coordinates.size();
        LatLng center = new LatLng(avgLat, avgLng);

        // Costruisci il parametro markers
        StringBuilder markers = new StringBuilder();
        for (LatLng coord : coordinates) {
            markers.append("markers=color:red%7C").append(coord.lat).append(",").append(coord.lng).append("&");
        }

        // Richiede le direzioni utilizzando le API Directions
        DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(context);
        directionsRequest.origin(coordinates.get(0));
        directionsRequest.destination(coordinates.get(coordinates.size() - 1));
        directionsRequest.waypoints(coordinates.subList(1, coordinates.size() - 1).toArray(new LatLng[0]));
        directionsRequest.mode(TravelMode.DRIVING);

        DirectionsResult directionsResult = directionsRequest.await();

        // Estrai il percorso dalle direzioni ottenute
        List<LatLng> routeCoordinates = new ArrayList<>();
        for (DirectionsRoute route : directionsResult.routes) {
            for (DirectionsLeg leg : route.legs) {
                for (DirectionsStep step : leg.steps) {
                    EncodedPolyline polyline = step.polyline;
                    List<LatLng> decodedPath = polyline.decodePath();
                    routeCoordinates.addAll(decodedPath);
                }
            }
        }

        RoadMapResource roadMapResource = new RoadMapResource();
        List<RoadAlerts> roadAlerts = new ArrayList<>();
        roadMapResource.setAlerts(roadAlerts);

        List<String> mapUrls = new ArrayList<>();
        String lastColor = "blue";
        StringBuilder path = new StringBuilder("&path=color:").append(lastColor).append("|weight:5");
        StringBuilder currentUrl = new StringBuilder();

        currentUrl.append(MapUtils.STATIC_MAP_URL+"?center=")
                .append(center.lat).append(",").append(center.lng)
                .append("&zoom=").append(computeZoomService.getDynamicZoom(coordinates, avgLat, avgLng))
                .append("&size=").append(MapUtils.RESOLUTION)
                .append("&scale=2&")
                .append(markers.toString())
                .append("&key=").append(MapUtils.GOOGLE_API_KEY);

        for (int i = 0; i < routeCoordinates.size() - 1; i++) {
            LatLng start = routeCoordinates.get(i);
            LatLng end = routeCoordinates.get(i + 1);
            boolean matched = false;

            for (RoadResponse point : pointsToCheck) {
                if (coordinateIsOnRoute(point.getPayload().toLatLng(), List.of(start, end), tolerance)) {
                    String currentColor = determineColorAndAlertBasedOnAcceleration(point, roadMapResource, 1);
                    if (!currentColor.equals(lastColor)) {
                        path.append("&path=color:").append(currentColor).append("|weight:5");
                        lastColor = currentColor;
                    }
                    matched = true;
                    break;
                }
            }

            if (!matched && !lastColor.equals("blue")) {
                path.append("&path=color:blue|weight:5");
                lastColor = "blue";
            }

            path.append("|").append(start.lat).append(",").append(start.lng);
            path.append("|").append(end.lat).append(",").append(end.lng);

            // Check if the URL length exceeds the limit
            if (currentUrl.length() + path.length() > MapUtils.MAX_URL_LENGTH) {
                currentUrl.append(path);
                mapUrls.add(currentUrl.toString());

                // Reset current URL and path
                currentUrl = new StringBuilder();
                currentUrl.append(MapUtils.STATIC_MAP_URL+"?center=")
                        .append(center.lat).append(",").append(center.lng)
                        .append("&zoom=").append(computeZoomService.getDynamicZoom(coordinates, avgLat, avgLng))
                        .append("&size=").append(MapUtils.RESOLUTION)
                        .append("&scale=2&")
                        .append("&key=").append(MapUtils.GOOGLE_API_KEY);
                path = new StringBuilder("&path=color:").append(lastColor).append("|weight:5");
            }
        }

        if (path.length() > 0) {
            currentUrl.append(path);
            mapUrls.add(currentUrl.toString());
        }

        roadMapResource.setUrls(mapUrls);
        return roadMapResource;
    }

    public boolean coordinateIsOnRoute(LatLng coordinate, List<LatLng> routeCoordinates, double tolerance) {
        for (LatLng routeCoordinate : routeCoordinates) {
            double distance = distanceBetweenCoordinates(coordinate, routeCoordinate);
            if (distance <= tolerance) {
                return true;
            }
        }
        return false;
    }

    private double distanceBetweenCoordinates(LatLng coord1, LatLng coord2) {
        double lat1 = coord1.lat;
        double lon1 = coord1.lng;
        double lat2 = coord2.lat;
        double lon2 = coord2.lng;

        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);

        // converte le miglia in metri
        dist = dist * 60 * 1.1515 * 1.609344 * 1000;
        return dist;
    }

    public RoadMapResource colorRoadsNearDataPoints(List<RoadResponse> data, double tolerance) throws ApiException, InterruptedException, IOException {
        if (data == null || data.isEmpty()) {
            throw new InvalidRequestException("Nessun dato fornito");
        }

        RoadMapResource roadMapResource = new RoadMapResource();
        List<RoadAlerts> alerts = new ArrayList<>();
        roadMapResource.setAlerts(alerts);

        StringBuilder path = new StringBuilder();

        for (RoadResponse point : data) {
            String color = determineColorAndAlertBasedOnAcceleration(point, roadMapResource, 2);
            LatLng coord = point.getPayload().toLatLng();

            // Define the fill and border color for the circle
            String fillColor = color + "33"; // Add transparency to fill color
            String borderColor = color + "FF"; // Solid border color

            // Generate a single filled circle
            path.append("&path=fillcolor:0x").append(fillColor)
                    .append("|color:0x").append(borderColor).append("|weight:1");

            List<LatLng> nearbyPoints = generateCircleAroundPoint(coord, tolerance);
            for (LatLng nearbyPoint : nearbyPoints) {
                path.append("|").append(nearbyPoint.lat).append(",").append(nearbyPoint.lng);
            }
        }

        // Calculate the center of the data points
        double avgLat = 0;
        double avgLng = 0;
        for (RoadResponse point : data) {
            avgLat += point.getPayload().getLat();
            avgLng += point.getPayload().getLng();
        }
        avgLat /= data.size();
        avgLng /= data.size();
        LatLng center = new LatLng(avgLat, avgLng);

        // Generate the static map URL
        String mapUrl = MapUtils.STATIC_MAP_URL+"?center=" +
                center.lat + "," + center.lng +
                "&zoom=14" +
                "&size=" + MapUtils.RESOLUTION +
                "&scale=2" +
                path.toString() +
                "&key=" + MapUtils.GOOGLE_API_KEY;

        List<String> urls = new ArrayList<>();
        urls.add(mapUrl);
        roadMapResource.setUrls(urls);

        return roadMapResource;
    }

    private List<LatLng> generateCircleAroundPoint(LatLng center, double radius) {
        List<LatLng> circlePoints = new ArrayList<>();
        int points = 18; // Number of points in the circle

        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians((360.0 / points) * i);
            double deltaLat = radius * Math.cos(angle) / 111320.0; // Approx conversion: 1 degree lat ≈ 111.32 km
            double deltaLng = radius * Math.sin(angle) / (111320.0 * Math.cos(Math.toRadians(center.lat))); // Adjust for longitude

            LatLng point = new LatLng(center.lat + deltaLat, center.lng + deltaLng);
            circlePoints.add(point);
        }

        return circlePoints;
    }

    public String determineColorAndAlertBasedOnAcceleration(RoadResponse point, RoadMapResource roadMapResource, int apiType) {

        double magnitude = Math.sqrt(Math.pow(point.getPayload().getAccelerationX(), 2) +
                Math.pow(point.getPayload().getAccelerationY(), 2) +
                Math.pow(point.getPayload().getAccelerationZ(), 2));


        if (magnitude < 0.5) {
            roadMapResource.getAlerts().add(new RoadAlerts(point.getPayload().getUid(), NOT_DAMAGED.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), magnitude, point.getPayload().getReliability(), point.getPayload().getRelevance()));
            return apiType == 1 ? "green" : MapUtils.COLOR_GREEN;  // Strada in buono stato
        } else if (magnitude < 1.0) {
            roadMapResource.getAlerts().add(new RoadAlerts(point.getPayload().getUid(), PARTIALLY_DAMAGED.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), magnitude, point.getPayload().getReliability(), point.getPayload().getRelevance()));
            return apiType == 1 ? "yellow" : MapUtils.COLOR_YELLOW;  // Strada parzialmente danneggiata
        } else {
            roadMapResource.getAlerts().add(new RoadAlerts(point.getPayload().getUid(), HIGHLY_DAMAGED.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), magnitude, point.getPayload().getReliability(), point.getPayload().getRelevance()));
            return apiType == 1 ? "red" : MapUtils.COLOR_RED;  // Strada molto danneggiata
        }
    }
}
