package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.CrV1DomainUploadedDocument;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.handler.TaskAttachmentsHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType.TELUS_RT_ORDER_VERIFICATION;
import static com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType.TELUS_WS_SOW_REVIEW;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskAttachmentsHandlerTest {

	private static final String TENANT = "TENANT";
	private static final String NEW_TENANT = "NEW_TENANT";
	private static final String OLD_TASK_ID = "OLD_TASK_ID";
	private static final String NEW_TASK_ID = "NEW_TASK_ID";
	private static final String STATEMENT_OF_WORK_DRAFT = "STATEMENT_OF_WORK_DRAFT";
	private static final String ATTACHMENT_ID = "ATTACHMENT_ID";
	private static final String ATTACHMENT_TYPE = "ATTACHMENT_TYPE";
	private static final String ATTACHMENT_NAME = "ATTACHMENT_NAME";
	private static final String ATTACHMENT_COMMENT = "ATTACHMENT_COMMENT";
	private static final String PRE_SOW_ORDER_SUMMARY = "PRE_SOW_ORDER_SUMMARY";

	@Mock
	TasksService tasksService;

	@InjectMocks
    TaskAttachmentsHandler unit;

	@Test
	void copyFirstAvailableAttachment_oldTenant() throws Exception {
		copyFirstAvailableAttachmentTest(true);
	}

	@Test
	void copyFirstAvailableAttachment_newTenant() throws Exception {
		copyFirstAvailableAttachmentTest(false);
	}

	@Test
	void copyFirstAvailableAttachment_NoProperAttachments() throws Exception {
		unit.copyFirstAvailableAttachment(TENANT, NEW_TENANT, OLD_TASK_ID, NEW_TASK_ID,
				TELUS_WS_SOW_REVIEW.name(), Map.of(TELUS_RT_ORDER_VERIFICATION, List.of(
						TaskAttachmentTypes.STATEMENT_OF_WORK_DRAFT)));

		verify(tasksService, never()).getLatestTaskAttachment(TENANT, OLD_TASK_ID, STATEMENT_OF_WORK_DRAFT);

		unit.copyFirstAvailableAttachment(TENANT, TENANT, OLD_TASK_ID, NEW_TASK_ID,
				TELUS_WS_SOW_REVIEW.name(), Map.of(TELUS_RT_ORDER_VERIFICATION, List.of(
						TaskAttachmentTypes.STATEMENT_OF_WORK_DRAFT)));

		verify(tasksService, never()).getLatestTaskAttachment(TENANT, OLD_TASK_ID, STATEMENT_OF_WORK_DRAFT);
	}

	void copyFirstAvailableAttachmentTest(boolean oldTenant) throws Exception {

		final CrV1DomainUploadedDocument attachment = mock(CrV1DomainUploadedDocument.class);
		when(attachment.getId()).thenReturn(ATTACHMENT_ID);
		when(attachment.getType()).thenReturn(ATTACHMENT_TYPE);
		when(attachment.getName()).thenReturn(ATTACHMENT_NAME);
		when(attachment.getComment()).thenReturn(ATTACHMENT_COMMENT);

		when(tasksService.getLatestTaskAttachment(TENANT, OLD_TASK_ID, PRE_SOW_ORDER_SUMMARY)).thenReturn(
				attachment);

		final byte[] attFile = {};
		when(tasksService.downloadAttachment(TENANT, OLD_TASK_ID, ATTACHMENT_ID)).thenReturn(attFile);

		if (oldTenant) {
			unit.copyFirstAvailableAttachment(TENANT, OLD_TASK_ID, NEW_TASK_ID,
					TELUS_WS_SOW_REVIEW.name(), Map.of(
							TELUS_WS_SOW_REVIEW, List.of(TaskAttachmentTypes.PRE_SOW_ORDER_SUMMARY, TaskAttachmentTypes.STATEMENT_OF_WORK_DRAFT)));
		} else {
			unit.copyFirstAvailableAttachment(TENANT, NEW_TENANT, OLD_TASK_ID, NEW_TASK_ID,
					TELUS_WS_SOW_REVIEW.name(), Map.of(
							TELUS_WS_SOW_REVIEW, List.of(TaskAttachmentTypes.PRE_SOW_ORDER_SUMMARY, TaskAttachmentTypes.STATEMENT_OF_WORK_DRAFT)));
		}


		verify(tasksService, times(1)).getLatestTaskAttachment(TENANT, OLD_TASK_ID, PRE_SOW_ORDER_SUMMARY);
		verify(tasksService, never()).getLatestTaskAttachment(TENANT, OLD_TASK_ID, STATEMENT_OF_WORK_DRAFT);

		verify(tasksService, times(1))
				.addAttachmentToTask( oldTenant ? TENANT : NEW_TENANT, NEW_TASK_ID, ATTACHMENT_TYPE, ATTACHMENT_NAME, attFile,
						ATTACHMENT_COMMENT);
	}

}