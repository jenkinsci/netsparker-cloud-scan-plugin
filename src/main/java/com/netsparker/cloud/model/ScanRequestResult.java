package com.netsparker.cloud.model;

import com.netsparker.cloud.utility.AppCommon;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ScanRequestResult extends ScanRequestBase{
	public static ScanRequestResult errorResult() {
		return new ScanRequestResult();
	}
	
	private final URI scanReportEndpointUri;
	
	private final int httpStatusCode;
	private final String data;
	
	private String scanTaskID;
	private boolean isError;
	private String errorMessage;
	
	//Response from Netsparker Cloud API
	private ScanReport report = null;
	private Date previousRequestTime;
	
	
	private ScanRequestResult() {
		super();
		isError = true;
		scanTaskID = null;
		data = null;
		httpStatusCode = 0;
		scanReportEndpointUri = null;
	}
	
	public ScanRequestResult(HttpResponse response, String apiURL, String apiToken) throws Exception {
		super(apiURL, apiToken);
		httpStatusCode = response.getStatusLine().getStatusCode();
		isError = httpStatusCode != 201;
		data = AppCommon.parseResponseToString(response);
		String scanReportRelativeUrl = "api/1.0/scans/report/%s" + getReportParams();
		scanReportEndpointUri = new URL(ApiURL, String.format(scanReportRelativeUrl, scanTaskID)).toURI();
		
		try {
			isError = !(boolean) AppCommon.parseJsonValue(data, "IsValid");
			if (!isError) {
				scanTaskID = (String) AppCommon.parseJsonValue(data, "ScanTaskId");
			} else {
				errorMessage = (String) AppCommon.parseJsonValue(data, "ErrorMessage");
			}
		} catch (Exception ex) {
			isError = true;
			errorMessage = "Scan request result is not parsable.";
		}
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
		//If scan request is failed we don't need additional check.
		if (isError()) {
			return false;
		} else if (isReportAvailable()) {
			return true;
		} else if (canAskForReportFromNCCloud()) {//If report is not requested or report wasn't ready in previous request we must check again.
			try {
				final ScanReport report = getReport();
				return report.isReportGenerated();
			} catch (Exception ex) {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public boolean canAskForReportFromNCCloud() {
		Date now = new Date();
		//Is report not requested or have request threshold passed
		//And report isn't generated yet
		boolean isTimeThresholdPassed = previousRequestTime == null || now.getTime() - previousRequestTime.getTime() >= 60 * 1000;//1 min
		return !isReportAvailable() && isTimeThresholdPassed;
	}
	
	
	private boolean isReportAvailable() {
		return report != null && report.isReportGenerated();
	}
	
	
	public ScanReport getReport() throws IOException, URISyntaxException {
		// if report is not generated and requested yet, request it from ncCloudserver.
		if (canAskForReportFromNCCloud()) {
			final ScanReport reportFromNcCloud = getReportFromNcCloud();
			previousRequestTime = new Date();
			return reportFromNcCloud;
		}
		return report;
	}
	
	private ScanReport getReportFromNcCloud() throws IOException, URISyntaxException {
		final HttpClient httpClient = getHttpClient();
		final HttpGet httpGet = new HttpGet(scanReportEndpointUri);
		httpGet.addHeader("Accept", "text/html");
		
		HttpResponse response = httpClient.execute(httpGet);
		ScanReport report = new ScanReport(response, isError());
		this.report = report;
		
		return report;
	}
	
	private String getReportParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Type", ReportType.ExecutiveSummary.getNumberAsString());
		map.put("Format", "3");
		
		return AppCommon.mapToQueryString(map);
	}
}
