package com.backend.app.service;

import com.backend.app.dto.output.RoadResponse;
import com.backend.app.dto.resource.RoadMapResource;
import com.backend.app.dto.resource.alerts.RoadAlerts;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.backend.app.dto.resource.alerts.RoadAlerts.AlertType.*;
import static com.backend.app.util.MapUtils.*;

@Service
public class RoadDataService {

    @Autowired
    ComputeZoomService computeZoomService;

    private final GeoApiContext context;

    public RoadDataService() {
        // inizializzazione del context
        this.context = new GeoApiContext.Builder()
                .apiKey(GOOGLE_API_KEY)
                .build();
    }

    public RoadMapResource drawMapWithColoredRoutes(List<LatLng> coordinates, List<RoadResponse> pointsToCheck, double tolerance) throws ApiException, InterruptedException, IOException {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new InvalidRequestException("Nessuna coordinata fornita");
        }

        // calcolo del centro della mappa
        double avgLat = 0;
        double avgLng = 0;
        for (LatLng coord : coordinates) {
            avgLat += coord.lat;
            avgLng += coord.lng;
        }
        avgLat /= coordinates.size();
        avgLng /= coordinates.size();
        LatLng center = new LatLng(avgLat, avgLng);

        // costruisce i markers
        StringBuilder markers = new StringBuilder();
        for (LatLng coord : coordinates) {
            markers.append("markers=color:red%7C").append(coord.lat).append(",").append(coord.lng).append("&");
        }

        // richiede le direzioni utilizzando l' API Directions
        DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(context);
        directionsRequest.origin(coordinates.get(0));
        directionsRequest.destination(coordinates.get(coordinates.size() - 1));
        directionsRequest.waypoints(coordinates.subList(1, coordinates.size() - 1).toArray(new LatLng[0]));
        directionsRequest.mode(TravelMode.DRIVING);

        DirectionsResult directionsResult = directionsRequest.await();

        // estrae il percorso dalle direzioni ottenute
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

        currentUrl.append(STATIC_MAP_URL+"?center=")
                .append(center.lat).append(",").append(center.lng)
                .append("&zoom=").append(computeZoomService.getDynamicZoom(coordinates, avgLat, avgLng))
                .append("&size=").append(RESOLUTION)
                .append("&scale=2&")
                .append(markers)
                .append("&key=").append(GOOGLE_API_KEY);

        // assicura che i dati non vengano considerati più volte nel percorso
        Set<RoadResponse> addedAlerts = new HashSet<>();

        for (int i = 0; i < routeCoordinates.size() - 1; i++) {
            LatLng start = routeCoordinates.get(i);
            LatLng end = routeCoordinates.get(i + 1);
            boolean matched = false;

            for (RoadResponse point : pointsToCheck) {
                // evita di considerare punti già trovati sul percorso
                if (addedAlerts.contains(point)) {
                    continue;
                }
                if (coordinateIsOnRoute(point.getPayload().toLatLng(), List.of(start, end), tolerance)) {
                    String currentColor = determineColorAndAlertBasedOnAcceleration(point, roadMapResource, 1);
                    if (!currentColor.equals(lastColor)) {
                        path.append("&path=color:").append(currentColor).append("|weight:5");
                        lastColor = currentColor;
                    }
                    matched = true;
                    addedAlerts.add(point);
                    break;
                }
            }

            if (!matched && !lastColor.equals("blue")) {
                path.append("&path=color:blue|weight:5");
                lastColor = "blue";
            }

            path.append("|").append(start.lat).append(",").append(start.lng);
            path.append("|").append(end.lat).append(",").append(end.lng);

            // controlla se l'url supera il limite di lunghezza
            if (currentUrl.length() + path.length() > MAX_URL_LENGTH) {
                currentUrl.append(path);
                mapUrls.add(currentUrl.toString());

                // reinizializza l'url e il path
                currentUrl = new StringBuilder();
                currentUrl.append(STATIC_MAP_URL+"?center=")
                        .append(center.lat).append(",").append(center.lng)
                        .append("&zoom=").append(computeZoomService.getDynamicZoom(coordinates, avgLat, avgLng))
                        .append("&size=").append(RESOLUTION)
                        .append("&scale=2&")
                        .append("&key=").append(GOOGLE_API_KEY);
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

    private List<LatLng> generateCircleAroundPoint(LatLng center, double radius) {
        List<LatLng> circlePoints = new ArrayList<>();
        int points = 18; // numero di punti che compongono il cerchio

        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians((360.0 / points) * i);
            double deltaLat = radius * Math.cos(angle) / 111320.0;
            double deltaLng = radius * Math.sin(angle) / (111320.0 * Math.cos(Math.toRadians(center.lat)));

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
            return apiType == 1 ? "green" : COLOR_GREEN;  // strada in buono stato
        } else if (magnitude < 1.0) {
            roadMapResource.getAlerts().add(new RoadAlerts(point.getPayload().getUid(), PARTIALLY_DAMAGED.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), magnitude, point.getPayload().getReliability(), point.getPayload().getRelevance()));
            return apiType == 1 ? "yellow" : COLOR_YELLOW;  // strada parzialmente danneggiata
        } else {
            roadMapResource.getAlerts().add(new RoadAlerts(point.getPayload().getUid(), HIGHLY_DAMAGED.getMessage(),
                    new LatLng(point.getPayload().getLat(),
                            point.getPayload().getLng()), magnitude, point.getPayload().getReliability(), point.getPayload().getRelevance()));
            return apiType == 1 ? "red" : COLOR_RED;  // strada molto danneggiata
        }
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

            // colori del cerchio
            String fillColor = color + "33"; // trasparenza per il riempimento
            String borderColor = color + "FF"; // colore del contorno

            // genera il cerchio
            path.append("&path=fillcolor:0x").append(fillColor)
                    .append("|color:0x").append(borderColor).append("|weight:1");

            List<LatLng> nearbyPoints = generateCircleAroundPoint(coord, tolerance);
            for (LatLng nearbyPoint : nearbyPoints) {
                path.append("|").append(nearbyPoint.lat).append(",").append(nearbyPoint.lng);
            }
        }

        // calcola il centro della mappa
        double avgLat = 0;
        double avgLng = 0;
        for (RoadResponse point : data) {
            avgLat += point.getPayload().getLat();
            avgLng += point.getPayload().getLng();
        }
        avgLat /= data.size();
        avgLng /= data.size();
        LatLng center = new LatLng(avgLat, avgLng);

        // genera l'url per la mappa statica
        String mapUrl = STATIC_MAP_URL+"?center=" +
                center.lat + "," + center.lng +
                "&zoom=14" +
                "&size=" + RESOLUTION +
                "&scale=2" +
                path.toString() +
                "&key=" + GOOGLE_API_KEY;

        List<String> urls = new ArrayList<>();
        urls.add(mapUrl);
        roadMapResource.setUrls(urls);

        return roadMapResource;
    }
}
