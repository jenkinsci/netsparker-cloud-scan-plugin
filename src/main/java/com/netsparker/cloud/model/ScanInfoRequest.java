package com.netsparker.cloud.model;

import hudson.util.Secret;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ScanInfoRequest extends ScanRequestBase {
    public ScanInfoRequest(String apiURL, Secret apiToken,String scanTaskId, Boolean doNotFail, Boolean isConfirmed, 
    IgnoredVulnerabilityStateFilters filters) throws MalformedURLException, NullPointerException, URISyntaxException {
        super(apiURL, apiToken);
        this.scanTaskId = scanTaskId;
        this.scanInfoUri = new URL(ApiURL, "api/1.0/scans/ScanInfoForPlugin/").toURI();
        this.filters = filters;
        this.doNotFail = doNotFail;
        this.isConfirmed = isConfirmed;
    }

    public final String scanTaskId;
    public final URI scanInfoUri;
    public final IgnoredVulnerabilityStateFilters filters;
    public final Boolean doNotFail;
    public final Boolean isConfirmed;

    public HttpResponse scanInfoRequestOld() throws IOException {
        HttpClient client = getHttpClient();
        final HttpGet httpGet = new HttpGet(scanInfoUri + scanTaskId);
        httpGet.setHeader("Accept", json);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

        HttpResponse response = client.execute(httpGet);

        return response;
    }

    public HttpResponse scanInfoRequest() throws IOException {
        HttpClient httpClient = getHttpClient();
        HttpResponse response = null;
        try {
            HttpPost request = new HttpPost(scanInfoUri);

            StringBuilder jsonString = new StringBuilder();
            jsonString.append("{");
            jsonString.append("'ScanId':'").append(scanTaskId).append("',");
            jsonString.append("'DoNotFail':").append(doNotFail).append(",");
            jsonString.append("'IsConfirmed':").append(isConfirmed).append(",");
            filters.setFiltersString();
            jsonString.append("'IgnoredVulnerabilityStateFilters':").append(filters.getFiltersString());
            jsonString.append("}");
            StringEntity bodyEntity = new StringEntity(jsonString.toString(), ContentType.APPLICATION_JSON);
            // send a JSON data
            request.setEntity(bodyEntity);
            request.addHeader("Accept","application/json");
            request.setHeader("Content-Type", "application/json");
            request.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());

            response = httpClient.execute(request);
        }catch (Exception ex) {
            Exception e = ex;
        }
        return response;
    }
}
