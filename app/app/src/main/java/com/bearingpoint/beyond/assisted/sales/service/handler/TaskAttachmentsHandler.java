package com.bearingpoint.beyond.test-bpintegration.service.handler;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.CrV1DomainUploadedDocument;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskAttachmentsHandler {

    public static final List<TaskAttachmentTypes> SOW_DEFAULT_ATTACHMENTS_TO_COPY = List.of(
            TaskAttachmentTypes.STATEMENT_OF_WORK_REVISED_DRAFT,
            TaskAttachmentTypes.STATEMENT_OF_WORK_DRAFT
    );

    private final TasksService tasksService;

    public void copyFirstAvailableAttachment(String tenant, String oldTaskId, String newTaskId, String oldTaskDefinition,
                                             Map<TaskType, List<TaskAttachmentTypes>> attachmentTypesToCopy) throws IOException {
        copyFirstAvailableAttachment(tenant, tenant, oldTaskId, newTaskId, oldTaskDefinition, attachmentTypesToCopy);
    }

    public void copyFirstAvailableAttachment(String tenant, String newTenant, String oldTaskId, String newTaskId,
                                             String oldTaskDefinition, Map<TaskType, List<TaskAttachmentTypes>> attachmentTypesToCopy) throws IOException {
        final TaskType oldTaskType = TaskType.valueOf(oldTaskDefinition);

        if (!attachmentTypesToCopy.containsKey(oldTaskType)) {
            log.warn("Old task type {} not found in available attachmentTypes: {}", oldTaskType, attachmentTypesToCopy);
            return;
        }

        final List<TaskAttachmentTypes> attachmentsToCheck = attachmentTypesToCopy.entrySet().stream()
                .filter((entry) -> oldTaskType.equals(entry.getKey()))
                .flatMap((entry) -> entry.getValue().stream())
                .collect(Collectors.toList());

        for (TaskAttachmentTypes attachmentType : attachmentsToCheck) {
            final CrV1DomainUploadedDocument attachment = tasksService.getLatestTaskAttachment(tenant, oldTaskId, attachmentType.name());

            if (attachment != null) {
                byte[] attachmentFile = tasksService.downloadAttachment(tenant, oldTaskId, attachment.getId());
                tasksService.addAttachmentToTask(newTenant, newTaskId, attachment.getType(), attachment.getName(),
                        attachmentFile, attachment.getComment());
                return;
            }
        }
    }
}
