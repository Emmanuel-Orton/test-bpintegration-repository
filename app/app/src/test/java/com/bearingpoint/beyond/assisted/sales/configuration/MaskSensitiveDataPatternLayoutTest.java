package com.bearingpoint.beyond.test-bpintegration.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaskSensitiveDataPatternLayoutTest {

    @InjectMocks
    private MaskSensitiveDataPatternLayout maskSensitiveDataPatternLayout;

    @BeforeEach
    public void setUp() {
        maskSensitiveDataPatternLayout.addMaskPattern("\".*SSN.*\"\\s*:\\s*\"(.*?)\"");
        maskSensitiveDataPatternLayout.addMaskPattern("\".*address.*\"\\s*:\\s*\"(.*?)\"");
        maskSensitiveDataPatternLayout.addMaskPattern("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
        maskSensitiveDataPatternLayout.addMaskPattern("(\\w+\\.?\\w+@\\w+\\.\\w+)");
        maskSensitiveDataPatternLayout.addMaskPattern("\".*password.*\"\\s*:\\s*\"(.*?)\"");

        maskSensitiveDataPatternLayout.addMaskPattern("address:\\s*(.*?)\\\\n");
        maskSensitiveDataPatternLayout.addMaskPattern("postalAddress:\\s*(.*?)\\\\n");
        maskSensitiveDataPatternLayout.addMaskPattern("emailAddress:\\s*(.*?)\\\\n");
        maskSensitiveDataPatternLayout.addMaskPattern("telephoneNumber:\\s*(.*?)\\\\n");
    }

    @Test
    void testMaskingLogs() {
        String fromLogfile = ("Retrieved related party organization: class PartyV1DomainOrganizationOrganization {\\n    businessNumber: null\\n    contactMediums: [class PartyV1DomainContactMedium {\\n        contactMedium: null\\n        contactType: null\\n        emailAddress: ivan.pesl@beyondnow.com \\n        parameters: null\\n        postalAddress: null\\n        preferred: false\\n        role: null\\n        telephoneNumber: null\\n        type: Email\\n        validFor: class CommonsTimeInterval {\\n            end: null\\n            start: 2022-05-02T11:45:59Z\\n        }\\n    }, class PartyV1DomainContactMedium {\\n        contactMedium: null\\n        contactType: null\\n        emailAddress: null\\n        parameters: null\\n        postalAddress: 25270388\\n        preferred: false\\n        role: CONTACT_ADDRESS\\n        telephoneNumber: null\\n        type: PostalAddress\\n        validFor: class CommonsTimeInterval {\\n            end: null\\n            start: 2022-05-02T11:45:59Z\\n        }\\n    }]\\n    contactPerson: null\\n    customerAccount: null\\n    externalReferences: null\\n    id: b9605359-8c00-4a4e-af35-8f96ab364847\\n    identifications: null\\n    industryType: HOSPITALITY_STUDENT_HOUSING\\n    legalEntity: false\\n    legalName: null\\n    nameType: null\\n    organizationType: null\\n    otherNames: null\\n    parameters: null\\n    partyType: Organization\\n    preferredLanguage: null\\n    primary: true\\n    relatedEntities: [class PartyV1DomainRelatedEntity {\\n        links: null\\n        contactMedium: null\\n        entity: 1191277019\\n        entityType: CustomerAccount\\n        name: null\\n        party: null\\n        primary: true\\n        role: CUSTOMER_ACCOUNT_OWNER\\n        type: null\\n    }]\\n    relatedParties: [class PartyV1DomainRelatedParty {\\n        links: null\\n        contactMedium: null\\n        id: null\\n        name: null\\n        party: 983aea64-6779-4ef1-a389-a100554b10e3\\n        primary: true\\n        role: ORGANIZATION_CONTACT\\n        type: Individual\\n    }]\\n    state: Validated\\n    tradingName: Nic Ne Delam 8 d.o.o.\\n    type: OTHER\\n    validFor: class CommonsTimeInterval {\\n        end: null\\n        start: 2022-05-02T11:45:***\\n    }\\n");
        String loggingEvent = "{\n" +
                "    \"user_id\":\"87656\",\n" +
                "    \"blassnbla\":\"786445563\",\n" +
                "    \"address\":\"22 Street\",\n" +
                "    address: 22 Street\\n" +
                "    address: 22 Street\\n   address: 22 Street\\n   address: 22 Street\\n" +
                "    \"city\":\"Chicago\",\n" +
                "    \"Country\":\"U.S.\",\n" +
                "    \"ip_address\":\"192.168.1.1\",\n" +
                "    \"email_id\":\"bla@bla.com\"\n" +
                "    email_id: bla.bla@bla.com\n" +
                "    \"googlePassword\":\"mypassword\"\n" +
                " }";

        String masked = maskSensitiveDataPatternLayout.maskMessage(fromLogfile);

        System.out.println(fromLogfile);
        System.out.println(masked);
    }
}