package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.api.TasksV2Api;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {

	@Mock
	TasksV2Api tasksV2Api;
	@Mock
	ObjectMapper objectMapper;

	@InjectMocks
	TasksService unit;

	@Test
	void addAttachmentToTask() throws Exception {
		final byte[] attachmentData = "test".getBytes(StandardCharsets.UTF_8);
		when(tasksV2Api.postTasksV2TasksTaskAttachments(eq("tenant"), eq("taskId"), any(File.class),
				eq("attachmentType"), isNull(), eq("comment"), isNull(), isNull(), isNull()))
				.thenReturn(Mono.empty());

		unit.addAttachmentToTask("tenant", "taskId", "attachmentType", "attachmentName", attachmentData, "comment");
	}
}