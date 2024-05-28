package com.backend.app.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClientBuilder;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.AWSIotDataClientBuilder;
import com.backend.app.dto.AppConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {
	
	@Bean
	public AWSIot getIotClient(AppConfig appConfig){
			return AWSIotClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider
							(new BasicAWSCredentials(appConfig.getAccessKeyId(),appConfig.getSecretKeyId())))
					.withRegion(Regions.EU_NORTH_1)
					.build();
	}
	
	@Bean
	public AWSIotDataClient getIotDataClient( final AppConfig appConfig) {

	return (AWSIotDataClient) AWSIotDataClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials(){
			
			public String getAWSSecretKey(){
				return appConfig.getSecretKeyId();
				
				}
			public String getAWSAccessKeyId(){
				return appConfig.getAccessKeyId();
				}
			}))
			.withRegion(Regions.EU_NORTH_1)
			.build();
	}

	@Bean
	public AmazonDynamoDB getDynamoDBClient(final AppConfig appConfig) {
		return AmazonDynamoDBClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(appConfig.getAccessKeyId(), appConfig.getSecretKeyId())))
				.withRegion(Regions.EU_NORTH_1)
				.build();
	}
}