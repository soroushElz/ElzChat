package com.example.ChatApplication.Notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayloadConverter implements AttributeConverter<INotificationPayload,String> {
    @Autowired
    ObjectMapper mapper;
    @Override
    public String convertToDatabaseColumn(INotificationPayload attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to serialize Payload!",e);
        }
    }

    @Override
    public INotificationPayload convertToEntityAttribute(String dbData) {
        if(dbData==null || dbData.isEmpty())
            return null;
        try {
            return mapper.readValue(dbData,INotificationPayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to deserialize ",e);
        }
    }
}
