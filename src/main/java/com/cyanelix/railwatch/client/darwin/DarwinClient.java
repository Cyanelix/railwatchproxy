package com.cyanelix.railwatch.client.darwin;

import javax.xml.bind.JAXBElement;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class DarwinClient extends WebServiceGatewaySupport {
	public <S, T> T sendAndReceive(DarwinRequest<?> request, DarwinActionType<S, T> actionType) {
		@SuppressWarnings("unchecked")
		JAXBElement<S> response = (JAXBElement<S>) getWebServiceTemplate().marshalSendAndReceive(
				"https://lite.realtime.nationalrail.co.uk/OpenLDBWS/ldb9.asmx",
				request.getSoapRequest(),
				new AccessTokenWebServiceMessageCallback(actionType.getAction(), getMarshaller()));
		
		return actionType.convertResponse(response.getValue());
	}
}