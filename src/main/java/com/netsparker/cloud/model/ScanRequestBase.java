package com.netsparker.cloud.model;

import com.netsparker.cloud.utility.AppCommon;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.MalformedURLException;
import java.net.URL;

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
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials
				= new UsernamePasswordCredentials("", ApiToken);
		provider.setCredentials(AuthScope.ANY, credentials);
		
		return HttpClientBuilder
				.create()
				.setDefaultCredentialsProvider(provider)
				.build();
	}
}