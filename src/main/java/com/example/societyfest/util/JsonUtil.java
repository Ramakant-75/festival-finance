package com.example.societyfest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // ✅ Add this line
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // optional: for ISO-8601
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String toJson(Object obj) {
        try {
            return obj == null ? null : mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{ \"error\": \"Could not serialize: " + e.getMessage().replace("\"", "'") + "\" }";
        }
    }
}
