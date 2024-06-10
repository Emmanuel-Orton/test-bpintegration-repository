package com.bearingpoint.beyond.test-bpintegration.api.common;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEventBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public abstract class MockMvcMockitoTest {
	protected static final String TENANT_URL = "/SB_TELUS_SIT";
	protected static final String TENANT = "SB_TELUS_SIT";
	protected static final String TASK_EVENT = "taskEvent";
	protected static final String TASK_ID = "736153447";

	protected MockMvc mockMvc;
	protected ObjectMapper objectMapper;

	@Mock
	protected WorkflowUtil workflowUtil;

	protected abstract Object getController();

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(getController(), "objectMapper", objectMapper);
		mockMvc = MockMvcBuilders.standaloneSetup(getController())
				.build();
	}

	protected TaskV2DomainTaskEvent prepareTaskEvent() {
		return new TaskV2DomainTaskEvent()
				.eventType(TASK_EVENT)
				.event(new TaskV2DomainTaskEventBody().current(new TaskV2DomainTask()
						.id(TASK_ID)));
	}

}
