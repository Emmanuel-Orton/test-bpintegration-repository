package com.bearingpoint.beyond.test-bpintegration.configuration;

import java.util.Set;

public interface InfonovaRoleProvider {
    Set<String> getActiveRoles(String tenant, String workGroup);
}
