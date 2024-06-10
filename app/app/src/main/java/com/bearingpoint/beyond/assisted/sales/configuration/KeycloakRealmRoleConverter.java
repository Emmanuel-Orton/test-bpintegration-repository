package com.bearingpoint.beyond.test-bpintegration.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Slf4j
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private InfonovaRoleProvider infonovaRoleProvider;

    public KeycloakRealmRoleConverter(InfonovaRoleProvider infonovaRoleProvider) {
        this.infonovaRoleProvider = infonovaRoleProvider;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (jwt.getClaims().containsKey("r6")) {
            return convertR6Claim(jwt);
        } else {
            return convertRealmAccessClaim(jwt);
        }
    }

    private List<GrantedAuthority> convertR6Claim(Jwt jwt) {
        List<Map> tenants = parseR6Claim((String) jwt.getClaims().get("r6"));
        if (tenants != null) {
            List<GrantedAuthority> roles = convertTenants(tenants);
            tenants.stream().forEach(tenant -> {
                List<GrantedAuthority> tenantRoles = convertTenantRoles(tenant);
                roles.addAll(tenantRoles);
            });
            return roles;
        }

        return null;
    }

    private List<GrantedAuthority> convertTenantRoles(Map tenant) {
        String tenantName = (String) tenant.get("name");
        List<Map> groups = (List<Map>) tenant.get("groups");
        List<String> groupStrings = groups.stream().map(group -> (String) group.get("name")).collect(Collectors.toList());
        Set<String> tenantRoleNames = new HashSet<>();
        groupStrings.forEach(group -> {
            log.debug("Checking roles for tenant:{}, and workGroup:{}.", tenantName, group);
            tenantRoleNames.addAll(infonovaRoleProvider.getActiveRoles(tenantName, group));
        });
        List<GrantedAuthority> tenantRoles = tenantRoleNames.stream()
                .map(roleName -> "ROLE_" + roleName) // prefix to map to a Spring Security "role"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return tenantRoles;
    }

    private List<GrantedAuthority> convertTenants(List<Map> tenants) {
        return tenants.stream().map(tenant -> (String) tenant.get("name"))
                .map(tenantName -> "TENANT_" + tenantName)
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private List<Map> parseR6Claim(String r6Claim) {
        byte[] r6gzip = Base64.getDecoder().decode(r6Claim);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(r6gzip);
             GZIPInputStream gis = new GZIPInputStream(bis);
             BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        ) {
            String jsonString = br.lines().collect(Collectors.joining());
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> r6ClaimsMap = mapper.readValue(jsonString, Map.class);
            List<Map> tenants = (List) r6ClaimsMap.get("tenants");
            return tenants;
        } catch (IOException e) {
            log.error("Error parsing r6 claim: {}", e.getMessage());
            throw new IllegalArgumentException(e);
        }
    }

    private List<GrantedAuthority> convertRealmAccessClaim(Jwt jwt) {
        final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        List<String> realmAccessRoles = (List<String>) realmAccess.get("roles");

        if (null == realmAccessRoles) {
            return null;
        }

        List<GrantedAuthority> roles = realmAccessRoles.stream()
                .filter(roleName -> !roleName.contains(":"))
                .map(roleName -> "ROLE_" + roleName) // prefix to map to a Spring Security "role"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        List<String> infonovaRoles = realmAccessRoles.stream()
                .filter(roleName -> roleName.contains(":")).collect(Collectors.toList());

        List<GrantedAuthority> tenants = infonovaRoles.stream()
                .map(roleName -> roleName.split(":")[0])
                .map(tenantName -> "TENANT_" + tenantName) // prefix to map to a Spring Security "role"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Set<String> tenantRoleNames = new HashSet<>();
        infonovaRoles.stream().forEach(tenantWorkgroup -> {
            String[] tWg = tenantWorkgroup.split(":");
            log.debug("Checking roles for tenant:{}, and workGroup:{}.", tWg[0], tWg[1]);
            tenantRoleNames.addAll(infonovaRoleProvider.getActiveRoles(tWg[0], tWg[1]));
        });
        List<GrantedAuthority> tenantRoles = tenantRoleNames.stream()
                .map(tenantName -> "ROLE_" + tenantName) // prefix to map to a Spring Security "role"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        roles.addAll(tenants);
        roles.addAll(tenantRoles);
        return roles;
    }

}
