package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.api.UsersV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.model.UserV1DomainUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService {
    private final UsersV1Api usersV1Api;

    public UserV1DomainUser retrieveUserByUserName(String tenant, String userName){
        return usersV1Api.getUsersV1UsersName(tenant, userName)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public String retrieveUserEmailByUserName(String tenant, String userName){
        return retrieveUserByUserName(tenant, userName).getEmail();
    }
}
