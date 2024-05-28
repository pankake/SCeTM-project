package com.backend.app.controller;

import com.backend.app.dto.output.RoadResponse;
import com.backend.app.service.CityFromCoordsService;
import com.backend.app.service.QueryDBService;
import com.backend.app.service.RoadDataService;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.backend.app.dto.resource.RoadMapResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
						.simpleScan(QueryDBController.TABLE_ROAD_DATA, RoadResponse.class);

		return roadDataService.drawMapWithColoredRoutes(coordinates, data, 20);
	}

	// rice in ingresso il nome di una città per disegnare su tutte le strade di quella città
	@GetMapping("/drawAllRoadsByCity")
	public RoadMapResource drawAllRoadsByCity(@RequestParam String city) throws IOException, InterruptedException, ApiException {
		List<RoadResponse> data =
				(List<RoadResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_ROAD_DATA, RoadResponse.class, city);

		return roadDataService.colorRoadsNearDataPoints(data, 15);
	}

	@GetMapping("/incrementReliability")
	public String incrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_ROAD_DATA, uid, 1, "reliability");
	}

	@GetMapping("/decrementReliability")
	public String decrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_ROAD_DATA, uid, -1, "reliability");
	}

	@GetMapping("/incrementRelevance")
	public String incrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_ROAD_DATA, uid, 1, "relevance");
	}

	@GetMapping("/decrementRelevance")
	public String decrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_ROAD_DATA, uid, -1, "relevance");
	}
}
