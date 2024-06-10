package com.bearingpoint.beyond.test-bpintegration.service.handler.co;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.model.UserV1DomainUser;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOfferingsService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.UserService;
import com.bearingpoint.beyond.test-bpintegration.service.handler.SalesAgentHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import helpers.CustomerDataServiceTestHelper;
import helpers.InfonovaLinkServiceTestHelper;
import helpers.ProductOfferingServiceTestHelper;
import helpers.ProductOrdersServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeOrderHandlerTest {
    private static final String OPERATOR_USERNAME = "operator";
    public static final String PRODUCT_OFFERING_NAME = "productofferingname";
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
    UserService userService;
    @Mock
    private TagService tagService;

    ProductOrdersServiceTestHelper productOrderTestHelper;
    InfonovaLinkServiceTestHelper infonovaLinkServiceTestHelper;
    ProductOfferingServiceTestHelper productOfferingServiceTestHelper;
    CustomerDataServiceTestHelper customerDataServiceTestHelper;
    @InjectMocks
    ChangeOrderHandler changeOrderHandler;

    @Mock
    SalesAgentHandler salesAgentHandler;


    public final String WS_TENANT = "ws";
    public final String RT_TENANT = "rt";
    public final Long WS_ORDER_ID_LONG = 1L;
    public final String WS_ORDER_ID = WS_ORDER_ID_LONG.toString();
    public final String RT_ORDER_ID = "2";
    public final String WS_BILLING_ACCOUNT = "123";
    public final String RT_BILLING_ACCOUNT = "456";
    public final String WS_ORDER_LINK = "https://test@test.com/orderlink/123";
    public final String RT_ORDER_LINK = "https://test@test.com/orderlink/456";
    public final String WS_CSM_ORDER_LINK = "https://test@test.com/orderlink/csm/123";
    public final String WORKFLOW_ID = "5";

    public final String OPERATOR_USER_EMAIL = "test@test.com";

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(changeOrderHandler, "rtTenant", RT_TENANT);
        productOrderTestHelper = new ProductOrdersServiceTestHelper(productOrdersService);
        infonovaLinkServiceTestHelper = new InfonovaLinkServiceTestHelper(infonovaLinkService);
        productOfferingServiceTestHelper = new ProductOfferingServiceTestHelper(productOfferingsService);
        customerDataServiceTestHelper = new CustomerDataServiceTestHelper(customerDataService);
    }

    @Test
    public void createReviewTask() {
        productOrderTestHelper.mockWsOrderBillingAccount(WS_TENANT, WS_BILLING_ACCOUNT);
        productOrderTestHelper.mockRtOrderBillingAccount(RT_BILLING_ACCOUNT);
        productOrderTestHelper.mockProductOfferingName(PRODUCT_OFFERING_NAME);
        productOrderTestHelper.mockOpiNumOrderParameter("opinumorderparameter");
        infonovaLinkServiceTestHelper.mockWsOrderLink(WS_ORDER_ID, WS_BILLING_ACCOUNT, WS_ORDER_LINK);
        infonovaLinkServiceTestHelper.expectWsCsmOrderLink(WS_ORDER_ID, WS_BILLING_ACCOUNT, WS_CSM_ORDER_LINK);
        productOfferingServiceTestHelper.mockInternalName(RT_TENANT, PRODUCT_OFFERING_NAME, "internalname");
        customerDataServiceTestHelper.mockCustomerTradingName(RT_TENANT, RT_BILLING_ACCOUNT, "customertradingname");

        changeOrderHandler.createReviewTask(WS_TENANT, WS_ORDER_ID_LONG, WORKFLOW_ID);

        final TaskV2DomainTask expectedTaskBody = new TaskV2DomainTask()
                .taskDefinition("TELUS_WS_CO_REVIEW")
                .billingAccount(WS_BILLING_ACCOUNT)
                .tags(Collections.emptyList())
                .parameters(Map.of(
                        "WS_ORDER_ID", WS_ORDER_ID,
                        "WS_ORDER_LINK", WS_ORDER_LINK,
                        ChangeOrderHandler.WS_CSM_ORDER_LINK, WS_CSM_ORDER_LINK,
                        "PRODUCT_NAME", "internalname",
                        "CUSTOMER_OPI_NUMBER", "opinumorderparameter",
                        "WS_BILLING_ACCOUNT_ID", WS_BILLING_ACCOUNT,
                        "WORKFLOW_ID", WORKFLOW_ID,
                        "CUSTOMER_BUSINESS_NAME", "customertradingname")
                );

        Mockito.verify(tasksService, times(1)).createTask(WS_TENANT, expectedTaskBody);
    }

    @Test
    public void createRetailCOCreateTaskTest() {
        productOrderTestHelper.mockWsOrderBillingAccount(WS_TENANT, WS_BILLING_ACCOUNT);
        productOrderTestHelper.expectRtOrder(RT_BILLING_ACCOUNT, RT_ORDER_ID, OPERATOR_USERNAME, PRODUCT_OFFERING_NAME);
        infonovaLinkServiceTestHelper.mockRtOrderLink(RT_ORDER_ID, RT_BILLING_ACCOUNT, RT_ORDER_LINK);
        infonovaLinkServiceTestHelper.mockWsOrderLink(WS_ORDER_ID, WS_BILLING_ACCOUNT, WS_ORDER_LINK);
        infonovaLinkServiceTestHelper.expectWsCsmOrderLink(WS_ORDER_ID, WS_BILLING_ACCOUNT, WS_CSM_ORDER_LINK);
        customerDataServiceTestHelper.mockCustomerTradingName(RT_TENANT, RT_BILLING_ACCOUNT, "customertradingname");

        when(userService.retrieveUserByUserName(RT_TENANT, OPERATOR_USERNAME)).thenReturn(new UserV1DomainUser().email(OPERATOR_USER_EMAIL));

        changeOrderHandler.createRetailCOCreateTask(RT_TENANT, WS_TENANT, WS_ORDER_ID_LONG, WORKFLOW_ID);

        final TaskV2DomainTask expectedTaskBody = new TaskV2DomainTask()
                .taskDefinition("TELUS_RT_CO_CREATION")
                .billingAccount(RT_BILLING_ACCOUNT)
                .parameters(Map.of(
                        "RT_ORDER_ID", RT_ORDER_ID,
                        "RT_ORDER_LINK", RT_ORDER_LINK,
                        "WORKFLOW_ID", WORKFLOW_ID,
                        "CUSTOMER_BUSINESS_NAME", "customertradingname",
                        "OPERATOR_EMAIL", OPERATOR_USER_EMAIL,
                        "WS_ORDER_ID", WS_ORDER_ID,
                        "WS_ORDER_LINK", WS_ORDER_LINK,
                        "WS_CSM_ORDER_LINK", WS_CSM_ORDER_LINK)
                );

    }
}
