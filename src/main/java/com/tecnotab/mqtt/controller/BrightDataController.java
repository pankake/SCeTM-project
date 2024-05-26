package com.tecnotab.mqtt.controller;

import com.google.maps.errors.ApiException;
import com.tecnotab.mqtt.dto.output.BrightResponse;
import com.tecnotab.mqtt.dto.resource.BrightMapResource;
import com.tecnotab.mqtt.dto.resource.StatsResource;
import com.tecnotab.mqtt.service.CityFromCoordsService;
import com.tecnotab.mqtt.service.QueryDBService;
import com.tecnotab.mqtt.service.BrightDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.tecnotab.mqtt.controller.QueryDBController.TABLE_BRIGHT_DATA;

@RestController
@RequestMapping("/bright")
public class BrightDataController {
	@Autowired
    QueryDBService queryDbService;
	@Autowired
    BrightDataService brightDataService;
	@Autowired
	CityFromCoordsService cityFromCoordsService;

	@GetMapping("/drawByCity")
	public BrightMapResource drawByCity(@RequestParam String city) throws ApiException {
		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCity(TABLE_BRIGHT_DATA, BrightResponse.class, city);

		return brightDataService.colorBrightNearDataPoints(data);
	}

	@GetMapping("/drawByAreaAndTime")
	public BrightMapResource drawByAreaAndTime(@RequestParam Double lat, Double lng, int range,
											  int hours, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<BrightResponse> data =
				queryDbService.filterByAreaAndTime(TABLE_BRIGHT_DATA, BrightResponse.class,
						city, lat, lng, range, hours, min);

		return brightDataService.colorBrightNearDataPoints(data);
	}

	@GetMapping("/drawByAreaAndDate")
	public BrightMapResource drawByZoneAndDate(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<BrightResponse> data =
				queryDbService.filterByAreaAndDate(TABLE_BRIGHT_DATA, BrightResponse.class,
						city, lat, lng, range, day, month, year);

		return brightDataService.colorBrightNearDataPoints(data);
	}

	@GetMapping("/minMaxByCity")
	public List<?> minMaxByCity(@RequestParam String city) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCity(TABLE_BRIGHT_DATA, BrightResponse.class, city);

		return brightDataService.findMinMaxBrightResponses(data);
	}

	@GetMapping("/minMaxByAreaAndTime")
	public List<?> minMaxByAreaAndTime(@RequestParam Double lat, Double lng, int range,
											  int hours, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<BrightResponse> data =
				queryDbService.filterByAreaAndTime(TABLE_BRIGHT_DATA, BrightResponse.class,
						city, lat, lng, range, hours, min);

		return brightDataService.findMinMaxBrightResponses(data);
	}

	@GetMapping("/minMaxByAreaAndDate")
	public List<?> minMaxByAreaAndDate(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<BrightResponse> data =
				queryDbService.filterByAreaAndDate(TABLE_BRIGHT_DATA, BrightResponse.class,
						city, lat, lng, range, day, month, year);

		return brightDataService.findMinMaxBrightResponses(data);
	}

	@GetMapping("/dailyTrendAnalysis")
	public StatsResource dailyTrendAnalysis(@RequestParam String city, int day, int month, int year) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCity(TABLE_BRIGHT_DATA, BrightResponse.class, city);

		return brightDataService.analyzeDaily(data, day, month, year);
	}

	@GetMapping("/weeklyTrendAnalysis")
	public StatsResource weeklyTrendAnalysis(@RequestParam String city, int week, int year) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCity(TABLE_BRIGHT_DATA, BrightResponse.class, city);

		return brightDataService.analyzeWeekly(data, week, year);
	}

	@GetMapping("/seasonalTrendAnalysis")
	public StatsResource seasonalTrendAnalysis(@RequestParam String city, String season, int year) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCity(TABLE_BRIGHT_DATA, BrightResponse.class, city);

		return brightDataService.analyzeSeasonally(data, season, year);
	}

	@GetMapping("/incrementReliability")
	public String incrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, 1, "reliability");
	}

	@GetMapping("/decrementReliability")
	public String decrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, -1, "reliability");
	}

	@GetMapping("/incrementRelevance")
	public String incrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, 1, "relevance");
	}

	@GetMapping("/decrementRelevance")
	public String decrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, -1, "relevance");
	}
}
