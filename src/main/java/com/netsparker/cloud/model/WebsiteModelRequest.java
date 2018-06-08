package com.netsparker.cloud.model;

import net.sf.corn.httpclient.HttpForm;
import net.sf.corn.httpclient.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class WebsiteModelRequest extends ScanRequestBase{
	private ArrayList<WebsiteModel> websiteModels = new ArrayList<>();
	private HttpResponse response;
	
	public WebsiteModelRequest(String apiURL, String apiToken) throws MalformedURLException, NullPointerException, URISyntaxException {
		super(apiURL, apiToken);
		pluginWebSiteModelsUri = getPluginWebSiteModelsEndpoint();
	}
	
	private final URI pluginWebSiteModelsUri;
	
	public ArrayList<WebsiteModel> getWebsiteModels() {
		return websiteModels;
	}
	
	public HttpResponse getPluginWebSiteModels() throws IOException, URISyntaxException, ParseException {
		HttpForm client = new HttpForm(pluginWebSiteModelsUri);
		// Basic Authentication
		client.setCredentials("", ApiToken);
		client.setAcceptedType(json);
		response = client.doGet();
		if (response.getCode() == 200) {
			parseWebsiteData();
		}
		return response;
	}
	
	private URI getPluginWebSiteModelsEndpoint() throws MalformedURLException, URISyntaxException {
		String relativePath = "api/1.0/scans/PluginWebSiteModels";
		return new URL(ApiURL, relativePath).toURI();
	}
	
	private void parseWebsiteData() throws ParseException {
		String data = response.getData();
		
		JSONParser parser = new JSONParser();
		Object jsonData = parser.parse(data);
		
		JSONArray WebsiteModelObjects = (JSONArray) jsonData;
		websiteModels = new ArrayList<>();
		
		for (Object wmo : WebsiteModelObjects) {
			if (wmo instanceof JSONObject) {
				JSONObject websiteModelObject = (JSONObject) wmo;
				
				WebsiteModel websiteModel = new WebsiteModel();
				websiteModel.setId((String) websiteModelObject.get("Id"));
				websiteModel.setName((String) websiteModelObject.get("Name"));
				websiteModel.setUrl((String) websiteModelObject.get("Url"));
				
				JSONArray WebsiteProfileModelObjects = (JSONArray) websiteModelObject.get("WebsiteProfiles");
				ArrayList<WebsiteProfileModel> profiles = new ArrayList<>();
				for (Object wmpo : WebsiteProfileModelObjects) {
					JSONObject websiteProfileModelObject = (JSONObject) wmpo;
					
					WebsiteProfileModel websiteProfileModel = new WebsiteProfileModel();
					websiteProfileModel.setId((String) websiteProfileModelObject.get("Id"));
					websiteProfileModel.setName((String) websiteProfileModelObject.get("Name"));
					
					profiles.add(websiteProfileModel);
				}
				
				websiteModel.setProfiles(profiles);
				websiteModels.add(websiteModel);
			}
		}
	}
}
