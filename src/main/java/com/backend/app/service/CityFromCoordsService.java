package com.backend.app.service;

import com.backend.app.util.MapUtils;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CityFromCoordsService {
    private final GeoApiContext context;

    public CityFromCoordsService() {
        this.context = new GeoApiContext.Builder()
                .apiKey(MapUtils.GOOGLE_API_KEY)
                .build();
    }

    public String getCityFromCoords(double lat, double lng) throws ApiException, InterruptedException, IOException {
        LatLng location = new LatLng(lat, lng);
        GeocodingResult[] results = GeocodingApi.reverseGeocode(context, location).await();

        for (GeocodingResult result : results) {
            for (com.google.maps.model.AddressComponent component : result.addressComponents) {
                for (com.google.maps.model.AddressComponentType type : component.types) {
                    if (type == com.google.maps.model.AddressComponentType.LOCALITY) {
                        return component.longName.toLowerCase();
                    }
                }
            }
        }
        return null;
    }
}
