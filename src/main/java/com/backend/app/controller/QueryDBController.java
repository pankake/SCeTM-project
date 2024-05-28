package com.backend.app.controller;

import com.backend.app.dto.output.BrightResponse;
import com.backend.app.dto.output.RoadResponse;
import com.backend.app.dto.output.SoundResponse;
import com.backend.app.service.CityFromCoordsService;
import com.google.maps.errors.ApiException;
import com.backend.app.service.QueryDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/query")
public class QueryDBController {

	@Autowired
    QueryDBService queryDbService;
	@Autowired
    CityFromCoordsService cityFromCoordsService;

	public final static String TABLE_ROAD_DATA = "road_data";
	public final static String TABLE_SOUND_DATA = "sound_data";
	public final static String TABLE_BRIGHT_DATA = "light_data";

	@GetMapping("/road")
	public List<?> queryRoadDataTable() {
		return queryDbService.simpleScan(TABLE_ROAD_DATA, RoadResponse.class);
	}

	@GetMapping("/sound")
	public List<?> querySoundDataTable() {
		return queryDbService.simpleScan(TABLE_SOUND_DATA, SoundResponse.class);
	}

	@GetMapping("/bright")
	public List<?> queryLightDataTable() {
		return queryDbService.simpleScan(TABLE_BRIGHT_DATA, BrightResponse.class);
	}

	@GetMapping("/allCityRoads")
	public List<?> allCityRoads(@RequestParam String city) {
		return queryDbService.filterByCity(TABLE_ROAD_DATA, RoadResponse.class, city);
	}

	@GetMapping("/soundByAreaAndTime")
	public List<?> soundByAreaAndTime(@RequestParam Double lat, Double lng, int range, int hours, int min) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		return queryDbService.filterByAreaAndTime(TABLE_SOUND_DATA, SoundResponse.class,
				city, lat, lng, range, hours, min);
	}

	@GetMapping("/soundByAreaAndDate")
	public List<?> soundByAreaAndDate(@RequestParam Double lat, Double lng, int range,
									  int day, int month, int year) throws IOException, InterruptedException, ApiException {

		String city = cityFromCoordsService.getCityFromCoords(lat, lng);

		return queryDbService.filterByAreaAndDate(TABLE_SOUND_DATA, SoundResponse.class,
				city, lat, lng, range, day, month, year);
	}

	@GetMapping("/sound/incrementReliability")
	public String incrementSoundReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_SOUND_DATA, uid, 1, "reliability");
	}

	@GetMapping("/sound/decrementReliability")
	public String decrementSoundReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_SOUND_DATA, uid, -1, "reliability");
	}

	@GetMapping("/bright/incrementReliability")
	public String incrementBrightReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, 1, "reliability");
	}

	@GetMapping("/bright/decrementReliability")
	public String decrementBrightReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, -1, "reliability");
	}

	@GetMapping("/road/incrementReliability")
	public String incrementRoadReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, 1, "reliability");
	}

	@GetMapping("/road/decrementReliability")
	public String decrementRoadReliability(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, -1, "reliability");
	}

	@GetMapping("/sound/incrementRelevance")
	public String incrementSoundRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_SOUND_DATA, uid, 1, "relevance");
	}

	@GetMapping("/sound/decrementRelevance")
	public String decrementSoundRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_SOUND_DATA, uid, -1, "relevance");
	}

	@GetMapping("/bright/incrementRelevance")
	public String incrementBrightRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, 1, "relevance");
	}

	@GetMapping("/bright/decrementRelevance")
	public String decrementBrightRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_BRIGHT_DATA, uid, -1, "relevance");
	}

	@GetMapping("/road/incrementRelevance")
	public String incrementRoadRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, 1, "relevance");
	}

	@GetMapping("/road/decrementRelevance")
	public String decrementRoadRelevance(@RequestParam List<String> uid) {
		return queryDbService.updateField(TABLE_ROAD_DATA, uid, -1, "relevance");
	}
}
