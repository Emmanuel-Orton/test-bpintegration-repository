package com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.InflightOrderChangesDto;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ProvisioningOrderItemDto;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.model.OrderingV1DomainOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.model.OrderingV1DomainOrderList;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.api.ProductInventoryV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.model.ProductinventoryV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainServiceOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainServiceOrderItem;
import com.bearingpoint.beyond.test-bpintegration.repository.ProvisioningOrderItemsRepository;
import com.bearingpoint.beyond.test-bpintegration.repository.domain.ProvisioningOrderItemEntity;
import com.bearingpoint.beyond.test-bpintegration.service.*;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.codehaus.plexus.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvisioningHandlerTest {

	public static final String TENANT_NAME = "test1";
	public static final String TENANT_WS_NAME = "test1_ws";
	public final String WS_ORDER_ID = "2541563";

	public final String RT_ORDER_ID = "2541560";
	public static final String MODIFIED_IDS = "123";

	@Mock
	ProvisioningOrderItemsRepository mockProvisioningOrderItemsRepository;
	@Mock
	ProductInventoryV1Api mockProductInventoryV1Api;
	@Mock
	TasksService mockTasksService;

	@Mock
	InfonovaLinkService infonovaLinkService;
	@Mock
	CustomerDataService mockCustomerDataService;
	@Mock
	OrderingService mockOrderingService;
	@Mock
	ProductOrdersService productOrdersService;
	//@Mock
	private ObjectMapper mapper;

	@InjectMocks
    ProvisioningHandler provisioningHandler;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(provisioningHandler, "retailTenant", TENANT_NAME);
		ReflectionTestUtils.setField(provisioningHandler, "wholesaleTenant", TENANT_WS_NAME);
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
		JsonNullableModule jnm = new JsonNullableModule();
		mapper.registerModule(jnm);
		ReflectionTestUtils.setField(provisioningHandler, "objectMapper", mapper);
	}

	@SneakyThrows
	@Test
	void raiseOrderServiceProvisioningTaskTest() {
		String tenant = TENANT_NAME;
		String exampleLink = "www.example.com";

		List<ProvisioningOrderItemEntity> provisioningOrderItems = Arrays.asList(mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/provisioningOrderItemEntity.json").toURI()))), ProvisioningOrderItemEntity[].class));
		ProductinventoryV1DomainProduct relatedBundle = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/relatedBundle.json").toURI()))), ProductinventoryV1DomainProduct.class);
		OrderingV1DomainOrderList orderHierarchy = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/orderHierarchy.json").toURI()))), OrderingV1DomainOrderList.class);
		List<ProvisioningOrderItemEntity> dbSaveResponses = Arrays.asList(mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/entityDBSaveOutput.json").toURI()))), ProvisioningOrderItemEntity[].class));
		TaskV2DomainTask serviceOrderTask = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/serviceOrderTask.json").toURI()))), TaskV2DomainTask.class);
		TaskV2DomainTask responseTask = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/responseTask.json").toURI()))), TaskV2DomainTask.class);

		String referenceId = orderHierarchy.getList().stream().filter(order -> StringUtils.equals(order.getType(), "NEW_BUNDLE"))
				.findFirst().map(OrderingV1DomainOrder::getReferenceId)
				.orElseThrow(() -> new IllegalArgumentException("Bundle reference does not exist!"));

		when(mockProvisioningOrderItemsRepository.findByWholesaleOrderIdAndTaskId(provisioningOrderItems.get(0).getWholesaleOrderId(), null))
				.thenReturn(provisioningOrderItems);
		when(mockOrderingService.getOrderHierarchy(tenant, provisioningOrderItems.get(0).getRetailOrderId().toString())).thenReturn(orderHierarchy);
		when(mockProductInventoryV1Api.getProductInventoryV1ProductsProduct(tenant, referenceId)).thenReturn(Mono.just(relatedBundle));
		when(mockTasksService.createTask(provisioningOrderItems.get(0).getTenant(), serviceOrderTask)).thenReturn(responseTask);
		when(infonovaLinkService.getCorrectTenant(TenantType.RETAIL)).thenReturn(TENANT_NAME);
		when(infonovaLinkService.getOrderLink(TenantType.WHOLESALE_CSM, provisioningOrderItems.get(0).getWholesaleOrderId().toString(), provisioningOrderItems.get(0).getWholesaleBillingAccountId().toString())).thenReturn(exampleLink);
		when(mockProvisioningOrderItemsRepository.save(any())).thenReturn(dbSaveResponses.get(0));

		provisioningHandler.raiseOrderServiceProvisioningTask(provisioningOrderItems.get(0).getWholesaleOrderId());

		verify(mockProvisioningOrderItemsRepository, times(1)).findByWholesaleOrderIdAndTaskId(provisioningOrderItems.get(0).getWholesaleOrderId(), null);
		verify(mockOrderingService, times(1)).getOrderHierarchy(tenant, provisioningOrderItems.get(0).getRetailOrderId().toString());
		verify(mockProductInventoryV1Api, times(1)).getProductInventoryV1ProductsProduct(tenant, referenceId);
		verify(mockTasksService, times(1)).createTask(provisioningOrderItems.get(0).getTenant(), serviceOrderTask);
		verify(infonovaLinkService, times(1)).getCorrectTenant(TenantType.RETAIL);
		verify(infonovaLinkService, times(1)).getOrderLink(TenantType.WHOLESALE_CSM, provisioningOrderItems.get(0).getWholesaleOrderId().toString(), provisioningOrderItems.get(0).getWholesaleBillingAccountId().toString());
		verify(mockProvisioningOrderItemsRepository, times(3)).save(any());
	}


	@Test
	void createWholesaleProvisioningDatabaseEntriesTest() throws Exception {
		OrderingV1DomainOrder retailOrder = new OrderingV1DomainOrder();
		retailOrder.setReferenceName("testReferenceName");
		retailOrder.setParameters(Map.of(
				"opi_num", "1234567",
				"draft_order_placed_dt", "2023-01-11T10:07:10.380138Z"
		));
		OrderingV1DomainOrderList orderHierarchy = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/orderHierarchy.json").toURI()))), OrderingV1DomainOrderList.class);

		when(mockOrderingService.getOrder(any(), any())).thenReturn(retailOrder);
		when(mockOrderingService.getOrderHierarchy(infonovaLinkService.getCorrectTenant(TenantType.WHOLESALE), "6953486")).thenReturn(orderHierarchy);
		when(mockProvisioningOrderItemsRepository.save(any())).thenReturn(null);

		ProductorderingV1DomainProductOrder productOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/productOrder.json").toURI()))), ProductorderingV1DomainProductOrder.class);

		when(infonovaLinkService.getCorrectTenant(TenantType.RETAIL)).thenReturn(TENANT_NAME);
		when(productOrdersService.getProductOrder(TENANT_NAME, RT_ORDER_ID)).thenReturn(productOrder);


		provisioningHandler.createWholesaleProvisioningDatabaseEntries(RT_ORDER_ID, "12345678", "6953486", "12345678", "12345678", "12345678");

		verify(mockOrderingService, times(1)).getOrder(any(), any());
		verify(mockOrderingService, times(1)).getOrderHierarchy(any(), any());
		verify(mockProvisioningOrderItemsRepository, times(3)).save(any());
	}

	@Test
	//@Disabled // TODO fix
	void createInflightWholesaleProvisioningDatabaseEntriesTest() throws Exception {
		OrderingV1DomainOrder retailOrder = new OrderingV1DomainOrder();
		retailOrder.setReferenceName("testReferenceName");
		retailOrder.setParameters(Map.of(
					"opi_num", "1234567",
					"draft_order_placed_dt", "2023-01-11T10:07:10.380138Z"
				));
		OrderingV1DomainOrderList orderHierarchy = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/orderHierarchy.json").toURI()))), OrderingV1DomainOrderList.class);

		when(infonovaLinkService.getCorrectTenant(TenantType.RETAIL)).thenReturn(TENANT_NAME);
		when(mockOrderingService.getOrder(any(), anyString())).thenReturn(retailOrder);
		when(infonovaLinkService.getCorrectTenant(TenantType.WHOLESALE)).thenReturn(TENANT_WS_NAME);
		when(mockOrderingService.getOrderHierarchy(anyString(), anyString())).thenReturn(orderHierarchy);

		ProductorderingV1DomainProductOrder productOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/productOrder.json").toURI()))), ProductorderingV1DomainProductOrder.class);

		when(infonovaLinkService.getCorrectTenant(TenantType.RETAIL)).thenReturn(TENANT_NAME);
		when(productOrdersService.getProductOrder(TENANT_NAME, RT_ORDER_ID)).thenReturn(productOrder);

		provisioningHandler.createInflightWholesaleProvisioningDatabaseEntries(RT_ORDER_ID, "6953486", WS_ORDER_ID, "12345678", "12345678", "12345678", "6953482, 6953483, 6953485");

		verify(infonovaLinkService, times(2)).getCorrectTenant(TenantType.RETAIL);
		verify(infonovaLinkService, times(1)).getCorrectTenant(TenantType.WHOLESALE);
		verify(mockOrderingService, times(1)).getOrder(ArgumentMatchers.eq(TENANT_NAME), anyString());
		verify(mockOrderingService, times(1)).getOrderHierarchy(ArgumentMatchers.eq(TENANT_WS_NAME), anyString());
		verify(mockProvisioningOrderItemsRepository, times(3)).save(any());
	}

	@SneakyThrows
	@Test
	void convertItemsTest() {
		List<ProvisioningOrderItemEntity> provisioningOrderItems = Arrays.asList(mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/provisioningOrderItemEntity.json").toURI()))), ProvisioningOrderItemEntity[].class));

		List<ProvisioningOrderItemDto> provisioningOrderItemDtosShort = provisioningHandler.convertItems(provisioningOrderItems, true);
		assertEquals(provisioningOrderItemDtosShort.size(), provisioningOrderItems.size());
		for(int i=0; i<provisioningOrderItemDtosShort.size(); i++) {
			assertEquals(provisioningOrderItemDtosShort.get(i).getId(), provisioningOrderItems.get(i).getId());
			assertEquals(provisioningOrderItemDtosShort.get(i).getOpiNumber(), provisioningOrderItems.get(i).getOpiNumber());
			assertEquals(provisioningOrderItemDtosShort.get(i).getWholesaleOrderId(), provisioningOrderItems.get(i).getWholesaleOrderId());
			assertEquals(provisioningOrderItemDtosShort.get(i).getWholesaleService(), provisioningOrderItems.get(i).getWholesaleService());
			assertEquals(provisioningOrderItemDtosShort.get(i).getWholesaleServiceOffer(), provisioningOrderItems.get(i).getWholesaleServiceOffer());
			assertEquals(provisioningOrderItemDtosShort.get(i).getWholesaleServiceOrderId(), provisioningOrderItems.get(i).getWholesaleServiceOrderId());
			assertEquals(provisioningOrderItemDtosShort.get(i).getOrderQuantity(), provisioningOrderItems.get(i).getOrderQuantity());
			assertEquals(provisioningOrderItemDtosShort.get(i).getRequestedQuantity(), provisioningOrderItems.get(i).getRequestedQuantity());
			assertEquals(provisioningOrderItemDtosShort.get(i).getRequestedActivationDate(), provisioningOrderItems.get(i).getRequestedActivationDate());
			assertEquals(provisioningOrderItemDtosShort.get(i).getEarliestActivationDate(), provisioningOrderItems.get(i).getEarliestActivationDate());
		}
		List<ProvisioningOrderItemDto> provisioningOrderItemDtosLong = provisioningHandler.convertItems(provisioningOrderItems, false);
		assertEquals(provisioningOrderItemDtosLong.size(), provisioningOrderItems.size());
		for(int i=0; i<provisioningOrderItemDtosLong.size(); i++) {
			assertEquals(provisioningOrderItemDtosLong.get(i).getId(), provisioningOrderItems.get(i).getId());
			assertEquals(provisioningOrderItemDtosLong.get(i).getRetailOrderId(), provisioningOrderItems.get(i).getRetailOrderId());
			assertEquals(provisioningOrderItemDtosLong.get(i).getRetailBillingAccountId(), provisioningOrderItems.get(i).getRetailBillingAccountId());
			assertEquals(provisioningOrderItemDtosLong.get(i).getWholesaleBillingAccountId(), provisioningOrderItems.get(i).getWholesaleBillingAccountId());
			assertEquals(provisioningOrderItemDtosLong.get(i).getWholesaleService(), provisioningOrderItems.get(i).getWholesaleService());
			assertEquals(provisioningOrderItemDtosLong.get(i).getDafStatus(), provisioningOrderItems.get(i).getDafStatus());
			assertEquals(provisioningOrderItemDtosLong.get(i).getServiceProvisioningId().longValue(), provisioningOrderItems.get(i).getServiceProvisioningId().longValue());
			assertEquals(provisioningOrderItemDtosLong.get(i).getTenant(), provisioningOrderItems.get(i).getTenant());
			assertEquals(provisioningOrderItemDtosLong.get(i).getTaskId(), provisioningOrderItems.get(i).getTaskId());
			assertEquals(provisioningOrderItemDtosLong.get(i).getOpiNumber(), provisioningOrderItems.get(i).getOpiNumber());
			assertEquals(provisioningOrderItemDtosLong.get(i).getWholesaleOrderId(), provisioningOrderItems.get(i).getWholesaleOrderId());
			assertEquals(provisioningOrderItemDtosLong.get(i).getWholesaleServiceOffer(), provisioningOrderItems.get(i).getWholesaleServiceOffer());
			assertEquals(provisioningOrderItemDtosLong.get(i).getWholesaleServiceOrderId(), provisioningOrderItems.get(i).getWholesaleServiceOrderId());
			assertEquals(provisioningOrderItemDtosLong.get(i).getOrderQuantity(), provisioningOrderItems.get(i).getOrderQuantity());
			assertEquals(provisioningOrderItemDtosLong.get(i).getRequestedQuantity(), provisioningOrderItems.get(i).getRequestedQuantity());
			assertEquals(provisioningOrderItemDtosLong.get(i).getRequestedActivationDate(), provisioningOrderItems.get(i).getRequestedActivationDate());
			assertEquals(provisioningOrderItemDtosLong.get(i).getEarliestActivationDate(), provisioningOrderItems.get(i).getEarliestActivationDate());
		}
	}

	@SneakyThrows
	@Test
	void findProvisioningItemsSpecifiedTest() {
		Long wholesaleOrderId = 6953486L;
        Set<String> removedIds = Set.of("6953488", "6953489", "1111111");
		List<ProvisioningOrderItemEntity> provisioningOrderItems = Arrays.asList(mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/provisioningOrderItemEntity.json").toURI()))), ProvisioningOrderItemEntity[].class));

		when(mockProvisioningOrderItemsRepository.findByWholesaleOrderId(wholesaleOrderId)).thenReturn(provisioningOrderItems);
		List<ProvisioningOrderItemEntity> provisioningItemsSpecified = provisioningHandler.findProvisioningItemsSpecified(wholesaleOrderId.toString(), removedIds);
		assertEquals(provisioningItemsSpecified.size(), 2);
		for(ProvisioningOrderItemEntity outEntity : provisioningItemsSpecified) {
			assertEquals(removedIds.contains(outEntity.getWholesaleServiceOrderId()), true);
		}
		verify(mockProvisioningOrderItemsRepository, times(1)).findByWholesaleOrderId(wholesaleOrderId);
	}

	@Test
	public void getQuantityFromProductOrder() throws Exception {
		ProductorderingV1DomainProductOrder productOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/productOrder.json").toURI()))), ProductorderingV1DomainProductOrder.class);

		Long result = provisioningHandler.getQuantityFromProductOrder(productOrder, "8a918f65-0125-4f11-91ce-0537ce4f6b41");

		Assertions.assertNotNull(result);
		Assertions.assertEquals(5, result);
	}


	@Test
	public void getQuantityFromProductOrderWithTAE() throws Exception {
		ProductorderingV1DomainProductOrder productOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/productOrderWithTAE.json").toURI()))), ProductorderingV1DomainProductOrder.class);

		Long result = provisioningHandler.getQuantityFromProductOrder(productOrder, "821afe52-9dbd-4e9a-a86b-63f2bddcf0f7");

		Assertions.assertNotNull(result);
		Assertions.assertEquals(5, result);
	}


	@Test
	public void getQuantityFromServiceOrder() throws Exception {
		com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder serviceOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/serviceOrder.json").toURI()))), com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder.class);

		Long result = provisioningHandler.getQuantityFromServiceOrder(serviceOrder, "7107823");

		Assertions.assertNotNull(result);
		Assertions.assertEquals(3, result);
	}

	@Test
	public void getQuantityFromServiceOrderItemOfEvent() throws Exception {
		ServiceorderingV1DomainServiceOrder serviceOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/serviceOrder.json").toURI()))), ServiceorderingV1DomainServiceOrder.class);

		Optional<ServiceorderingV1DomainServiceOrderItem> first = serviceOrder.getOrderItems().stream()
				.filter(pre -> "7107823".equals(pre.getId()))
				.findFirst();

		if (first.isPresent()) {
			String result = provisioningHandler.getCharacteristicValueFromServiceOrderItemOfEvent(first.get(), "QUANTITY");
			Assertions.assertEquals("3", result);
		} else {
			Assertions.fail("No service order item found.");
		}


	}





	@Test
	public void getDateFromString_withDateSet() {
		Instant dateFromString = ProvisioningHandler.getDateFromStringAsInstant("2023-01-04");
		Assertions.assertNotNull(dateFromString);

		OffsetDateTime offsetDateTime = dateFromString.atOffset(ZoneOffset.UTC);

		Assertions.assertEquals(2023, offsetDateTime.get(ChronoField.YEAR));
		Assertions.assertEquals(1, offsetDateTime.get(ChronoField.MONTH_OF_YEAR));
		Assertions.assertEquals(4, offsetDateTime.get(ChronoField.DAY_OF_MONTH));
	}

	@Test
	public void getDateFromString_withNoDateSet() {
		Instant dateFromString = ProvisioningHandler.getDateFromStringAsInstant(null);

		Assertions.assertNull(dateFromString);
	}

	@Test
	public void getDateAsString_withDateSet() {
		//OffsetDateTime oft = OffsetDateTime.of(
		LocalDateTime of = LocalDateTime.of(2023, 01, 04, 0, 0, 0);


		Instant instant = of.toInstant(ZoneOffset.UTC);


		String dateFromString = ProvisioningHandler.getDateAsString(instant);

		Assertions.assertNotNull(dateFromString);
		Assertions.assertEquals("2023-01-04", dateFromString);
//		Assertions.assertEquals(2023, dateFromString.get(ChronoField.YEAR));
//		Assertions.assertEquals(1, dateFromString.get(ChronoField.MONTH_OF_YEAR));
//		Assertions.assertEquals(4, dateFromString.get(ChronoField.DAY_OF_MONTH));
	}

	@Test
	public void getDateAsString_withNoDateSet() {
		String dateAsString = ProvisioningHandler.getDateAsString(null);

		Assertions.assertNull(dateAsString);
	}

	@Test
	@Disabled
	void inflightModifyWholesaleProvisioningItems() {
		// TODO
	}

	@Test
	void removeProvisioningItemsFromDatabase() {
		when(mockProvisioningOrderItemsRepository.findByWholesaleOrderId(Long.parseLong(WS_ORDER_ID)))
				.thenReturn(List.of(ProvisioningOrderItemEntity.builder()
						.wholesaleOrderId(Long.parseLong(WS_ORDER_ID))
						.wholesaleServiceOrderId(MODIFIED_IDS)
						.build()));

		Set<String> removedIdsSet = Set.of(MODIFIED_IDS);

		List<ProvisioningOrderItemEntity> provisioningItemsSpecified = new ArrayList<>();
		ProvisioningOrderItemEntity provisioningItem = ProvisioningOrderItemEntity.builder()
				.wholesaleOrderId(Long.parseLong(WS_ORDER_ID))
				.wholesaleServiceOrderId(MODIFIED_IDS)
				.build();
		provisioningItemsSpecified.add(provisioningItem);

		provisioningHandler.removeProvisioningItemsFromDatabase(WS_ORDER_ID, removedIdsSet);
		Mockito.verify(mockProvisioningOrderItemsRepository, Mockito.times(1)).deleteAll(provisioningItemsSpecified);
	}


	@Test
	void fakeTestMethod() {

		//		OffsetDateTime now = OffsetDateTime.now();
//		now = now.minus(20, ChronoUnit.DAYS);
//
//		String date = offsetDateTimeFormatter.format(now);
//		System.out.println("Result: "+ date);


		//offsetDateTimeFormatter.parse()

		//OffsetDateTime.parse("2023-01-11T10:07:10.380138Z");

		OffsetDateTime parse = OffsetDateTime.parse("2023-01-11T10:07:10.380138Z");

		Set<String> strings = Set.of("33432=20", "3277=2", "4377=6");

		Map<String, String> map=
				strings.stream()
						.map(s -> s.split("="))
						.collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));

		System.out.println("Map: " + map);

	}

	@Test
	void getDateFromStringAsOffsetDateTime() {

		OffsetDateTime offsetDateTime = ProvisioningHandler.getDateFromStringAsOffsetDateTime("2023-02-02");

		Assertions.assertEquals(2, offsetDateTime.get(ChronoField.DAY_OF_MONTH));
		Assertions.assertEquals(2, offsetDateTime.get(ChronoField.MONTH_OF_YEAR));
		Assertions.assertEquals(2023, offsetDateTime.get(ChronoField.YEAR));

	}


	@Test
	public void determineInflightOrderChanges() throws Exception {
		ServiceorderingV1DomainServiceOrder inflightServiceOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/inflightOrderChange3x.json").toURI()))), ServiceorderingV1DomainServiceOrder.class);

		InflightOrderChangesDto inflightOrderChangesDto = provisioningHandler.determineInflightOrderChanges(inflightServiceOrder);

		Assertions.assertNotNull(inflightOrderChangesDto);
		Assertions.assertEquals(1, inflightOrderChangesDto.getAddedItems().size());
		Assertions.assertEquals(1, inflightOrderChangesDto.getModifiedItems().size());
		Assertions.assertEquals(1, inflightOrderChangesDto.getRemovedItems().size());
		Assertions.assertEquals("7126880", inflightOrderChangesDto.getAddedItems().get(0));
		Assertions.assertEquals("7126876=8", inflightOrderChangesDto.getModifiedItems().get(0));
		Assertions.assertEquals("7126874", inflightOrderChangesDto.getRemovedItems().get(0));
	}


	@Test
	public void determineInflightOrderChanges_whenModifyHasNoQuantity() throws Exception {
		ServiceorderingV1DomainServiceOrder inflightServiceOrder = mapper.readValue(new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/inflightOrderChangeModifyNoQuantity.json").toURI()))), ServiceorderingV1DomainServiceOrder.class);

		InflightOrderChangesDto inflightOrderChangesDto = provisioningHandler.determineInflightOrderChanges(inflightServiceOrder);

		Assertions.assertNotNull(inflightOrderChangesDto);
		Assertions.assertEquals(0, inflightOrderChangesDto.getAddedItems().size());
		Assertions.assertEquals(0, inflightOrderChangesDto.getModifiedItems().size());
		Assertions.assertEquals(0, inflightOrderChangesDto.getRemovedItems().size());
	}


}