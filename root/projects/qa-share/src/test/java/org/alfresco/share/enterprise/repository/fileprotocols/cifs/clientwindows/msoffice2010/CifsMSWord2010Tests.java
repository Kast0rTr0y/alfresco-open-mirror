package org.alfresco.share.enterprise.repository.fileprotocols.cifs.clientwindows.msoffice2010;

import java.awt.AWTException;
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

@Listeners(FailedTestListener.class)
public class CifsMSWord2010Tests extends AbstractUtils
{
    private static Log logger = LogFactory.getLog(CifsMSWord2010Tests.class);

    private String testName;
    private String testUser;
    private String siteName;
    private String docxFileType = ".docx";
    String docFileName_6265;
    String docFileName_6266;
    String docxFileName_6267;
    String docxFileName_6268;
    String docxFileName_6269;
    String docxFileName_6270;

    String image_1 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic1.jpg";
    String image_2 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic2.jpg";
    String image_3 = DATA_FOLDER + CIFS_LOCATION + SLASH + "CifsPic3.jpg";

    private static DocumentLibraryPage documentLibPage;
    private static final String CIFS_LOCATION = "cifs";
    public String officePath;
    WindowsExplorer explorer = new WindowsExplorer();
    MicorsoftOffice2010 word = new MicorsoftOffice2010(Application.WORD, "2010");
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
        cifsPath = word.getCIFSPath();
        networkDrive = word.getMapDriver();
        networkPath = word.getMapPath();

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

    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception
    {
        super.setup();
        // word files
        docFileName_6265 = "AONE-6265";
        docFileName_6266 = "AONE-6266";
        docxFileName_6267 = "AONE-6267";
        docxFileName_6268 = "AONE-6268";
        docxFileName_6269 = "AONE-6269";
        docxFileName_6270 = "AONE-6270";

    }

    @AfterMethod(alwaysRun = true)
    public void teardownMethod() throws Exception
    {
        super.tearDown();
        Runtime.getRuntime().exec("taskkill /F /IM WINWORD.EXE");
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

    @Test(groups = { "DataPrepWord" })
    public void dataPrep_6267() throws Exception
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

    @Test(groups = { "DataPrepWord" })
    public void dataPrep_6268() throws Exception
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

    @Test(groups = { "DataPrepWord" })
    public void dataPrep_6269() throws Exception
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

    @Test(groups = { "DataPrepWord" })
    public void dataPrep_6270() throws Exception
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

    @Test(groups = { "DataPrepWord" })
    public void dataPrep_6265() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName);

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        File file = new File(DATA_FOLDER + CIFS_LOCATION + SLASH + docFileName_6265 + docxFileType);
        ShareUserSitePage.uploadFile(drone, file).render();

        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_6265 + docxFileType);

        // Click "Edit Properties" in Actions section;
        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.setDocumentTitle(testName);

        detailsPage = editPropertiesPage.selectSave().render();
        detailsPage.render();
    }

    /** AONE-6265:MS Word 2010 - uploaded to Share */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6265() throws IOException
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String first_modification = testName + "1";
        String second_modification = testName + "2";
        String last_modification = testName + "3";

        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        // ---- Step 1 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // The document is opened in write mode.
        Ldtp ldtp = word.openFileFromCMD(fullPath, docFileName_6265 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docFileName_6265);
        // ---- Step 2 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        word.editOffice(ldtp, " " + first_modification + " ");

        // ---- Step 3 ----
        // ---- Step Action -----
        // Save the document (Ctrl+S, for example) and close it
        // Expected Result
        // The document is saved. No errors occur in UI and in the log. No
        // tmp files are left.
        word.saveOffice(ldtp);
        ldtp.waitTime(2);
        word.exitOfficeApplication(ldtp, docFileName_6265);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));

        // ---- Step 4 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They
        // are displayed correctly.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_6265 + docxFileType);

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
        ldtp = word.openFileFromCMD(fullPath, docFileName_6265 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docFileName_6265);
        // ---- Step 7 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        word.editOffice(ldtp, " " + second_modification + " ");

        // ---- Step 8 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        word.saveOffice(ldtp);
        ldtp.waitTime(2);
        word.exitOfficeApplication(ldtp, docFileName_6265);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));

        // ---- Step 9 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They
        // are displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(docFileName_6265 + docxFileType);

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
        ldtp = word.openFileFromCMD(fullPath, docFileName_6265 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docFileName_6265);
        // ---- Step 12 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        word.editOffice(ldtp, " " + last_modification + " ");

        // ---- Step 13 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        word.saveOffice(ldtp);
        ldtp.waitTime(2);
        word.exitOfficeApplication(ldtp, docFileName_6265);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));

        // ---- Step 14 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They
        // are displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(docFileName_6265 + docxFileType);

        editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.getDocumentTitle();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 15 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(last_modification));

    }

    @Test(groups = { "DataPrepWord" })
    public void dataPrep_6266() throws Exception
    {
        String testName = getTestName();
        String siteName = getSiteName(testName);

        // Login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Create Site
        ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

        File file = new File(DATA_FOLDER + CIFS_LOCATION + SLASH + docFileName_6266 + docxFileType);
        ShareUserSitePage.uploadFile(drone, file).render();

        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_6266 + docxFileType);

        // Click "Edit Properties" in Actions section;
        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.setDocumentTitle(testName);

        detailsPage = editPropertiesPage.selectSave().render();
        detailsPage.render();
    }

    /** AONE-6266:MS Word 2010 - uploaded to Share (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6266() throws IOException, AWTException
    {
        String testName = getTestName();
        String siteName = getSiteName(testName).toLowerCase();
        String first_modification = testName + "1";
        String second_modification = testName + "2";
        String last_modification = testName + "3";

        String fullPath = networkDrive + cifsPath + siteName.toLowerCase() + SLASH + "documentLibrary" + SLASH;

        // ---- Step 1 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // The document is opened in write mode.
        Ldtp ldtp = word.openFileFromCMD(fullPath, docFileName_6266 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docFileName_6266);
        // ---- Step 2 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_1);
        word.editOffice(ldtp, " " + first_modification + " ");

        // ---- Step 3 ----
        // ---- Step Action -----
        // Save the document (Ctrl+S, for example) and close it
        // Expected Result
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(ldtp);
        ldtp.waitTime(5);
        word.exitOfficeApplication(ldtp, docFileName_6266);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));

        // ---- Step 4 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are
        // displayed correctly.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_6266 + docxFileType);

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
        ldtp = word.openFileFromCMD(fullPath, docFileName_6266 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docFileName_6266);
        // ---- Step 7 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_2);
        word.editOffice(ldtp, " " + second_modification + " ");

        // ---- Step 8 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        word.saveOffice(ldtp);
        ldtp.waitTime(5);
        word.exitOfficeApplication(ldtp, docFileName_6266);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));

        // ---- Step 9 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are
        // displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(docFileName_6266 + docxFileType);

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
        ldtp = word.openFileFromCMD(fullPath, docFileName_6266 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docFileName_6266);
        // ---- Step 12 ----
        // ---- Step Action -----
        // Add any data.
        // Expected Result
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_3);
        word.editOffice(ldtp, " " + last_modification + " ");

        // ---- Step 13 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly
        word.saveOffice(ldtp);
        ldtp.waitTime(5);
        word.exitOfficeApplication(ldtp, docFileName_6266);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // ---- Step 14 ----
        // ---- Step Action -----
        // Verify the document's metadata and version history in the Share.
        // Expected Result
        // The document's metadata and version history are not broken. They are
        // displayed correctly.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = documentLibPage.selectFile(docFileName_6266 + docxFileType);

        editPropertiesPage = detailsPage.selectEditProperties().render();
        editPropertiesPage.getDocumentTitle();
        Assert.assertTrue(editPropertiesPage.getDocumentTitle().equals(testName));
        editPropertiesPage.clickCancel();

        // ---- Step 15 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        body = detailsPage.getDocumentBody();
        Assert.assertTrue(body.contains(last_modification));

    }

    /** AONE-6269:MS Word 2010 - saved into CIFS */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6269() throws Exception
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

        l1 = word.openFileFromCMD(localPath, docxFileName_6269 + docxFileType, testUser, DEFAULT_PASSWORD, false);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6269);
        word.goToFile(l1);
        l1.waitTime(1);
        word.getAbstractUtil().clickOnObject(l1, "SaveAs");

        word.operateOnSaveAsWithFullPath(l1, fullPath, docxFileName_6269, testUser, DEFAULT_PASSWORD);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6269);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6269);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 2 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docxFileName_6269 + docxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(docxFileName_6269 + docxFileType));
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
        String documentTitle = "Title " + docxFileName_6269;
        String documentDescription = "Description for " + docxFileName_6269;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.docx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.docx";
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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6269 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6269);
        String actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(docxFileName_6269), "Microsoft Excel - " + docxFileName_6269 + " window is not active.");

        // --- Step 5 ---
        // --- Step action ---
        // Add any data.
        // --- Expected results --
        // The data is entered.
        word.editOffice(l1, " " + edit2 + " ");

        // --- Step 6 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6269);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 7 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6269 + docxFileType).render();

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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6269 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6269);
        actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(docxFileName_6269), "Microsoft Excel - " + docxFileName_6269 + " window is not active.");

        // --- Step 10 ---
        // --- Step action ---
        // Add any data.
        // --- Expected results --
        // The data is entered.
        word.editOffice(l1, " " + edit3 + " ");

        // --- Step 11 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6269);
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6269);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 12 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6269 + docxFileType).render();

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

    /** AONE-6270:MS Word 2010 - saved into CIFS (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6270() throws Exception
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

        l1 = word.openFileFromCMD(localPath, docxFileName_6270 + docxFileType, testUser, DEFAULT_PASSWORD, false);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6270);
        word.goToFile(l1);
        l1.waitTime(1);
        word.getAbstractUtil().clickOnObject(l1, "SaveAs");

        word.operateOnSaveAsWithFullPath(l1, fullPath, docxFileName_6270, testUser, DEFAULT_PASSWORD);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6270);

        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 2 ---
        // --- Step action ---
        // Verify the document's content.
        // --- Expected results --
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docxFileName_6269 + docxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(docxFileName_6269 + docxFileType));
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
        String documentTitle = "Title " + docxFileName_6270;
        String documentDescription = "Description for " + docxFileName_6270;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.docx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.docx";
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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6270 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6270);
        String actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(docxFileName_6270), "Microsoft Excel - " + docxFileName_6270 + " window is active.");

        // --- Step 5 ---
        // --- Step action ---
        // Add any data (5-10 mb).
        // --- Expected results --
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_1);
        word.editOffice(l1, " " + edit2 + " ");

        // --- Step 6 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6270);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 7 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6270 + docxFileType).render();

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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6270 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6270);
        actualName = l1.getWindowName();
        Assert.assertTrue(actualName.contains(docxFileName_6270), "Microsoft Excel - " + docxFileName_6270 + " window is active.");

        // --- Step 10 ---
        // --- Step action ---
        // Add any data (5-10 mb).
        // --- Expected results --
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_2);
        word.editOffice(l1, " " + edit3 + " ");

        // --- Step 11 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6269);
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6270);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 12 ---
        // --- Step action ---
        // Verify the document's metadata and version history in the Share.
        // --- Expected results --
        // The document's metadata and version history are not broken. They are
        // displayed correctly..
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6270 + docxFileType).render();

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

    /** AONE-6267:MS Word 2010 - created via context menu */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6267() throws Exception
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

        l1 = word.openOfficeApplication();
        word.editOffice(l1, addText);
        word.saveAsOffice(l1, fullPath + docxFileName_6267);
        word.operateOnSecurityAndWait(security, testUser, DEFAULT_PASSWORD);
        word.getAbstractUtil().waitForWindow(docxFileName_6267);
        word.exitOfficeApplication(l1, docxFileName_6267);

        // ---- Step 2 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = word.openFileFromCMD(fullPath, docxFileName_6267 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        l1 = word.getAbstractUtil().setOnWindow(docxFileName_6267);

        // ---- Step 3 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        word.editOffice(l1, " " + edit1 + " ");

        // --- Step 4 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6267);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docxFileName_6267 + docxFileType);

        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(docxFileName_6267 + docxFileType));
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
        String documentTitle = "Title " + docxFileName_6267;
        String documentDescription = "Description for " + docxFileName_6267;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.docx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.docx";
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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6267 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docxFileName_6267);
        // ---- Step 8 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        word.editOffice(l1, " " + edit2 + " ");

        // --- Step 9 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6267);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 10 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6267 + docxFileType).render();

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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6267 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docxFileName_6267);
        // ---- Step 13 ----
        // ---- Step Action -----
        // Add any data.
        // ---- Expected Result -----
        // The data is entered.
        word.editOffice(l1, " " + edit3 + " ");

        // --- Step 14 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6267);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 15 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6267 + docxFileType).render();

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

    /** AONE-6268:MS Word 2010 - created via context menu (big) */
    @Test(groups = { "CIFSWindowsClient", "EnterpriseOnly" })
    public void AONE_6268() throws Exception
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

        l1 = word.openOfficeApplication();
        word.editOffice(l1, addText);
        word.saveAsOffice(l1, fullPath + docxFileName_6268);
        word.operateOnSecurityAndWait(security, testUser, DEFAULT_PASSWORD);
        word.exitOfficeApplication(l1, docxFileName_6268);

        // ---- Step 2 ----
        // ---- Step Action -----
        // Open .docx document for editing.
        // ---- Expected Result -----
        // The document is opened in write mode.
        l1 = word.openFileFromCMD(fullPath, docxFileName_6268 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().setOnWindow(docxFileName_6268);

        // ---- Step 3 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_1);
        word.editOffice(l1, " " + edit1 + " ");

        // --- Step 4 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6268);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // ---- Step 5 ----
        // ---- Step Action -----
        // Verify the document's content.
        // Expected Result
        // All changes are present and displayed correctly.
        ShareUser.login(drone, testUser);
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
        DocumentDetailsPage detailsPage = documentLibPage.selectFile(docxFileName_6268 + docxFileType);
        EditDocumentPropertiesPage editPropertiesPage = detailsPage.selectEditProperties().render();
        Assert.assertTrue(editPropertiesPage.getName().equals(docxFileName_6268 + docxFileType));
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
        String documentTitle = "Title " + docxFileName_6268;
        String documentDescription = "Description for " + docxFileName_6268;
        editDocPropPage.setDocumentTitle(documentTitle);
        editDocPropPage.setDescription(documentDescription);
        detailsPage = editDocPropPage.selectSave().render();

        String newFileVersion2 = CIFS_LOCATION + SLASH + "SaveCIFSv2.docx";
        String newFileVersion3 = CIFS_LOCATION + SLASH + "SaveCIFSv3.docx";
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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6268 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().waitForWindow(docxFileName_6268);
        // ---- Step 8 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_2);
        word.editOffice(l1, " " + edit2 + " ");

        // --- Step 9 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6268);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 10 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6268 + docxFileType).render();

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
        l1 = word.openFileFromCMD(fullPath, docxFileName_6268 + docxFileType, testUser, DEFAULT_PASSWORD, true);
        word.getAbstractUtil().waitForWindow(docxFileName_6268);
        // ---- Step 13 ----
        // ---- Step Action -----
        // Add any data (5-10 mb).
        // ---- Expected Result -----
        // The data is entered.
        CifsUtil.uploadImageInOffice(image_3);
        word.editOffice(l1, " " + edit3 + " ");

        // --- Step 14 ---
        // --- Step action ---
        // Save the document (Ctrl+S, for example) and close it.
        // --- Expected results --
        // The document is saved. No errors occur in UI and in the log. No tmp
        // files are left.
        word.saveOffice(l1);
        l1.waitTime(2);
        word.exitOfficeApplication(l1, docxFileName_6268);
        Assert.assertTrue(CifsUtil.checkTemporaryFileDoesntExists(fullPath, docxFileType, 6));
        // --- Step 15 ---
        // --- Step action ---
        // Specify the document's metadata, e.g. title, description and so on,
        // and create some version history (i.e. add several new versions (minor
        // and major))
        // via the Share.
        // --- Expected results --
        // The document's metadata and version history are specified.
        documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();
        detailsPage = ShareUserSitePage.openDetailsPage(drone, docxFileName_6268 + docxFileType).render();

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

}
