package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.api.ServiceInventoryV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainService;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainServiceList;
import helpers.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceInventoryServiceUTest {

    private static final String SERVICE_INSTANCE_NAME = "1192598181_b4b3aed47cf343e19d6bfa3d44ab68d0";

    private static final String WS_TENANT = "SB_TELUS_SIT_WS";

    @Mock
    ServiceInventoryV1Api serviceInventoryV1Api;

    ObjectMapper objectMapper;

    @InjectMocks
    ServiceInventoryService unitToTest;

    @BeforeEach
    void setUp() {
        objectMapper = TestUtils.createObjectMapper();
        //ReflectionTestUtils.setField(unitToTest, "objectMapper", objectMapper);
    }

    @Test
    void getServiceByServiceIdentifier() throws Exception {
        ServiceinventoryV1DomainServiceList serviceinventoryV1DomainServiceList = TestUtils.readObjectFromFile("/serviceListBySI.json", ServiceinventoryV1DomainServiceList.class);
        when(serviceInventoryV1Api.getServiceInventoryV1Services(WS_TENANT, String.format("serviceIdentifier:%s", SERVICE_INSTANCE_NAME)))
                .thenReturn(Mono.just(serviceinventoryV1DomainServiceList));

        ServiceinventoryV1DomainService serviceByServiceIdentifier = unitToTest.getServiceByServiceIdentifier(WS_TENANT, SERVICE_INSTANCE_NAME);
        Assertions.assertNotNull(serviceByServiceIdentifier);
    }
}