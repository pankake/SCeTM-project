package com.backend.app.service;

import com.backend.app.dto.output.RoadResponse;
import com.backend.app.dto.resource.RoadMapResource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


public class RoadDataServiceTest {

    RoadDataService roadDataService = new RoadDataService();

    @Test
    public void calculateMagnitudeTest() {
        RoadResponse roadResponse = new RoadResponse();
        RoadMapResource roadMapResource = new RoadMapResource();
        roadMapResource.setAlerts(new ArrayList<>());

        roadResponse.setPayload(new RoadResponse.RoadPayload());
        roadResponse.getPayload().setAccelerationX(0.5);
        roadResponse.getPayload().setAccelerationY(0.7);
        roadResponse.getPayload().setAccelerationZ(0.3);
        String color = roadDataService.determineColorAndAlertBasedOnAcceleration(roadResponse, roadMapResource, 1);

        System.out.println(color);
    }
}
