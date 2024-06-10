package helpers;

import com.bearingpoint.beyond.test-bpintegration.repository.domain.ProvisioningOrderItemEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class TestUtils {

    static ObjectMapper objectMapper;

    public static ObjectMapper createObjectMapper() {
        if (objectMapper==null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
            JsonNullableModule jnm = new JsonNullableModule();
            objectMapper.registerModule(jnm);
        }

        return objectMapper;
    }

    public static ObjectMapper getObjectMapper() {
        return createObjectMapper();
    }

    public static  <T> T readObjectFromFile(String filename, Class<T> clazz) throws URISyntaxException, IOException {
        return getObjectMapper().readValue(new String(Files.readAllBytes(Paths.get(clazz.getResource(filename).toURI()))), clazz);
    }


}
