package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1SummaryDomainCustomerOrderSummary;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullableModule;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class TemplateDataFilteringServiceTest {
	public static final String DRAFT_ORDER_ID = "73914";
	private ObjectMapper mapper;

	TemplateDataFilteringService templateDataFilteringService;

	@BeforeEach
	void setUp() {
		templateDataFilteringService = new TemplateDataFilteringService();
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
		JsonNullableModule jnm = new JsonNullableModule();
		mapper.registerModule(jnm);
	}

	@Test
	void filterOrderSummaryServiceCharacteristics() throws Exception {
		ProductorderingV1SummaryDomainCustomerOrderSummary orderSummary = getOrderSummaryFromFile(
				"/getCustomerOrderSummary.json");
		final Set<String> filterList = Set.of("RT_CUSTOMER", "SALES_AGENT_USERNAME", "SALES_AGENT_NAME",
				"SALES_AGENT_EMAIL", "OPI_NUMBER");
		templateDataFilteringService.filterOrderSummaryServiceCharacteristics(orderSummary, DRAFT_ORDER_ID, filterList);

		final String res = mapper.writeValueAsString(orderSummary);
		filterList.forEach(el -> Assertions.assertThat(res).doesNotContain(":" + el));
	}

	private ProductorderingV1SummaryDomainCustomerOrderSummary getOrderSummaryFromFile(String fileName)
			throws Exception {
		return mapper.readValue(new String(
						Files.readAllBytes(Paths.get(this.getClass().getResource(fileName).toURI()))),
				ProductorderingV1SummaryDomainCustomerOrderSummary.class);
	}
}