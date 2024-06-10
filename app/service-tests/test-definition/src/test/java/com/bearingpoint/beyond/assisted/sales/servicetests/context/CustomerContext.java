package com.bearingpoint.beyond.test-bpintegration.servicetests.context;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("cucumber-glue")
public class CustomerContext {

    private final Map<String, String> billingAccounts = new HashMap<>();

    private String lastCreatedBillingAccount;

    public void addBillingAccount(String lastNamePostfix, String billingAccountId) {

        billingAccounts.put(lastNamePostfix, billingAccountId);
        lastCreatedBillingAccount = billingAccountId;
    }

    public Map<String, String> getBillingAccounts() {
        return billingAccounts;
    }

    public String getLastCreatedBillingAccount(){
        return lastCreatedBillingAccount;
    }

    public String getBillingAccountByName(String lastNamePostfix) {
        return billingAccounts.get(lastNamePostfix);
    }
}
