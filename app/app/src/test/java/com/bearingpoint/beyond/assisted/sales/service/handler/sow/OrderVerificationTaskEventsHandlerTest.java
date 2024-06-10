package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.SalesAgentHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import helpers.CustomerDataServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.service.handler.sow.OrderVerificationTaskEventsHandler.ORDER_VERIFICATION_TASK_DEFINITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderVerificationTaskEventsHandlerTest {

    private static final String TENANT = "SB_TELUS_SIT";
    private static final String DRAFT_ORDER_ID = "2934792374";
    private static final String DRAFT_INTERNAL_NAME = "draftInternalName";
    private static final String BILLING_ACCOUNT = "billingAccount";
    private static final String DRAFT_ORDER_LINK = "DRAFT_ORDER_LINK";
    private static final String DRAFT_ORDER_LINK_VALUE = "/productOrdering/v1/draftOrders/10";
    private static final String SAVED_BY = "savedBy";

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
    OrderVerificationTaskEventsHandler unit;

    CustomerDataServiceTestHelper customerDataServiceTestHelper;

    @BeforeEach
    public void setUp() {
        customerDataServiceTestHelper = new CustomerDataServiceTestHelper(customerDataService);
    }

    @Test
    void createOrderVerificationTask() {
        final ProductorderingV1DomainDraftOrder draftOrder = mock(ProductorderingV1DomainDraftOrder.class);
        when(draftOrder.getBillingAccount()).thenReturn(BILLING_ACCOUNT);
        when(draftOrder.getSavedBy()).thenReturn(SAVED_BY);
        when(productOrdersService.getDraftOrder(TENANT, DRAFT_ORDER_ID)).thenReturn(draftOrder);
        final ProductorderingV1DomainDraftProduct domainDraftProduct = mock(ProductorderingV1DomainDraftProduct.class);
        when(domainDraftProduct.getInternalName()).thenReturn(DRAFT_INTERNAL_NAME);
        when(draftOrder.getDraftProducts()).thenReturn(List.of(domainDraftProduct));

        final ProductorderingV1DomainShoppingCart shoppingCart = mock(ProductorderingV1DomainShoppingCart.class);
        when(productOrdersService.createCartFromDraft(TENANT, DRAFT_ORDER_ID)).thenReturn(shoppingCart);
        final ProductorderingV1DomainProduct product = mock(ProductorderingV1DomainProduct.class);
        when(product.getParameters()).thenReturn(Map.of("opi_num", "OPI-1234567"));
        when(shoppingCart.getProducts()).thenReturn(List.of(product));

        final ArgumentCaptor<TaskV2DomainTask> taskArgCpt = ArgumentCaptor.forClass(TaskV2DomainTask.class);
        final TaskV2DomainTask task = mock(TaskV2DomainTask.class);
        when(tasksService.createTask(eq(TENANT), taskArgCpt.capture())).thenReturn(task);

        when(infonovaLinkService.getDraftOrderLink(TenantType.RETAIL, DRAFT_ORDER_ID, BILLING_ACCOUNT)).thenReturn(DRAFT_ORDER_LINK_VALUE);
        customerDataServiceTestHelper.mockCustomerTradingName(TENANT, BILLING_ACCOUNT, "customertradingname");

        assertThat(unit.createOrderVerificationTask(TENANT, "wsTenant", DRAFT_ORDER_ID, null)).isEqualTo(task);

        assertThat(taskArgCpt.getValue()).isNotNull();

        assertThat(taskArgCpt.getValue().getTaskDefinition()).isEqualTo(ORDER_VERIFICATION_TASK_DEFINITION);
        assertThat(taskArgCpt.getValue().getBillingAccount()).isEqualTo(BILLING_ACCOUNT);
        assertThat(taskArgCpt.getValue().getParameters()).containsEntry(OrderVerificationTaskEventsHandler.PRODUCT_NAME_PARAMETER, DRAFT_INTERNAL_NAME);
        assertThat(taskArgCpt.getValue().getParameters()).containsEntry(OrderVerificationTaskEventsHandler.DRAFT_ORDER_ID, DRAFT_ORDER_ID);
        assertThat(taskArgCpt.getValue().getParameters()).containsEntry(DRAFT_ORDER_LINK, DRAFT_ORDER_LINK_VALUE);
        assertThat(taskArgCpt.getValue().getParameters()).containsEntry(OrderVerificationTaskEventsHandler.RT_BILLING_ACCOUNT_ID_PARAMETER, BILLING_ACCOUNT);
        assertThat(taskArgCpt.getValue().getParameters()).containsEntry(OrderVerificationTaskEventsHandler.SALES_USERNAME_PARAMETER, SAVED_BY);
    }
}
