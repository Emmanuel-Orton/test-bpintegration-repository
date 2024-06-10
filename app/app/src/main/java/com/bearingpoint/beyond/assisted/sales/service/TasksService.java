package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.api.TasksV2Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.CrV1DomainUploadedDocument;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.CrV1DomainUploadedDocumentList;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainNote;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainNoteList;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TasksService {

    private final TasksV2Api tasksV2Api;
    private final ObjectMapper objectMapper;

    public TaskV2DomainTask createTask(String tenant, TaskV2DomainTask task) {
        return tasksV2Api.postTasksV2Tasks(tenant, task, UUID.randomUUID().toString())
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .doOnSuccess(taskOut -> log.debug("Task with id {} created on tenant {}", taskOut.getId(), tenant))
                .block();
    }

    public TaskV2DomainTask getTask(String tenant, String id) {
        return tasksV2Api.getTasksV2TasksId(tenant, id)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public String getTaskResolutionText(String tenant, String taskId) {
        TaskV2DomainNoteList taskNoteList = tasksV2Api.getTasksV2TasksTaskNotes(tenant, taskId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();

        List<TaskV2DomainNote> listOfNotes = taskNoteList.getList();

        Optional<TaskV2DomainNote> foundNote = listOfNotes.stream()
                .filter(it -> it.getCreatedForState() != null &&
                        "Resolved".equals(it.getCreatedForState()))
                .findFirst();

        return foundNote.map(TaskV2DomainNote::getText).orElse("");
    }


    @SneakyThrows
    public void updateTask(String tenant, TaskV2DomainTask task) {
        tasksV2Api.patchTasksV2TasksId(tenant, task.getId(), task)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public void addAttachmentToTask(String tenant, String taskId, String attachmentType, String attachmentName,
                                    byte[] attachmentData, @Nullable String comment) throws IOException {
        final Path tempFilePath = Files.createTempFile(null, null);
        final Path filePath = Files.move(tempFilePath, tempFilePath.getParent().resolve(attachmentName),
                StandardCopyOption.REPLACE_EXISTING);
        Files.write(filePath, attachmentData);

        tasksV2Api.postTasksV2TasksTaskAttachments(tenant, taskId, filePath.toFile(), attachmentType, null,
                        comment, null, null, null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();

        log.debug("Attachment {} added to task {} on tenant {}", filePath.toFile(), taskId, tenant);

        Files.deleteIfExists(filePath);
    }

    public CrV1DomainUploadedDocument getLatestTaskAttachment(String tenant, String taskId, String attachmentType) {
        final String query = String.format("type:%s", attachmentType);
        final CrV1DomainUploadedDocumentList documentList = tasksV2Api.getTasksV2TasksTaskAttachments(tenant, taskId,
                        BigDecimal.ONE, BigDecimal.ONE, query, List.of("created"), "desc")
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
        return documentList.getList().stream().findFirst().orElse(null);
    }

    public byte[] downloadAttachment(String tenant, String taskId, String attachmentId) {
        return tasksV2Api.getTasksV2TasksTaskAttachmentsIdDownload(tenant, taskId, attachmentId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public void addAdditionalParametersToTask(Map<String, String> additionalParameters, TaskV2DomainTask task) {
        Map<String, String> allParameters = task.getParameters() == null ?
                new HashMap<>() : new HashMap<>(task.getParameters());
        allParameters.putAll(additionalParameters);
        task.setParameters(allParameters);
    }

    public void addAdditionalParameterToTask(String key, String value, TaskV2DomainTask task) {
        Map<String, String> allParameters = task.getParameters() == null ?
                new HashMap<>() : new HashMap<>(task.getParameters());
        allParameters.put(key, value);
        task.setParameters(allParameters);
    }


    private String getObjectAsString(Object item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception ex) {
            return item.toString();
        }
    }
}
