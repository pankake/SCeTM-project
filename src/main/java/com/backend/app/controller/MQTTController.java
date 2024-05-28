package com.backend.app.controller;

import com.backend.app.dto.input.BrightRequest;
import com.backend.app.dto.input.RoadRequest;
import com.backend.app.dto.input.SoundRequest;
import com.backend.app.service.MQTTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publish")
public class MQTTController {
	
	@Autowired
	MQTTService mqttService;

	private static final String ROAD_CONDITIONS_TOPIC = "road_conditions_topic";
	private static final String SOUND_DATA_TOPIC = "sound_data_topic";
	private static final String BRIGHT_DATA_TOPIC = "light_data_topic";
	private static final String PUBLISHED_RETURN_MSG = "message Published Successfully";
	
	@PostMapping("/road")
	public String roadDataMessage(@RequestBody RoadRequest roadData) {

		mqttService.publish(roadData, ROAD_CONDITIONS_TOPIC);
		return PUBLISHED_RETURN_MSG;
	}

	@PostMapping("/sound")
	public String soundDataMessage(@RequestBody SoundRequest soundData) {

		mqttService.publish(soundData, SOUND_DATA_TOPIC);
		return PUBLISHED_RETURN_MSG;
	}

	@PostMapping("/bright")
	public String brightDataMessage(@RequestBody BrightRequest brightRequest) {

		mqttService.publish(brightRequest, BRIGHT_DATA_TOPIC);
		return PUBLISHED_RETURN_MSG;
	}
}
