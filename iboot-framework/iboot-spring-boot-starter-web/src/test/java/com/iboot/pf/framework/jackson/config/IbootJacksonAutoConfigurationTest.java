package com.iboot.pf.framework.jackson.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IbootJacksonAutoConfigurationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        IbootJacksonAutoConfiguration configuration = new IbootJacksonAutoConfiguration();
        objectMapper = JsonMapper.builder()
                .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(configuration.timestampSupportModuleBean())
                .build();
    }

    @Test
    public void testSerializeLong() throws Exception {
        assertEquals("9007199254740991", objectMapper.writeValueAsString(9007199254740991L));
        assertEquals("\"9007199254740992\"", objectMapper.writeValueAsString(9007199254740992L));
        assertEquals("-9007199254740991", objectMapper.writeValueAsString(-9007199254740991L));
        assertEquals("\"-9007199254740992\"", objectMapper.writeValueAsString(-9007199254740992L));
    }

    @Test
    public void testSerializeAndDeserializeLocalDate() throws Exception {
        LocalDate value = LocalDate.of(2026, 8, 25);

        assertEquals("\"2026-08-25\"", objectMapper.writeValueAsString(value));
        assertEquals(value, objectMapper.readValue("\"2026-08-25\"", LocalDate.class));
    }

    @Test
    public void testSerializeAndDeserializeLocalTime() throws Exception {
        LocalTime value = LocalTime.of(8, 30);

        assertEquals("\"08:30:00\"", objectMapper.writeValueAsString(value));
        assertEquals(value, objectMapper.readValue("\"08:30:00\"", LocalTime.class));
    }

}
