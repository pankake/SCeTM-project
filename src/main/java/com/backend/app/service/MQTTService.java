package com.backend.app.service;

import com.amazonaws.services.iotdata.model.PublishRequest;
import com.backend.app.dto.AppConfig;
import com.backend.app.dto.input.Request;
import com.backend.app.util.AwsConfig;
import com.google.gson.Gson;
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
		PublishRequest publishRequest =	new PublishRequest();

		publishRequest.withPayload(bb);
		publishRequest.withTopic(topic);
		publishRequest.setQos(1); // QoS 1: "almeno una volta"

		iotClient.getIotDataClient(appConfig).publish(publishRequest);
		System.out.println("message Published successfully");
	}
}
