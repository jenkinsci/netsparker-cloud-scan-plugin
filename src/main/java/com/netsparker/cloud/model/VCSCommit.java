package com.netsparker.cloud.model;

import jenkins.model.Jenkins;
import net.sf.corn.httpclient.HttpForm;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VCSCommit{
	
	public static VCSCommit Empty() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		String dateString = dateFormat.format(new Date());
		return new VCSCommit("", "","", false, "", "", "", dateString);
	}
	
	public VCSCommit(String buildId, String buildConfigurationName, String buildURL, boolean buildHasChange, String versionControlName, String Committer, String vcsVersion, String ciTimestamp) {
		this.ciBuildServerVersion = Jenkins.getVersion().toString();
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
	
	public void addVcsCommitInfo(HttpForm client) {
		client.putFieldValue("VcsCommitInfoModel.CiBuildId", buildId);
		client.putFieldValue("VcsCommitInfoModel.IntegrationSystem", "Jenkins");
		client.putFieldValue("VcsCommitInfoModel.CiBuildServerVersion", ciBuildServerVersion);
		client.putFieldValue("VcsCommitInfoModel.CiNcPluginVersion", ciNcPluginVersion);
		client.putFieldValue("VcsCommitInfoModel.CiBuildConfigurationName", buildConfigurationName);
		client.putFieldValue("VcsCommitInfoModel.CiBuildUrl", rootURL + buildURL);
		client.putFieldValue("VcsCommitInfoModel.CiBuildHasChange", String.valueOf(buildHasChange));
		client.putFieldValue("VcsCommitInfoModel.CiTimestamp", ciTimestamp);
		client.putFieldValue("VcsCommitInfoModel.VcsName", versionControlName);
		client.putFieldValue("VcsCommitInfoModel.VcsVersion", vcsVersion);
		client.putFieldValue("VcsCommitInfoModel.Committer", committer);
	}
}