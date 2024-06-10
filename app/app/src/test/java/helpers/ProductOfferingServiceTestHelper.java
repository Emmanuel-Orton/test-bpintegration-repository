package helpers;

import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductInventoryService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOfferingsService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import org.mockito.Mockito;

public class ProductOfferingServiceTestHelper {
	ProductOfferingsService mockProductOfferingsService;

	public ProductOfferingServiceTestHelper(ProductOfferingsService mockProductOfferingsService) {
		this.mockProductOfferingsService = mockProductOfferingsService;
	}

	public void mockInternalName(String rtTenant, String productOfferingName, String internalName) {
		Mockito.when(mockProductOfferingsService.getProductOfferingInternalName(rtTenant, productOfferingName))
				.thenReturn(internalName);
	}

}
