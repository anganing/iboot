package com.iboot.pf.framework.jackson.config;

import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.framework.common.util.json.databind.NumberSerializer;
import com.iboot.pf.framework.common.util.json.databind.TimestampLocalDateTimeDeserializer;
import com.iboot.pf.framework.common.util.json.databind.TimestampLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@AutoConfiguration(after = JacksonAutoConfiguration.class)
@Slf4j
public class IbootJacksonAutoConfiguration {

    private static final LocalDateSerializer LOCAL_DATE_SERIALIZER =
            new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE);
    private static final LocalDateDeserializer LOCAL_DATE_DESERIALIZER =
            new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE);
    private static final LocalTimeSerializer LOCAL_TIME_SERIALIZER =
            new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME);
    private static final LocalTimeDeserializer LOCAL_TIME_DESERIALIZER =
            new LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME);

    /**
     * 从 Builder 源头定制（关键：使用 *ByType，避免 handledType 要求）
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer ldtEpochMillisCustomizer() {
        return builder -> builder
                // Long -> Number
                .serializerByType(Long.class, NumberSerializer.INSTANCE)
                .serializerByType(Long.TYPE, NumberSerializer.INSTANCE)
                // LocalDate / LocalTime
                .serializerByType(LocalDate.class, LOCAL_DATE_SERIALIZER)
                .deserializerByType(LocalDate.class, LOCAL_DATE_DESERIALIZER)
                .serializerByType(LocalTime.class, LOCAL_TIME_SERIALIZER)
                .deserializerByType(LocalTime.class, LOCAL_TIME_DESERIALIZER)
                // LocalDateTime < - > EpochMillis
                .serializerByType(LocalDateTime.class, TimestampLocalDateTimeSerializer.INSTANCE)
                .deserializerByType(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE);
    }

    /**
     * 以 Bean 形式暴露 Module（Boot 会自动注册到所有 ObjectMapper）
     */
    @Bean
    public Module timestampSupportModuleBean() {
        SimpleModule m = new SimpleModule("TimestampSupportModule");
        // Long -> Number，避免前端精度丢失
        m.addSerializer(Long.class, NumberSerializer.INSTANCE);
        m.addSerializer(Long.TYPE, NumberSerializer.INSTANCE);
        // LocalDate / LocalTime
        m.addSerializer(LocalDate.class, LOCAL_DATE_SERIALIZER);
        m.addDeserializer(LocalDate.class, LOCAL_DATE_DESERIALIZER);
        m.addSerializer(LocalTime.class, LOCAL_TIME_SERIALIZER);
        m.addDeserializer(LocalTime.class, LOCAL_TIME_DESERIALIZER);
        // LocalDateTime < - > EpochMillis
        m.addSerializer(LocalDateTime.class, TimestampLocalDateTimeSerializer.INSTANCE);
        m.addDeserializer(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE);
        return m;
    }

    /**
     * 初始化全局 JsonUtils，直接使用主 ObjectMapper
     */
    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public JsonUtils jsonUtils(ObjectMapper objectMapper) {
        JsonUtils.init(objectMapper);
        log.debug("[init][初始化 JsonUtils 成功]");
        return new JsonUtils();
    }

}
