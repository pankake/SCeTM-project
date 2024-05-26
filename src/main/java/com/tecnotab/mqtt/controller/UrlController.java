package com.tecnotab.mqtt.controller;

import com.google.maps.errors.ApiException;
import com.tecnotab.mqtt.dto.output.SoundResponse;
import com.tecnotab.mqtt.dto.resource.SoundMapResource;
import com.tecnotab.mqtt.dto.resource.StatsResource;
import com.tecnotab.mqtt.service.CityFromCoordsService;
import com.tecnotab.mqtt.service.QueryDBService;
import com.tecnotab.mqtt.service.SoundDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tecnotab.mqtt.controller.QueryDBController.TABLE_SOUND_DATA;

@RestController
public class UrlController {
	private static final String BASE_URL = "http://example.com/share/";
	@GetMapping("/generateUrl")
	public String generateUrl(@RequestParam List<String> uid) {
		String uidsString = String.join(",", uid);
		String uniqueId = UUID.randomUUID().toString();
		return BASE_URL + uniqueId + "?uids=" + uidsString;
	}
}
