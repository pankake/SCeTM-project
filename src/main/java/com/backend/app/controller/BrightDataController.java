package com.backend.app.controller;

import com.backend.app.dto.output.BrightResponse;
import com.backend.app.dto.resource.BrightMapResource;
import com.backend.app.dto.resource.StatsResource;
import com.backend.app.service.BrightDataService;
import com.backend.app.service.CityFromCoordsService;
import com.backend.app.service.QueryDBService;
import com.google.maps.errors.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

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
						.filterByCity(QueryDBController.TABLE_BRIGHT_DATA, BrightResponse.class, city);

		return brightDataService.colorBrightNearDataPoints(data);
	}

	@GetMapping("/drawByAreaAndDateTime")
	public BrightMapResource drawByAreaAndDateTime(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year, int hour, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<BrightResponse> data =
				queryDbService.filterByAreaAndPeriod(QueryDBController.TABLE_BRIGHT_DATA, BrightResponse.class,
						city, lat, lng, range, day, month, year, hour, min, "");

		return brightDataService.colorBrightNearDataPoints(data);
	}

	@GetMapping("/minMaxByCity")
	public List<?> minMaxByCity(@RequestParam String city) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_BRIGHT_DATA, BrightResponse.class, city);

		return brightDataService.findMinMaxBrightResponses(data);
	}

	@GetMapping("/minMaxByAreaAndDateTime")
	public List<?> minMaxByAreaAndDateTime(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year, int hour, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<BrightResponse> data =
				queryDbService.filterByAreaAndPeriod(QueryDBController.TABLE_BRIGHT_DATA, BrightResponse.class,
						city, lat, lng, range, day, month, year, hour, min, "");

		return brightDataService.findMinMaxBrightResponses(data);
	}

	@GetMapping("/dailyTrendAnalysis")
	public StatsResource dailyTrendAnalysis(@RequestParam String city, int day, int month, int year) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCityAndPeriod(QueryDBController.TABLE_BRIGHT_DATA, BrightResponse.class,
								city, year, month, day, -1, -1, "day");

		return brightDataService.computeStatistics(data);
	}

	@GetMapping("/weeklyTrendAnalysis")
	public StatsResource weeklyTrendAnalysis(@RequestParam String city, int day, int month, int year) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCityAndPeriod(QueryDBController.TABLE_BRIGHT_DATA, BrightResponse.class,
								city, year, month, day, -1, -1, "week");

		return brightDataService.computeStatistics(data);
	}

	@GetMapping("/seasonalTrendAnalysis")
	public StatsResource seasonalTrendAnalysis(@RequestParam String city, int season, int year) {

		List<BrightResponse> data =
				(List<BrightResponse>) queryDbService
						.filterByCityAndPeriod(QueryDBController.TABLE_BRIGHT_DATA, BrightResponse.class,
								city, year, season, -1, -1, -1, "season");

		return brightDataService.computeStatistics(data);
	}

	@GetMapping("/incrementReliability")
	public String incrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_BRIGHT_DATA, uid, 1, "reliability");
	}

	@GetMapping("/decrementReliability")
	public String decrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_BRIGHT_DATA, uid, -1, "reliability");
	}

	@GetMapping("/incrementRelevance")
	public String incrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_BRIGHT_DATA, uid, 1, "relevance");
	}

	@GetMapping("/decrementRelevance")
	public String decrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_BRIGHT_DATA, uid, -1, "relevance");
	}
}
