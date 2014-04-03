package org.alfresco.share.util;

import java.io.File;
import java.util.List;

import org.alfresco.po.share.RepositoryPage;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.enums.ViewType;
import org.alfresco.po.share.exception.ShareException;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.po.share.site.document.CopyOrMoveContentPage;
import org.alfresco.po.share.site.document.CreatePlainTextContentPage;
import org.alfresco.po.share.site.document.DetailsPage;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.FileDirectoryInfo;
import org.alfresco.po.share.site.document.TagPage;
import org.alfresco.webdrone.HtmlPage;
import org.alfresco.webdrone.WebDrone;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.SkipException;

public class ShareUserRepositoryPage extends AbstractTests
{
    private static Log logger = LogFactory.getLog(ShareUserRepositoryPage.class);

    public ShareUserRepositoryPage()
    {
        if (logger.isTraceEnabled())
        {
            logger.debug(this.getClass().getSimpleName() + " instantiated");
        }
    }

    /**
     * Open Repository Page: Top Level Assumes User is logged in - Opens
     * repository in simple View
     * 
     * @param driver
     *            WebDrone Instance
     * @return RepositoryPage
     */

    public static RepositoryPage openRepositorySimpleView(WebDrone driver)
    {

        // Assumes User is logged in
        SharePage page = ShareUser.getSharePage(driver);

        RepositoryPage repositorypage = page.getNav().selectRepository();
        repositorypage = ((RepositoryPage) ShareUserSitePage.selectView(driver, ViewType.SIMPLE_VIEW));
        logger.info("Opened RepositoryPage");
        return repositorypage;
    }

    /**
     * Open Repository Page: Top Level Assumes User is logged in - Opens
     * repository in detailed View
     * 
     * @param driver
     *            WebDrone Instance
     * @return RepositoryPage
     */

    public static RepositoryPage openRepositoryDetailedView(WebDrone driver)
    {

        // Assumes User is logged in
        RepositoryPage repositorypage = openRepository(driver);
        
        repositorypage = ((RepositoryPage) ShareUserSitePage.selectView(driver, ViewType.DETAILED_VIEW)).render();
        logger.info("Opened RepositoryPage");
        return repositorypage;
    }

    /**
     * Open Repository Page: Default View
     * 
     * @param driver
     *            WebDrone Instance
     * @return RepositoryPage
     */
    public static RepositoryPage openRepository(WebDrone driver)
    {
        SharePage page = ShareUser.getSharePage(driver);

        RepositoryPage repositorypage = page.getNav().selectRepository().render();
        logger.info("Opened RepositoryPage");
        return repositorypage;
    }

    /**
     * Assumes Repository Page is open and navigates to the Path specified.
     * 
     * @param driver
     *            WebDrone Instance
     * @param folderPath
     *            : String folder path relative to RepositoryPage e.g. Repo +
     *            file.seperator + folderName1
     * @throws SkipException
     *             if error in this API
     * @return RepositoryPage
     */

    public static RepositoryPage navigateToFolderInRepository(WebDrone driver, String folderPath) throws Exception
    {
        openRepository(driver);
        RepositoryPage repositoryPage = ((RepositoryPage) ShareUserSitePage.navigateToFolder(driver, folderPath)).render();
        return repositoryPage;
    }

    /**
     * Assumes User is logged in and a specific Site's RepositoryPage is open,
     * Parent Folder is pre-selected.
     * 
     * @param file
     *            File Object for the file in reference
     * @return RepositoryPage
     * @throws SkipException
     *             if error in this API
     */
    public static RepositoryPage uploadFileInRepository(WebDrone driver, File file) throws Exception
    {
        
        RepositoryPage repositoryPage = ((RepositoryPage) ShareUserSitePage.uploadFile(driver, file)).render();
        return repositoryPage;
    }

    /**
     * Creates a new folder at the Path specified, Starting from the Document
     * Library Page. Assumes User is logged in and a specific Site is open.
     * 
     * @param driver
     *            WebDrone Instance
     * @param folderName
     *            String Name of the folder to be created
     * @param folderDesc
     *            String Description of the folder to be created
     * @return RepositoryPage
     */
    public static RepositoryPage createFolderInRepository(WebDrone driver, String folderName, String folderDesc)
    {
        return createFolderInRepository(driver, folderName, null, folderDesc);
    }

    /**
     * Creates a new folder at the Path specified, Starting from the
     * RepositoryPage Page. Assumes User is logged in and a specific Site is
     * open.
     * 
     * @param driver
     *            WebDrone Instance
     * @param folderName
     *            String Name of the folder to be created
     * @param folderTitle
     *            String Title of the folder to be created
     * @param folderDesc
     *            String Description of the folder to be created
     * @return RepositoryPage
     */
    public static RepositoryPage createFolderInRepository(WebDrone driver, String folderName, String folderTitle, String folderDesc)
    {
        RepositoryPage repositoryPage = ((RepositoryPage) ShareUserSitePage.createFolder(driver, folderName, folderTitle, folderDesc)).render();
        return repositoryPage;
    }

    /**
     * Creates a new folder at the Path specified, Starting from the
     * RepositoryPage Page. Assumes User is logged in and a specific Site is
     * open.
     * 
     * @param driver
     *            WebDrone Instance
     * @param folderName
     *            String Name of the folder to be created
     * @param folderTitle
     *            String Title of the folder to be created
     * @param folderDesc
     *            String Description of the folder to be created
     * @return RepositoryPage
     */
    public static HtmlPage createFolderInRepositoryWithValidation(WebDrone driver, String folderName, String folderTitle, String folderDesc)
    {
        HtmlPage htmlPage = ShareUserSitePage.createFolderWithValidation(driver, folderName, folderTitle, folderDesc);
        if (htmlPage instanceof DocumentLibraryPage)
        {
            RepositoryPage repositoryPage = htmlPage.render();
            return repositoryPage;
        }
        return htmlPage;
    }

    /**
     * This method does the copy or move the folder or document into another
     * folder. User should be on RepositoryPage Page.
     * 
     * @param isCopy
     * @param testFolderName
     * @param copyFolderName
     * @param docLibPage
     * @return CopyOrMoveContentPage
     */
    public static CopyOrMoveContentPage copyOrMoveToFolderInRepository(WebDrone drone, String sourceFolder, String[] destinationFolders, boolean isCopy)
    {
        if ((StringUtils.isEmpty(sourceFolder)) || (destinationFolders.length == 0))
        {
            throw new IllegalArgumentException("sitename/sourceFolder/destinationFolders should not be empty or null");
        }
        CopyOrMoveContentPage copyOrMoveContentPage;
        RepositoryPage repoPage = (RepositoryPage) ShareUser.getSharePage(drone);
        FileDirectoryInfo contentRow = repoPage.getFileDirectoryInfo(sourceFolder);
        if (isCopy)
        {
            copyOrMoveContentPage = contentRow.selectCopyTo().render();
        }
        else
        {
            copyOrMoveContentPage = contentRow.selectMoveTo().render();
        }
        copyOrMoveContentPage = copyOrMoveContentPage.selectDestination(REPO).render();
        copyOrMoveContentPage = copyOrMoveContentPage.selectPath(destinationFolders).render();
        return copyOrMoveContentPage;
    }

    /**
     * Selects cancel button
     * 
     * @param drone
     * @param sourceFolder
     * @param destinationFolders
     * @param isCopy
     * @return RepositoryPage
     */
    public static RepositoryPage copyOrMoveToFolderInRepositoryCancel(WebDrone drone, String sourceFolder, String[] destinationFolders, boolean isCopy)
    {

        CopyOrMoveContentPage copyOrMoveContentPage = copyOrMoveToFolderInRepository(drone, sourceFolder, destinationFolders, isCopy);

        RepositoryPage repoPage = copyOrMoveContentPage.selectCancelButton().render();

        return repoPage;
    }

    /**
     * Selects ok button
     * 
     * @param drone
     * @param sourceFolder
     * @param destinationFolders
     * @param isCopy
     * @return
     */
    public static RepositoryPage copyOrMoveToFolderInRepositoryOk(WebDrone drone, String sourceFolder, String[] destinationFolders, boolean isCopy)
    {

        CopyOrMoveContentPage copyOrMoveContentPage = copyOrMoveToFolderInRepository(drone, sourceFolder, destinationFolders, isCopy);

        RepositoryPage repoPage = copyOrMoveContentPage.selectOkButton().render();

        return repoPage;
    }

    /**
     * Creates a new folder at the Path specified, Starting from the Re. Assumes
     * User is logged in and a specific Site is open.
     * 
     * @param driver
     *            WebDrone Instance
     * @param folderName
     *            String Name of the folder to be created
     * @param folderDesc
     *            String Description of the folder to be created
     * @param parentFolderPath
     *            String Path for the folder to be created, under
     *            DocumentLibrary : such as ConstRepo + file.seperator +
     *            parentFolderName1 + file.seperator + parentFolderName2
     * @throws Excetion
     */
    public static void createFolderInFolderInRepository(WebDrone driver, String folderName, String folderDesc, String parentFolderPath) throws Exception
    {
        ShareUser.createFolderInFolder(driver, folderName, folderDesc, parentFolderPath);
    }

    /**
     * Navigates to the Path specified, Starting from the Repository Page.
     * Assumes User is logged in and a specific Site is open.
     * 
     * @param fileName
     * @param parentFolderPath
     *            : such as Repository + file.seperator + parentFolderName1
     * @throws SkipException
     *             if error in this API
     */
    public static RepositoryPage uploadFileInFolderInRepository(WebDrone driver, String[] fileInfo) throws Exception
    {
        Integer argCount = fileInfo.length;
        if (argCount < 1)
        {
            throw new IllegalArgumentException("Specify at least Filename");
        }
        else if (argCount == 1)
        {
            fileInfo[1] = REPO;
        }
        
        openRepository(driver);
        RepositoryPage repositoryPage = ((RepositoryPage) ShareUser.uploadFileInFolder(driver, fileInfo)).render();
        return repositoryPage;
    }

    /**
     * This method is used to create content with name, title and description.
     * User should be logged in
     * 
     * @param drone
     * @param contentDetails
     * @param contentType
     * @return {@link RepositoryPage}
     * @throws Exception
     */
    public static RepositoryPage createContentInFolder(WebDrone drone, ContentDetails contentDetails, ContentType contentType, String... folderPath)
            throws Exception
    {
        // Open Folder in repository Library
        RepositoryPage repositoryPage = navigateFoldersInRepositoryPage(drone, folderPath);
        DocumentDetailsPage detailsPage = null;

        try
        {
            CreatePlainTextContentPage contentPage = repositoryPage.getNavigation().selectCreateContent(contentType).render();
            detailsPage = contentPage.create(contentDetails).render();
            repositoryPage = detailsPage.navigateToFolderInRepositoryPage().render();
        }
        catch (Exception e)
        {
            throw new SkipException("Error in creating content." + e);
        }

        return repositoryPage;
    }

    /**
     * This method is used to create content with name, title and description.
     * User should be logged in
     * 
     * @param drone
     * @param contentDetails
     * @param contentType
     * @return {@link RepositoryPage}
     * @throws Exception
     */
    public static HtmlPage createContentInFolderWithValidation(WebDrone drone, ContentDetails contentDetails, ContentType contentType, String... folderPath)
            throws Exception
    {
        // Open Folder in repository Library
        RepositoryPage repositoryPage = navigateFoldersInRepositoryPage(drone, folderPath);

        try
        {
            CreatePlainTextContentPage contentPage = repositoryPage.getNavigation().selectCreateContent(contentType).render();

            HtmlPage page = contentPage.createWithValidation(contentDetails).render();
            
            if(page instanceof DocumentDetailsPage)
            {
                DocumentDetailsPage detailsPage = page.render();
                repositoryPage = detailsPage.navigateToFolderInRepositoryPage().render();
                
                return repositoryPage;
            }
            return page;
        }
        catch (Exception e)
        {
            throw new SkipException("Error in creating content." + e.getMessage());
        }
    }

    /**
     * Method to navigate to site dashboard url, based on siteshorturl, rather
     * than sitename This is to be used to navigate only as a util, not to test
     * getting to the site dashboard
     * 
     * @param drone
     * @param siteShortURL
     * @return {@link SiteDashBoardPage
     *
     */
    public static RepositoryPage openSiteFromSitesFolderOfRepository(WebDrone drone, String siteName)
    {
        String url = drone.getCurrentUrl();      
        //http://127.0.0.1:8081/share  /page/repository#filter=path|%2FUser%2520Homes%2F userEnterprise42-5405%40freetht1.test-1  |&page=1
        String target = url.substring(0, url.indexOf("/page/")) + "/page/repository#filter=path|%2FSites%2F" + SiteUtil.getSiteShortname(siteName)
                + "|&page=1";
        
        drone.navigateTo(target);
        drone.waitForPageLoad(maxWaitTime);
        RepositoryPage repoPage = (RepositoryPage) ShareUser.getSharePage(drone);

        return repoPage.render();
    }
    
    public static RepositoryPage openUserFromUserHomesFolderOfRepository(WebDrone drone, String usrName)
    {
        String url = drone.getCurrentUrl();      
        //http://127.0.0.1:8081/share  /page/repository#filter=path|%2FUser%2520Homes%2F userEnterprise42-5405%40freetht1.test-1  |&page=1
        String target = url.substring(0, url.indexOf("/page/")) + "/page/repository#filter=path|%2FUser%2520Homes%2F" + StringUtils.replace(usrName, "@", "%40")
                + "|&page=1";
        
        drone.navigateTo(target);
        drone.waitForPageLoad(maxWaitTime);
        RepositoryPage repoPage = (RepositoryPage) ShareUser.getSharePage(drone);

        return repoPage.render();
    }

    /**
     * method to Navigate folder
     * 
     * @param drone
     * @param List
     *            of Folders
     */
    public static RepositoryPage navigateFoldersInRepositoryPage(WebDrone drone, String... folderPath)
    {
        boolean selected = false;
        if (folderPath == null || folderPath.length < 1)
        {
            throw new IllegalArgumentException("Invalid Folder path!!");
        }

        @SuppressWarnings("unused")
        List<FileDirectoryInfo> folderNames;
        RepositoryPage repoPage = (RepositoryPage) ShareUser.getSharePage(drone);

        try
        {

            for (String folder : folderPath)
            {
                repoPage.selectFolder(folder).render();
                selected = true;
                logger.info("Folder \"" + folder + "\" selected");

            }
            if (!selected)
            {
                throw new ShareException("Cannot select the folder metioned in the path");
            }
        }
        catch (Exception e)
        {
            throw new ShareException("Cannot select the folder metioned in the path");
        }

        return (RepositoryPage) ShareUser.getSharePage(drone);

    }

    /**
     * @param drone
     * @param folderName
     * @param properString
     * @param doSave
     * @return
     */
    public static RepositoryPage editContentProperties(WebDrone drone, String folderName, String properString, boolean doSave)
    {
        return ((RepositoryPage) ShareUserSitePage.editContentProperties(drone, folderName, properString, doSave)).render();
    }

    /**
     * Add given tags to file or folder in {@link TagPage}.
     * Assume that user currently in {@link RepositoryPage}.
     * 
     * @param drone
     * @param contentName
     * @param tags - Tags to be added to content
     * @return {@link RepositoryPage}
     */
    public static RepositoryPage addTagsInRepo(WebDrone drone, String contentName, List<String> tags)
    {        
        DetailsPage detailsPage = ShareUserSitePage.addTags(drone, contentName, tags).render();
        RepositoryPage repoPage = detailsPage.navigateToParentFolder().render();        
        
        return repoPage;
    }
    
    /**
     * Opens the content details page starting from the parent folder within DocumentLibrary
     * Assume that user currently in {@link RepositoryPage}.
     * 
     * @param drone
     * @param contentName
     * @param isFile
     * @return DetailsPage
     */
    public static DetailsPage getContentDetailsPage(WebDrone drone, String contentName)
    {
        return ShareUserSitePage.getContentDetailsPage(drone, contentName);
    }

    public static DocumentDetailsPage uploadNewVersionFromDocDetail(WebDrone drone, boolean majorVersion, String fileName, String comments)
    {
        return ShareUserSitePage.uploadNewVersionFromDocDetail(drone, majorVersion, fileName, comments);
    }
}
