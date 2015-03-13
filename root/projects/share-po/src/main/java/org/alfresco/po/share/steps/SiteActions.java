/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.po.share.steps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.exception.ShareException;
import org.alfresco.po.share.site.CreateSitePage;
import org.alfresco.po.share.site.NewFolderPage;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.SitePage;
import org.alfresco.po.share.site.UpdateFilePage;
import org.alfresco.po.share.site.UploadFilePage;
import org.alfresco.po.share.site.document.ConfirmDeletePage;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.po.share.site.document.CopyOrMoveContentPage;
import org.alfresco.po.share.site.document.CreatePlainTextContentPage;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.EditDocumentPropertiesPage;
import org.alfresco.po.share.site.document.FileDirectoryInfo;
import org.alfresco.po.share.site.document.ConfirmDeletePage.Action;
import org.alfresco.webdrone.HtmlPage;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.exception.PageException;
import org.alfresco.webdrone.exception.PageOperationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.NoSuchElementException;

/**
 * Share actions - All the common steps of site action
 * 
 * @author sprasanna
 */

public class SiteActions extends CommonActions
{
    private static Log logger = LogFactory.getLog(SiteActions.class);
    public static long refreshDuration = 25000;
    final static String SITE_VISIBILITY_PUBLIC = "public";
    protected static final String SITE_VISIBILITY_PRIVATE = "private";
    protected static final String SITE_VISIBILITY_MODERATED = "moderated";
    public final static String DOCLIB = "DocumentLibrary";
    protected static final String UNIQUE_TESTDATA_STRING = "sync";
    private static final String SITE_DASH_LOCATION_SUFFIX = "/page/site/";

    /**
     * Create site
     */
    public boolean createSite(WebDrone drone, final String siteName, String desc, String siteVisibility)
    {
        if (siteName == null || siteName.isEmpty())
        {
            throw new IllegalArgumentException("site name is required");
        }
        boolean siteCreated = false;
        DashBoardPage dashBoard;
        SiteDashboardPage site = null;
        try
        {
            SharePage page = drone.getCurrentPage().render();
            dashBoard = page.getNav().selectMyDashBoard().render();
            CreateSitePage createSite = dashBoard.getNav().selectCreateSite().render();
            if (siteVisibility == null)
            {
                siteVisibility = SITE_VISIBILITY_PUBLIC;
            }
            if (siteVisibility.equalsIgnoreCase(SITE_VISIBILITY_MODERATED))
            {
                site = createSite.createModerateSite(siteName, desc).render();
            }
            else if (siteVisibility.equalsIgnoreCase(SITE_VISIBILITY_PRIVATE))
            {
                site = createSite.createPrivateSite(siteName, desc).render();
            }
            // Will create public site
            else
            {
                site = createSite.createNewSite(siteName, desc).render();
            }

            site.render();

            if (siteName.equalsIgnoreCase(site.getPageTitle()))
            {
                siteCreated = true;
            }
            return siteCreated;
        }
        catch (UnsupportedOperationException une)
        {
            String msg = String.format("Failed to create a new site %n Site Name: %s", siteName);
            throw new RuntimeException(msg, une);
        }
        catch (NoSuchElementException nse)
        {
            return false;
        }
    }

    /**
     * Creates a new folder at the Path specified, Starting from the Document Library Page.
     * Assumes User is logged in and a specific Site is open.
     * 
     * @param drone WebDrone Instance
     * @param folderName String Name of the folder to be created
     * @param folderTitle String Title of the folder to be created
     * @param folderDesc String Description of the folder to be created
     * @return DocumentLibraryPage
     */
    public DocumentLibraryPage createFolder(WebDrone drone, String folderName, String folderTitle, String folderDesc)
    {
        DocumentLibraryPage docPage = null;

        // Open Document Library
        SharePage thisPage = getSharePage(drone);

        if (!(thisPage instanceof DocumentLibraryPage))
        {
            throw new PageOperationException("the current page is not documentlibrary page");
        }
        else
        {
            docPage = (DocumentLibraryPage) thisPage;
        }

        NewFolderPage newFolderPage = docPage.getNavigation().selectCreateNewFolder().render();
        docPage = newFolderPage.createNewFolder(folderName, folderTitle, folderDesc).render();

        logger.info("Folder Created" + folderName);
        return docPage;
    }

    /**
     * Open document Library: Top Level Assumes User is logged in and a Specific
     * Site is open.
     *
     * @param drone WebDrone Instance
     * @return DocumentLibraryPage
     */
    public DocumentLibraryPage openDocumentLibrary(WebDrone drone)
    {
        // Assumes User is logged in
        /*
         * SharePage page = getSharePage(drone); if (page instanceof
         * DocumentLibraryPage) { return (DocumentLibraryPage) page; }
         */

        // Open DocumentLibrary Page from Site Page
        SitePage site = (SitePage) getSharePage(drone);

        DocumentLibraryPage docPage = site.getSiteNav().selectSiteDocumentLibrary().render();
        logger.info("Opened Document Library");
        return docPage;
    }

    /**
     * Assumes a specific Site is open Opens the Document Library Page and navigates to the Path specified.
     * 
     * @param drone WebDrone Instance
     * @param folderPath: String folder path relative to DocumentLibrary e.g. DOCLIB + file.seperator + folderName1
     * @throws SkipException if error in this API
     */
    public DocumentLibraryPage navigateToFolder(WebDrone drone, String folderPath) throws Exception
    {
        DocumentLibraryPage docPage;

        try
        {
            if (folderPath == null)
            {
                throw new UnsupportedOperationException("Incorrect FolderPath: Null");
            }

            // check whether we are in the document libary page
            SharePage thisPage = getSharePage(drone);

            if (!(thisPage instanceof DocumentLibraryPage))
            {
                throw new PageOperationException("the current page is not documentlibrary page");
            }
            else
            {
                docPage = (DocumentLibraryPage) thisPage;
            }

            // Resolve folderPath, considering diff treatment for non-windows OS
            logger.info(folderPath);
            String[] path = folderPath.split(Pattern.quote(File.separator));

            // Navigate to the parent Folder where the file needs to be uploaded
            for (int i = 0; i < path.length; i++)
            {
                if (path[i].isEmpty())
                {
                    // Ignore, Continue to the next;
                    logger.debug("Empty Folder Path specified: " + path.toString());
                }
                else
                {
                    if ((i == 0) && (path[i].equalsIgnoreCase(DOCLIB)))
                    {
                        // Repo or Doclib is already open
                        logger.info("Base Folder: " + path[i]);
                    }
                    else
                    {
                        logger.info("Navigating to Folder: " + path[i]);
                        docPage = selectContent(drone, path[i]).render();
                    }
                }
            }
            logger.info("Selected Folder:" + folderPath);
        }
        catch (Exception e)
        {
            throw new ShareException("Skip test. Error in navigateToFolder: " + e.getMessage());
        }

        return docPage;
    }

    /**
     * Util traverses through all the pages of the doclib to find the content within the folder and clicks on the contentTile
     * 
     * @param drone
     * @param contentName
     * @return
     */
    public HtmlPage selectContent(WebDrone drone, String contentName)
    {
        return getFileDirectoryInfo(drone, contentName).clickOnTitle().render();
    }

    /**
     * Util traverses through all the pages of the doclib to find the content within the folder
     * 
     * @param drone
     * @param contentName
     * @return
     */
    public FileDirectoryInfo getFileDirectoryInfo(WebDrone drone, String contentName)
    {
        Boolean moreResultPages = true;
        FileDirectoryInfo contentRow = null;
        DocumentLibraryPage docLibPage = getSharePage(drone).render();

        // Start from first page
        while (docLibPage.hasPreviousPage())
        {
            docLibPage = docLibPage.selectPreviousPage().render();
        }

        while (moreResultPages)
        {
            // Get Search Results
            try
            {
                contentRow = docLibPage.getFileDirectoryInfo(contentName);
                break;
            }
            catch (PageException pe)
            {
                // Check next Page if available
                moreResultPages = docLibPage.hasNextPage();

                if (moreResultPages)
                {
                    docLibPage = docLibPage.selectNextPage().render();
                }
            }
        }

        // Now return the content found else throw PageException
        if (contentRow == null)
        {
            throw new PageException(String.format("File directory info with title %s was not found in the selected folder", contentName));
        }

        return contentRow;
    }

    /**
     * Creates a new folder at the Path specified, Starting from the Document
     * Library Page. Assumes User is logged in and a specific Site is open.
     *
     * @param drone WebDrone Instance
     * @param folderName String Name of the folder to be created
     * @param folderDesc String Description of the folder to be created
     * @param parentFolderPath String Path for the folder to be created, under
     *            DocumentLibrary : such as constDoclib + file.seperator +
     *            parentFolderName1 + file.seperator + parentFolderName2
     * @throws Exception
     */
    public DocumentLibraryPage createFolderInFolder(WebDrone drone, String folderName, String folderDesc, String folderTitle, String parentFolderPath)
            throws Exception
    {
        try
        {
            // Data setup Options: Use UI, Use API, Copy, Data preloaded?

            // Using Share UI
            // Navigate to the parent Folder where the file needs to be uploaded
            navigateToFolder(drone, parentFolderPath);

            // Create Folder
            return createFolder(drone, folderName, folderTitle, folderDesc);
        }
        catch (Exception ex)
        {
            throw new ShareException("Skip test. Error in Create Folder: " + ex.getMessage());
        }
    }

    /**
     * Assumes User is logged in and a specific Site's Doclib is open, Parent Folder is pre-selected.
     * 
     * @param file File Object for the file in reference
     * @return DocumentLibraryPage
     * @throws SkipException if error in this API
     */
    public HtmlPage uploadFile(WebDrone drone, File file)
    {
        DocumentLibraryPage docPage;
        try
        {
            checkIfDriverIsNull(drone);
            docPage = drone.getCurrentPage().render(refreshDuration);
            // Upload File
            UploadFilePage upLoadPage = docPage.getNavigation().selectFileUpload().render();
            docPage = upLoadPage.uploadFile(file.getCanonicalPath()).render();
            docPage.setContentName(file.getName());
            logger.info("File Uploaded:" + file.getCanonicalPath());
        }
        catch (Exception e)
        {
            throw new ShareException("Skip test. Error in UploadFile: " + e);
        }

        return docPage.render();
    }

    /**
     * This method is used to create content with name, title and description.
     * User should be logged in and present on site page.
     *
     * @param drone
     * @param contentDetails
     * @param contentType
     * @return {@link DocumentLibraryPage}
     * @throws Exception
     */
    public DocumentLibraryPage createContent(WebDrone drone, ContentDetails contentDetails, ContentType contentType) throws Exception
    {
        // Open Document Library
        DocumentLibraryPage documentLibPage = drone.getCurrentPage().render();
        DocumentDetailsPage detailsPage = null;

        try
        {
            CreatePlainTextContentPage contentPage = documentLibPage.getNavigation().selectCreateContent(contentType).render();
            detailsPage = contentPage.create(contentDetails).render();
            documentLibPage = (DocumentLibraryPage) detailsPage.getSiteNav().selectSiteDocumentLibrary();
            documentLibPage.render();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new ShareException("Error in creating content." + e);
        }

        return documentLibPage;
    }

    /**
     * isFileVisible is to check whether file or folder visible..
     * 
     * @param drone
     * @param contentName
     * @return
     */
    public boolean isFileVisible(WebDrone drone, String contentName)
    {
        try
        {
            getFileDirectoryInfo(drone, contentName);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Open Site and then Open Document Library Assumes User is logged in and a
     * Specific Site Dashboard is open.
     *
     * @param driver WebDrone Instance
     * @param siteName String Name of the Site
     * @return DocumentLibraryPage
     */
    public DocumentLibraryPage openSitesDocumentLibrary(WebDrone driver, String siteName)
    {
        // Assumes User is logged in

        // Checking for site doc lib to be open.
        HtmlPage page = getSharePage(driver).render();
        if (page instanceof DocumentLibraryPage)
        {
            if (((DocumentLibraryPage) page).isSite(siteName) && ((DocumentLibraryPage) page).isDocumentLibrary())
            {
                logger.info("Site doc lib page open ");
                return ((DocumentLibraryPage) page);
            }
        }

        // Open Site
        openSiteDashboard(driver, siteName);

        // Open DocumentLibrary Page from SiteDashBoard
        DocumentLibraryPage docPage = openDocumentLibrary(driver);

        // Return DocLib Page
        return docPage;
    }

    /**
     * From the User DashBoard, navigate to the Site DashBoard and waits for the
     * page render to complete. Assumes User is logged in.
     *
     * @param driver WebDrone Instance
     * @param siteName String Name of the site to be opened
     * @return SiteDashboardPage
     * @throws PageException
     */
    public SiteDashboardPage openSiteDashboard(WebDrone driver, String siteName) throws PageException
    {
        // Assumes User is logged in
        HtmlPage page = getSharePage(driver).render();

        // Check if site dashboard is already open. Return
        if (page instanceof SiteDashboardPage)
        {
            if (((SiteDashboardPage) page).isSite(siteName))
            {
                logger.info("Site dashboad page already open for site - " + siteName);
                return page.render();
            }
        }

        // Open User DashBoard: Using SiteURL
        SiteDashboardPage siteDashPage = openSiteURL(driver, getSiteShortname(siteName));

        // Open User DashBoard: Using SiteFinder
        // SiteDashboardPage siteDashPage = SiteUtil.openSiteFromSearch(driver, siteName);

        // logger.info("Opened Site Dashboard using SiteURL: " + siteName);

        return siteDashPage;
    }

    /**
     * Method to navigate to site dashboard url, based on siteshorturl, rather than sitename
     * This is to be used to navigate only as a util, not to test getting to the site dashboard
     * 
     * @param drone
     * @param siteShortURL
     * @return {@link SiteDashBoardPage}
     */
    public SiteDashboardPage openSiteURL(WebDrone drone, String siteShortURL)
    {
        String url = drone.getCurrentUrl();
        String target = url.substring(0, url.indexOf("/page/")) + SITE_DASH_LOCATION_SUFFIX + getSiteShortname(siteShortURL) + "/dashboard";
        drone.navigateTo(target);
        SiteDashboardPage siteDashboardPage = getSharePage(drone).render();

        return siteDashboardPage.render();
    }

    /**
     * Helper to consistently get the Site Short Name.
     *
     * @param siteName String Name of the test for uniquely identifying / mapping
     *            test data with the test
     * @return String site short name
     */
    public String getSiteShortname(String siteName)
    {
        String siteShortname = "";
        String[] unallowedCharacters = { "_", "!" };

        for (String removeChar : unallowedCharacters)
        {
            siteShortname = siteName.replace(removeChar, "");
        }

        return siteShortname;
    }

    /**
     * Helper to create a new file, empty or with specified contents if one does
     * not exist. Logs if File already exists
     *
     * @param filename String Complete path of the file to be created
     * @param contents String Contents for text file
     * @return File
     */
    public File newFile(String filename, String contents)
    {
        File file = new File(filename);

        try
        {
            if (!file.exists())
            {

                if (!contents.isEmpty())
                {
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
                    writer.write(contents);
                    writer.close();
                }
                else
                {
                    file.createNewFile();
                }
            }
            else
            {
                logger.debug("Filename already exists: " + filename);
            }
        }
        catch (IOException ex)
        {
            logger.error("Unable to create sample file", ex);
        }
        return file;
    }

    /**
     * Util to download a file in a particular path
     */

    public void shareDownloadFileFromDocLib(WebDrone drone, String FileName, String path)
    {
        FileDirectoryInfo fileInfo = getFileDirectoryInfo(drone, FileName);
        fileInfo.selectDownload();
        DocumentLibraryPage docLib = drone.getCurrentPage().render();
        docLib.waitForFile(path);
    }

    /**
     * Delete content in share
     */
    public void deleteContentInDocLib(WebDrone drone, String contentName)
    {
        selectContentCheckBox(drone, contentName);
        DocumentLibraryPage doclib = deleteDocLibContents(drone);
        doclib.render();

    }

    /**
     * Checks the checkbox for a content if not selected on the document library
     * page.
     * 
     * @param drone
     * @param contentName
     * @return DocumentLibraryPage
     * @IMP Note: Expects the user is logged in and document library page within
     *      the selected site is open.
     */
    private DocumentLibraryPage selectContentCheckBox(WebDrone drone, String contentName)
    {
        DocumentLibraryPage docLibPage = ((DocumentLibraryPage) getSharePage(drone)).render();
        if (!docLibPage.getFileDirectoryInfo(contentName).isCheckboxSelected())
        {
            docLibPage.getFileDirectoryInfo(contentName).selectCheckbox();
        }
        return docLibPage.render();
    }

    /**
     * Delete doc lib contents.
     * 
     * @param drone
     * @return
     */
    private DocumentLibraryPage deleteDocLibContents(WebDrone drone)
    {
        ConfirmDeletePage deletePage = ((DocumentLibraryPage) getSharePage(drone)).getNavigation().render().selectDelete();
        return deletePage.selectAction(Action.Delete).render();
    }

    /**
     * This method uploads the new version for the document with the given file
     * from data folder. User should be on Document details page.
     * 
     * @param fileName
     * @param drone
     * @return DocumentDetailsPage
     * @throws IOException
     */
    public void uploadNewVersionOfDocument(WebDrone drone, String title, String fileName, String comments) throws IOException
    {
        String fileContents = "New File being created via newFile:" + fileName;
        File newFileName = newFile(fileName, fileContents);
        DocumentLibraryPage doclib = (DocumentLibraryPage) drone.getCurrentPage();
        DocumentDetailsPage detailsPage = doclib.selectFile(title).render();
        UpdateFilePage updatePage = detailsPage.selectUploadNewVersion().render();
        updatePage.selectMajorVersionChange();
        updatePage.uploadFile(newFileName.getCanonicalPath());
        updatePage.setComment(comments);
        detailsPage = updatePage.submit().render();
        detailsPage.selectDownload(null);
    }

    /**
     * Just get version number of the file
     */
    public String getVersionNumber(WebDrone drone, String title)
    {
        DocumentLibraryPage doclib = (DocumentLibraryPage) drone.getCurrentPage().render();
        DocumentDetailsPage detailsPage = doclib.selectFile(title).render();
        return detailsPage.getDocumentVersion();
    }

    /**
     * Get Version info from Document Library
     */
    public String getDocLibVersionInfo(WebDrone drone, String contentName)
    {
        FileDirectoryInfo fileInfo = getFileDirectoryInfo(drone, contentName);
        return fileInfo.getVersionInfo();
    }

    /**
     * Navigate to Document library
     */
    public void navigateToDocuemntLibrary(WebDrone drone, String siteName)
    {
        openSiteURL(drone, siteName);
        openDocumentLibrary(drone);

    }

    /**
     * Copy or Move to File or folder from document library.
     * 
     * @param drone
     * @param destination
     * @param siteName
     * @param fileName
     * @return
     */
    public HtmlPage copyOrMoveArtifact(WebDrone drone, String destination, String siteName, String moveFolderName, String fileName, String type)
    {
        DocumentLibraryPage docPage = (DocumentLibraryPage) getSharePage(drone);
        CopyOrMoveContentPage copyOrMoveToPage;

        if (type.equals("Copy"))
        {
            copyOrMoveToPage = docPage.getFileDirectoryInfo(fileName).selectCopyTo().render();
        }
        else
        {
            copyOrMoveToPage = docPage.getFileDirectoryInfo(fileName).selectMoveTo().render();
        }

        copyOrMoveToPage.selectDestination(destination);
        copyOrMoveToPage.selectSite(siteName).render();
        if (moveFolderName != null)
        {
            copyOrMoveToPage.selectPath(moveFolderName).render();
        }
        copyOrMoveToPage.selectOkButton().render();
        return getSharePage(drone);
    }

    /**
     * Uses the in-line rename function to rename content
     * Assumes User is logged in and a DocumentLibraryPage of the selected site is open
     * 
     * @param drone
     * @param contentName
     * @param newName
     * @param saveChanges <code>true</code> saves the changes, <code>false</code> cancels without saving.
     * @return
     */
    public DocumentLibraryPage editContentNameInline(WebDrone drone, String contentName, String newName, boolean saveChanges)
    {
        FileDirectoryInfo fileDirInfo = getFileDirectoryInfo(drone, contentName);

        fileDirInfo.contentNameEnableEdit();
        fileDirInfo.contentNameEnter(newName);
        if (saveChanges)
        {
            fileDirInfo.contentNameClickSave();
        }
        else
        {
            fileDirInfo.contentNameClickCancel();
        }

        return getSharePage(drone).render();
    }
    
    /**
     * In the document library page select edit properties to set a new title , description or name for the content
     * Assume the user is logged in and a documentLibraryPage of the selected site is open
     * 
     * @author sprasanna
     * @param - Webdrone
     * @param - String contentName
     * @param - String newContentName
     * @param - String title
     * @param - String descirption
     */
    public DocumentLibraryPage editProperties(WebDrone drone, String contentName, String newContentName, String title, String description)
    {
        DocumentLibraryPage documentLibraryPage = ((DocumentLibraryPage) getSharePage(drone)).render();
        EditDocumentPropertiesPage editProp = documentLibraryPage.getFileDirectoryInfo(contentName).selectEditProperties().render();
        // Check the newContent is present
        if (newContentName != null)
        {
            editProp.setName(newContentName);
        }
        // Check the newContent is present
        if (title != null)
        {
            editProp.setDocumentTitle(title);
        }
        // Check the newContent is present
        if (description != null)
        {
            editProp.setDescription(description);
        }

        return editProp.selectSave().render();
    }
    
}
