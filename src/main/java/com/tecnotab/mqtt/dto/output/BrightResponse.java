package com.tecnotab.mqtt.dto.output;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.google.maps.model.LatLng;
import lombok.Data;

@Data
public class BrightResponse implements Response {
    private long sampleTime;
    private BrightPayload payload;

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
    @DynamoDBAttribute(attributeName = "sample_time")
    public long getSampleTime() {
        return sampleTime;
    }

    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.M)
    @DynamoDBAttribute(attributeName = "payload")
    public BrightPayload getPayload() {
        return payload;
    }

    @Data
    public static class BrightPayload implements Payload {
        private double lux;
        private double lat;
        private double lng;
        private String city;
        private String uid;
        private int reliability;
        private int relevance;

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        @DynamoDBAttribute(attributeName = "luxLevel")
        public double getLux() {
            return lux;
        }

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        @DynamoDBAttribute(attributeName = "lat")
        public double getLat() {
            return lat;
        }

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        @DynamoDBAttribute(attributeName = "lng")
        public double getLng() {
            return lng;
        }

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
        @DynamoDBAttribute(attributeName = "city")
        public String getCity() {
            return city;
        }

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
        @DynamoDBAttribute(attributeName = "uid")
        public String getUid() {
            return uid;
        }

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        @DynamoDBAttribute(attributeName = "reliability")
        public int getReliability() {
            return reliability;
        }

        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        @DynamoDBAttribute(attributeName = "relevance")
        public int getRelelevance() {
            return relevance;
        }

        public LatLng toLatLng() {
            return new LatLng(lat, lng);
        }
    }
}
