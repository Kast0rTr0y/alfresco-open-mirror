package org.alfresco.share.enterprise.repository.fileprotocols.cifs.clientwindows.msoffice2013;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.alfresco.application.windows.MicrosoftOffice2013;
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

@Listeners(FailedTestListener.class)
public class CifsMSExcel2013Tests extends AbstractUtils
{
    private static Log logger = LogFactory.getLog(CifsMSExcel2013Tests.class);

    private String testName;
    private String testUser;
    private String siteName;
    private String xlsxFileType = ".xlsx";
    String fileName_6289;
    String fileName_6290;
    String fileName_6291;
    String fileName_6292;
    String fileName_6293;
    String fileName_6294;

    String image_1 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic1.jpg";
    String image_2 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic2.jpg";
    String image_3 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic3.jpg";

    private static DocumentLibraryPage documentLibPage;
    private static final String CIFS_LOCATION = "cifs";
    public String officePath;
    WindowsExplorer explorer = new WindowsExplorer();
    MicrosoftOffice2013 excel = new MicrosoftOffice2013(Application.EXCEL, "2013");
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

        // excel files
        fileName_6289 = "AONE-6289";
        fileName_6290 = "AONE-6290";
        fileName_6291 = "AONE-6291";
        fileName_6292 = "AONE-6292";
        fileName_6293 = "AONE-6293";
        fileName_6294 = "AONE-6294";

        cifsPath = excel.getCIFSPath();

        networkDrive = excel.getMapDriver();
        networkPath = excel.getMapPath();
        try
        {
            ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        }
        catch (Exception e)
        {
            // create user
            String[] testUser1 = new String[] { testUser };
            CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);
        }
        mapConnect = "cmd /c start /WAIT net use" + " " + networkDrive + " " + networkPath + " " + "/user:admin admin";
        Runtime.getRuntime().exec(mapConnect);
        if (CifsUtil.checkDirOrFileExists(10, 200, networkDrive + cifsPath))
        {
            logger.info("----------Mapping succesfull " + testUser);
        }
        else
        {
            logger.error("----------Mapping was not done " + testUser);
        }

        super.tearDown();

    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception
    {
        super.setup();

    }

    @AfterMethod(alwaysRun = true)
    public void teardownMethod() throws Exception
    {

        Runtime.getRuntime().exec("taskkill /F /IM EXCEL.EXE");
        Runtime.getRuntime().exec("taskkill /F /IM CobraWinLDTP.EXE");
        super.tearDown();
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

    @Test(groups = { "DataPrepExcel" })
    public void dataPrep_6289() throws Exception
    {
        String testName = getTestName() + 3;
        String siteName = getSiteName(testName);

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        File file = new File(DATA_FOLDER + CIFS_LOCATION + SLASH + fileName_6289 + xlsxFileType);
        ShareUserSitePage.uploadFile(drone, file).render();

        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6289 + xlsxFileType);

        // Click "Edit Properties" in Actions section;
        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.setDocumentTitle(testName);

        detailsPage = editPropertiesPage.selectSave().render();
        detailsPage.render();
    }

    /** AONE-6289:MS Excel 2013 - uploaded to Share */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6289() throws IOException
    {
        String testName = getTestName() + 3;
        String siteName = getSiteName(testName).toLowerCase();
        String first_modification = testName + "1";
        String second_modification = testName + "2";
        String last_modification = testName + "3";

        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        // ---- Step 1 ----
        // ---- Step Action -----
        // Open .xlsx document for editing.
        // The document is opened in write mode.
        Ldtp ldtp = excel.openFileFromCMD(fullPath, fileName_6289 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().setOnWindow(fileName_6289);
        // ---- Step 2 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        excel.editOffice(ldtp, " " + first_modification + " ");

        // ---- Step 3 ----
        // ---- Step Action -----
        // Save the document (Ctrl+S, for example) and close it
        // Expected Result
        // The document is saved. No errors occur in UI and in the log. No tmp files are left.
        excel.saveOffice(ldtp);
        // ldtp.waitTime(3);
        excel.exitOfficeApplication(ldtp, fileName_6289);
        // ldtp.waitTime(3);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // ---- Step 4 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are displayed correctly.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6289 + xlsxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        String body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(first_modification));

        // ---- Step 6 ----
        // ---- Step Action -----
        // 6. Open the document for editing again.
        // Expected Result
        // 6. The document is opened in write mode.
        ldtp = excel.openFileFromCMD(fullPath, fileName_6289 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6289);
        excel.getAbstractUtil().setOnWindow(fileName_6289);
        // ---- Step 7 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        excel.editOffice(ldtp, " " + second_modification + " ");

        // ---- Step 8 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        excel.saveOffice(ldtp);
        // ldtp.waitTime(2);
        excel.exitOfficeApplication(ldtp, fileName_6289);
        // ldtp.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));
        // ---- Step 9 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(fileName_6289 + xlsxFileType);

        editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 10 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(second_modification));

        // ---- Step 11 ----
        // ---- Step Action -----
        // 6. Open the document for editing again.
        // Expected Result
        // 6. The document is opened in write mode.
        ldtp = excel.openFileFromCMD(fullPath, fileName_6289 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6289);
        excel.getAbstractUtil().setOnWindow(fileName_6289);
        // ---- Step 12 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        excel.editOffice(ldtp, " " + last_modification + " ");

        // ---- Step 13 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        excel.saveOffice(ldtp);
        ldtp.waitTime(2);
        excel.exitOfficeApplication(ldtp);
        ldtp.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));
        // ---- Step 14 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(fileName_6289 + xlsxFileType);

        editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 15 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(last_modification));

    }

    @Test(groups = { "DataPrepExcel" })
    public void dataPrep_6290() throws Exception
    {
        String testName = getTestName() + 3;
        String siteName = getSiteName(testName);

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        File file = new File(DATA_FOLDER + CIFS_LOCATION + SLASH + fileName_6290 + xlsxFileType);
        ShareUserSitePage.uploadFile(drone, file).render();

        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6290 + xlsxFileType);

        // Click "Edit Properties" in Actions section;
        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.setDocumentTitle(testName);

        detailsPage = editPropertiesPage.selectSave().render();
        detailsPage.render();
    }

    /** AONE-6290:MS Excel 2013 - uploaded to Share (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6290() throws IOException, AWTException
    {
        String testName = getTestName() + 3;
        String siteName = getSiteName(testName).toLowerCase();
        String first_modification = testName + "1";
        String second_modification = testName + "2";
        String last_modification = testName + "3";

        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        // ---- Step 1 ----
        // ---- Step Action -----
        // Open .xlsx document for editing.
        // The document is opened in write mode.
        Ldtp ldtp = excel.openFileFromCMD(fullPath, fileName_6290 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6290);
        excel.getAbstractUtil().setOnWindow(fileName_6290);
        // ---- Step 2 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_1);
        excel.editOffice(ldtp, " " + first_modification + " ");

        // ---- Step 3 ----
        // ---- Step Action -----
        // Save the document (Ctrl+S, for example) and close it
        // Expected Result
        // The document is saved. No errors occur in UI and in the log. No tmp files are left.
        excel.saveOffice(ldtp);
        ldtp.waitTime(3);
        excel.exitOfficeApplication(ldtp);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // ---- Step 4 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are displayed correctly.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6290 + xlsxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        String body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(first_modification));

        // ---- Step 6 ----
        // ---- Step Action -----
        // 6. Open the document for editing again.
        // Expected Result
        // 6. The document is opened in write mode.
        ldtp = excel.openFileFromCMD(fullPath, fileName_6290 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6290);
        excel.getAbstractUtil().setOnWindow(fileName_6290);
        // ---- Step 7 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_2);
        excel.editOffice(ldtp, " " + second_modification + " ");

        // ---- Step 8 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        excel.saveOffice(ldtp);
        ldtp.waitTime(2);
        excel.exitOfficeApplication(ldtp);
        ldtp.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // ---- Step 9 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(fileName_6290 + xlsxFileType);

        editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 10 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(second_modification));

        // ---- Step 11 ----
        // ---- Step Action -----
        // 6. Open the document for editing again.
        // Expected Result
        // 6. The document is opened in write mode.
        ldtp = excel.openFileFromCMD(fullPath, fileName_6290 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6290);
        excel.getAbstractUtil().setOnWindow(fileName_6290);
        // ---- Step 12 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_3);
        excel.editOffice(ldtp, " " + last_modification + " ");

        // ---- Step 13 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        excel.saveOffice(ldtp);
        ldtp.waitTime(2);
        excel.exitOfficeApplication(ldtp);
        ldtp.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // ---- Step 14 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(fileName_6290 + xlsxFileType);

        editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 15 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(last_modification));

    }

    @Test(groups = { "DataPrepExcel" })
    public void dataPrep_6291() throws Exception
    {

        testName = getTestName() + 3;
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

    @Test(groups = { "DataPrepExcel" })
    public void dataPrep_6292() throws Exception
    {

        testName = getTestName() + 3;
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

    @Test(groups = { "DataPrepExcel" })
    public void dataPrep_6293() throws Exception
    {
        String testName = getTestName() + 30;
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

    @Test(groups = { "DataPrepExcel" })
    public void dataPrep_6294() throws Exception
    {
        String testName = getTestName() + 3;
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

    /** AONE-6291:MS Excel 2013 - created via context menu */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6291() throws Exception
    {
        String testName = getTestName() + 3;
        String siteName = getSiteName(testName).toLowerCase();
        String addText = "First text";
        String edit1 = "New text1";
        String edit2 = "New text2";
        String edit3 = "New text3";
        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;
        Ldtp l1;

        // --- Step 1 ---
        // --- Step action ---
        // RBC in the window -> New -> New Microsoft Office Word Document
        // --- Expected results --
        // The document .docx is created
        l1 = excel.openOfficeApplication();
        excel.editOffice(l1, addText);
        excel.saveAsOffice(l1, fullPath + fileName_6291);
        l1.waitTime(2);
        excel.getAbstractUtil().waitForWindow(fileName_6291);
        excel.exitOfficeApplication(l1);

        // ---- Step 2 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = excel.openFileFromCMD(fullPath, fileName_6291 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6291);
        excel.getAbstractUtil().setOnWindow(fileName_6291);
        // ---- Step 3 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        excel.editOffice(l1, " " + edit1 + " ");

        // --- Step 4 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);

        excel.exitOfficeApplication(l1);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6291 + xlsxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6291 + xlsxFileType));
        editPropertiesPage.clickCancel();
        String body = detailsPage.getDocumentBody().replaceAll("\\n", "");
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
        String documentTitle = "Title " + fileName_6291;
        String documentDescription = "Description for " + fileName_6291;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.xlsx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.xlsx";
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
        l1 = excel.openFileFromCMD(fullPath, fileName_6291 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6291);
        excel.getAbstractUtil().setOnWindow(fileName_6291);
        // ---- Step 8 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        excel.editOffice(l1, " " + edit2 + " ");

        // --- Step 9 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        l1.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 10 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6291 + xlsxFileType).render();

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
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(edit2));

        // ---- Step 12 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = excel.openFileFromCMD(fullPath, fileName_6291 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6291);
        excel.getAbstractUtil().setOnWindow(fileName_6291);
        // ---- Step 13 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        excel.editOffice(l1, " " + edit3 + " ");

        // --- Step 14 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        l1.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 15 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6291 + xlsxFileType).render();

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
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(edit3));

    }

    /** AONE-6292:MS Excel 2013 - created via context menu (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6292() throws Exception
    {
        String testName = getTestName() + 3;
        String siteName = getSiteName(testName).toLowerCase();
        String addText = "First text";
        String edit1 = "New text1";
        String edit2 = "New text2";
        String edit3 = "New text3";
        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        Ldtp l1;

        // --- Step 1 ---
        // --- Step action ---
        // RBC in the window -> New -> New Microsoft Office Word Document
        // --- Expected results --
        // The document .docx is created

        l1 = excel.openOfficeApplication();
        excel.editOffice(l1, addText);
        excel.saveAsOffice(l1, fullPath + fileName_6292);
        l1.waitTime(3);
        excel.getAbstractUtil().waitForWindow(fileName_6292);
        excel.exitOfficeApplication(l1);

        // ---- Step 2 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = excel.openFileFromCMD(fullPath, fileName_6292 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6292);
        excel.getAbstractUtil().setOnWindow(fileName_6292);
        // ---- Step 3 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_1);
        excel.editOffice(l1, " " + edit1 + " ");

        // --- Step 4 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6292 + xlsxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6292 + xlsxFileType));
        editPropertiesPage.clickCancel();
        String body = detailsPage.getDocumentBody().replaceAll("\\n", "");
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
        String documentTitle = "Title " + fileName_6292;
        String documentDescription = "Description for " + fileName_6292;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.xlsx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.xlsx";
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
        l1 = excel.openFileFromCMD(fullPath, fileName_6292 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6292);
        excel.getAbstractUtil().setOnWindow(fileName_6292);
        // ---- Step 8 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_2);
        excel.editOffice(l1, " " + edit2 + " ");

        // --- Step 9 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        l1.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 10 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6292 + xlsxFileType).render();

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
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(edit2));

        // ---- Step 12 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = excel.openFileFromCMD(fullPath, fileName_6292 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6292);
        excel.getAbstractUtil().setOnWindow(fileName_6292);
        // ---- Step 13 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_3);
        excel.editOffice(l1, " " + edit3 + " ");

        // --- Step 14 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        l1.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 15 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6292 + xlsxFileType).render();

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
        body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(edit3));

    }

    /** AONE-6293:MS Excel 2013 - saved into CIFS */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6293() throws Exception
    {
        String testName = getTestName() + 30;
        String siteName = getSiteName(testName).toLowerCase();
        String edit2 = "new text2";
        String edit3 = "new text3";
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

        l1 = excel.openFileFromCMD(localPath, fileName_6293 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6293);
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6293);
        excel.navigateToSaveAsSharePointBrowse(l1);
        excel.operateOnSaveAsWithFullPath(l1, fullPath, fileName_6293, testUser, DEFAULT_PASSWORD);
        excel.getAbstractUtil().waitForWindow(fileName_6293);

        excel.exitOfficeApplication(l1);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 2 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6293 + xlsxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6293 + xlsxFileType));
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
        String documentTitle = "Title " + fileName_6293;
        String documentDescription = "Description for " + fileName_6293;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.xlsx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.xlsx";
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
        l1 = excel.openFileFromCMD(fullPath, fileName_6293 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6293);
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6293);
        String actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6293), "Microsoft Excel - " + fileName_6293 + " window is not active.");

        // --- Step 5 ---
        // --- Step action ---
        // Add any data.
        // --- Expected results --
        // The data is entered.
        excel.editOffice(l1, " " + edit2 + " ");

        // --- Step 6 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 7 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6293 + xlsxFileType).render();

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
        String body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(edit2));

        // --- Step 9 ---
        // --- Step action ---
        // Open the document for editing again.
        // --- Expected results --
        // The document is opened in write mode.
        l1 = excel.openFileFromCMD(fullPath, fileName_6293 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6293);
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6293);
        actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6293), "Microsoft Excel - " + fileName_6293 + " window is not active.");

        // --- Step 10 ---
        // --- Step action ---
        // Add any data.
        // --- Expected results --
        // The data is entered.
        excel.editOffice(l1, " " + edit3 + " ");

        // --- Step 11 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6293);
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 12 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6293 + xlsxFileType).render();

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
        String body3 = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body3.contains(edit3));
    }

    /** AONE-6294:MS Excel 2013 - saved into CIFS (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6294() throws Exception
    {
        String testName = getTestName() + 3;
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

        l1 = excel.openFileFromCMD(localPath, fileName_6294 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6294);
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6294);
        excel.navigateToSaveAsSharePointBrowse(l1);
        excel.operateOnSaveAsWithFullPath(l1, fullPath, fileName_6294, testUser, DEFAULT_PASSWORD);
        excel.getAbstractUtil().waitForWindow(fileName_6294);

        excel.exitOfficeApplication(l1);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));

        // --- Step 2 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(fileName_6294 + xlsxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(fileName_6294 + xlsxFileType));
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
        String documentTitle = "Title " + fileName_6294;
        String documentDescription = "Description for " + fileName_6294;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.xlsx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.xlsx";
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
        l1 = excel.openFileFromCMD(fullPath, fileName_6294 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6294);
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6294);
        String actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6294), "Microsoft Excel - " + fileName_6294 + " window is active.");

        // --- Step 5 ---
        // --- Step action ---
        // Add any data (5-10 mb).
        // --- Expected results --
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_1);
        excel.editOffice(l1, " " + edit2 + " ");

        // --- Step 6 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        l1.waitTime(5);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));
        // --- Step 7 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6294 + xlsxFileType).render();

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
        String body = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body.contains(edit2));

        // --- Step 9 ---
        // --- Step action ---
        // Open the document for editing again.
        // --- Expected results --
        // The document is opened in write mode.
        l1 = excel.openFileFromCMD(fullPath, fileName_6294 + xlsxFileType, testUser, DEFAULT_PASSWORD, false);
        excel.getAbstractUtil().waitForWindow(fileName_6294);
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6294);
        actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(fileName_6294), "Microsoft Excel - " + fileName_6294 + " window is active.");

        // --- Step 10 ---
        // --- Step action ---
        // Add any data (5-10 mb).
        // --- Expected results --
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_2);
        excel.editOffice(l1, " " + edit3 + " ");

        // --- Step 11 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        l1 = excel.getAbstractUtil().setOnWindow(fileName_6294);
        excel.saveOffice(l1);
        l1.waitTime(2);
        excel.exitOfficeApplication(l1);
        l1.waitTime(3);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, xlsxFileType, 6));
        // --- Step 12 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, fileName_6294 + xlsxFileType).render();

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
        String body3 = detailsPage.getDocumentBody().replaceAll("\\n", "");
        Assert.assertTrue(body3.contains(edit3));
    }

}
