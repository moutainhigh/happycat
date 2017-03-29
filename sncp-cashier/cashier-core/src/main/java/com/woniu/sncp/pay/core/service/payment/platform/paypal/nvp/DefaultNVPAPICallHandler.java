package com.woniu.sncp.pay.core.service.payment.platform.paypal.nvp;

import java.util.HashMap;
import java.util.Map;

import com.paypal.core.APICallPreHandler;
import com.paypal.core.Constants;
import com.paypal.core.CredentialManager;
import com.paypal.core.SDKUtil;
import com.paypal.core.credential.CertificateCredential;
import com.paypal.core.credential.ICredential;
import com.paypal.core.credential.SignatureCredential;
import com.paypal.core.nvp.CertificateHttpHeaderAuthStrategy;
import com.paypal.core.nvp.SignatureHttpHeaderAuthStrategy;
import com.paypal.exception.ClientActionRequiredException;
import com.paypal.exception.InvalidCredentialException;
import com.paypal.exception.MissingCredentialException;
import com.paypal.sdk.exceptions.OAuthException;
import com.paypal.sdk.util.UserAgentHeader;

public class DefaultNVPAPICallHandler implements APICallPreHandler {

	public static final String MERCHANT_SANDBOX_NVP_ENDPOINT = "https://api-3t.sandbox.paypal.com/nvp";
	public static final String MERCHANT_LIVE_NVP_ENDPOINT = "https://api-3t.paypal.com/nvp";

	private Map<String, String> configurationMap = null;

	private String methodName;

	/**
	 * API Username for authentication
	 */
	private String apiUserName;

	/**
	 * SDK Version
	 */
	private String version;

	/**
	 * PortName to which a particular operation is bound;
	 */
	private String portName;

	/**
	 * Internal variable to hold headers
	 */
	private Map<String, String> headers;

	private String payload;

	/**
	 * {@link ICredential} for authentication
	 */
	private ICredential credential;

	public DefaultNVPAPICallHandler(String methodName, String apiUserName, String version, String portName,
			String payload, Map<String, String> configurationMap) throws InvalidCredentialException,
			MissingCredentialException {
		this.apiUserName = apiUserName;
		this.methodName = methodName;
		this.payload = payload;
		this.version = version;
		this.portName = portName;
		this.configurationMap = SDKUtil.combineDefaultMap(configurationMap);
		initCredential();
	}
	
	public DefaultNVPAPICallHandler(String methodName, String version, String portName,
			String payload,ICredential credential, Map<String, String> configurationMap) {
		this.methodName = methodName;
		this.payload = payload;
		this.version = version;
		this.portName = portName;
		this.credential = credential;
		this.configurationMap = SDKUtil.combineDefaultMap(configurationMap);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public Map<String, String> getHeaderMap() throws OAuthException {
		if (headers == null) {
			headers = new HashMap<String, String>();
			if (credential instanceof SignatureCredential) {
				SignatureHttpHeaderAuthStrategy signatureHttpHeaderAuthStrategy = new SignatureHttpHeaderAuthStrategy(
						getEndPoint());
				headers = signatureHttpHeaderAuthStrategy
						.generateHeaderStrategy((SignatureCredential) credential);
			} else if (credential instanceof CertificateCredential) {
				CertificateHttpHeaderAuthStrategy certificateHttpHeaderAuthStrategy = new CertificateHttpHeaderAuthStrategy(
						getEndPoint());
				headers = certificateHttpHeaderAuthStrategy.generateHeaderStrategy((CertificateCredential) credential);
			}
			// Append HTTP Content-Type
			headers.put(Constants.HTTP_CONTENT_TYPE_HEADER, Constants.HTTP_CONFIG_DEFAULT_CONTENT_TYPE);
			headers.putAll(getDefaultHttpHeadersNVP());
		}
		return headers;
	}

	@Override
	public String getPayLoad() {
		
		StringBuilder builder = new StringBuilder();
		builder.append("METHOD=").append(getMethodName()).append("&");
		builder.append("VERSION=").append(getVersion()).append("&");
		if (credential instanceof SignatureCredential) {
			SignatureCredential sc = (SignatureCredential)credential;
			builder.append("PWD=").append(sc.getPassword()).append("&");
			builder.append("USER=").append(sc.getUserName()).append("&");
			builder.append("SIGNATURE=").append(sc.getSignature());
		}
		if (!payload.startsWith("&")) {
			builder.append("&");
		}
		builder.append(payload);
		return builder.toString();
	}

	@Override
	public String getEndPoint() {
		String endPoint = searchEndpoint();
		if (endPoint != null) {

		} else if ((Constants.SANDBOX.equalsIgnoreCase(this.configurationMap
				.get(Constants.MODE).trim()))) {
			endPoint = MERCHANT_SANDBOX_NVP_ENDPOINT;
		} else if ((Constants.LIVE.equalsIgnoreCase(this.configurationMap.get(
				Constants.MODE).trim()))) {
			endPoint = MERCHANT_LIVE_NVP_ENDPOINT;
		}
		return endPoint;
	}

	@Override
	public ICredential getCredential() {
		return credential;
	}

	@Override
	public void validate() throws ClientActionRequiredException {
		String mode = configurationMap.get(Constants.MODE);
		if (mode == null && searchEndpoint() == null) {
			// Mandatory Mode not specified.
			throw new ClientActionRequiredException(
					"mode[production/live] OR endpoint not specified");
		}
		if ((mode != null)
				&& (!mode.trim().equalsIgnoreCase(Constants.LIVE) && !mode
						.trim().equalsIgnoreCase(Constants.SANDBOX))) {
			// Mandatory Mode not specified.
			throw new ClientActionRequiredException(
					"mode[production/live] OR endpoint not specified");
		}
	}

	/*
	 * Search a valid endpoint in the configuration, returning null if not found
	 */
	private String searchEndpoint() {
		String endPoint = this.configurationMap.get(Constants.ENDPOINT + "."
				+ getPortName()) != null ? this.configurationMap
				.get(Constants.ENDPOINT + "." + getPortName())
				: (this.configurationMap.get(Constants.ENDPOINT) != null ? this.configurationMap
						.get(Constants.ENDPOINT) : null);
		if (endPoint != null && endPoint.trim().length() <= 0) {
			endPoint = null;
		}
		return endPoint;
	}

	private ICredential getCredentials() throws InvalidCredentialException,
			MissingCredentialException {
		CredentialManager credentialManager = new CredentialManager(this.configurationMap);
		ICredential returnCredential = credentialManager.getCredentialObject(apiUserName);
		return returnCredential;
	}

	private Map<String, String> getDefaultHttpHeadersNVP() {
		Map<String, String> returnMap = new HashMap<String, String>();
		returnMap.put(Constants.PAYPAL_REQUEST_DATA_FORMAT_HEADER, Constants.PAYLOAD_FORMAT_NVP);
		returnMap.put(Constants.PAYPAL_RESPONSE_DATA_FORMAT_HEADER, Constants.PAYLOAD_FORMAT_NVP);

		// Add user-agent header
		UserAgentHeader uaHeader = new UserAgentHeader(Constants.SDK_ID, Constants.SDK_VERSION);
		returnMap.putAll(uaHeader.getHeader());

		String sandboxEmailAddress = this.configurationMap.get(Constants.SANDBOX_EMAIL_ADDRESS);
		if (sandboxEmailAddress != null 
				&& Constants.SANDBOX.equalsIgnoreCase(this.configurationMap.get(Constants.MODE).trim())) {
			returnMap.put(Constants.PAYPAL_SANDBOX_EMAIL_ADDRESS_HEADER, sandboxEmailAddress);
		}
		return returnMap;
	}
	
	private void initCredential() throws InvalidCredentialException,
			MissingCredentialException {
		if (credential == null) {
			credential = getCredentials();
		}
	}

}
