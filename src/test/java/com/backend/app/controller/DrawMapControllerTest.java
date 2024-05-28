package com.backend.app.controller;

import com.google.maps.model.LatLng;
import com.backend.app.service.RoadDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


public class DrawMapControllerTest {

    RoadDataService mapService = new RoadDataService();

    @Test
    public void coordinateOnRouteTest() {

        Assertions.assertTrue(mapService.coordinateIsOnRoute(
                new LatLng(44.05923594666094, 12.568551232015462), getRiminiCoordinates(), 20));

        Assertions.assertTrue(mapService.coordinateIsOnRoute(
                new LatLng(44.05916798845072, 12.568620114470239), getRiminiCoordinates(), 20));

        Assertions.assertFalse(mapService.coordinateIsOnRoute(
                new LatLng(44.059016130932385, 12.568804579349127), getRiminiCoordinates(), 20));
    }

    public static List<LatLng> getRiminiCoordinates() {
        return Arrays.asList(
                new LatLng(44.05927562867706, 12.5685059421121)  // Piazza Tre Martiri
/*				new LatLng(44.0584, 12.5695),  // Arco di Augusto
				new LatLng(44.0646, 12.5659)  // Ponte di Tiberio
				new LatLng(44.0593, 12.5698),  // Piazza Tre Martiri
				new LatLng(44.0614, 12.5669),  // Castel Sismondo
				new LatLng(44.0598, 12.5702)   // Tempio Malatestiano*/
        );
    }

}
