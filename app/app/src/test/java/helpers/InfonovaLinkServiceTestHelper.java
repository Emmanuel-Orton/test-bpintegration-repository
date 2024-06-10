package helpers;

import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import org.mockito.Mockito;

public class InfonovaLinkServiceTestHelper {
	InfonovaLinkService mockInfonovaLinkService;

	public InfonovaLinkServiceTestHelper(InfonovaLinkService mockInfonovaLinkService) {
		this.mockInfonovaLinkService = mockInfonovaLinkService;
	}

	public void mockWsOrderLink(String wsOrderId, String wsBillingAccount, String orderLink) {
		Mockito.when(mockInfonovaLinkService.getOrderLink(TenantType.WHOLESALE, wsOrderId, wsBillingAccount))
				.thenReturn(orderLink);
	}

	public void mockRtOrderLink(String rtOrderId, String rtBillingAccount, String orderLink) {
		Mockito.when(mockInfonovaLinkService.getOrderLink(TenantType.RETAIL, rtOrderId, rtBillingAccount))
				.thenReturn(orderLink);
	}

	public void expectWsCsmOrderLink(String wsOrderId, String wsBillingAccount, String orderLink) {
		Mockito.when(mockInfonovaLinkService.getOrderLink(TenantType.WHOLESALE_CSM, wsOrderId, wsBillingAccount))
				.thenReturn(orderLink);
	}


}
