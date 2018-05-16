package NCJenkinsPlugin;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class NCScanBuilderTest{

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    final String ncServerURL="http://localhost:2097";
    final String ncApiToken="JQsrdNhsZjjCNfBU7bedl2h7IwJ6H1ZzXD4TkBIAgqo=";
    final String ncScanType="DefaultSettings";
    final String ncTargetURL="http://php.testsparker.com/";
    final String ncProfileName="x";
    final String ncBaseScanId="702c2a8a-a6a8-42fe-55a0-a84703d44023";

    @Test
    public void testConfigRoundtrip() throws Exception {
       // FreeStyleProject project = jenkins.createFreeStyleProject();
       // project.getBuildersList().add(new NCScanBuilder(ncServerURL,ncApiToken,ncScanType,ncTargetURL));
       // project = jenkins.configRoundtrip(project);
       // jenkins.assertEqualDataBoundBeans(new NCScanBuilder(ncServerURL,ncApiToken,ncScanType,ncTargetURL), project.getBuildersList().get(0));
    }



    @Test
    public void testBuild() throws Exception {
       // FreeStyleProject project = jenkins.createFreeStyleProject();
       // project.getBuildersList().add(new NCScanBuilder(ncServerURL,ncApiToken,ncScanType,ncTargetURL));

       // FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
       // jenkins.assertLogContains("Scan requested successfully.", build);
    }
    

    @Test
    public void testScriptedPipeline() throws Exception {
        //String agentLabel = "my-agent";
        //jenkins.createOnlineSlave(Label.get(agentLabel));
        //WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        //String pipelineScript
        //        = "node {\n"
        //        + "  greet '" + name + "'\n"
        //        + "}";
        //job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        //WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        //String expectedString = "Hello, " + name + "!";
        //jenkins.assertLogContains(expectedString, completedBuild);
    }

}