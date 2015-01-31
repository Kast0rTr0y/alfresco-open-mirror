package org.alfresco.share.enterprise.repository.fileprotocols.cifs.clientwindows.msoffice2010;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.alfresco.application.windows.MicorsoftOffice2010;
import org.alfresco.explorer.WindowsExplorer;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.EditDocumentPropertiesPage;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.CifsUtil;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserSitePage;
import org.alfresco.share.util.api.CreateUserAPI;
import org.alfresco.test.FailedTestListener;
import org.alfresco.utilities.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.cobra.ldtp.Ldtp;
import com.cobra.ldtp.LdtpExecutionError;

@Listeners(FailedTestListener.class)
public class CifsMSPPoint2010Tests extends AbstractUtils
{
    private static Log logger = LogFactory.getLog(CifsMSPPoint2010Tests.class);

    private String testName;
    private String testUser;
    private String siteName;
    private String pptxFileType = ".pptx";
    String docFileName_6265;
    String docFileName_6266;
    String docxFileName_6269;
    String fileName_6271;
    String fileName_6272;
    String fileName_6277;
    String fileName_6278;
    String fileName_6279;
    String fileName_6280;
    String fileName_6281;
    String fileName_6282;

    String image_1 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic1.jpg";
    String image_2 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic2.jpg";
    String image_3 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic3.jpg";

    private static DocumentLibraryPage documentLibPage;
    private static final String CIFS_LOCATION = "cifs";
    public String officePath;
    WindowsExplorer explorer = new WindowsExplorer();
    MicorsoftOffice2010 power = new MicorsoftOffice2010(Application.POWERPOINT, "2010");
    String mapConnect;
    String networkDrive;
    String networkPath;
    String cifsPath;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        super.setup();

        testName = this.getClass().getSimpleName();
        testUser = getUserNameFreeDomain(testName);

        cifsPath = power.getCIFSPath();

        networkDrive = power.getMapDriver();
        networkPath = power.getMapPath();

        // create user
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        mapConnect = "cmd /c start /WAIT net use" + " " + networkDrive + " " + networkPath + " " + "/user:admin admin";

        Runtime.getRuntime().exec(mapConnect);
        if (CifsUtil.checkDirOrFileExists(15, 200, networkDrive + cifsPath))
        {
            logger.info("----------Mapping succesfull " + testUser);
        }
        else
        {
            logger.error("----------Mapping was not done " + testUser);
        }

        super.tearDown();
        super.tearDown();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception
    {
        super.setup();

        // power point files
        fileName_6277 = "AONE-62771";
        fileName_6278 = "AONE-6278";
        fileName_6279 = "AONE-6279";
        fileName_6280 = "AONE-6280";
        fileName_6281 = "AONE-6281";
        fileName_6282 = "AONE-6282";

    }

    @AfterMethod(alwaysRun = true)
    public void teardownMethod() throws Exception
    {
        super.tearDown();

        Runtime.getRuntime().exec("taskkill /F /IM POWERPNT.EXE");
        Runtime.getRuntime().exec("taskkill /F /IM CobraWinLDTP.EXE");

    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws IOException
    {

        Runtime.getRuntime().exec("cmd /c start /WAIT net use * /d /y");

        if (CifsUtil.checkDirOrFileNotExists(7, 200, networkDrive + cifsPath))
        {
            logger.info("--------Unmapping succesfull " + testUser);
        }
        else
        {
            logger.error("--------Unmapping was not done correctly " + testUser);
        }

    }

    @Test(groups = { "DataPrepPowerPoint" })
    public void dataPrep_6277() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName);

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        File file = new File(DATA_FOLDER + CIFS_LOCATION + SLASH + fileName_6277 + pptxFileType);
        ShareUserSitePage.uploadFile(drone, file).render();

        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6277 + pptxFileType);

        // Click "Edit Properties" in Actions section;
        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.setDocumentTitle(testName);

        detailsPage = editPropertiesPage.selectSave().render();
        detailsPage.render();
    }

    /** AONE-6277:MS PowerPoint 2010 - uploaded to Share */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6277() throws IOException
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String first_modification = testName + "1";
        String second_modification = testName + "2";
        String last_modification = testName + "3";

        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        try
        {
            // ---- Step 1 ----
            // ---- Step Action -----
            // Open .pptx document for editing.
            // The document is opened in write mode.
            Ldtp ldtp = power.openFileFromCMD(fullPath, fileName_6277 + pptxFileType, testUser, DEFAULT_PASSWORD, true);
            // ---- Step 2 ----
            // ---- Step Action -----
            // Add any data.
            // Expected Result
            // The data is entered.
            ldtp = power.getAbstractUtil().setOnWindow(fileName_6277);
            ldtp.click("paneSlide");
            power.editOffice(ldtp, " " + first_modification + " ");

            // ---- Step 3 ----
            // ---- Step Action -----
            // Save the document (Ctrl+S, for example) and close it
            // Expected Result
            // The document is saved. No errors occur in UI and in the log. No tmp files are left.
            power.saveOffice(ldtp);
            ldtp.waitTime(3);
            power.exitOfficeApplication(ldtp, fileName_6277);
            Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

            // ---- Step 4 ----
            // ---- Step Action -----
            // Verify the document's metadata and version history in the Share.
            // Expected Result
            // The document's metadata and version history are not broken. They are displayed correctly.
            ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
            documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
            DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6277 + pptxFileType);

            EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
            Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6277 + pptxFileType));
            editPropertiesPage.clickCancel();

            // ---- Step 5 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly.
            String body = detailsPage.getDocumentBody();
            Assert.assertTrue(body.contains(first_modification));

            // ---- Step 6 ----
            // ---- Step Action -----
            // 6. Open the document for editing again.
            // Expected Result
            // 6. The document is opened in write mode.
            ldtp = power.openFileFromCMD(fullPath, fileName_6277 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

            // ---- Step 7 ----
            // ---- Step Action -----
            // Add any data.
            // Expected Result
            // The data is entered.
            ldtp = power.getAbstractUtil().setOnWindow(fileName_6277);
            power.editOffice(ldtp, " " + second_modification + " ");

            // ---- Step 8 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly
            power.saveOffice(ldtp);
            ldtp.waitTime(2);
            power.exitOfficeApplication(ldtp, fileName_6277);
            Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

            // ---- Step 9 ----
            // ---- Step Action -----
            // Verify the document's metadata and version history in the Share.
            // Expected Result
            // The document's metadata and version history are not broken. They are displayed correctly.
            documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
            detailsPage = documentLibPage.selectFile(fileName_6277 + pptxFileType);

            editPropertiesPage = detailsPage.selectEditProperties().render();
            Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6277 + pptxFileType));
            editPropertiesPage.clickCancel();

            // ---- Step 10 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly.
            body = detailsPage.getDocumentBody();
            Assert.assertTrue(body.contains(second_modification));

            // ---- Step 11 ----
            // ---- Step Action -----
            // 6. Open the document for editing again.
            // Expected Result
            // 6. The document is opened in write mode.
            ldtp = power.openFileFromCMD(fullPath, fileName_6277 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

            // ---- Step 12 ----
            // ---- Step Action -----
            // Add any data.
            // Expected Result
            // The data is entered.
            ldtp = power.getAbstractUtil().setOnWindow(fileName_6277);
            ldtp.click("paneSlide");
            power.editOffice(ldtp, " " + last_modification + " ");

            // ---- Step 13 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly
            power.saveOffice(ldtp);
            ldtp.waitTime(2);
            power.exitOfficeApplication(ldtp, fileName_6277);
            Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

            // ---- Step 14 ----
            // ---- Step Action -----
            // Verify the document's metadata and version history in the Share.
            // Expected Result
            // The document's metadata and version history are not broken. They are displayed correctly.
            documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
            detailsPage = documentLibPage.selectFile(fileName_6277 + pptxFileType);

            editPropertiesPage = detailsPage.selectEditProperties().render();
            Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6277 + pptxFileType));
            editPropertiesPage.clickCancel();

            // ---- Step 15 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly.
            body = detailsPage.getDocumentBody();
            Assert.assertTrue(body.contains(last_modification));

        }
        catch (Exception e)
        {
            throw new LdtpExecutionError("Not working");
        }

    }

    @Test(groups = { "DataPrepPowerPoint" })
    public void dataPrep_6278() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName);

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        File file = new File(DATA_FOLDER + CIFS_LOCATION + SLASH + fileName_6278 + pptxFileType);
        ShareUserSitePage.uploadFile(drone, file).render();

        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6278 + pptxFileType);

        // Click "Edit Properties" in Actions section;
        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.setDocumentTitle(testName);

        detailsPage = editPropertiesPage.selectSave().render();
        detailsPage.render();
    }

    /** AONE-6278:MS PowerPoint 2010 - uploaded to Share (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6278() throws IOException
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String first_modification = testName + "1";
        String second_modification = testName + "2";
        String last_modification = testName + "3";

        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        try
        {
            // ---- Step 1 ----
            // ---- Step Action -----
            // Open .pptx document for editing.
            // The document is opened in write mode.
            Ldtp ldtp = power.openFileFromCMD(fullPath, fileName_6278 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

            // ---- Step 2 ----
            // ---- Step Action -----
            // Add any data.
            // Expected Result
            // The data is entered.
            ldtp = power.getAbstractUtil().setOnWindow(fileName_6278);
            ldtp.click("paneSlide");
            CifsUtil.uploadImageInOffice(image_1);
            ldtp.waitTime(2);
            ldtp.click("paneSlide");
            power.editOffice(ldtp, " " + first_modification + " ");

            // ---- Step 3 ----
            // ---- Step Action -----
            // Save the document (Ctrl+S, for example) and close it
            // Expected Result
            // The document is saved. No errors occur in UI and in the log. No
            // tmp files are left.
            power.saveOffice(ldtp);
            ldtp.waitTime(3);
            power.exitOfficeApplication(ldtp, fileName_6278);
            Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

            // ---- Step 4 ----
            // ---- Step Action -----
            // Verify the document's metadata and version history in the Share.
            // Expected Result
            // The document's metadata and version history are not broken. They
            // are displayed correctly.
            ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
            documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
            DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6278 + pptxFileType);

            EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
            editPropertiesPage.getDocumentTitle();
            Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
            editPropertiesPage.clickCancel();

            // ---- Step 5 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly.
            String body = detailsPage.getDocumentBody();
            Assert.assertTrue(body.contains(first_modification));

            // ---- Step 6 ----
            // ---- Step Action -----
            // 6. Open the document for editing again.
            // Expected Result
            // 6. The document is opened in write mode.
            ldtp = power.openFileFromCMD(fullPath, fileName_6278 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

            // ---- Step 7 ----
            // ---- Step Action -----
            // Add any data.
            // Expected Result
            // The data is entered.
            ldtp = power.getAbstractUtil().setOnWindow(fileName_6278);
            ldtp.click("btnNextSlide");
            ldtp.waitTime(1);
            ldtp.click("paneSlide");
            CifsUtil.uploadImageInOffice(image_2);
            ldtp.waitTime(2);
            ldtp.click("paneSlide");
            power.editOffice(ldtp, " " + second_modification + " ");

            // ---- Step 8 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly
            power.saveOffice(ldtp);
            ldtp.waitTime(2);
            power.exitOfficeApplication(ldtp, fileName_6278);
            Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

            // ---- Step 9 ----
            // ---- Step Action -----
            // Verify the document's metadata and version history in the Share.
            // Expected Result
            // The document's metadata and version history are not broken. They
            // are displayed correctly.
            documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
            detailsPage = documentLibPage.selectFile(fileName_6278 + pptxFileType);

            editPropertiesPage = detailsPage.selectEditProperties().render();
            editPropertiesPage.getDocumentTitle();
            Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
            editPropertiesPage.clickCancel();

            // ---- Step 10 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly.
            body = detailsPage.getDocumentBody();
            Assert.assertTrue(body.contains(second_modification));

            // ---- Step 11 ----
            // ---- Step Action -----
            // 6. Open the document for editing again.
            // Expected Result
            // 6. The document is opened in write mode.
            ldtp = power.openFileFromCMD(fullPath, fileName_6278 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

            // ---- Step 12 ----
            // ---- Step Action -----
            // Add any data.
            // Expected Result
            // The data is entered.
            ldtp = power.getAbstractUtil().setOnWindow(fileName_6278);
            ldtp.click("btnNextSlide");
            ldtp.waitTime(1);
            ldtp.click("btnNextSlide");
            ldtp.waitTime(1);
            ldtp.click("paneSlide");
            CifsUtil.uploadImageInOffice(image_3);
            ldtp.waitTime(2);
            ldtp.click("paneSlide");
            power.editOffice(ldtp, " " + last_modification + " ");

            // ---- Step 13 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly
            power.saveOffice(ldtp);
            ldtp.waitTime(2);
            power.exitOfficeApplication(ldtp, fileName_6278);
            Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

            // ---- Step 14 ----
            // ---- Step Action -----
            // Verify the document's metadata and version history in the Share.
            // Expected Result
            // The document's metadata and version history are not broken. They
            // are displayed correctly.
            documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
            detailsPage = documentLibPage.selectFile(fileName_6278 + pptxFileType);

            editPropertiesPage = detailsPage.selectEditProperties().render();
            Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6278 + pptxFileType));
            editPropertiesPage.clickCancel();

            // ---- Step 15 ----
            // ---- Step Action -----
            // Verify the document's content.
            // Expected Result
            // All changes are present and displayed correctly.
            body = detailsPage.getDocumentBody();
            Assert.assertTrue(body.contains(last_modification));

        }
        catch (Exception e)
        {
            throw new LdtpExecutionError("Not working");
        }

    }

    @Test(groups = { "DataPrepPowerPoint" })
    public void dataPrep_6279() throws Exception
    {

        testName = getTestName();
        siteName = getSiteName(testName);

        ShareUser.login(drone, testUser);

        // --- Step 2 ---
        // --- Step action ---
        // Any site is created;
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC).render();

        // --- Step 3 ---
        // --- Step action ---
        // CIFS is mapped as a network drive as a site manager user; -- already
        // performed manually
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

    }

    @Test(groups = { "DataPrepPowerPoint" })
    public void dataPrep_6280() throws Exception
    {

        testName = getTestName();
        siteName = getSiteName(testName);

        ShareUser.login(drone, testUser);

        // --- Step 2 ---
        // --- Step action ---
        // Any site is created;
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC).render();

        // --- Step 3 ---
        // --- Step action ---
        // CIFS is mapped as a network drive as a site manager user; -- already
        // performed manually
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

    }

    @Test(groups = { "DataPrepPowerPoint" })
    public void dataPrep_6281() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName);

        ShareUser.login(drone, testUser);

        // --- Step 2 ---
        // --- Step action ---
        // Any site is created;
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC).render();

        // --- Step 3 ---
        // --- Step action ---
        // CIFS is mapped as a network drive as a site manager user; -- already
        // performed manually
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

    }

    @Test(groups = { "DataPrepPowerPoint" })
    public void dataPrep_6282() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName);

        ShareUser.login(drone, testUser);

        // --- Step 2 ---
        // --- Step action ---
        // Any site is created;
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC).render();

        // --- Step 3 ---
        // --- Step action ---
        // CIFS is mapped as a network drive as a site manager user; -- already
        // performed manually
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

    }

    /** AONE-6279:MS PowerPoint 2010 - created via context menu */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6279() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String addText = "First text";
        String edit1 = "New text1";
        String edit2 = "New text2";
        String edit3 = "New text3";
        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        Ldtp security = new Ldtp("Windows Security");
        Ldtp l1;

        // --- Step 1 ---
        // --- Step action ---
        // RBC in the window -> New -> New Microsoft Office Word Document
        // --- Expected results --
        // The document .docx is created

        l1 = power.openOfficeApplication();
        power.editOffice(l1, addText);
        power.saveAsOffice(l1, fullPath + fileName_6279);
        power.operateOnSecurityAndWait(security, testUser, DEFAULT_PASSWORD);
        l1.waitTime(3);
        power.getAbstractUtil().waitForWindow(fileName_6279);
        power.exitOfficeApplication(l1, fileName_6279);
        // l1.waitTime(3);

        // ---- Step 2 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6279 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

        // ---- Step 3 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        l1 = power.getAbstractUtil().setOnWindow(fileName_6279);
        l1.click("btnNewSlide");
        power.editOffice(l1, " " + edit1 + " ");

        // --- Step 4 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6279);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6279 + pptxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6279 + pptxFileType));
        editPropertiesPage.clickCancel();
        String body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit1));

        // --- Step 6 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        EditDocumentPropertiesPage editDocPropPage = detailsPage.selectEditProperties();
        String documentTitle = "Title " + fileName_6279;
        String documentDescription = "Description for " + fileName_6279;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.pptx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.pptx";
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion2, "", true).render();
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion3, "", true).render();

        Map<String, Object> propertiesValues = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // ---- Step 7 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6279 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

        // ---- Step 8 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        l1 = power.getAbstractUtil().setOnWindow(fileName_6279);
        power.editOffice(l1, " " + edit2 + " ");

        // --- Step 9 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6279);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

        // --- Step 10 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6279 + pptxFileType).render();

        Map<String, Object> propertiesValues2 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues2.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues2.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // ---- Step 11 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit2));

        // ---- Step 12 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6279 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

        // ---- Step 13 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        l1 = power.getAbstractUtil().setOnWindow(fileName_6279);
        l1.click("paneSlide");
        power.editOffice(l1, " " + edit3 + " ");

        // --- Step 14 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6279);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

        // --- Step 15 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6279 + pptxFileType).render();

        Map<String, Object> propertiesValues3 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues3.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues3.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // ---- Step 16 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit3));

    }

    /** AONE-6280:MS PowerPoint 2010 - created via context menu (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6280() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String addText = "First text";
        String edit1 = "New text1";
        String edit2 = "New text2";
        String edit3 = "New text3";
        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        Ldtp security = new Ldtp("Windows Security");
        Ldtp l1;

        // --- Step 1 ---
        // --- Step action ---
        // RBC in the window -> New -> New Microsoft Office Word Document
        // --- Expected results --
        // The document .docx is created

        l1 = power.openOfficeApplication();
        power.editOffice(l1, addText);
        power.saveAsOffice(l1, fullPath + fileName_6280);
        power.operateOnSecurityAndWait(security, testUser, DEFAULT_PASSWORD);
        l1.waitTime(3);
        power.getAbstractUtil().waitForWindow(fileName_6280);
        power.exitOfficeApplication(l1, fileName_6280);
        // l1.waitTime(3);

        // ---- Step 2 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6280 + pptxFileType, testUser, DEFAULT_PASSWORD, true);

        // ---- Step 3 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        l1 = power.getAbstractUtil().setOnWindow(fileName_6280);
        l1.click("btnNewSlide");
        power.editOffice(l1, " " + edit1 + " ");
        l1.click("btnNewSlide");
        CifsUtil.uploadImageInOffice(image_1);

        // --- Step 4 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6280);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6280 + pptxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6280 + pptxFileType));
        editPropertiesPage.clickCancel();
        String body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit1));

        // --- Step 6 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        EditDocumentPropertiesPage editDocPropPage = detailsPage.selectEditProperties();
        String documentTitle = "Title " + fileName_6280;
        String documentDescription = "Description for " + fileName_6280;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.pptx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.pptx";
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion2, "", true).render();
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion3, "", true).render();

        Map<String, Object> propertiesValues = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // ---- Step 7 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6280 + pptxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6280);

        // ---- Step 8 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        l1.click("paneSlide");
        power.editOffice(l1, " " + edit2 + " ");
        l1.click("btnNewSlide");
        CifsUtil.uploadImageInOffice(image_2);

        // --- Step 9 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6280);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

        // --- Step 10 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6280 + pptxFileType).render();

        Map<String, Object> propertiesValues2 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues2.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues2.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // ---- Step 11 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit2));

        // ---- Step 12 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6280 + pptxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6280);

        // ---- Step 13 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        l1.click("btnNextSlide");
        l1.waitTime(2);
        l1.click("paneSlide");
        power.editOffice(l1, " " + edit3 + " ");
        l1.click("btnNewSlide");
        CifsUtil.uploadImageInOffice(image_3);

        // --- Step 14 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6280);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

        // --- Step 15 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6280 + pptxFileType).render();

        Map<String, Object> propertiesValues3 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues3.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues3.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // ---- Step 16 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit3));

    }

    /** AONE-6281:MS PowerPoint 2010 - saved into CIFS */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6281() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String edit2 = "New text2";
        String edit3 = "New text3";
        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;
        String localPath = DATA_FOLDER + CIFS_LOCATION + SLASH;
        Ldtp l1;

        // --- Step 1 ---
        // --- Step action ---
        // Save a document into the CIFS drive (into the Document Library space)
        // and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.

        l1 = power.openFileFromCMD(localPath, fileName_6281 + pptxFileType, testUser, DEFAULT_PASSWORD, false);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6281);
        power.goToFile(l1);
        l1.waitTime(1);
        power.getAbstractUtil().clickOnObject(l1, "SaveAs");

        power.operateOnSaveAsWithFullPath(l1, fullPath, fileName_6281, testUser, DEFAULT_PASSWORD);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6281);
        l1.waitTime(4);
        power.exitOfficeApplication(l1, fileName_6281);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));
        // --- Step 2 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6281 + pptxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6281 + pptxFileType));
        editPropertiesPage.clickCancel();

        // --- Step 3 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        EditDocumentPropertiesPage editDocPropPage = detailsPage.selectEditProperties();
        String documentTitle = "Title " + fileName_6281;
        String documentDescription = "Description for " + fileName_6281;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.pptx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.pptx";
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion2, "", true).render();
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion3, "", true).render();

        Map<String, Object> propertiesValues = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // --- Step 4 ---
        // --- Step action ---
        // Open the document for editing again.
        // --- Expected results --
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6281 + pptxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6281);
        String actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6281), "Microsoft Excel - " + fileName_6281 + " window is active.");

        // --- Step 5 ---
        // --- Step action ---
        // Add any data.
        // --- Expected results --
        // The data is entered.
        power.editOffice(l1, " " + edit2 + " ");

        // --- Step 6 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6281);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));
        // --- Step 7 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6281 + pptxFileType).render();

        Map<String, Object> propertiesValues2 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues2.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues2.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // --- Step 8 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        String body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit2));

        // --- Step 9 ---
        // --- Step action ---
        // Open the document for editing again.
        // --- Expected results --
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6281 + pptxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6281);
        actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6281), "Microsoft Excel - " + fileName_6281 + " window is active.");

        // --- Step 10 ---
        // --- Step action ---
        // Add any data.
        // --- Expected results --
        // The data is entered.
        l1.click("paneSlide");
        power.editOffice(l1, " " + edit3 + " ");

        // --- Step 11 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        l1 = power.getAbstractUtil().setOnWindow(fileName_6281);
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6281);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));
        // --- Step 12 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6281 + pptxFileType).render();

        Map<String, Object> propertiesValues3 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues3.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues3.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // --- Step 13 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        String body3 = detailsPage.getDocumentBody();
        Assert.assertTrue(body3.contains(edit3));
    }

    /** AONE-6282:MS PowerPoint 2010 - saved into CIFS (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6282() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String edit2 = "New text2";
        String edit3 = "New text3";
        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;
        String localPath = DATA_FOLDER + CIFS_LOCATION + SLASH;
        Ldtp l1;

        // --- Step 1 ---
        // --- Step action ---
        // Save a document into the CIFS drive (into the Document Library space)
        // and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.

        l1 = power.openFileFromCMD(localPath, fileName_6282 + pptxFileType, testUser, DEFAULT_PASSWORD, false);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6282);
        power.goToFile(l1);
        l1.waitTime(1);
        power.getAbstractUtil().clickOnObject(l1, "SaveAs");

        power.operateOnSaveAsWithFullPath(l1, fullPath, fileName_6282, testUser, DEFAULT_PASSWORD);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6282);
        l1.waitTime(4);
        power.exitOfficeApplication(l1, fileName_6282);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));

        // --- Step 2 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6282 + pptxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6282 + pptxFileType));
        editPropertiesPage.clickCancel();

        // --- Step 3 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        EditDocumentPropertiesPage editDocPropPage = detailsPage.selectEditProperties();
        String documentTitle = "Title " + fileName_6282;
        String documentDescription = "Description for " + fileName_6282;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.pptx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.pptx";
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion2, "", true).render();
        detailsPage = ShareUser.uploadNewVersionOfDocument(drone, newFileVersion3, "", true).render();

        Map<String, Object> propertiesValues = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // --- Step 4 ---
        // --- Step action ---
        // Open the document for editing again.
        // --- Expected results --
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6282 + pptxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6282);
        String actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6282), "Microsoft Excel - " + fileName_6282 + " window is active.");

        // --- Step 5 ---
        // --- Step action ---
        // Add any data (5-10 mb).
        // --- Expected results --
        // The data is entered.
        l1.click("paneSlide");
        CifsUtil.uploadImageInOffice(image_1);
        l1.click("paneSlide");
        power.editOffice(l1, " " + edit2 + " ");

        // --- Step 6 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6282);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));
        // --- Step 7 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6282 + pptxFileType).render();

        Map<String, Object> propertiesValues2 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues2.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues2.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // --- Step 8 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        String body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(edit2));

        // --- Step 9 ---
        // --- Step action ---
        // Open the document for editing again.
        // --- Expected results --
        // The document is opened in write mode.
        l1 = power.openFileFromCMD(fullPath, fileName_6282 + pptxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = power.getAbstractUtil().setOnWindow(fileName_6282);
        actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6282), "Microsoft Excel - " + fileName_6282 + " window is active.");

        // --- Step 10 ---
        // --- Step action ---
        // Add any data (5-10 mb).
        // --- Expected results --
        // The data is entered.
        l1.click("btnNextSlide");
        l1.waitTime(2);
        l1.click("paneSlide");
        CifsUtil.uploadImageInOffice(image_2);
        l1.click("paneSlide");
        power.editOffice(l1, " " + edit3 + " ");

        // --- Step 11 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        power.saveOffice(l1);
        l1.waitTime(2);
        power.exitOfficeApplication(l1, fileName_6282);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, pptxFileType, 6));
        // --- Step 12 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6282 + pptxFileType).render();

        Map<String, Object> propertiesValues3 = detailsPage.getProperties();
        Assert.assertEquals(propertiesValues3.get("Title").toString(), documentTitle, "Document title is not " + documentTitle);
        Assert.assertEquals(propertiesValues3.get("Description").toString(), documentDescription, "Document description is not " + documentDescription);
        Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent());
        Assert.assertTrue(detailsPage.isVersionPresentInVersionHistoryPanel("1.0") && detailsPage.isVersionPresentInVersionHistoryPanel("2.0")
                && detailsPage.isVersionPresentInVersionHistoryPanel("3.0"));

        // --- Step 13 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        String body3 = detailsPage.getDocumentBody();
        Assert.assertTrue(body3.contains(edit3));
    }

}
