package com.backend.app.service;

import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class ComputeBoundsService {

    public long[] calculateDayBounds(int day, int month, int year) {
        LocalDate specifiedDate = LocalDate.of(year, month, day);

        ZonedDateTime startOfDay = specifiedDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfDay = specifiedDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());

        return new long[]{startOfDay.toInstant().toEpochMilli(), endOfDay.toInstant().toEpochMilli()};
    }

    public boolean isWithinDayBounds(long sampleTimeMillis, long[] timeBounds) {
        return sampleTimeMillis >= timeBounds[0] && sampleTimeMillis <= timeBounds[1];
    }

    public long[] calculateTimeBounds(int hour, int minute) {
        LocalTime specifiedTime = LocalTime.of(hour, minute);

        LocalTime lowerBoundTime = specifiedTime.minusMinutes(30);
        LocalTime upperBoundTime = specifiedTime.plusMinutes(30);

        return new long[]{toMillisOfDay(lowerBoundTime), toMillisOfDay(upperBoundTime)};
    }

    private long toMillisOfDay(LocalTime time) {
        // converte nanosec in ms
        return time.toNanoOfDay() / 1_000_000;
    }

    public boolean isWithinTimeBounds(long sampleTimeMillis, long[] timeBounds) {
        LocalTime sampleTime = Instant.ofEpochMilli(sampleTimeMillis)
                .atZone(ZoneId.systemDefault()).toLocalTime();

        long sampleMillisOfDay = toMillisOfDay(sampleTime);

        long lowerBoundMillis = timeBounds[0];
        long upperBoundMillis = timeBounds[1];

        return sampleMillisOfDay >= lowerBoundMillis && sampleMillisOfDay <= upperBoundMillis;
    }

    // calcola la distanza tra due punti geografici utilizzando la formula dell'emissione e distanza (Haversine)
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raggio della Terra in chilometri
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // Converti in metri
        return distance;
    }
}
