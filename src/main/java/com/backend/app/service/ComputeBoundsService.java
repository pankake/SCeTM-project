package com.backend.app.service;

import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class ComputeBoundsService {

    // calcola i millisecondi per anno, mese, giorno, ora e minuto specificati
    // con margine di tolleranza
    public long[] computeDateTimeBoundsInMillis(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // i mesi in calendar sono zero-indicizzati
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long targetTimeMillis = calendar.getTimeInMillis();
        long toleranceMillis = 30 * 60 * 1000; // 30 minuti di tolleranza

        long startTimeMillis = targetTimeMillis - toleranceMillis;
        long endTimeMillis = targetTimeMillis + toleranceMillis;

        System.out.println("startTimeMillis: " + startTimeMillis);
        System.out.println("endTimeMillis: " + endTimeMillis);

        return new long[]{startTimeMillis, endTimeMillis};
    }

    // calcola la distanza tra due punti geografici utilizzando la formula dell'emissione e distanza (Haversine)
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // raggio della terra in Km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // conversione in m
        return distance;
    }

    public long[] computeDayBoundsInMillis(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTimeMillis = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endTimeMillis = calendar.getTimeInMillis();

        return new long[]{startTimeMillis, endTimeMillis};
    }

    public long[] computeWeekBoundsInMillis(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // calcola l'inizio della settimana
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        calendar.add(Calendar.DAY_OF_MONTH, firstDayOfWeek - currentDayOfWeek);

        long startTimeMillis = calendar.getTimeInMillis();

        // calcola la fine della settimana
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endTimeMillis = calendar.getTimeInMillis();

        return new long[]{startTimeMillis, endTimeMillis};
    }

    public long[] computeSeasonBoundsInMillis(int year, int season) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);

        switch (season) {
            case 1: // spring
                calendar.set(Calendar.MONTH, Calendar.MARCH);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case 2: // summer
                calendar.set(Calendar.MONTH, Calendar.JUNE);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case 3: // autumn
                calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case 4: // winter
                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            default:
                throw new IllegalArgumentException("Invalid season: " + season);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startTimeMillis = calendar.getTimeInMillis();

        switch (season) {
            case 1: // spring
                calendar.set(Calendar.MONTH, Calendar.MAY);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 2: // summer
                calendar.set(Calendar.MONTH, Calendar.AUGUST);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 3: // autumn
                calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
                calendar.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 4: // winter
                calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
        }

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endTimeMillis = calendar.getTimeInMillis();

        return new long[]{startTimeMillis, endTimeMillis};
    }
}
