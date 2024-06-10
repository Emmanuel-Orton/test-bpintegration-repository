package helpers;

import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomerDataServiceTestHelper {

	CustomerDataService mockCustomerDataService;


	public CustomerDataServiceTestHelper(CustomerDataService mockCustomerDataService) {
		this.mockCustomerDataService = mockCustomerDataService;
	}

	public void mockCustomerTradingName(String tenant, String rtBillingAccount, String customerTradingName) {
		when(mockCustomerDataService.getCustomerTradingName(tenant, rtBillingAccount)).thenReturn(customerTradingName);
	}

}
