package com.tecnotab.mqtt.service;

import com.google.gson.Gson;
import com.tecnotab.mqtt.dto.AppConfig;
import com.tecnotab.mqtt.dto.input.Request;
import com.tecnotab.mqtt.util.AwsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Configuration
public class MQTTService {

	@Autowired
	private AwsConfig iotClient;
	
	@Autowired
	private AppConfig appConfig;
	private static final Gson gson = new Gson();
	
	public void publish(Request publishDTO, String topic) {

		ByteBuffer bb = StandardCharsets.UTF_8.encode(gson.toJson(publishDTO));
		com.amazonaws.services.iotdata.model.PublishRequest publishRequest = new com.amazonaws.services.iotdata.model.PublishRequest();
		publishRequest.withPayload(bb);
		publishRequest.withTopic(topic);
		publishRequest.setQos(0);
		iotClient.getIotDataClient(appConfig).publish(publishRequest);
		System.out.println("message Published successfully");
	}
}
