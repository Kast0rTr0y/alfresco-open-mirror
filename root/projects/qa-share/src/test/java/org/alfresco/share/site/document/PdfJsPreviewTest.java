package org.alfresco.share.site.document;

import org.alfresco.po.share.preview.PdfJsPlugin;
import org.alfresco.po.share.util.FailedTestListener;
import org.alfresco.po.share.util.SiteUtil;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserPdfJsPreview;
import org.alfresco.share.util.api.CreateUserAPI;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * HTML5 previewer tests
 * 
 * @author wabson
 *
 */
@Listeners(FailedTestListener.class)
public class PdfJsPreviewTest extends AbstractUtils
{
    private static final Logger logger = Logger.getLogger(PdfJsPreviewTest.class);    
    
    private static String testPassword = DEFAULT_PASSWORD;
    protected String testUser;
    protected String siteName = "";

    private static final String FILES_PREFIX = "preview" + SLASH;
    private static final String TESTFILE_PDF =  "PreviewTest.pdf";
    private static final String TESTFILE_DOC = "PreviewTest.doc";

    @Override
    @BeforeClass(alwaysRun=true)
    public void setup() throws Exception
    {
        super.setup();
        testName = this.getClass().getSimpleName();
        testUser = testName + "@" + DOMAIN_FREE;
        logger.info("Starting Tests: " + testName);
    }
    
    /**
     * DataPreparation method - ACE_1292_01
     *   
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create and upload PDF file with content
     * 
     * @throws Exception
     */
    
    @Test(groups={"DataPrepPdfJsPreview"})
    public void dataPrep_PdfJsPreview_ACE_1292_01() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_FREE;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        try
        {
            //Create user
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);
 
            //Login as created user 
            ShareUser.login(drone, testUser, testPassword);

            //Create site
            SiteUtil.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Upload test file
            ShareUser.uploadFileInFolder(drone, new String[] { FILES_PREFIX + TESTFILE_PDF });

            //Created user logs  out
            ShareUser.logout(drone);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }
    
    private void testDocument(String siteName, String docName)
    {
        ShareUser.openSiteDashboard(drone, siteName);
        ShareUser.openDocumentLibrary(drone);
        ShareUser.openDocumentDetailPage(drone, docName);

        PdfJsPlugin viewer = ShareUserPdfJsPreview.preview(drone);
        Assert.assertEquals(viewer.getMainViewNumDisplayedPages(), 3);
        Assert.assertEquals(viewer.getNumClaimedPages(), 3);

        Assert.assertEquals(viewer.getCurrentPageNum(), 1);
        Assert.assertFalse(viewer.isToolbarButtonEnabled("previous"));
        Assert.assertTrue(viewer.isToolbarButtonEnabled("next"));
        viewer.clickToolbarButton("next");
        Assert.assertEquals(viewer.getCurrentPageNum(), 2);
        Assert.assertTrue(viewer.isToolbarButtonEnabled("previous"));
        Assert.assertTrue(viewer.isToolbarButtonEnabled("next"));
        viewer.clickToolbarButton("next");
        Assert.assertEquals(viewer.getCurrentPageNum(), 3);
        Assert.assertTrue(viewer.isToolbarButtonEnabled("previous"));
        Assert.assertFalse(viewer.isToolbarButtonEnabled("next"));
        Assert.assertFalse(viewer.isSidebarVisible());

        viewer.clickToolbarButton("sidebarBtn");
        Assert.assertTrue(viewer.isSidebarVisible());
        Assert.assertEquals(viewer.getSidebarNumDisplayedPages(), 3);
    }
    
    /**
     * Check that a 3-page PDF file is displayed correctly in the viewer
     * 
     * 1) User logs in and navigates to the previously-uploaded content in the test site
     * 2) Check that the number of pages displayed in the main view is correct
     * 3) Check that the total number of pages shown in the toolbar is correct
     * 4) Check that the Previous page button is DISABLED and the Next button is ENABLED
     * 5) Click the next button and check the Next and Previous buttons are ENABLED
     * 6) Click the next button and check the Previous page button is ENABLED and the Next button is DISABLED
     * 7) Check that the zoom out and zoom in buttons are ENABLED
     * 8) Check the sidebar is not displayed
     * 9) Click the sidebar button, make sure the sidebar displays and that the number of pages shown there is correct
     * 10) User logs out
     */
    @Test(groups={"TestPdfJsPreview"})
    public void pdfJsPreview_ACE_1292_01()
    {
        // Search name is the same as the test name
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_FREE;
        ShareUser.login(drone, testUser, testPassword);

        testDocument(getSiteName(testName), TESTFILE_PDF);

        ShareUser.logout(drone);
    }
    
    
    /**
     * 
     * DataPreparation method - ACE_1292_02
     * 
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create and upload MS Word file with content
     *  
     * @throws Exception
     */
    
    @Test(groups={"DataPrepPdfJsPreview"})
    public void dataPrep_PdfJsPreview_ACE_1292_02() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_FREE;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        try
        {
            //Create user
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);
 
            //Login as created user 
            ShareUser.login(drone, testUser, testPassword);

            //Create site
            SiteUtil.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Upload test file
            ShareUser.uploadFileInFolder(drone, new String[] { FILES_PREFIX + TESTFILE_DOC });

            //Created user logs  out
            ShareUser.logout(drone);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }
    
    
    /**
     * Upload a Word document and check that it is displayed correctly in the viewer
     */
    @Test(groups={"TestPdfJsPreview"})
    public void pdfJsPreview_ACE_1292_02()
    {
        // Search name is the same as the test name
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_FREE;
        ShareUser.login(drone, testUser, testPassword);

        testDocument(getSiteName(testName), TESTFILE_DOC);

        ShareUser.logout(drone);
    }
}
