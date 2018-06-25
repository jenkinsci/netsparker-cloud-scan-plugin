package com.netsparker.cloud.model;

import hudson.util.VersionNumber;
import jenkins.model.Jenkins;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VCSCommit{
	
	public static VCSCommit empty() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		String dateString = dateFormat.format(new Date());
		return new VCSCommit("", "","", false, "", "", "", dateString);
	}
	
	public VCSCommit(String buildId, String buildConfigurationName, String buildURL, boolean buildHasChange, String versionControlName, String Committer, String vcsVersion, String ciTimestamp) {
		VersionNumber versionNumber=Jenkins.getVersion();
		this.ciBuildServerVersion = versionNumber != null ? versionNumber.toString() : "Not found.";
		this.ciNcPluginVersion = "1.0.0";
		this.buildId = buildId;
		this.buildConfigurationName = buildConfigurationName;
		this.buildURL=buildURL;
		this.buildHasChange = buildHasChange;
		this.versionControlName = versionControlName;
		this.committer = Committer;
		this.vcsVersion = vcsVersion;
		this.ciTimestamp = ciTimestamp;
	}
	
	private final String ciBuildServerVersion;
	private final String ciNcPluginVersion;
	private final String buildId;
	private final String buildConfigurationName;
	private final String buildURL;
	private final boolean buildHasChange;
	private final String versionControlName;
	private final String committer;
	private final String vcsVersion;
	private final String ciTimestamp;
	
	private String rootURL="";
	
	public void setRootURL(String rootURL) {
		if(rootURL==null){
			this.rootURL="";
			return;
		}
		this.rootURL = rootURL;
	}
	
	public void addVcsCommitInfo(List<NameValuePair> params) {
		params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildId", buildId));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.IntegrationSystem", "Jenkins"));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildServerVersion", ciBuildServerVersion));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.CiNcPluginVersion", ciNcPluginVersion));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildConfigurationName", buildConfigurationName));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildUrl", rootURL + buildURL));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildHasChange", String.valueOf(buildHasChange)));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.CiTimestamp", ciTimestamp));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.VcsName", versionControlName));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.VcsVersion", vcsVersion));
		params.add(new BasicNameValuePair("VcsCommitInfoModel.Committer", committer));
	}
}