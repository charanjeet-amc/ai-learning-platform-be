package com.ailearning.platform.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    /**
     * Server stores LocalDateTime in UTC (Railway JVM runs in UTC).
     * Append 'Z' so browsers interpret the ISO string as UTC and convert to local time.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.serializerByType(LocalDateTime.class,
                new JsonSerializer<LocalDateTime>() {
                    private static final DateTimeFormatter FMT =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    @Override
                    public void serialize(LocalDateTime value, JsonGenerator gen,
                                          SerializerProvider provider) throws IOException {
                        gen.writeString(value.format(FMT));
                    }
                });
    }
}
