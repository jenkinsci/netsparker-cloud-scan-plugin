package com.netsparker.cloud.plugin;

import com.netsparker.cloud.model.VCSCommit;
import com.netsparker.cloud.utility.AppCommon;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This class registers an {@link SCMListener} with Jenkins which allows us to create
 * the "Checkout successful" event.
 */
@Extension
public class NCScanSCMListener<Entry> extends SCMListener{
	/**
	 * Invoked right after the source code for the build has been checked out. It will NOT be
	 * called if a checkout fails.
	 *
	 * @param build     - Current build
	 * @param scm       - Configured SCM
	 * @param listener  - Current build listener
	 * @param changelog - Changelog
	 * @throws Exception if an error is encountered
	 */
	@Override
	public void onChangeLogParsed(Run<?, ?> build, SCM scm, TaskListener listener, ChangeLogSet<?> changelog) throws Exception {
		super.onChangeLogParsed(build, scm, listener, changelog);
		boolean buildHasChange = !changelog.isEmptySet();
		String vcsName = changelog.getKind();
		String buildId = String.valueOf(build.number);
		String buildConfigurationName = build.getParent().getName();
		String buildURL = getBuildURL(build);
		
		VCSCommit vcsCommit;
		//ISO 8601-compliant date and time format
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		String dateString;
		if (buildHasChange) {
			final ChangeLogSet.Entry change = (ChangeLogSet.Entry) changelog.getItems()[0];
			final User author = change.getAuthor();
			
			long timestamp = change.getTimestamp();
			Date date = new Date(timestamp);
			dateString = dateFormat.format(date);
			String version = change.getCommitId();
			String fullName = author.getFullName();
			String displayName = author.getDisplayName();
			String userName;
			if (AppCommon.isValidEmailAddress(fullName)) {
				userName = fullName;
			} else if (AppCommon.isValidEmailAddress(displayName)) {
				userName = displayName;
			} else {
				userName = fullName;
			}
			
			vcsCommit = new VCSCommit(buildId, buildConfigurationName, buildURL, buildHasChange, vcsName, userName, version, dateString);
		} else {
			dateString = dateFormat.format(new Date());
			vcsCommit = new VCSCommit(buildId, buildConfigurationName, buildURL, buildHasChange, vcsName, "", "", dateString);
		}
		
		NCScanSCMAction ncScanSCMAction = new NCScanSCMAction(vcsCommit);
		build.addAction(ncScanSCMAction);
	}
	
	private String getBuildURL(Run<?, ?> build){

		try {
			return build.getUrl();
		}catch (Exception ex){
			return "";
		}
		
	}
}
