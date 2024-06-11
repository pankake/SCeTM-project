package com.backend.app.controller;

import com.backend.app.dto.output.SoundResponse;
import com.backend.app.dto.resource.SoundMapResource;
import com.backend.app.dto.resource.StatsResource;
import com.backend.app.service.CityFromCoordsService;
import com.backend.app.service.QueryDBService;
import com.backend.app.service.SoundDataService;
import com.google.maps.errors.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/sound")
public class SoundDataController {
	@Autowired
    QueryDBService queryDbService;
	@Autowired
    SoundDataService soundDataService;
	@Autowired
	CityFromCoordsService cityFromCoordsService;

	@GetMapping("/drawByCity")
	public SoundMapResource drawByCity(@RequestParam String city) throws IOException, InterruptedException, ApiException {
		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class, city);

		return soundDataService.colorSoundNearDataPoints(data);
	}

	@GetMapping("/drawByAreaAndDateTime")
	public SoundMapResource drawByAreaAndDateTime(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year, int hour, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<SoundResponse> data =
				queryDbService.filterByAreaAndPeriod(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
						city, lat, lng, range, day, month, year, hour, min, "");

		return soundDataService.colorSoundNearDataPoints(data);
	}

	@GetMapping("/minMaxByCity")
	public List<?> minMaxByCity(@RequestParam String city) {

		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class, city);

		return soundDataService.findMinMaxSoundResponses(data);
	}

	@GetMapping("/minMaxByAreaAndDateTime")
	public List<?> minMaxByAreaAndDate(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year, int hour, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<SoundResponse> data =
				queryDbService.filterByAreaAndPeriod(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
						city, lat, lng, range, day, month, year, hour, min, "");

		return soundDataService.findMinMaxSoundResponses(data);
	}

	@GetMapping("/dailyTrendAnalysis")
	public StatsResource dailyTrendAnalysis(@RequestParam String city, int day, int month, int year) {

		List<SoundResponse> data =
                (List<SoundResponse>) queryDbService
                       .filterByCityAndPeriod(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
                               city, year, month, day, -1, -1, "day");

		return soundDataService.computeStatistics(data);
	}

	@GetMapping("/weeklyTrendAnalysis")
	public StatsResource weeklyTrendAnalysis(@RequestParam String city, int day, int month, int year) {

		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCityAndPeriod(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
								city, year, month, day, -1, -1, "week");

		return soundDataService.computeStatistics(data);
	}

	@GetMapping("/seasonalTrendAnalysis")
	public StatsResource seasonalTrendAnalysis(@RequestParam String city, int season, int year) {

		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCityAndPeriod(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
								city, year, season, -1, -1, -1, "season");

		return soundDataService.computeStatistics(data);
	}

	@GetMapping("/incrementReliability")
	public String incrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_SOUND_DATA, uid, 1, "reliability");
	}

	@GetMapping("/decrementReliability")
	public String decrementReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_SOUND_DATA, uid, -1, "reliability");
	}

	@GetMapping("/incrementRelevance")
	public String incrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_SOUND_DATA, uid, 1, "relevance");
	}

	@GetMapping("/decrementRelevance")
	public String decrementRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(QueryDBController.TABLE_SOUND_DATA, uid, -1, "relevance");
	}
}
