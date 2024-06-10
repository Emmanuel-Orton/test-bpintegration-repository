package com.bearingpoint.beyond.test-bpintegration.configuration;

import com.bearingpoint.beyond.test-bpintegration.configuration.InfonovaRoleProvider;
import com.bearingpoint.beyond.test-bpintegration.configuration.KeycloakRealmRoleConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakRealmRoleConverterTest {
    @Mock
    private InfonovaRoleProvider infonovaRoleProvider;
    @InjectMocks
    private KeycloakRealmRoleConverter converter;

    @Test
    void convertPlainRoles() {
        Jwt jwt = givenTokenWithRole("super_admin");
        Collection<GrantedAuthority> result = converter.convert(jwt);
        assertThat(result, containsInAnyOrder(new SimpleGrantedAuthority("ROLE_super_admin")));
    }

    private Jwt givenTokenWithRole(String role) {
        Map<String, List> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList(role));
        Jwt jwt = Jwt.withTokenValue("bla")
                .header("header", "value")
                .claim("realm_access", realmAccess)
                .build();
        return jwt;
    }

    @Test
    void convertInfonovaRoles() {
        when(infonovaRoleProvider.getActiveRoles("MY_TENANT", "super_admin")).thenReturn(new HashSet<>(Arrays.asList("my_role")));
        Jwt jwt = givenTokenWithRole("MY_TENANT:super_admin");
        Collection<GrantedAuthority> result = converter.convert(jwt);
        assertThat(result, containsInAnyOrder(new SimpleGrantedAuthority("ROLE_my_role"), new SimpleGrantedAuthority("TENANT_MY_TENANT")));
    }

}