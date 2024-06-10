package com.bearingpoint.beyond.test-bpintegration.configuration;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.api.AuthorizationV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.model.SecurityAuthorizationDomainWorkGroup;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CachedInfonovaRoleProvider implements InfonovaRoleProvider {

    private AuthorizationV1Api authorizationApi;

    @Autowired
    public CachedInfonovaRoleProvider(AuthorizationV1Api authorizationApi) {
        this.authorizationApi = authorizationApi;
    }

    @Override
    @Cacheable(
            value = "tenantWorkGroupRolesCache",
            key = "#tenant + #workGroupName"
    )
    public Set<String> getActiveRoles(String tenant, String workGroupName) {
        SecurityAuthorizationDomainWorkGroup workGroup = authorizationApi.getAuthorizationV1WorkGroupsName(tenant, workGroupName).block();
        Set<String> activeRoles = new HashSet<>();
        if (BooleanUtils.isTrue(workGroup.getActive())) {
            activeRoles.addAll(workGroup.getRoles());
        }
        return activeRoles;
    }
}