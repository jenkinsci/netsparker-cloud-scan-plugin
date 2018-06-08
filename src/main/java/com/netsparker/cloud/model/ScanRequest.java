package com.netsparker.cloud.model;

import net.sf.corn.httpclient.HttpForm;
import net.sf.corn.httpclient.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ScanRequest extends ScanRequestBase{
	public ScanRequest(String apiURL, String apiToken, String scanType, String websiteId, String profileId, VCSCommit vcsCommit) throws MalformedURLException, NullPointerException, URISyntaxException {
		super(apiURL, apiToken);
		this.scanType = ScanType.valueOf(scanType);
		this.websiteId = websiteId;
		this.profileId = profileId;
		this.vcsCommit = vcsCommit;
		scanUri = getRequestEndpoint();
		testUri = getTestEndpoint();
	}
	
	public final ScanType scanType;
	public final String websiteId;
	public final String profileId;
	public final VCSCommit vcsCommit;
	public final URI scanUri;
	public final URI testUri;
	
	
	public HttpResponse scanRequest() throws IOException, URISyntaxException {
		HttpForm client = getHttpClient();
		HttpResponse response = client.doPost();
		
		return response;
	}
	
	public HttpResponse testRequest() throws IOException, URISyntaxException {
		HttpForm client = getHttpTestClient();
		HttpResponse response = client.doPost();
		
		return response;
	}
	
	
	private URI getRequestEndpoint() throws MalformedURLException, URISyntaxException {
		String relativePath = "api/1.0/scans/CreateFromPluginScanRequest";
		return new URL(ApiURL, relativePath).toURI();
	}
	
	
	private URI getTestEndpoint() throws MalformedURLException, URISyntaxException {
		String relativePath = "api/1.0/scans/VerifyPluginScanRequest";
		return new URL(ApiURL, relativePath).toURI();
	}
	
	private HttpForm getHttpTestClient() throws MalformedURLException, URISyntaxException {
		HttpForm client = new HttpForm(testUri);
		//default is XML
		client.setAcceptedType(json);
		// Basic Authentication
		client.setCredentials("", ApiToken);
		setScanParams(client);
		
		return client;
	}
	
	private HttpForm getHttpClient() throws MalformedURLException, URISyntaxException {
		HttpForm client = new HttpForm(scanUri);
		//default is XML
		client.setAcceptedType(json);
		// Basic Authentication
		client.setCredentials("", ApiToken);
		setScanParams(client);
		vcsCommit.addVcsCommitInfo(client);
		
		return client;
	}
	
	private void setScanParams(HttpForm client) {
		switch (scanType) {
			case Incremental:
				client.putFieldValue("WebsiteId", websiteId);
				client.putFieldValue("ProfileId", profileId);
				client.putFieldValue("ScanType", "Incremental");
				break;
			case FullWithPrimaryProfile:
				client.putFieldValue("WebsiteId", websiteId);
				client.putFieldValue("ScanType", "FullWithPrimaryProfile");
				break;
			case FullWithSelectedProfile:
				client.putFieldValue("WebsiteId", websiteId);
				client.putFieldValue("ProfileId", profileId);
				client.putFieldValue("ScanType", "FullWithSelectedProfile");
				break;
		}
	}
}

