package com.bearingpoint.beyond.test-bpintegration.service.handler.daf;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafServiceItemDto;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.*;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.co.ChangeOrderHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.CustomerDataServiceTestHelper;
import helpers.InfonovaLinkServiceTestHelper;
import helpers.ProductOfferingServiceTestHelper;
import helpers.ProductOrdersServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DafReviewTaskHandlerUTest {

    @Mock
    ProductOrdersService productOrdersService;
    @Mock
    ProductOfferingsService productOfferingsService;
    @Mock
    TasksService tasksService;
    @Mock
    InfonovaLinkService infonovaLinkService;
    @Mock
    CustomerDataService customerDataService;
    @Mock
    DafHandler dafHandler;

    ProductOrdersServiceTestHelper productOrderTestHelper;
    InfonovaLinkServiceTestHelper infonovaLinkServiceTestHelper;
    ProductOfferingServiceTestHelper productOfferingServiceTestHelper;
    CustomerDataServiceTestHelper customerDataServiceTestHelper;
    @InjectMocks
    DafReviewTaskHandler dafReviewTaskHandler;

    ObjectMapper objectMapper;

    public final String WS_TENANT = "ws";
    public final String RT_TENANT = "rt";
    public final Long WS_ORDER_ID_LONG = 1L;
    public final String WS_ORDER_ID = WS_ORDER_ID_LONG.toString();
    public final String RT_ORDER_ID = "2";
    public final String WS_BILLING_ACCOUNT = "123";
    public final String RT_BILLING_ACCOUNT = "456";
    public final String WS_ORDER_LINK = "https://test@test.com/orderlink/123";
    public final String WS_CSM_ORDER_LINK = "https://test@test.com/orderlink/csm/123";
    public final String WORKFLOW_ID = "5";


    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dafReviewTaskHandler, "rtTenant", RT_TENANT);
        ReflectionTestUtils.setField(dafReviewTaskHandler, "objectMapper", objectMapper);
        productOrderTestHelper = new ProductOrdersServiceTestHelper(productOrdersService);
        infonovaLinkServiceTestHelper = new InfonovaLinkServiceTestHelper(infonovaLinkService);
        productOfferingServiceTestHelper = new ProductOfferingServiceTestHelper(productOfferingsService);
        customerDataServiceTestHelper = new CustomerDataServiceTestHelper(customerDataService);
    }

    @Test
    @Disabled // TODO needs to be fixed or better rewritten
    public void createDafReviewTask() throws Exception {
        productOrderTestHelper.mockWsOrderBillingAccount(WS_TENANT, WS_BILLING_ACCOUNT);
        productOrderTestHelper.mockRtOrderBillingAccount(RT_BILLING_ACCOUNT);
        productOrderTestHelper.mockProductOfferingName("productofferingname");
        productOrderTestHelper.mockOpiNumOrderParameter("opinumorderparameter");
        infonovaLinkServiceTestHelper.mockWsOrderLink(WS_ORDER_ID, WS_BILLING_ACCOUNT, WS_ORDER_LINK);
        infonovaLinkServiceTestHelper.expectWsCsmOrderLink(WS_ORDER_ID, WS_BILLING_ACCOUNT, WS_CSM_ORDER_LINK);
        productOfferingServiceTestHelper.mockInternalName(RT_TENANT, "productofferingname", "internalname");
        customerDataServiceTestHelper.mockCustomerTradingName(RT_TENANT, RT_BILLING_ACCOUNT, "customertradingname");
        customerDataServiceTestHelper.mockCustomerTradingName(WS_TENANT, WS_BILLING_ACCOUNT, "partnertradingname");

        when(dafHandler.getDafServiceOrderItemsList(WORKFLOW_ID)).thenReturn(List.of(DafServiceItemDto.builder().build()));
        when(infonovaLinkService.getOrderLink(TenantType.RETAIL, RT_ORDER_ID, RT_BILLING_ACCOUNT)).thenReturn(WS_ORDER_LINK);

        dafReviewTaskHandler.createDafReviewTask(WS_TENANT, WS_ORDER_ID_LONG, WORKFLOW_ID);

        final TaskV2DomainTask expectedTaskBody = new TaskV2DomainTask()
                .taskDefinition("TELUS_WS_DAF_REVIEW")
                .billingAccount(WS_BILLING_ACCOUNT)
                .parameters(Map.of(
                        "WS_ORDER_ID", WS_ORDER_ID,
                        "WS_ORDER_LINK", WS_ORDER_LINK,
                        ChangeOrderHandler.WS_CSM_ORDER_LINK, WS_CSM_ORDER_LINK,
                        "PRODUCT_NAME", "internalname",
                        "CUSTOMER_OPI_NUMBER", "opinumorderparameter",
                        "WS_BILLING_ACCOUNT_ID", WS_BILLING_ACCOUNT,
                        "WORKFLOW_ID", WORKFLOW_ID,
                        "CUSTOMER_BUSINESS_NAME", "customertradingname",
                        "PARTNER_NAME", "partnertradingname")
                );

        Mockito.verify(tasksService, times(1)).createTask(WS_TENANT, expectedTaskBody);
    }
}
