package com.bearingpoint.beyond.test-bpintegration.configuration;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Config for serializing payloads when returning syncronous responses to API calls.
 */
@Configuration
public class SerializationConfig {
    public static final String DATE_TIME_FORMAT = "uuuu-MM-dd'T'HH:mm:ssXXX";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withResolverStyle(ResolverStyle.STRICT);

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.serializerByType(BigDecimal.class, ToStringSerializer.instance);
            builder.serializers(offsetDateTimeSerializer(DATE_TIME_FORMATTER));
            builder.deserializers(offsetDateTimeDeserializer(DATE_TIME_FORMATTER));
        };
    }
    private StdSerializer<OffsetDateTime> offsetDateTimeSerializer(DateTimeFormatter formatter) {
        return new OffsetDateTimeSerializer(OffsetDateTimeSerializer.INSTANCE, false, formatter) {};
    }

    private StdDeserializer<OffsetDateTime> offsetDateTimeDeserializer(DateTimeFormatter formatter) {
        return new InstantDeserializer<OffsetDateTime>(InstantDeserializer.OFFSET_DATE_TIME, formatter) {};
    }
}
