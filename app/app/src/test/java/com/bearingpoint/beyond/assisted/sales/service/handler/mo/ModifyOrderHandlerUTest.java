package com.bearingpoint.beyond.test-bpintegration.service.handler.mo;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOfferingReference;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrderItem;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainProductRelationship;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainService;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.ServiceInventoryService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import helpers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WS_ORDER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModifyOrderHandlerUTest {

    private static final String CURRENT_TENANT_WS = "SB_TELUS_ANDYROZMAN_WS";

    private static final String PREVIOUS_TASK_ID = "88748573";
    private static final Long TASK_ID = 88748574L;

    private static final String TASK_ID_NEW = "88748575";
    private static final String PREVIOUS_TASK_DEFINITION = "PREVIOUS_TASK_DEFINITION";

    private static final String WORKFLOW_ID_VALUE = "MO_1192598181_b4b3aed47cf343e19d6bfa3d44ab68d0";
    private static final Long CUSTOMER_ID = 375734878L;
    private static final String CUSTOMER_LEGAL_NAME = "Customer Legal Name";
    private static final String SERVICE_INSTANCE_NAME = "1192598181_b4b3aed47cf343e19d6bfa3d44ab68d0";
    private static final String SERVICE_NAME = "WSO Monitor It Service";
    private static final String CSM_USER = "jolera_user";
    private static final String PRODUCT_ID = "560d7b6f-cbbc-4d78-9ee5-03d89b8e2649";

    private static final String WS_ORDER_ID_VALUE = "23455454";
    private static final Long WS_ORDER_ID_LONG = 123456L;

    @Mock
    TasksService tasksService;
    @Mock
    BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    @Mock
    ServiceInventoryService serviceInventoryService;
    @Mock
    ProductOrdersService productOrdersService;
    @Mock
    TagService tagService;
    ObjectMapper objectMapper;

    @InjectMocks
    ModifyOrderHandler unitToTest;


    @BeforeEach
    void setUp() {
        objectMapper = TestUtils.createObjectMapper();
        ReflectionTestUtils.setField(unitToTest, "objectMapper", objectMapper);
    }




    @Test
    @Disabled
    void createReviewTask() {
        // TODO
    }

    @Captor
    private ArgumentCaptor<TaskV2DomainTask> taskCaptor;



    @Test
    void createInitiateMoTask() {

        when(tasksService.createTask(any(), taskCaptor.capture())).thenReturn(new TaskV2DomainTask()
                        .id(TASK_ID.toString())
                .taskDefinition(TaskType.TELUS_WS_CO_DAY2_INITIATE.name()));
        ProductorderingV1DomainProductOrder productOrder  = mock(ProductorderingV1DomainProductOrder.class);
         ProductorderingV1DomainProductOrderItem orderItems = mock(ProductorderingV1DomainProductOrderItem.class);
         List<ProductorderingV1DomainProductOrderItem> orders = mock(List.class);
         ProductorderingV1DomainProduct product = mock(ProductorderingV1DomainProduct.class);
        ProductorderingV1DomainProductOfferingReference offers = mock(ProductorderingV1DomainProductOfferingReference.class);
         when(productOrder.getOrderItems()).thenReturn(orders);
         when(orders.get(0)).thenReturn(orderItems);
         when(orderItems.getProduct()).thenReturn(product);
         when(product.getProductOffering()).thenReturn(offers);

        when(productOrdersService.getProductOrderWithServiceIdentifier(SERVICE_INSTANCE_NAME, CURRENT_TENANT_WS))
                .thenReturn(new ProductorderingV1DomainProductOrder().id(WS_ORDER_ID_VALUE));

        when(productOrdersService.getProductOrder(CURRENT_TENANT_WS, WS_ORDER_ID_VALUE))
                .thenReturn(new ProductorderingV1DomainProductOrder().id(WS_ORDER_ID_VALUE));

        when(productOrdersService.getRetailOrderForTheWholesale(any())).thenReturn(productOrder);

        TaskV2DomainTask returnTask = unitToTest.createInitiateMoTask(CUSTOMER_ID, CURRENT_TENANT_WS,
                CUSTOMER_LEGAL_NAME, SERVICE_INSTANCE_NAME, SERVICE_NAME, CSM_USER, WORKFLOW_ID_VALUE);

        Assertions.assertNotNull(returnTask);

        TaskV2DomainTask initiateMoTask = taskCaptor.getValue();

        Assertions.assertNotNull(initiateMoTask);
        Assertions.assertEquals(CUSTOMER_ID.toString(), initiateMoTask.getBillingAccount());
        Assertions.assertNotNull(initiateMoTask.getParameters());
        Assertions.assertEquals(7, initiateMoTask.getParameters().size());
        Assertions.assertEquals(SERVICE_INSTANCE_NAME, initiateMoTask.getParameters().get(ModifyOrderHandler.SERVICE_INSTANCE_NAME));
        Assertions.assertEquals(CSM_USER, initiateMoTask.getParameters().get(ModifyOrderHandler.CSM_CREATOR));
        Assertions.assertEquals(WORKFLOW_ID_VALUE, initiateMoTask.getParameters().get(WorkflowUtil.WORKFLOW_ID));
        Assertions.assertEquals(CUSTOMER_LEGAL_NAME, initiateMoTask.getParameters().get(ModifyOrderHandler.CUSTOMER_LEGAL_NAME));
        Assertions.assertEquals(CUSTOMER_LEGAL_NAME, initiateMoTask.getParameters().get(ModifyOrderHandler.CUSTOMER_NAME));
    }

    @Test
    void createFinalizeTask() {

        when(basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
                CURRENT_TENANT_WS,
                TASK_ID.toString(),
                TaskType.TELUS_WS_CO_DAY2_FINALIZE,
                false
        )).thenReturn(new TaskV2DomainTask()
                .id(TASK_ID_NEW)
                        .parameters(Map.of("WORKFLOW_ID", WORKFLOW_ID_VALUE))
                );

        when(productOrdersService.getProductOrderWithServiceIdentifier(SERVICE_INSTANCE_NAME, CURRENT_TENANT_WS))
                .thenReturn(new ProductorderingV1DomainProductOrder().id(WS_ORDER_ID_VALUE));

        Mockito.doCallRealMethod().when(tasksService).addAdditionalParameterToTask(any(), any(), any());

        when(tasksService.createTask(eq(CURRENT_TENANT_WS), taskCaptor.capture()))
                .thenReturn(new TaskV2DomainTask().id(TASK_ID_NEW));

        unitToTest.createFinalizeTask(CURRENT_TENANT_WS, TASK_ID.toString(), SERVICE_INSTANCE_NAME);

        TaskV2DomainTask value = taskCaptor.getValue();

        Assertions.assertNotNull(value);
        Assertions.assertNotNull(value.getParameters());
        Assertions.assertEquals(WS_ORDER_ID_VALUE, value.getParameters().get(WS_ORDER_ID));

    }
}
