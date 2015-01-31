package org.alfresco.share.workflow.actionsWithDocuments;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.po.share.MyTasksPage;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.CopyOrMoveContentPage;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.EditDocumentPropertiesPage;
import org.alfresco.po.share.site.document.EditTextDocumentPage;
import org.alfresco.po.share.task.TaskStatus;
import org.alfresco.po.share.workflow.CloudTaskOrReviewPage;
import org.alfresco.po.share.workflow.KeepContentStrategy;
import org.alfresco.po.share.workflow.Priority;
import org.alfresco.po.share.workflow.TaskType;
import org.alfresco.po.share.workflow.WorkFlowFormDetails;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.AbstractWorkflow;
import org.alfresco.share.util.EditTaskAction;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserSitePage;
import org.alfresco.share.util.ShareUserWorkFlow;
import org.alfresco.share.util.api.CreateUserAPI;
import org.alfresco.webdrone.testng.listener.FailedTestListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(FailedTestListener.class)
public class CompletedWorkflowSyncedTests extends AbstractWorkflow
{
    private String testDomain;
    private String completeWorkflow = "complete_workflow";
    private static Log logger = LogFactory.getLog(CompletedWorkflowSyncedTests.class);
    private String user1;
    private String cloudUser;

    @Override
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        super.setup();
        testName = this.getClass().getSimpleName();
        testDomain = DOMAIN_HYBRID;
        
        user1 = getUserNameForDomain(testName + "opUser", testDomain);
        cloudUser = getUserNameForDomain(testName + "cloudUser", testDomain);
    }

//    @BeforeClass(groups = "DataPrepHybridWorkflow", dependsOnMethods = "setup")
    public void dataPrep() throws Exception
    {
        String user1 = getUserNameForDomain(testName + "opUser", testDomain);
        String[] userInfo1 = new String[] { user1 };
        String cloudUser = getUserNameForDomain(testName + "cloudUser", testDomain);
        String[] cloudUserInfo1 = new String[] { cloudUser };

        // Create User1 (On-premise)
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, userInfo1);

        // Create User1 (Cloud)
        CreateUserAPI.CreateActivateUser(hybridDrone, ADMIN_USERNAME, cloudUserInfo1);
        CreateUserAPI.upgradeCloudAccount(hybridDrone, ADMIN_USERNAME, DOMAIN_HYBRID, "1000");

        // Login to User1, set up the cloud sync
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        signInToAlfrescoInTheCloud(drone, cloudUser, DEFAULT_PASSWORD);
    }

    // /**
    // * Data preparation for Complete Workflow Synced tests
    // */
    // @Test(groups = "DataPrepHybrid")
    // public void dataPrep_createUsers() throws Exception
    // {
    // dataPrep(completeWorkflow);
    // }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15690() throws Exception
    {

        createCompletedWorkflow("15690" + "A4");
    }

    /**
     * AONE-15690: Modify properties (OP)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15690() throws Exception
    {

        String prefix = "15690" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";
        String modifiedTitle = testName + "modifiedBy 1";
        String modifiedDescription = simpleTaskFile + " modified by 1";

        // ---- Step 1 ----
        // ---- Step action ---
        // OP Modify the synced document's properties, e.g. change title and description.
        // ---- Expected results ----
        // The properties are changed successfully.

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);

        EditDocumentPropertiesPage editDocumentProperties = ShareUserSitePage.getEditPropertiesFromDocLibPage(drone, opSiteName, simpleTaskFile);
        editDocumentProperties.setDocumentTitle(modifiedTitle + user1);
        editDocumentProperties.setDescription(modifiedDescription + user1);
        editDocumentProperties.selectSave().render();

        ShareUser.openSiteDashboard(drone, opSiteName).render();
        DocumentLibraryPage docLib = ShareUser.openDocumentLibrary(drone).render();
        docLib.getFileDirectoryInfo(simpleTaskFile).selectRequestSync().render();
        waitForSync(simpleTaskFile, opSiteName);
        ShareUser.logout(drone);

        // ---- Step 3 ----
        // ---- Step action ---
        // Cloud Verify the document.
        // ---- Expected results ----
        // Changes appeared to Cloud. The changed content is correctly displayed.
        // ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();

        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);

        EditDocumentPropertiesPage editDocumentPropertiescl = ShareUserSitePage.getEditPropertiesFromDocLibPage(hybridDrone, cloudSite, simpleTaskFile).render(
                maxWaitTime);

        Assert.assertTrue((modifiedTitle + user1).equals(editDocumentPropertiescl.getDocumentTitle()),
                "Document Title modified by OP User is not present for Cloud.");
        Assert.assertTrue((modifiedDescription + user1).equals(editDocumentPropertiescl.getDescription()),
                "Document Description modified by OP User is not present for Cloud.");

    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15691() throws Exception
    {

        createCompletedWorkflow("15691" + "A41");
    }

    /**
     * AONE-15691: Modify properties (Cloud)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15691() throws Exception
    {

        String prefix = "15691" + "A41";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";
        String modifiedTitle = testName + "modifiedBy 5";
        String modifiedDescription = simpleTaskFile + " modified by 5";

        // ---- Step 1 ----
        // ---- Step action ---
        // Cloud Modify the synced document's properties, e.g. change title and description.
        // ---- Expected results ----
        // The properties are changed successfully.

        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);

        EditDocumentPropertiesPage editDocumentProperties = ShareUserSitePage.getEditPropertiesFromDocLibPage(hybridDrone, cloudSite, simpleTaskFile);
        editDocumentProperties.setDocumentTitle(modifiedTitle + cloudUser);
        editDocumentProperties.setDescription(modifiedDescription + cloudUser);
        editDocumentProperties.selectSave().render();

        DocumentLibraryPage cldocumentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite);
        cldocumentLibraryPage.selectFile(simpleTaskFile);
        editDocumentProperties = ShareUserSitePage.getEditPropertiesFromDocLibPage(hybridDrone, cloudSite, simpleTaskFile);
        Assert.assertTrue((modifiedTitle + cloudUser).equals(editDocumentProperties.getDocumentTitle()),
                "Document Title modified by CL User is not present for Cloud.");

        ShareUser.logout(hybridDrone);
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);

        // ---- Step 3 ----
        // ---- Step action ---
        // OP Verify the document.
        // ---- Expected results ----
        // Changes appeared to OP. The changed content is correctly displayed.
        
        ShareUser.openSiteDashboard(drone, opSiteName).render();
        DocumentLibraryPage docLib = ShareUser.openDocumentLibrary(drone).render();
        docLib.getFileDirectoryInfo(simpleTaskFile).selectRequestSync().render();
        waitForSync(simpleTaskFile, opSiteName);
        
        editDocumentProperties = ShareUserSitePage.getEditPropertiesFromDocLibPage(drone, opSiteName, simpleTaskFile).render();

        Assert.assertTrue((modifiedTitle + cloudUser).equals(editDocumentProperties.getDocumentTitle()),
                "Document Title modified by Cloud User is not present for OP.");
        Assert.assertTrue((modifiedDescription + cloudUser).equals(editDocumentProperties.getDescription()),
                "Document Description modified by CLoud User is not present for OP.");

    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15692() throws Exception
    {

        createCompletedWorkflow("15692" + "A4");
    }

    /**
     * AONE-15692: Modify content (OP)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15692() throws Exception
    {

        String prefix = "15692" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";

        String simpleTaskFile = getFileName(prefix + testName) + ".txt";
        String modifiedContent = testName + "modified content";

        // ---- Step 1 ----
        // ---- Step action ---
        // OP Modify the synced document's content
        // Cloud Modify the synced document's properties, e.g. change title and description.
        // ---- Expected results ----
        // The content is changed successfully.
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        DocumentDetailsPage documentDetailsPage = documentLibraryPage.selectFile(simpleTaskFile);
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setContent(modifiedContent);
        contentDetails.setName(simpleTaskFile);

        EditTextDocumentPage inlineEditPage = documentDetailsPage.selectInlineEdit().render();
        documentDetailsPage = inlineEditPage.save(contentDetails).render();

        ShareUser.openSiteDashboard(drone, opSiteName).render();
        DocumentLibraryPage docLib = ShareUser.openDocumentLibrary(drone).render();
        docLib.getFileDirectoryInfo(simpleTaskFile).selectRequestSync().render();
        waitForSync(simpleTaskFile, opSiteName);

        ShareUser.logout(drone);

        // ---- Step 3 ----
        // ---- Step action ---
        // Cloud Verify the document.
        // ---- Expected results ----
        // Changes appeared to Cloud. The changed content is correctly displayed.

        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPageCL;
        DocumentDetailsPage documentDetailsPageCL;
        EditTextDocumentPage inlineEditPageCL = new EditTextDocumentPage(hybridDrone);

        documentLibraryPageCL = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();
        documentDetailsPageCL = documentLibraryPageCL.selectFile(simpleTaskFile);
        inlineEditPageCL = documentDetailsPageCL.selectInlineEdit().render();
        Assert.assertTrue(inlineEditPageCL.getDetails().getContent().contains(modifiedContent));
    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15693() throws Exception
    {

        createCompletedWorkflow("15693" + "A4");
    }

    /**
     * AONE-15683:Incomplete workflow - modify content (Cloud)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15693() throws Exception
    {

        String prefix = "15693" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";

        String simpleTaskFile = getFileName(prefix + testName) + ".txt";
        String modifiedContentOnCloud = testName + "modified content in CLOUD";

        // ---- Step 1 ----
        // ---- Step action ---
        // Cloud Modify the synced document's content.
        // ---- Expected results ----
        // The content is changed successfully.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite);
        DocumentDetailsPage documentDetailsPage = documentLibraryPage.selectFile(simpleTaskFile);
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setContent(modifiedContentOnCloud);
        contentDetails.setName(simpleTaskFile);

        EditTextDocumentPage inlineEditPage = documentDetailsPage.selectInlineEdit().render();
        documentDetailsPage = inlineEditPage.save(contentDetails).render();
        ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite);
        documentLibraryPage.selectFile(simpleTaskFile);
        inlineEditPage = documentDetailsPage.selectInlineEdit().render();
        Assert.assertTrue(inlineEditPage.getDetails().getContent().contains(modifiedContentOnCloud));

        ShareUser.logout(hybridDrone);

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);

        // ---- Step 3 ----
        // ---- Step action ---
        // OP Verify the document.
        // ---- Expected results ----
        // Changes appeared to OP. The changed content is correctly displayed.

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        ShareUser.openSiteDashboard(drone, opSiteName).render();
        DocumentLibraryPage docLib = ShareUser.openDocumentLibrary(drone).render();
        docLib.getFileDirectoryInfo(simpleTaskFile).selectRequestSync().render();
        waitForSync(simpleTaskFile, opSiteName);

        documentLibraryPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        documentDetailsPage = documentLibraryPage.selectFile(simpleTaskFile);
        inlineEditPage = documentDetailsPage.selectInlineEdit().render();
        Assert.assertTrue(inlineEditPage.getDetails().getContent().contains(modifiedContentOnCloud));
    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15694() throws Exception
    {

        createCompletedWorkflow("15694" + "A4");
    }

    /**
     * AONE-15694: Move (OP)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15694() throws Exception
    {
        String prefix = "15694" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";
        String folderName = getFolderName(prefix + testName);

        // ---- Step 1, 2 ----
        // ---- Step action ---
        // OP Move the synced document to another location.
        // ---- Expected results ----
        // The content is moved successfully.

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        CopyOrMoveContentPage moveToPage = documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).selectMoveTo().render();
        moveToPage.selectPath(folderName).render().selectOkButton().render();
        ShareUser.logout(drone);

        // ---- Step 3 ----
        // ---- Step action ---
        // Cloud Verify the document
        // ---- Expected results ----
        // The document is still synced.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();
        Assert.assertTrue(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isCloudSynced());
        ShareUser.logout(hybridDrone);
    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15695() throws Exception
    {

        createCompletedWorkflow("15695" + "A4");
    }

    /**
     * AONE-15695:Move (Cloud)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15695() throws Exception
    {
        String prefix = "15695" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";
        String folderName = getFolderName(prefix + testName);

        // ---- Step 1, 2 ----
        // ---- Step action ---
        // Cloud Move the synced document to another location
        // ---- Expected results ----
        // The content is moved successfully.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();
        CopyOrMoveContentPage moveToPage = documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).selectMoveTo().render();
        moveToPage.selectPath(folderName).render().selectOkButton().render();
        ShareUser.logout(hybridDrone);

        // ---- Step 3 ----
        // ---- Step action ---
        // OP Verify the document.
        // ---- Expected results ----
        // The document is still synced. Another location is displayed in the Sync details section.
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        documentLibraryPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        Assert.assertTrue(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isCloudSynced());
        Assert.assertTrue(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).clickOnViewCloudSyncInfo().render().getCloudSyncLocation()
                .contains(folderName));
        ShareUser.logout(hybridDrone);
    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15696() throws Exception
    {

        createCompletedWorkflow("15696" + "A4");
    }

    /**
     * AONE-15696: Remove (OP)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15696() throws Exception
    {
        String prefix = "15696" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";

        // ---- Step 1 ----
        // ---- Step action ---
        // OP Remove the synced document.
        // ---- Expected results ----
        // The content is removed successfully.
        // TODO: Modify step 1 in TestLink accordingly to the Assert !

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        documentLibraryPage.deleteItem(simpleTaskFile).render();
        Assert.assertFalse(documentLibraryPage.isFileVisible(simpleTaskFile));
        ShareUser.logout(drone);

        // ---- Step 3 ----
        // ---- Step action ---
        // Cloud Verify the document.
        // ---- Expected results ----
        // The document is removed.

        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();
        Assert.assertFalse(documentLibraryPage.isFileVisible(simpleTaskFile));
        ShareUser.logout(hybridDrone);
    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15697() throws Exception
    {

        createCompletedWorkflow("15697" + "A4");
    }

    /**
     * AONE-15697:Remove (Cloud)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15697() throws Exception
    {
        String prefix = "15697" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";

        // ---- Step 1 ----
        // ---- Step action ---
        // Cloud Remove the synced document.
        // ---- Expected results ----
        // The content is removed.
        // TODO: Modify step 1 in TestLink accordingly to the Assert !
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();
        documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).selectCheckbox();
        documentLibraryPage = ShareUser.deleteSelectedContent(hybridDrone).render();
        //
        // DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();
        // documentLibraryPage.deleteItem(simpleTaskFile).render(5000);
        Assert.assertFalse(documentLibraryPage.isFileVisible(simpleTaskFile));

        ShareUser.logout(hybridDrone);

        // ---- Step 2 ----
        // ---- Step action ---
        // OP Verify the document.
        // ---- Expected results ----
        // Document is present. Sync icon is still present for the document. (Won't be actual once ACE-529 is implemented); //ACE-529 was impelmented
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        Assert.assertFalse(documentLibraryPage.isFileVisible(simpleTaskFile));

        // Assert.assertTrue(opDocumentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isCloudSynced());
        // Assert.assertTrue(opDocumentLibraryPage.getFileDirectoryInfo(simpleTaskFile).clickOnViewCloudSyncInfo().render().getCloudSyncLocation()
        // .contains(cloudSite));
    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15698() throws Exception
    {

        createCompletedWorkflow("15698" + "A4");
    }

    /**
     * AONE-15698:Unsync (OP)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15698() throws Exception
    {
        String prefix = "15698" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";

        // ---- Step 1 ----
        // ---- Step action ---
        // OP Unsync the synced document.
        // ---- Expected results ----
        // The content is unsynced correctly.
        //TODO Update the test accordingly to assert
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        Assert.assertFalse(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isUnSyncFromCloudLinkPresent());

        ShareUser.logout(drone);
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();

        // ---- Step 3 ----
        // ---- Step action ---
        // Cloud Verify the document.
        // ---- Expected results ----
        // The document is unsynced.
        Assert.assertTrue(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isCloudSynced());
        Assert.assertFalse(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isUnSyncFromCloudLinkPresent());
        ShareUser.logout(hybridDrone);
    }

    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15699() throws Exception
    {

        createCompletedWorkflow("15699" + "A4");
    }

    /**
     * AONE-15699:Unsync (Cloud)
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15699() throws Exception
    {

        String prefix = "15699" + "A4";
        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSite = getSiteName(prefix + testName) + "-CL";
        String simpleTaskFile = getFileName(prefix + testName) + ".txt";

        // ---- Step 1 ----
        // ---- Step action ---
        // Cloud Unsync the synced document i.e as network admin click 'Force Unsync' option
        // ---- Expected results ----
        // The content is unsynced.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage documentLibraryPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSite).render();

        Assert.assertTrue(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isCloudSynced());
        Assert.assertFalse(documentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isUnSyncFromCloudLinkPresent());

        ShareUser.logout(hybridDrone);

        // ---- Step 2 ----
        // ---- Step action ---
        // OP Verify the document.
        // ---- Expected results ----
        // Sync icon is still displayed (pl, see the resolution in ALF-15734)
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        DocumentLibraryPage opDocumentLibraryPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        Assert.assertTrue(opDocumentLibraryPage.getFileDirectoryInfo(simpleTaskFile).isCloudSynced());
        Assert.assertTrue(opDocumentLibraryPage.getFileDirectoryInfo(simpleTaskFile).clickOnViewCloudSyncInfo().render().getCloudSyncLocation()
                .contains(cloudSite));
    }

    private void waitForSync(String fileName, String siteName)
    {
        int counter = 1;
        int retryRefreshCount = 4;
        while (counter <= retryRefreshCount)
        {
            if (checkIfContentIsSynced(drone, fileName))
            {
                break;
            }
            else
            {
                drone.refresh();
                counter++;

                if (counter == 2 || counter == 3)
                {
                    DocumentLibraryPage docLib = ShareUser.openSitesDocumentLibrary(drone, siteName);
                    docLib.getFileDirectoryInfo(fileName).selectRequestSync().render();
                }
            }
        }
    }

    private void completeWorkflow(String workflowName, String opUser, String clUser)
    {
        // login as cloud user and complete workflow
        SharePage sharePage = ShareUser.login(hybridDrone, clUser, DEFAULT_PASSWORD);
        MyTasksPage myTasksPage = sharePage.getNav().selectMyTasks().render();
        myTasksPage = ShareUserWorkFlow.completeTaskFromMyTasksPage(hybridDrone, workflowName, TaskStatus.COMPLETED, EditTaskAction.TASK_DONE).render();
        Assert.assertFalse(myTasksPage.isTaskPresent(workflowName));
        myTasksPage = myTasksPage.selectCompletedTasks().render();
        Assert.assertTrue(myTasksPage.isTaskPresent(workflowName));
        ShareUser.logout(hybridDrone);

        // Login as OP user and complete workflow
        ShareUser.login(drone, opUser, DEFAULT_PASSWORD);
        ShareUserWorkFlow.navigateToMyTasksPage(drone).render();
        ShareUser.checkIfTaskIsPresent(drone, workflowName);
        ShareUserWorkFlow.completeWorkFlow(drone, opUser, workflowName).render();
        ShareUser.logout(drone);

    }

    private void createCompletedWorkflow(String prefix) throws Exception
    {
        testName = this.getClass().getSimpleName();

        List<String> userNames = new ArrayList<String>();
        userNames.add(cloudUser);

        String opSiteName = getSiteName(prefix + testName) + "-OP";
        String cloudSiteName = getSiteName(prefix + testName) + "-CL";

        String fileName = getFileName(prefix + testName) + ".txt";
        String folderName = getFolderName(prefix + testName);

        String workFlowName = prefix + testName + "-WF";

        // Cloud user logins and create site.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        ShareUser.createSite(hybridDrone, cloudSiteName, SITE_VISIBILITY_PUBLIC);
        ShareUserSitePage.createFolder(hybridDrone, folderName, folderName);
        ShareUser.logout(hybridDrone);

        // User1 starts Workflow
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        ShareUser.createSite(drone, opSiteName, SITE_VISIBILITY_PUBLIC);
        ShareUserSitePage.createFolder(drone, folderName, folderName);
        ShareUser.uploadFileInFolder(drone, new String[] { fileName, DOCLIB });

        WorkFlowFormDetails formDetails = new WorkFlowFormDetails();
        formDetails.setMessage(workFlowName);

        formDetails.setTaskPriority(Priority.MEDIUM);
        formDetails.setSiteName(cloudSiteName);
        formDetails.setTaskType(TaskType.CLOUD_REVIEW_TASK);
        formDetails.setReviewers(userNames);
        formDetails.setApprovalPercentage(100);
        formDetails.setContentStrategy(KeepContentStrategy.KEEPCONTENT);

        // Start Cloud Task or Review workflow
        CloudTaskOrReviewPage cloudTaskOrReviewPage = ShareUserWorkFlow.startCloudReviewTaskWorkFlow(drone);

        cloudTaskOrReviewPage.selectItem(fileName, opSiteName);
        // Fill the form details and start workflow
        cloudTaskOrReviewPage.startWorkflow(formDetails);

        ShareUser.logout(drone);

        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        MyTasksPage myTasksPage = ShareUserWorkFlow.navigateToMyTasksPage(hybridDrone).render();
        ShareUser.checkIfTaskIsPresent(hybridDrone, workFlowName);
        myTasksPage.navigateToEditTaskPage(workFlowName).render();
        ShareUserWorkFlow.completeTask(hybridDrone, TaskStatus.COMPLETED, EditTaskAction.APPROVE).render();
        ShareUser.logout(hybridDrone);

        // Login as OP user
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        myTasksPage = ShareUserWorkFlow.navigateToMyTasksPage(drone).render();
        ShareUser.checkIfTaskIsPresent(drone, workFlowName);
        myTasksPage.navigateToEditTaskPage(workFlowName).render();
        ShareUserWorkFlow.completeTask(drone, TaskStatus.COMPLETED, EditTaskAction.TASK_DONE).render();
        ShareUser.logout(drone);

    }

}