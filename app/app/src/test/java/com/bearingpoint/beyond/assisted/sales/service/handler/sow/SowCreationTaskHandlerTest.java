package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.SalesAgentHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SowCreationTaskHandlerTest {

    private static final String TENANT = "SB_TELUS_SIT";
    private static final String WS_TENANT = "SB_TELUS_SIT_WS";
    private static final String DRAFT_ORDER_ID = "31651613";
    private static final String BUSINESS_KEY = "business_key";
    private static final String PROCESS_INSTANCE_ID = "341451";
    private static final String BILLING_ACCOUNT = "1182916039";
    private static final String WS_BILLING_ACCOUNT = "ws_billingAccount";
    private static final String DRAFT_ORDER_LINK_VALUE = "/productOrdering/v1/draftOrders/10";
    public static final String RETAIL_CUSTOMER_ACCOUNT_LINK = "RETAIL_CUSTOMER_ACCOUNT_LINK";
    public static final String PRODUCT_OFFERING_NAME = "ManageIt";
    private static final String CUSTOMER_RETAIL_NAME = "CUSTOMER_RETAIL_NAME";
    private static final String CUSTOMER_BUSINESS_NAME = "CUSTOMER_BUSINESS_NAME";

    @Mock
    TasksService tasksService;
    @Mock
    ProductOrdersService productOrdersService;
    @Mock
    CustomerDataService customerDataService;
    @Mock
    SalesAgentHandler salesAgentHandler;
    @Mock
    InfonovaLinkService infonovaLinkService;
    @Mock
    TagService tagService;

    @InjectMocks
    SowCreationTaskHandler sowCreationTaskHandler;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        JsonNullableModule jnm = new JsonNullableModule();
        mapper.registerModule(jnm);

        ReflectionTestUtils.setField(sowCreationTaskHandler, "wsTenant", WS_TENANT);
    }

    @Test
    void createSowCreationTask() throws Exception {

        final ProductorderingV1DomainShoppingCart shoppingCart = mapper.readValue(new String(
                        Files.readAllBytes(Paths.get(this.getClass().getResource("/shopingCart.json").toURI()))),
                ProductorderingV1DomainShoppingCart.class);

        when(productOrdersService.createCartFromDraft(TENANT, DRAFT_ORDER_ID))
                .thenReturn(shoppingCart);

        final ProductorderingV1DomainDraftOrder draftOrder = mapper.readValue(new String(
                        Files.readAllBytes(Paths.get(this.getClass().getResource("/draftOrder.json").toURI()))),
                ProductorderingV1DomainDraftOrder.class);

        when(productOrdersService.getDraftOrder(TENANT, DRAFT_ORDER_ID))
                .thenReturn(draftOrder);

        when(infonovaLinkService.getDraftOrderLink(TenantType.RETAIL, DRAFT_ORDER_ID, BILLING_ACCOUNT)).thenReturn(DRAFT_ORDER_LINK_VALUE);
        when(customerDataService.getCustomerTradingName(TENANT, BILLING_ACCOUNT)).thenReturn(CUSTOMER_BUSINESS_NAME);
        when(infonovaLinkService.getRetailCustomerAccountLink(BILLING_ACCOUNT)).thenReturn(RETAIL_CUSTOMER_ACCOUNT_LINK);
        when(customerDataService.getCustomerTradingName(WS_TENANT, WS_BILLING_ACCOUNT)).thenReturn(CUSTOMER_RETAIL_NAME);
        when(customerDataService.getWholesaleBillingAccount(TENANT, PRODUCT_OFFERING_NAME)).thenReturn(
                WS_BILLING_ACCOUNT);

        final TaskV2DomainTask taskStub = new TaskV2DomainTask();
        when(tasksService.createTask(eq(WS_TENANT), any())).thenReturn(taskStub);

        final TaskV2DomainTask task = sowCreationTaskHandler.createSowCreationTask(TENANT, WS_TENANT, DRAFT_ORDER_ID);

        Assertions.assertThat(task).isEqualTo(taskStub);
    }
}
