package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.main;

        import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafOrderItem;
        import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
        import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
        import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
        import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
        import com.fasterxml.jackson.core.type.TypeReference;
        import lombok.extern.slf4j.Slf4j;
        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.Test;
        import org.junit.jupiter.api.extension.ExtendWith;
        import org.mockito.InjectMocks;
        import org.mockito.Mock;
        import org.mockito.junit.jupiter.MockitoExtension;

        import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
        import static org.mockito.Mockito.doNothing;
        import org.junit.jupiter.api.Disabled;

        import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PrepareServiceOrderItemsStepUTest extends AbstractWorkflowStepTest {

    private static final String WORKFLOW_ID_DATA = "DAF_43748738748";


    @Mock
    DafHandler dafHandler;

    @InjectMocks
    PrepareServiceOrderItemsStep unitToTest;

    @BeforeEach
    public void setUp() {
        setObjectMapper(unitToTest);
    }

    @Test
    public void hasRetry() {
        expectHasRetry(unitToTest, true);
    }


    @Test
    public void getStepTenantType() {
        expectStepTenantType(unitToTest, TenantType.WHOLESALE);
    }


    @Test
    public void executeStep() throws Exception {

        List<DafOrderItem> listOfItems = List.of(
                DafOrderItem.builder().id(1).build(),
                DafOrderItem.builder().id(2).build());

        String data = objectMapper.writeValueAsString(listOfItems);

        expectGetMandatoryVariable(WF_WS_PROVISIONING_ITEMS_REQUEST_DATA, data);
        expectGetMandatoryVariable(WORKFLOW_ID, WORKFLOW_ID_DATA);

        doNothing().when(dafHandler)
                .prepareServiceOrderItems(WORKFLOW_ID_DATA, listOfItems);

        unitToTest.executeStep(execution);
    }

}

