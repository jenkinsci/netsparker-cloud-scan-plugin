package com.netsparker.cloud.model;

import com.netsparker.cloud.utility.AppCommon;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class ScanRequestBase{
	protected static final String json = "application/json";
	
	//Called from server-side
	public ScanRequestBase(String apiURL, String apiToken) throws MalformedURLException {
		this.ApiURL = AppCommon.getBaseURL(apiURL);
		this.ApiToken = apiToken;
	}
	
	public ScanRequestBase() {
		this.ApiURL = null;
		this.ApiToken = null;
	}
	
	public final URL ApiURL;
	public final String ApiToken;
	
	protected HttpClient getHttpClient() {
		return HttpClientBuilder
				.create()
				.build();
	}
	
	protected String getAuthHeader() {
		String auth = ":" + ApiToken;
		byte[] encodedAuth = Base64.encodeBase64(
				auth.getBytes(StandardCharsets.ISO_8859_1));
		String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
		
		return authHeader;
	}
}