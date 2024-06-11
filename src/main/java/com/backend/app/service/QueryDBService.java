package com.backend.app.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.backend.app.dto.AppConfig;
import com.backend.app.dto.output.Response;
import com.backend.app.util.AwsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public <T extends Response> List<T> filterByAreaAndPeriod(String tableName, Class<T> responseType, String city,
															  Double referenceLat, Double referenceLng, int maxDistanceInMeters,
															  int day, int month, int year, int hour, int min, String period) {

		List<T> allItems = (List<T>) filterByCityAndPeriod(tableName, responseType, city,
				year, month, day, hour, min, period);

		List<T> filteredItems = new ArrayList<>();

		for (T item : allItems) {

			double itemLat = item.getPayload().getLat();
			double itemLng = item.getPayload().getLng();

			double distance = calculateDataBoundsService.calculateDistance(referenceLat,
					referenceLng, itemLat, itemLng);

			// filtra per distanza in metri
			if (distance <= maxDistanceInMeters) {
				filteredItems.add(item);
			}
		}

		System.out.println("filtered for distance size: " + filteredItems.size());

		return filteredItems;
	}
	public String updateField(String tableName, List<String> uids, int value, String field) {
		AmazonDynamoDB client = iotClient.getDynamoDBClient(appConfig);

		for (String uid : uids) {
			// scan della tabella per trovare il record con l'uid passato come parametro
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

			// aggiorna il campo del record
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

	public List<?> filterByCityAndPeriod(String tableName, Class<?> responseClass, String city,
										 int year, int month, int day, int hour, int minute, String period) {
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

		long[] timeBounds = getBounds(year, month, day, hour, minute, period);

		// aggiunge i valori dell'espressione per l'intervallo di tempo
		expressionAttributeValues.put(":start_time", new AttributeValue().withN(Long.toString(timeBounds[0])));
		expressionAttributeValues.put(":end_time", new AttributeValue().withN(Long.toString(timeBounds[1])));

		// definisce i nomi degli attributi per il tempo
		expressionAttributeNames.put("#sample_time", "sample_time");

		// crea l'espressione per la scansione con filtro sulla città e sul tempo
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
				.withFilterExpression("#payload.#city = :cityValue AND #sample_time BETWEEN :start_time AND :end_time")
				.withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);

		List<?> scanResult = mapper.scan(responseClass, scanExpression);

		System.out.println("results size: " + scanResult.size());

		for (Object item : scanResult) {
			System.out.println(item.toString());
		}
		return scanResult;
	}

	private long[] getBounds(int year, int month, int day, int hour, int minute, String period) {
		// calcola i millisecondi per l'intervallo di tempo specificato
		long[] timeBounds;
		switch (period.toLowerCase()) {
			case "day":
				timeBounds = calculateDataBoundsService.computeDayBoundsInMillis(year, month, day);
				break;
			case "week":
				timeBounds = calculateDataBoundsService.computeWeekBoundsInMillis(year, month, day);
				break;
			case "season":
				// in questo caso month rappresenta la stagione (1: Spring, 2: Summer, 3: Fall, 4: Winter)
				timeBounds = calculateDataBoundsService.computeSeasonBoundsInMillis(year, month);
				break;
			default:
				// default per date-time
				timeBounds = calculateDataBoundsService.computeDateTimeBoundsInMillis(year, month, day, hour, minute);
				break;
		}
		return timeBounds;
	}
}
