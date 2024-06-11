package com.backend.app.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties( prefix ="aws")
@Component
public class AppConfig{
	private String accessKeyId;
	private String secretKeyId;
	private String clientEndpoint;
}
