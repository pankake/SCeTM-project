package com.backend.app.controller;

import com.backend.app.dto.output.SoundResponse;
import com.backend.app.service.CityFromCoordsService;
import com.backend.app.service.QueryDBService;
import com.backend.app.service.SoundDataService;
import com.google.maps.errors.ApiException;
import com.backend.app.dto.resource.SoundMapResource;
import com.backend.app.dto.resource.StatsResource;
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

	@GetMapping("/drawByAreaAndTime")
	public SoundMapResource drawByAreaAndTime(@RequestParam Double lat, Double lng, int range,
											  int hours, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<SoundResponse> data =
				queryDbService.filterByAreaAndTime(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
						city, lat, lng, range, hours, min);

		return soundDataService.colorSoundNearDataPoints(data);
	}

	@GetMapping("/drawByAreaAndDate")
	public SoundMapResource drawByZoneAndDate(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<SoundResponse> data =
				queryDbService.filterByAreaAndDate(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
						city, lat, lng, range, day, month, year);

		return soundDataService.colorSoundNearDataPoints(data);
	}

	@GetMapping("/minMaxByCity")
	public List<?> minMaxByCity(@RequestParam String city) {

		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class, city);

		return soundDataService.findMinMaxSoundResponses(data);
	}

	@GetMapping("/minMaxByAreaAndTime")
	public List<?> minMaxByAreaAndTime(@RequestParam Double lat, Double lng, int range,
											  int hours, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<SoundResponse> data =
				queryDbService.filterByAreaAndTime(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
						city, lat, lng, range, hours, min);

		return soundDataService.findMinMaxSoundResponses(data);
	}

	@GetMapping("/minMaxByAreaAndDate")
	public List<?> minMaxByAreaAndDate(@RequestParam Double lat, Double lng, int range,
											  int day, int month, int year) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		List<SoundResponse> data =
				queryDbService.filterByAreaAndDate(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class,
						city, lat, lng, range, day, month, year);

		return soundDataService.findMinMaxSoundResponses(data);
	}

	@GetMapping("/dailyTrendAnalysis")
	public StatsResource dailyTrendAnalysis(@RequestParam String city, int day, int month, int year) {

		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class, city);

		return soundDataService.analyzeDaily(data, day, month, year);
	}

	@GetMapping("/weeklyTrendAnalysis")
	public StatsResource weeklyTrendAnalysis(@RequestParam String city, int week, int year) {

		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class, city);

		return soundDataService.analyzeWeekly(data, week, year);
	}

	@GetMapping("/seasonalTrendAnalysis")
	public StatsResource seasonalTrendAnalysis(@RequestParam String city, String season, int year) {

		List<SoundResponse> data =
				(List<SoundResponse>) queryDbService
						.filterByCity(QueryDBController.TABLE_SOUND_DATA, SoundResponse.class, city);

		return soundDataService.analyzeSeasonally(data, season, year);
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
