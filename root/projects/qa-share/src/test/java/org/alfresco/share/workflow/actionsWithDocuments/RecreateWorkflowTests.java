package org.alfresco.share.workflow.actionsWithDocuments;

import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.SharePopup;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.task.TaskStatus;
import org.alfresco.po.share.workflow.CloudTaskOrReviewPage;
import org.alfresco.po.share.workflow.KeepContentStrategy;
import org.alfresco.po.share.workflow.Priority;
import org.alfresco.po.share.workflow.TaskType;
import org.alfresco.po.share.workflow.WorkFlowFormDetails;
import org.alfresco.share.util.AbstractWorkflow;
import org.alfresco.share.util.EditTaskAction;
import org.alfresco.share.util.ShareUser;
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
public class RecreateWorkflowTests extends AbstractWorkflow
{
    private String testDomain;
    private static Log logger = LogFactory.getLog(RecreateWorkflowTests.class);
    String keepStrategy = "Keep";
    String removeSyncStrategy = "Remove";
    String deleteContentStrategy = "Delete";

    @Override
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        super.setup();
        testName = this.getClass().getSimpleName();
        testDomain = DOMAIN_HYBRID;
    }

    private void dataPrep_recreate(String testName, String strategy) throws Exception
    {
        logger.info("Start data prep for test: " + testName);

        String user1 = getUserNameForDomain(testName, testDomain);
        String[] userInfo1 = new String[] { user1 };

        String cloudUser = getUserNameForDomain(testName, testDomain);
        String[] cloudUserInfo1 = new String[] { cloudUser };

        String opSiteName = getSiteName(testName) + "-OP";
        String cloudSiteName = getSiteName(testName) + "-CL";
        String fileName = getFileName(testName) + ".txt";

        String workFlowName = testName + "-WF";
        String dueDate = getDueDateString();

        // Create User1 (On-premise)
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, userInfo1);

        // Create User1 (Cloud)
        CreateUserAPI.CreateActivateUser(hybridDrone, ADMIN_USERNAME, cloudUserInfo1);
        CreateUserAPI.upgradeCloudAccount(hybridDrone, ADMIN_USERNAME, testDomain, "1000");

        // Login to User1, set up the cloud sync
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        signInToAlfrescoInTheCloud(drone, cloudUser, DEFAULT_PASSWORD);
        ShareUser.logout(drone);

        ShareUser.login(hybridDrone, user1, DEFAULT_PASSWORD);
        ShareUser.createSite(hybridDrone, cloudSiteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.logout(hybridDrone);

        // Login as Enterprise user, create site and upload a file.
        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        ShareUser.createSite(drone, opSiteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();
        ShareUser.uploadFileInFolder(drone, new String[] { fileName, DOCLIB });

        // Select "Cloud Task or Review" from select a workflow drop down
        CloudTaskOrReviewPage cloudTaskOrReviewPage = ShareUserWorkFlow.startWorkFlowFromDocumentLibraryPage(drone, fileName).render();

        WorkFlowFormDetails formDetails = new WorkFlowFormDetails();
        formDetails.setDueDate(dueDate);
        formDetails.setTaskPriority(Priority.MEDIUM);
        formDetails.setSiteName(cloudSiteName);
        formDetails.setAssignee(cloudUser);
        formDetails.setMessage(workFlowName);
        formDetails.setTaskType(TaskType.SIMPLE_CLOUD_TASK);
        if (strategy.equals(keepStrategy))
        {
            formDetails.setContentStrategy(KeepContentStrategy.KEEPCONTENT);
        }
        else if (strategy.equals(removeSyncStrategy))
        {
            formDetails.setContentStrategy(KeepContentStrategy.KEEPCONTENTREMOVESYNC);
        }
        else if (strategy.equals(deleteContentStrategy))
        {
            formDetails.setContentStrategy(KeepContentStrategy.DELETECONTENT);
        }

        cloudTaskOrReviewPage.startWorkflow(formDetails).render(maxWaitTimeCloudSync);
        waitForSync(fileName, opSiteName);

        completeWorkflow(workFlowName, user1, cloudUser);
    }

    /**
     * Data preparation for test AONE-15715
     */
    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15715() throws Exception
    {
        dataPrep_recreate(getTestName(), keepStrategy);
    }

    /**
     * AONE-15715: Recreate Workflow - The document is already synced
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15715() throws Exception
    {
        String testName = getTestName();
        String user1 = getUserNameForDomain(testName, testDomain);
        String cloudUser = getUserNameForDomain(testName, testDomain);
        String opSiteName = getSiteName(testName) + "-OP";
        String cloudSiteName = getSiteName(testName) + "-CL";
        String fileName = getFileName(testName) + ".txt";
        String workFlowName = testName + "-WF";
        String dueDate = getDueDateString();

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();

        // Select "Cloud Task or Review" from select a workflow drop down
        CloudTaskOrReviewPage cloudTaskOrReviewPage = ShareUserWorkFlow.startWorkFlowFromDocumentLibraryPage(drone, fileName).render();

        // --- Step 1 ---
        // OP Create another workflow with any data specified and with the same document attached
        WorkFlowFormDetails formDetails = new WorkFlowFormDetails();
        formDetails.setDueDate(dueDate);
        formDetails.setTaskPriority(Priority.MEDIUM);
        formDetails.setSiteName(cloudSiteName);
        formDetails.setAssignee(cloudUser);
        formDetails.setContentStrategy(KeepContentStrategy.KEEPCONTENT);
        formDetails.setMessage(workFlowName);
        formDetails.setTaskType(TaskType.SIMPLE_CLOUD_TASK);

        // --- Expected Result ---
        // Workflow is not created. Friendly behavior occurs - 'Workflow could not be started' dialog: 08110558 One of the selected documents is already
        // syncronized with the Cloud. You can only use content that is not yet syncronized with the Cloud to start a new Hybrid Workflow.
        SharePopup errorPopup = cloudTaskOrReviewPage.startWorkflow(formDetails).render();
        Assert.assertTrue(errorPopup
                .getShareMessage()
                .contains(
                        "One of the selected documents is already syncronized with the Cloud. You can only use content that is not yet syncronized with the Cloud to start a new Hybrid Workflow."));

        ShareUser.logout(drone);

        // --- Step 3 ---
        // Cloud Verify the workflow and the document.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSiteName).render();

        // --- Expected result ----
        // The document is still synchronized. The document is not a part of any workflow. No workflow is created
        Assert.assertTrue(docLibPage.getFileDirectoryInfo(fileName).isCloudSynced());
        Assert.assertFalse(docLibPage.getFileDirectoryInfo(fileName).isPartOfWorkflow());
        ShareUser.logout(hybridDrone);
    }

    /**
     * Data preparation for test AONE-15716
     */
    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15716() throws Exception
    {
        dataPrep_recreate(getTestName(), removeSyncStrategy);
    }

    /**
     * AONE-15716:Recreate Workflow - The document exists in Cloud
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15716() throws Exception
    {
        String testName = getTestName();
        String user1 = getUserNameForDomain(testName, testDomain);
        String cloudUser = getUserNameForDomain(testName, testDomain);
        String opSiteName = getSiteName(testName) + "-OP";
        String cloudSiteName = getSiteName(testName) + "-CL";
        String fileName = getFileName(testName) + ".txt";
        String workFlowName = testName + "-WF";
        String dueDate = getDueDateString();

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();

        // Select "Cloud Task or Review" from select a workflow drop down
        CloudTaskOrReviewPage cloudTaskOrReviewPage = ShareUserWorkFlow.startWorkFlowFromDocumentLibraryPage(drone, fileName).render();

        // --- Step 1 ---
        // OP Create another workflow with any data specified and with the same document attached
        WorkFlowFormDetails formDetails = new WorkFlowFormDetails();
        formDetails.setDueDate(dueDate);
        formDetails.setTaskPriority(Priority.MEDIUM);
        formDetails.setSiteName(cloudSiteName);
        formDetails.setAssignee(cloudUser);
        formDetails.setContentStrategy(KeepContentStrategy.KEEPCONTENT);
        formDetails.setMessage(workFlowName);
        formDetails.setTaskType(TaskType.SIMPLE_CLOUD_TASK);

        // --- Expected Result ---
        // Workflow is not created. Friendly behavior occurs - 'Workflow could not be started' dialog: 08110558 One of the selected documents is already
        // syncronized with the Cloud. You can only use content that is not yet syncronized with the Cloud to start a new Hybrid Workflow.
        SharePopup errorPopup = cloudTaskOrReviewPage.startWorkflow(formDetails).render();
        Assert.assertTrue(errorPopup
                .getShareMessage()
                .contains(
                        "One of the selected documents is already syncronized with the Cloud. You can only use content that is not yet syncronized with the Cloud to start a new Hybrid Workflow."));

        ShareUser.logout(drone);

        // --- Step 3 ---
        // Cloud Verify the workflow and the document.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSiteName).render();

        // --- Expected result ----
        // The document is still synchronized. The document is not a part of any workflow. No workflow is created
        Assert.assertFalse(docLibPage.getFileDirectoryInfo(fileName).isCloudSynced());
        Assert.assertFalse(docLibPage.getFileDirectoryInfo(fileName).isPartOfWorkflow());
        ShareUser.logout(hybridDrone);
    }

    /**
     * Data preparation for test AONE-15717
     */
    @Test(groups = "DataPrepHybrid")
    public void dataPrep_15717() throws Exception
    {
        dataPrep_recreate(getTestName(), deleteContentStrategy);
    }

    /**
     * AONE-15717:Recreate Workflow - The document was removed from Cloud
     */
    @Test(groups = "Hybrid", enabled = true)
    public void AONE_15717() throws Exception
    {
        String testName = getTestName();
        String user1 = getUserNameForDomain(testName, testDomain);
        String cloudUser = getUserNameForDomain(testName, testDomain);
        String opSiteName = getSiteName(testName) + "-OP";
        String cloudSiteName = getSiteName(testName) + "-CL";
        String fileName = getFileName(testName) + ".txt";
        String workFlowName = testName + "-WF";
        String dueDate = getDueDateString();

        ShareUser.login(drone, user1, DEFAULT_PASSWORD);
        DocumentLibraryPage docLibPage = ShareUser.openSitesDocumentLibrary(drone, opSiteName).render();

        // Select "Cloud Task or Review" from select a workflow drop down
        CloudTaskOrReviewPage cloudTaskOrReviewPage = ShareUserWorkFlow.startWorkFlowFromDocumentLibraryPage(drone, fileName).render();

        // --- Step 1 ---
        // OP Create another workflow with any data specified, with the same destination chosen and with the same document attached.
        WorkFlowFormDetails formDetails = new WorkFlowFormDetails();
        formDetails.setDueDate(dueDate);
        formDetails.setTaskPriority(Priority.MEDIUM);
        formDetails.setSiteName(cloudSiteName);
        formDetails.setAssignee(cloudUser);
        formDetails.setContentStrategy(KeepContentStrategy.KEEPCONTENT);
        formDetails.setMessage(workFlowName);
        formDetails.setTaskType(TaskType.SIMPLE_CLOUD_TASK);

        cloudTaskOrReviewPage.startWorkflow(formDetails).render(maxWaitTimeCloudSync);
        waitForSync(fileName, opSiteName);

        Assert.assertTrue(docLibPage.getFileDirectoryInfo(fileName).isCloudSynced());

        ShareUser.logout(drone);

        // --- Step 3 ---
        // Cloud Verify the workflow and the document.
        ShareUser.login(hybridDrone, cloudUser, DEFAULT_PASSWORD);
        DocumentLibraryPage docLibPageCloud = ShareUser.openSitesDocumentLibrary(hybridDrone, cloudSiteName).render();

        // --- Expected result ----
        // The document is created in Cloud. The document is a part of a newly created workflow. The workflow is created successfully.
        Assert.assertTrue(docLibPageCloud.getFileDirectoryInfo(fileName).isCloudSynced());
        Assert.assertTrue(docLibPageCloud.getFileDirectoryInfo(fileName).isPartOfWorkflow());
        ShareUser.logout(hybridDrone);

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
        sharePage.getNav().selectMyTasks().render();
        ShareUserWorkFlow.completeTaskFromMyTasksPage(hybridDrone, workflowName, TaskStatus.COMPLETED, EditTaskAction.TASK_DONE).render();
        ShareUser.logout(hybridDrone);

        // Login as OP user and complete workflow
        ShareUser.login(drone, opUser, DEFAULT_PASSWORD);
        ShareUserWorkFlow.navigateToMyTasksPage(drone).render();
        ShareUser.checkIfTaskIsPresent(drone, workflowName);
        ShareUserWorkFlow.completeWorkFlow(drone, opUser, workflowName).render();
        ShareUser.logout(drone);

    }
}