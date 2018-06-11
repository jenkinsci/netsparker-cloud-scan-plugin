package com.netsparker.cloud.model;

import net.sf.corn.httpclient.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Date;

public class ScanReport{
	private final HttpResponse reportRequestResponse;
	private final boolean hasError;
	
	public ScanReport(final HttpResponse reportRequestResponse, final boolean hasError) {
		this.reportRequestResponse = reportRequestResponse;
		this.hasError = hasError;
	}
	
	public boolean isReportGenerated() {
		//when report stored, it will be loaded from disk for later requests. There is an exception potential.
		try {
			return getContentType().equalsIgnoreCase("text/html");
		} catch (Exception ex) {
			return false;
		}
	}
	
	private String getContentType() {
		return reportRequestResponse.getHeaderFields().get("Content-Type").get(0);
	}
	
	public String getContent() throws ParseException {
		String content;
		try {
			if (hasError) {
				content = "Scan report is not available because scan request failed.";
			} else {
				String contentData = reportRequestResponse.getData();
				if(isReportGenerated()){
					content=contentData;
				}else{
					JSONParser parser = new JSONParser();
					JSONObject obj = (JSONObject) parser.parse(contentData);
					content = (String) obj.get("Message");
				}
			}
		} catch (Exception ex) {
			content = "An error occurred during the requesting scan report.";
		}
		return content;
	}
}