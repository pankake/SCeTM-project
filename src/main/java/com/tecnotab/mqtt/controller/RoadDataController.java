package com.tecnotab.mqtt.controller;

import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.tecnotab.mqtt.dto.output.RoadResponse;
import com.tecnotab.mqtt.dto.resource.RoadMapResource;
import com.tecnotab.mqtt.service.CityFromCoordsService;
import com.tecnotab.mqtt.service.QueryDBService;
import com.tecnotab.mqtt.service.RoadDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.tecnotab.mqtt.controller.QueryDBController.TABLE_ROAD_DATA;

@RestController
@RequestMapping("/road")
public class RoadDataController {

	@Autowired
	RoadDataService roadDataService;
	@Autowired
    QueryDBService queryDbService;
	@Autowired
	CityFromCoordsService cityFromCoordsService;

	// riceve in ingresso due coppie di coordinate: punto di partenza e arrivo
	@PostMapping("/drawByRoute")
	public RoadMapResource drawByRoute(@RequestBody List<LatLng> coordinates) throws IOException, InterruptedException, ApiException {

		List<RoadResponse> data =
                (List<RoadResponse>) queryDbService
						.simpleScan(TABLE_ROAD_DATA, RoadResponse.class);

		return roadDataService.drawMapWithColoredRoutes(coordinates, data, 20);
	}

	// rice in ingresso il nome di una città per disegnare su tutte le strade di quella città
	@GetMapping("/drawAllRoadsByCity")
	public RoadMapResource drawAllRoadsByCity(@RequestParam String city) throws IOException, InterruptedException, ApiException {
		List<RoadResponse> data =
				(List<RoadResponse>) queryDbService
						.filterByCity(TABLE_ROAD_DATA, RoadResponse.class, city);

		return roadDataService.colorRoadsNearDataPoints(data, 15);
	}

	@GetMapping("/incrementReliability")
	public String incrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, 1, "reliability");
	}

	@GetMapping("/decrementReliability")
	public String decrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, -1, "reliability");
	}

	@GetMapping("/incrementRelevance")
	public String incrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, 1, "relevance");
	}

	@GetMapping("/decrementRelevance")
	public String decrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, -1, "relevance");
	}
}
