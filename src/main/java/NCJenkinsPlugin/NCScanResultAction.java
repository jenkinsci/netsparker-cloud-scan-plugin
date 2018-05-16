package NCJenkinsPlugin;

import Model.ScanRequestResult;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.HttpResponses;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.GET;

import javax.annotation.CheckForNull;

public class NCScanResultAction implements Action, RunAction2{
	
	private ScanRequestResult scanRequestResult;
	
	public NCScanResultAction(ScanRequestResult scanRequestResult) {
		this.scanRequestResult = scanRequestResult;
	}
	
	public ScanRequestResult getScanRequestResult() {
		return scanRequestResult;
	}
	
	public void setScanRequestResult(ScanRequestResult scanRequestResult) {
		this.scanRequestResult = scanRequestResult;
	}
	
	public String getContent(){
		try {
			return scanRequestResult.getReport().getContent();
		} catch (Exception e) {
			return "An error occurred during the requesting scan report.";
		}
	}
	
	public String getError(){
		if(scanRequestResult.isError()){
			return "true";
		}else{
			return "false";
		}
	}
	
	public String getReportGenerated(){
		if(scanRequestResult.isReportGenerated()){
			return "true";
		}else{
			return "false";
		}
	}
	
	@GET
	public HttpResponse doGetContent() {
		String content=getContent();
		HttpResponse html = HttpResponses.html(content);
		return html;
	}
	
	@CheckForNull
	@Override
	public String getIconFileName() {
		return "document.png";
	}
	
	@CheckForNull
	@Override
	public String getDisplayName() {
		return "Netsparker Cloud Report";
	}
	
	@CheckForNull
	@Override
	public String getUrlName() {
		return "netsparkercloudreport";
	}
	
	private transient Run run;
	
	@Override
	public void onAttached(Run<?, ?> run) {
		this.run = run;
	}
	
	@Override
	public void onLoad(Run<?, ?> run) {
		this.run = run;
	}
	
	public Run getRun() {
		return run;
	}
}
