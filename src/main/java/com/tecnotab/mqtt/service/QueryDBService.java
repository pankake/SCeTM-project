package com.tecnotab.mqtt.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.tecnotab.mqtt.dto.AppConfig;
import com.tecnotab.mqtt.dto.output.Response;
import com.tecnotab.mqtt.util.AwsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QueryDBService {
	@Autowired
	private AwsConfig iotClient;
	@Autowired
	private AppConfig appConfig;
	@Autowired
	private ComputeBoundsService calculateDataBoundsService;

	public List<?> simpleScan(String tableName, Class<?> responseClass) {
		AmazonDynamoDB client = iotClient.getDynamoDBClient(appConfig);

		DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
				.withTableNameOverride(DynamoDBMapperConfig.TableNameOverride
						.withTableNameReplacement(tableName))
				.build();

		DynamoDBMapper mapper = new DynamoDBMapper(client, mapperConfig);
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

		List<?> scanResult = mapper.scan(responseClass, scanExpression);

		System.out.println("results size: " + scanResult.size());

		for (Object item : scanResult) {
			System.out.println(item.toString());
		}
		return scanResult;
	}

	public List<?> filterByCity(String tableName, Class<?> responseClass, String city) {
		AmazonDynamoDB client = iotClient.getDynamoDBClient(appConfig);

		DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
				.withTableNameOverride(DynamoDBMapperConfig.TableNameOverride
						.withTableNameReplacement(tableName))
				.build();

		DynamoDBMapper mapper = new DynamoDBMapper(client, mapperConfig);

		// definisce i valori dell'espressione
		Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
		expressionAttributeValues.put(":cityValue", new AttributeValue().withS(city));

		// definisce i nomi degli attributi per le chiavi annidate
		Map<String, String> expressionAttributeNames = new HashMap<>();
		expressionAttributeNames.put("#payload", "payload");
		expressionAttributeNames.put("#city", "city");

		// crea l'espressione per la scansione
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
				.withFilterExpression("#payload.#city = :cityValue")
				.withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);

		List<?> scanResult = mapper.scan(responseClass, scanExpression);

		System.out.println("results size: " + scanResult.size());

		for (Object item : scanResult) {
			System.out.println(item.toString());
		}
		return scanResult;
	}

	public <T extends Response> List<T> filterByAreaAndTime(String table, Class<T> responseType,
															String city, Double referenceLat, Double referenceLng,
															int maxDistanceInMeters, int hour, int minute) {

		List<T> allItems =
				(List<T>) filterByCity(table, responseType, city);

		List<T> filteredItems = new ArrayList<>();

		long[] timeBounds = calculateDataBoundsService.calculateTimeBounds(hour, minute);

		for (T item : allItems) {

			double itemLat = item.getPayload().getLat();
			double itemLng = item.getPayload().getLng();
			long sampleTimeMillis = item.getSampleTime();

			double distance = calculateDataBoundsService.calculateDistance(referenceLat, referenceLng, itemLat, itemLng);

			// filtra per distanza e tempo
			if (distance <= maxDistanceInMeters &&
					calculateDataBoundsService.isWithinTimeBounds(sampleTimeMillis, timeBounds)) {
				filteredItems.add(item);
			}
		}

		System.out.println("filtered for timezone size: " + filteredItems.size());

		return filteredItems;
	}

	public <T extends Response> List<T> filterByAreaAndDate(String tableName, Class<T> responseType, String city,
                                                            Double referenceLat, Double referenceLng, int maxDistanceInMeters,
                                                            int day, int month, int year) {

		List<T> allItems = (List<T>) filterByCity(tableName, responseType, city);

		List<T> filteredItems = new ArrayList<>();

		long[] timeBounds = calculateDataBoundsService.calculateDayBounds(day, month, year);

		for (T item : allItems) {

			double itemLat = item.getPayload().getLat();
			double itemLng = item.getPayload().getLng();
			long sampleTimeMillis = item.getSampleTime();

			double distance = calculateDataBoundsService.calculateDistance(referenceLat, referenceLng, itemLat, itemLng);

			// filtra per distanza e giorno
			if (distance <= maxDistanceInMeters &&
					calculateDataBoundsService.isWithinDayBounds(sampleTimeMillis, timeBounds)) {
				filteredItems.add(item);
			}
		}

		System.out.println("filtered for day size: " + filteredItems.size());

		return filteredItems;
	}
	public String updateField(String tableName, List<String> uids, int value, String field) {
		AmazonDynamoDB client = iotClient.getDynamoDBClient(appConfig);

		for (String uid : uids) {
			// Step 1: Scan the table to find the item with the given UID
			Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
			expressionAttributeValues.put(":uid", new AttributeValue().withS(uid));

			ScanRequest scanRequest = new ScanRequest()
					.withTableName(tableName)
					.withFilterExpression("payload.uid = :uid")
					.withExpressionAttributeValues(expressionAttributeValues);

			ScanResult scanResult = client.scan(scanRequest);

			List<Map<String, AttributeValue>> items = scanResult.getItems();

			if (items.isEmpty()) {
				System.out.println("No item found with uid: " + uid);
				continue;
			}

			Map<String, AttributeValue> item = items.get(0);
			String sampleTime = item.get("sample_time").getN();

			// update the field of the found item
			Map<String, AttributeValue> key = new HashMap<>();
			key.put("sample_time", new AttributeValue().withN(sampleTime));

			Map<String, AttributeValue> attributeValues = new HashMap<>();
			attributeValues.put(":value", new AttributeValue().withN(Integer.toString(value)));

			UpdateItemRequest updateRequest = new UpdateItemRequest()
					.withTableName(tableName)
					.withKey(key)
					.withUpdateExpression("SET payload."+field+" = payload."+field+" + :value")
					.withExpressionAttributeValues(attributeValues);

			client.updateItem(updateRequest);

			System.out.println("UpdateItem succeeded for uid: " + uid);
		}

		return "UpdateItem operations completed";
	}
}
