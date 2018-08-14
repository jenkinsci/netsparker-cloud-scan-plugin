package com.netsparker.cloud.model;

import com.netsparker.cloud.utility.AppCommon;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ScanRequestResult extends ScanRequestBase{
	public static ScanRequestResult errorResult(String errorMessage) {
		return new ScanRequestResult(errorMessage);
	}
	
	private String scanReportEndpoint;
	private final int httpStatusCode;
	private String data;
	private String scanTaskID;
	private boolean isError;
	private String errorMessage;
	private ScanReport report = null;
	private Date previousRequestTime;
	
	private ScanRequestResult(String errorMessage) {
		super();
		this.errorMessage = errorMessage;
		httpStatusCode = 0;
		isError = true;
		data = "";
	}
	
	public ScanRequestResult(HttpResponse response, String apiURL, String apiToken) throws MalformedURLException, URISyntaxException {
		super(apiURL, apiToken);
		httpStatusCode = response.getStatusLine().getStatusCode();
		isError = httpStatusCode != 201;
		
		if (!isError) {
			try {
				data = AppCommon.parseResponseToString(response);
				isError = !(boolean) AppCommon.parseJsonValue(data, "IsValid");
				if (!isError) {
					scanTaskID = (String) AppCommon.parseJsonValue(data, "ScanTaskId");
				} else {
					errorMessage = (String) AppCommon.parseJsonValue(data, "ErrorMessage");
				}
			} catch (ParseException ex) {
				isError = true;
				errorMessage = "Scan request result is not parsable::: " + ex.toString();
			} catch (IOException ex) {
				isError = true;
				errorMessage = "Scan request result is not readable::: " + ex.toString();
			}
		}
		
		String scanReportRelativeUrl = "api/1.0/scans/report/";
		URI scanReportEndpointUri = new URL(ApiURL, scanReportRelativeUrl).toURI();
		
		Map<String, String> queryparams = new HashMap<>();
		queryparams.put("Type", "ExecutiveSummary");
		queryparams.put("Format", "Html");
		queryparams.put("Id", scanTaskID);
		
		scanReportEndpoint = scanReportEndpointUri.toString() + "?" + AppCommon.mapToQueryString(queryparams);
	}
	
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isError() {
		return isError;
	}
	
	public boolean isReportGenerated() {
		return getReport().isReportGenerated();
	}
	
	private boolean canAskForReportFromNCCloud() {
		return previousRequestTime == null || new Date().getTime() - previousRequestTime.getTime() >= 60 * 1000;//1 min
	}
	
	public ScanReport getReport() {
		if (canAskForReportFromNCCloud()) {
			final ScanReport reportFromNcCloud = getReportFromNcCloud();
			previousRequestTime = new Date();
			
			return reportFromNcCloud;
		}
		
		return report;
	}
	
	private ScanReport getReportFromNcCloud() {
		if (!isError) {
			try {
				final HttpClient httpClient = getHttpClient();
				final HttpGet httpGet = new HttpGet(scanReportEndpoint);
				httpGet.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
				
				HttpResponse response = httpClient.execute(httpGet);
				report = new ScanReport(response, scanReportEndpoint);
			} catch (IOException ex) {
				String reportRequestErrorMessage = "Report result is not readable::: " + ex.toString();
				report = new ScanReport(false, "",
						true, reportRequestErrorMessage, scanReportEndpoint);
			}
		} else {
			report = new ScanReport(true, errorMessage,
					false, "", scanReportEndpoint);
		}
		
		return report;
	}
}
