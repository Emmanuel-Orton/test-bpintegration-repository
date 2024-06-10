package com.bearingpoint.beyond.test-bpintegration.configuration;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Optional;

@Component
public class TenantPermissionEvaluator implements PermissionEvaluator {

    private HttpServletRequest request;

    public TenantPermissionEvaluator(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if ((auth == null) || (targetDomainObject == null) || !(targetDomainObject instanceof String) || !(permission instanceof String)) {
            return false;
        }
        String tenant = (String) targetDomainObject;

        if (hasWildcardTenantRole(auth) ||
                hasNoTenantLimitation(auth) ||
                hasTenantPermission(auth, tenant)) {
            return true;
        }

        return false;
    }

    private boolean hasTenantPermission(Authentication auth, String tenant) {
        Optional<String> tenantPermission = auth.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .filter(authorityString -> authorityString.equals("TENANT_" + tenant))
                .findFirst();
        if (tenantPermission.isPresent()) {
            return true;
        }
        return false;
    }

    private boolean hasNoTenantLimitation(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(authority -> authority.startsWith("TENANT_"));
    }

    private boolean hasWildcardTenantRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("TENANT_*"));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}