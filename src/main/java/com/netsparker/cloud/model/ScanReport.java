package com.netsparker.cloud.model;

import com.netsparker.cloud.utility.AppCommon;
import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ScanReport{
	private String content = "";
	private String contentType = "";
	private int statusCode = 0;
	private final boolean scanRequestHasError;
	private final String scanRequestErrorMessage;
	private final boolean reportRequestHasError;
	private final String reportRequestErrorMessage;
	private final String requestURI;
	
	public ScanReport(HttpResponse reportRequestResponse, String requestURI) {
		setStatusCode(reportRequestResponse);
		setContentType(reportRequestResponse);
		setContent(reportRequestResponse);
		this.scanRequestHasError = false;
		this.scanRequestErrorMessage = "";
		this.reportRequestHasError = false;
		this.reportRequestErrorMessage = "";
		this.requestURI = requestURI;
	}
	
	public ScanReport(boolean scanRequestHasError, String scanRequestErrorMessage,
	                  boolean reportRequestHasError, String reportRequestErrorMessage, String requestURI) {
		this.scanRequestHasError = scanRequestHasError;
		this.scanRequestErrorMessage = scanRequestErrorMessage;
		this.reportRequestHasError = reportRequestHasError;
		this.reportRequestErrorMessage = reportRequestErrorMessage;
		this.requestURI = requestURI;
	}
	
	public boolean isReportGenerated() {
		//when report stored, it will be loaded from disk for later requests. There is an exception potential.
		try {
			return getContentType().equalsIgnoreCase("text/html");
		} catch (Exception ex) {
			return false;
		}
	}
	
	private void setStatusCode(HttpResponse httpResponse) {
		if (httpResponse != null && httpResponse.getStatusLine() != null) {
			statusCode = httpResponse.getStatusLine().getStatusCode();
		}
	}
	
	private String getContentType() {
		return contentType;
	}
	
	private void setContentType(HttpResponse httpResponse) {
		if (httpResponse == null) {
			contentType = "Error-Content";
		} else {
			contentType = httpResponse.getHeaders("Content-Type")[0].getValue();
		}
	}
	
	public String getContent() {
		return content;
	}
	
	private void setContent(HttpResponse httpResponse) {
		String content = "";
		try {
			if (scanRequestHasError) {
				content = ExceptionContent(content, scanRequestErrorMessage);
			} else if (reportRequestHasError) {
				content = ExceptionContent(content, reportRequestErrorMessage);
			} else {
				String contentData = AppCommon.parseResponseToString(httpResponse);
				if (isReportGenerated()) {
					content = contentData;
				} else {
					JSONParser parser = new JSONParser();
					JSONObject obj = (JSONObject) parser.parse(contentData);
					content = (String) obj.get("Message");
				}
			}
		} catch (ParseException ex) {
			content = ExceptionContent("Report result is not parsable.", ex.toString());
		} catch (Exception ex) {
			content = ExceptionContent(content, ex.toString());
		}
		
		this.content = content;
	}
	
	private String ExceptionContent(String content, String ExceptionMessage) {
		if (content != null && !content.isEmpty()) {
			content = "<p>" + content + "</p>";
		}
		if (requestURI != null) {
			content = content
					+ "<p>Request URL: " + requestURI + "</p>";
		}
		content = content + "<p>HttpStatusCode: " + statusCode + "</p>";
		
		if (ExceptionMessage != null) {
			content = content
					+ "<p>Exception Message:: " + ExceptionMessage + "</p>";
		}
		
		return content;
	}
}