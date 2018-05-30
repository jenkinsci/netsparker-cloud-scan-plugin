package com.netsparker.cloud.plugin;

import com.netsparker.cloud.utility.AppCommon;
import com.netsparker.cloud.model.*;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.corn.httpclient.HttpResponse;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;


public class NCScanBuilder extends Builder implements SimpleBuildStep{
	
	private String ncScanType;
	private String ncWebsiteId;
	private String ncProfileId;
	
	@DataBoundConstructor
	public NCScanBuilder(String ncServerURL, String ncApiToken, String ncScanType, String ncWebsiteId, String ncProfileId) {
		this.ncScanType = ncScanType == null ? "" : ncScanType;
		this.ncWebsiteId = ncWebsiteId == null ? "" : ncWebsiteId;
		this.ncProfileId = ncProfileId == null ? "" : ncProfileId;
		
	}
	
	public String getNcScanType() {
		return ncScanType;
	}
	
	public void setNcScanType(String ncScanType) {
		this.ncScanType = ncScanType;
	}
	
	public String getNcWebsiteId() {
		return ncWebsiteId;
	}
	
	public void setNcWebsiteId(String ncTargetURL) {
		this.ncWebsiteId = ncTargetURL;
	}
	
	public String getNcProfileId() {
		return ncProfileId;
	}
	
	@DataBoundSetter
	public void setNcProfileId(String ncProfileName) {
		this.ncProfileId = ncProfileName;
	}
	
	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		logInfo("Scan step created...", listener);
		NCScanSCMAction scmAction = build.getAction(NCScanSCMAction.class);
		VCSCommit commit = scmAction == null ? VCSCommit.Empty() : scmAction.getVcsCommit();
		
		try {
			ScanRequestHandler(build, commit, listener);
		} catch (Exception e) {
			build.addAction(new NCScanResultAction(ScanRequestResult.ErrorResult()));
			throw new IOException(e);
		}
	}
	
	private void ScanRequestHandler(Run<?, ?> build, VCSCommit commit, TaskListener listener) throws Exception {
		DescriptorImpl descriptor = getDescriptor();
		String ncServerURL = descriptor.getNcServerURL();
		String ncApiToken = descriptor.getNcApiToken();
		commit.setRootURL(descriptor.getRootURL());
		
		ScanRequest scanRequest = new ScanRequest(
				descriptor.getNcServerURL(), descriptor.getNcApiToken(), ncScanType, ncWebsiteId, ncProfileId, commit);
		
		logInfo("Requesting scan...", listener);
		HttpResponse scanRequestResponse = scanRequest.scanRequest();
		logInfo("Response status code: " + scanRequestResponse.getCode(), listener);
		
		ScanRequestResult scanRequestResult = new ScanRequestResult(scanRequestResponse, ncServerURL, ncApiToken);
		// HTTP status code 201 refers to created. This means our request added to queue. Otherwise it is failed.
		if (scanRequestResult.getHttpStatusCode() == 201 && !scanRequestResult.isError()) {
			ScanRequestSuccessHandler(build, scanRequestResult, listener);
		} else {
			ScanRequestFailureHandler(scanRequestResult, listener);
		}
	}
	
	private void ScanRequestSuccessHandler(Run<?, ?> build, ScanRequestResult scanRequestResult, TaskListener listener) throws IOException {
		build.addAction(new NCScanResultAction(scanRequestResult));
		logInfo("Scan requested successfully.", listener);
	}
	
	private void ScanRequestFailureHandler(ScanRequestResult scanRequestResult, TaskListener listener) throws Exception {
		logError("Scan request failed. Error Message: " + scanRequestResult.getErrorMessage(), listener);
		throw new Exception("Netsparker Cloud Plugin: Failed to start the scan. Response status code: " + scanRequestResult.getHttpStatusCode());
	}
	
	private void logInfo(String message, TaskListener listener) {
		listener.getLogger().println("> Netsparker Cloud Plugin: " + message);
	}
	
	private void logError(String message, TaskListener listener) {
		listener.error("> Netsparker Cloud Plugin: " + message);
	}
	
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}
	
	@Symbol("NCScanBuilder")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder>{
		private long lastEditorId = 0;
		private ArrayList<WebsiteModel> websiteModels = new ArrayList<>();
		
		private String ncServerURL;
		private String ncApiToken;
		private String rootURL;
		
		public DescriptorImpl() {
			super(NCScanBuilder.class);
			load();
		}
		
		public String getNcServerURL() {
			return ncServerURL;
		}
		
		public void setNcServerURL(String ncServerURL) {
			this.ncServerURL = ncServerURL;
		}
		
		public String getNcApiToken() {
			return ncApiToken;
		}
		
		public void setNcApiToken(String ncApiToken) {
			this.ncApiToken = ncApiToken;
		}
		
		public String getRootURL() {
			return rootURL;
		}
		
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}
		
		@Override
		public String getDisplayName() {
			return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
		}
		
		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			req.bindParameters(this);
			this.ncServerURL = formData.getString("ncServerURL");
			this.ncApiToken = formData.getString("ncApiToken");
			this.rootURL = Jenkins.getInstance().getRootUrl();
			save();
			return super.configure(req, formData);
		}
		
		@Override
		public String getConfigPage() {
			try {
				doValidateAPI(ncServerURL, ncApiToken);
			} catch (Exception e) {
			}
			
			return super.getConfigPage();
		}
		
		@JavaScriptMethod
		public synchronized String createEditorId() {
			return String.valueOf(lastEditorId++);
		}
		
		public ListBoxModel doFillNcScanTypeItems() {
			ListBoxModel model = new ListBoxModel();
			model.add("-- Please select a scan type --", "");
			model.add("Incremental", "Incremental");
			model.add("Full (With primary profile)", "FullWithPrimaryProfile");
			model.add("Full (With selected profile)", "FullWithSelectedProfile");
			
			return model;
		}
		
		public ListBoxModel doFillNcWebsiteIdItems() {
			ListBoxModel model = new ListBoxModel();
			model.add("-- Please select a website --", "");
			for (WebsiteModel websiteModel : websiteModels) {
				model.add(websiteModel.getDisplayName(), websiteModel.getId());
			}
			
			return model;
		}
		
		public ListBoxModel doFillNcProfileIdItems(@QueryParameter String ncWebsiteId) {
			WebsiteModel websiteModel = new WebsiteModel();
			for (WebsiteModel wm : websiteModels) {
				if (ncWebsiteId != null && wm.getId().equals(ncWebsiteId)) {
					websiteModel = wm;
					break;
				}
			}
			
			String placeholderText = "";
			final ArrayList<WebsiteProfileModel> websiteProfileModels = websiteModel.getProfiles();
			if (websiteProfileModels.isEmpty()) {
				placeholderText = "-- No profile found --";
			} else {
				placeholderText = "-- Please select a profile name --";
			}
			
			ListBoxModel model = new ListBoxModel();
			model.add(placeholderText, "");
			
			for (WebsiteProfileModel websiteProfileModel : websiteProfileModels) {
				model.add(websiteProfileModel.getName(), websiteProfileModel.getId());
			}
			
			return model;
		}
		
		public FormValidation doValidateAPI(@QueryParameter final String ncServerURL,
		                                    @QueryParameter final String ncApiToken) throws IOException, ServletException {
			try {
				WebsiteModelRequest websiteModelRequest = new WebsiteModelRequest(ncServerURL, ncApiToken);
				final HttpResponse response = websiteModelRequest.getPluginWebSiteModels();
				
				if (response.getCode() == 200) {
					websiteModels = new ArrayList<>();
					websiteModels.addAll(websiteModelRequest.getWebsiteModels());
					return FormValidation.ok("Successfully connected to the Netsparker Cloud.");
				} else {
					return FormValidation.error("Netsparker Cloud rejected the request. HTTP status code: " + response.getCode());
				}
			} catch (Exception e) {
				return FormValidation.error("Failed to connect to the Netsparker Cloud. : " + e.toString());
			}
		}
		
		public FormValidation doCheckNcServerURL(@QueryParameter String value)
				throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_missingApiURL());
			} else if (!AppCommon.IsUrlValid(value)) {
				return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidApiURL());
			}
			
			return FormValidation.ok();
		}
		
		public FormValidation doCheckNcApiToken(@QueryParameter String value)
				throws IOException, ServletException {
			if (value.length() == 0) {
				return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_missingApiToken());
			}
			
			return FormValidation.ok();
		}
		
		public FormValidation doCheckNcScanType(@QueryParameter String value)
				throws IOException, ServletException {
			
			try {
				ScanType type = ScanType.valueOf(value);
			} catch (Exception ex) {
				return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidScanType());
			}
			
			return FormValidation.ok();
		}
		
		
		public FormValidation doCheckNcWebsiteID(@QueryParameter String value)
				throws IOException, ServletException {
			
			if (!AppCommon.IsGUIDValid(value)) {
				return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidWebsiteId());
			}
			
			return FormValidation.ok();
		}
		
		public FormValidation doCheckNcProfileId(@QueryParameter String value, @QueryParameter String ncScanType)
				throws IOException, ServletException {
			
			boolean isRequired;
			
			try {
				ScanType type = ScanType.valueOf(ncScanType);
				isRequired = type != ScanType.FullWithPrimaryProfile;
			} catch (Exception ex) {
				return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidProfileId());
			}
			
			if (isRequired && !AppCommon.IsGUIDValid(value)) {
				return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidProfileId());
			}
			
			return FormValidation.ok();
		}
	}
}
