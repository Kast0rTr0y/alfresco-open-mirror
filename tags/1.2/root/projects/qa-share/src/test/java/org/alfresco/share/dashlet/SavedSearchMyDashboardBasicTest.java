/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.share.dashlet;

import static java.util.Arrays.asList;
import static org.alfresco.po.share.enums.DataLists.CONTACT_LIST;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.dashlet.ConfigureSavedSearchDialogBoxPage;
import org.alfresco.po.share.dashlet.SavedSearchDashlet;
import org.alfresco.po.share.dashlet.SearchLimit;
import org.alfresco.po.share.dashlet.SiteSearchItem;
import org.alfresco.po.share.enums.Dashlets;
import org.alfresco.po.share.site.CustomizeSitePage;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.SitePageType;
import org.alfresco.po.share.site.blog.BlogPage;
import org.alfresco.po.share.site.calendar.CalendarPage;
import org.alfresco.po.share.site.calendar.EditEventForm;
import org.alfresco.po.share.site.calendar.InformationEventForm;
import org.alfresco.po.share.site.datalist.DataListPage;
import org.alfresco.po.share.site.discussions.DiscussionsPage;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.EditDocumentPropertiesPage;
import org.alfresco.po.share.site.links.LinksPage;
import org.alfresco.po.share.site.wiki.WikiPage;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserDashboard;
import org.alfresco.share.util.ShareUserSitePage;
import org.alfresco.share.util.api.CreateUserAPI;
import org.alfresco.test.FailedTestListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


/**
 * @author Ranjith Manyam
 */
@Listeners(FailedTestListener.class)
public class SavedSearchMyDashboardBasicTest extends AbstractUtils {

    private static Log logger = LogFactory.getLog(SavedSearchMyDashboardBasicTest.class);
    private String siteDomain = "savedSearch.test";
    private String expectedHelpBallonMsg = "Use this dashlet to set up a search and view the results.\n"
            + "Configure the dashlet to save the search and set the title text of the dashlet.\n"
            + "Only a Site Manager can configure the search and title - this dashlet is ideal for generating report views in a site.";
    public static final String BALLOON_TEXT_VALUE_NOT_EMPTY = "The value cannot be empty.";

    private static final SitePageType[] PAGE_TYPES = {
            SitePageType.BLOG,
            SitePageType.CALENDER,
            SitePageType.DATA_LISTS,
            SitePageType.DISCUSSIONS,
            SitePageType.LINKS,
            SitePageType.WIKI
    };

    @Override
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.setup();
        testName = this.getClass().getSimpleName();
        logger.info("Start Tests in: " + testName);
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14650() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Add Saved Search Dashlet
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);

        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14650() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);

        DashBoardPage dashBoardPage = (DashBoardPage) ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Verify Saved search Dashlet has been added to the Site dashboard
        Assert.assertNotNull(dashBoardPage);
        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        Assert.assertNotNull(searchDashlet);
        Assert.assertEquals(searchDashlet.getTitle(), Dashlets.SAVED_SEARCH.getDashletName());
        Assert.assertEquals(searchDashlet.getContent(), "No results found.");

        // Verify Help balloon message has been displayed correctly
        searchDashlet.clickOnHelpIcon();
        Assert.assertTrue(searchDashlet.isBalloonDisplayed());
        Assert.assertEquals(searchDashlet.getHelpBalloonMessage(), expectedHelpBallonMsg);
        searchDashlet.closeHelpBallon().render();
        Assert.assertFalse(searchDashlet.isBalloonDisplayed());

        // Verify expected Configure Saved search elements are displayed
        ConfigureSavedSearchDialogBoxPage configureSavedSearchPage = searchDashlet.clickOnEditButton().render();
        configureSavedSearchPage.clickOnCloseButton().render();
        searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        Assert.assertNotNull(searchDashlet);
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14651() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String contentPrefix = testName + "-Test-";

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);

        // TODO: For quicker file creation, consider using
        // ShareUser.createCopyOfAllContent(drone);
        // Upload 30 Files
        for (int i = 1; i <= 30; i++)
        {
            String fileName = contentPrefix + getFileName(testName) + "-" + i + ".txt";
            String[] fileInfo = {fileName, DOCLIB};
            ShareUser.uploadFileInFolder(drone, fileInfo);
        }

        // Add Saved Search Dashlet
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14651() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String savedSearchTitle = "SiteDocs";
        String contentPrefix = testName + "-Test-";

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Configure Saved Search dashlet with "Test", set the SearchLimit to 10
        // and verify Correct number of results displayed
        ShareUserDashboard.configureSavedSearch(drone, contentPrefix, savedSearchTitle, SearchLimit.TEN);

        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);

        List<SiteSearchItem> searchResults = searchDashlet.getSearchItems();
        for (SiteSearchItem result : searchResults)
        {
            Assert.assertTrue(result.getItemName().getDescription().startsWith(contentPrefix));
        }
        Assert.assertEquals(searchDashlet.getTitle(), savedSearchTitle);
        Assert.assertEquals(searchResults.size(), SearchLimit.TEN.getValue());

        // Increase the limit to 25 and verify the number of results
        ConfigureSavedSearchDialogBoxPage configureSavedSearchDialogBoxPage = searchDashlet.clickOnEditButton().render();
        configureSavedSearchDialogBoxPage.setSearchLimit(SearchLimit.TWENTY_FIVE);
        configureSavedSearchDialogBoxPage.clickOnOKButton().render();

        searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);

        searchResults = searchDashlet.getSearchItems();
        for (SiteSearchItem result : searchResults)
        {
            Assert.assertTrue(result.getItemName().getDescription().startsWith(contentPrefix));
        }
        Assert.assertEquals(searchDashlet.getTitle(), savedSearchTitle);
        Assert.assertEquals(searchResults.size(), SearchLimit.TWENTY_FIVE.getValue());
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14652() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String contentPrefix = "Test-";

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        // Upload File
        String fileName = contentPrefix + getFileName(testName) + "-.txt";
        String[] fileInfo = {fileName, DOCLIB};
        ShareUser.uploadFileInFolder(drone, fileInfo);
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14652() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String searchTerm = "Test-";
        String savedSearchTitle = "SiteDocs";

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);

        // Saved Search details before configure
        Assert.assertNotNull(searchDashlet);
        Assert.assertEquals(searchDashlet.getTitle(), Dashlets.SAVED_SEARCH.getDashletName());
        Assert.assertEquals(searchDashlet.getContent(), "No results found.");

        // Configure Saved Search and Cancel
        ConfigureSavedSearchDialogBoxPage configureSavedSearchPage = searchDashlet.clickOnEditButton().render();
        configureSavedSearchPage.setSearchTerm(searchTerm);
        configureSavedSearchPage.setTitle(savedSearchTitle);
        configureSavedSearchPage.setSearchLimit(SearchLimit.TEN);
        configureSavedSearchPage.clickOnCancelButton().render();

        // Verify Saved Search dashlet after cancel
        searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        Assert.assertNotNull(searchDashlet);
        Assert.assertEquals(searchDashlet.getTitle(), Dashlets.SAVED_SEARCH.getDashletName());
        Assert.assertEquals(searchDashlet.getContent(), "No results found.");
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14653() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        // Upload File
        String fileName = getFileName(testName) + "-.txt";
        String[] fileInfo = {fileName, DOCLIB};
        ShareUser.uploadFileInFolder(drone, fileInfo);
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14653() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String title = "£$%^&";
        String searchTerm = title;

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Configure search with wild chars
        ShareUserDashboard.configureSavedSearch(drone, searchTerm, title, SearchLimit.TEN);
        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);

        Assert.assertNotNull(searchDashlet);
        Assert.assertEquals(searchDashlet.getTitle(), title);
        Assert.assertEquals(searchDashlet.getContent(), "No results found.");

        title = ShareUser.getRandomStringWithNumders(2100);
        searchTerm = title;

        // Configure search with more than 2048 chars
        ShareUserDashboard.configureSavedSearch(drone, searchTerm, title, SearchLimit.TEN);
        searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);

        Assert.assertNotNull(searchDashlet);
        Assert.assertEquals(searchDashlet.getTitle().length(), 2048);
        Assert.assertEquals(searchDashlet.getTitle(), title.substring(0, 2048));
        Assert.assertEquals(searchDashlet.getContent(), "No results found.");
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14654() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String fileName = getFileName(testName) + "-.txt";
        String[] fileInfo = {fileName, DOCLIB};

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.uploadFileInFolder(drone, fileInfo);
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14654() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String fileName = getFileName(testName) + "-.txt";
        String searchTerm_Empty = "";
        String searchTerm_Spaces = "   ";
        String searchTerm_Valid = testName;

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SavedSearchDashlet savedSearchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);

        // Configure Saved Search with blank Search term and verify
        ConfigureSavedSearchDialogBoxPage configureSavedSearchPage = savedSearchDashlet.clickOnEditButton().render();
        configureSavedSearchPage.setSearchTerm(searchTerm_Empty);
        configureSavedSearchPage = configureSavedSearchPage.clickOnOKButton().render();
        Assert.assertTrue(configureSavedSearchPage.isHelpBalloonDisplayed());
        Assert.assertEquals(configureSavedSearchPage.getHelpBalloonMessage(), BALLOON_TEXT_VALUE_NOT_EMPTY);

        // Configure Saved Search with search term as spaces
        configureSavedSearchPage.setSearchTerm(searchTerm_Spaces);
        configureSavedSearchPage = configureSavedSearchPage.clickOnOKButton().render();
        Assert.assertTrue(configureSavedSearchPage.isHelpBalloonDisplayed());
        Assert.assertEquals(configureSavedSearchPage.getHelpBalloonMessage(), BALLOON_TEXT_VALUE_NOT_EMPTY);

        // Configure Saved Search with valid data
        configureSavedSearchPage.setSearchTerm(searchTerm_Valid);
        configureSavedSearchPage.clickOnOKButton().render();
        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);

        Assert.assertNotNull(searchDashlet);
        Assert.assertEquals(searchDashlet.getTitle(), "Saved Search");
        Assert.assertEquals(searchDashlet.getSearchItems().size(), 1);
        Assert.assertEquals(searchDashlet.getSearchItems().get(0).getItemName().getDescription(), fileName);
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14655() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);

        String content = "content";
        String test = "end";

        String fileName = content + testName + test + ".doc";
        String[] fileInfo = {fileName, DOCLIB};

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.uploadFileInFolder(drone, fileInfo);

        SiteDashboardPage siteDashboardPage = ShareUser.openSiteDashboard(drone, siteName).render();
        CustomizeSitePage customizeSitePage = siteDashboardPage.getSiteNav().selectCustomizeSite();
        customizeSitePage.addPages(asList(PAGE_TYPES));

        // Create Wiki Page
        WikiPage wikiPage = siteDashboardPage.getSiteNav().selectWikiPage().render();

        List<String> txtLines = new ArrayList<>();
        txtLines.add(0, content + "wiki" + test);
        wikiPage.createWikiPage(content + "wiki" + test, txtLines).render();

        // Create Blog Post
        BlogPage blogPage = siteDashboardPage.getSiteNav().selectBlogPage().render();
        blogPage.createPostInternally(content + "blog" + test, content + "blog" + test).render();

        // Create any event
        CalendarPage calendarPage = siteDashboardPage.getSiteNav().selectCalendarPage().render();
        calendarPage.createEvent(content + "event" + test, content + "event" + test, content + "event" + test, true).render();

        // Create Data List
        siteDashboardPage.getSiteNav().selectDataListPage();
        DataListPage dataListPage = new DataListPage(drone).render();
        dataListPage.createDataList(CONTACT_LIST, content + "dataList" + test, content + "dataList" + test).render();

        //Create a topic
        DiscussionsPage discussionsPage = siteDashboardPage.getSiteNav().selectDiscussionsPage().render();
        discussionsPage.createTopic(content + "discussion" + test, content + "discussion" + test).render();

        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14655() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String content = "content";
        String test = "end";
        String fileName = content + testName + test + ".doc";
        String title = "Saved Search";


        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        ShareUserDashboard.configureSavedSearch(drone, content + "*", title, SearchLimit.HUNDRED);
        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        List<SiteSearchItem> items = searchDashlet.getSearchItems();

        // Configure Saved search with "content*"
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "wiki" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "blog" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "event" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "dataList" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "discussion" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));

        // Configure Saved search with "*test"
        ShareUserDashboard.configureSavedSearch(drone, "*" + test, title, SearchLimit.HUNDRED);
        searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        items = searchDashlet.getSearchItems();

        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "wiki" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "blog" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "event" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "dataList" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "discussion" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));

        // Configure Saved search with "*test*"
        ShareUserDashboard.configureSavedSearch(drone, "*" + test + "*", title, SearchLimit.HUNDRED);
        searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        items = searchDashlet.getSearchItems();

        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "wiki" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "blog" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "event" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "dataList" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, content + "discussion" + test));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14656() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);


        String fileName = testName + ".doc";
        String[] fileInfo = {fileName, DOCLIB};

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.uploadFileInFolder(drone, fileInfo);

        SiteDashboardPage siteDashboardPage = ShareUser.openSiteDashboard(drone, siteName).render();
        CustomizeSitePage customizeSitePage = siteDashboardPage.getSiteNav().selectCustomizeSite();
        customizeSitePage.addPages(asList(PAGE_TYPES));

        // Create Wiki Page
        WikiPage wikiPage = siteDashboardPage.getSiteNav().selectWikiPage().render();

        List<String> txtLines = new ArrayList<>();
        txtLines.add(0, testName + "wiki");
        wikiPage.createWikiPage(testName + "wiki", txtLines).render();

        // Create Blog Post
        BlogPage blogPage = siteDashboardPage.getSiteNav().selectBlogPage().render();
        blogPage.createPostInternally(testName + "blog", testName + "blog").render();

        // Create any event
        CalendarPage calendarPage = siteDashboardPage.getSiteNav().selectCalendarPage().render();
        calendarPage.createEvent(testName + "event", testName + "event", testName + "event", true).render();

        // Create Data List
        siteDashboardPage.getSiteNav().selectDataListPage();
        DataListPage dataListPage = new DataListPage(drone).render();
        dataListPage.createDataList(CONTACT_LIST, testName + "dataList", testName + "dataList").render();

        //Create a topic
        DiscussionsPage discussionsPage = siteDashboardPage.getSiteNav().selectDiscussionsPage().render();
        discussionsPage.createTopic(testName + "discussion", testName + "discussion").render();

        //Create a link
        LinksPage linksPage = siteDashboardPage.getSiteNav().selectLinksPage().render();
        String url = getRandomString(7);
        linksPage.createLink(testName + "link", url).render();

        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14656() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String fileName = testName + ".doc";


        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Configure Saved search with Title(Name)
        List<SiteSearchItem> items = ShareUserDashboard.searchSavedSearchDashlet(drone, testName);
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "wiki"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "blog"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "event"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "dataList"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "discussion"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "link"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14657() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String description = testName + 1;


        String fileName = testName + ".doc";

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.openDocumentLibrary(drone);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName);
        contentDetails.setDescription(description);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

        SiteDashboardPage siteDashboardPage = ShareUser.openSiteDashboard(drone, siteName).render();
        CustomizeSitePage customizeSitePage = siteDashboardPage.getSiteNav().selectCustomizeSite();
        customizeSitePage.addPages(asList(PAGE_TYPES));

        // Create Wiki Page
        WikiPage wikiPage = siteDashboardPage.getSiteNav().selectWikiPage().render();

        List<String> txtLines = new ArrayList<>();
        txtLines.add(0, description);
        wikiPage.createWikiPage(testName + "wiki", txtLines).render();

        // Create Blog Post
        BlogPage blogPage = siteDashboardPage.getSiteNav().selectBlogPage().render();
        blogPage.createPostInternally(testName + "blog", description).render();

        // Create any event
        CalendarPage calendarPage = siteDashboardPage.getSiteNav().selectCalendarPage().render();
        calendarPage.createEvent(testName + "event", description, description, true).render();

        // Create Data List
        siteDashboardPage.getSiteNav().selectDataListPage();
        DataListPage dataListPage = new DataListPage(drone).render();
        dataListPage.createDataList(CONTACT_LIST, testName + "dataList", description).render();

        //Create a topic
        DiscussionsPage discussionsPage = siteDashboardPage.getSiteNav().selectDiscussionsPage().render();
        discussionsPage.createTopic(testName + "discussion", description).render();

        //Create a link
        LinksPage linksPage = siteDashboardPage.getSiteNav().selectLinksPage().render();
        String url = getRandomString(7);
        linksPage.createLink(testName + "link", url, description, description).render();

        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14657() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String fileName = testName + ".doc";
        String description = testName + 1;


        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Configure Saved search with Description
        List<SiteSearchItem> items = ShareUserDashboard.searchSavedSearchDashlet(drone, description);
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "wiki"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "blog"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "event"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "dataList"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "discussion"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "link"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14658() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String tag = testName + 2;

        String fileName = testName + ".doc";

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.openDocumentLibrary(drone);

        // Create File
        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName);
        ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

        ShareUser.openDocumentLibrary(drone);
        ShareUserSitePage.addTag(drone, fileName, tag);

        SiteDashboardPage siteDashboardPage = ShareUser.openSiteDashboard(drone, siteName).render();
        CustomizeSitePage customizeSitePage = siteDashboardPage.getSiteNav().selectCustomizeSite();
        customizeSitePage.addPages(asList(PAGE_TYPES));

        // Create Wiki Page
        WikiPage wikiPage = siteDashboardPage.getSiteNav().selectWikiPage().render();

        List<String> txtLines = new ArrayList<>();
        txtLines.add(0, testName + "wiki");
        List<String> tagLines = new ArrayList<>();
        txtLines.add(0, tag);
        wikiPage.createWikiPage(testName + "wiki", txtLines, tagLines).render();

        // Create Blog Post
        BlogPage blogPage = siteDashboardPage.getSiteNav().selectBlogPage().render();
        blogPage.createPostInternally(testName + "blog", testName + "blog", tag).render();

        // Create any event
        CalendarPage calendarPage = siteDashboardPage.getSiteNav().selectCalendarPage().render();
        calendarPage = (CalendarPage) calendarPage.chooseMonthTab();

        calendarPage = calendarPage.createEvent(testName + "event", testName + "event", testName + "event", false);

        InformationEventForm informationEventForm = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_SINGLE_EVENT, testName + "event");
        EditEventForm editEventForm = informationEventForm.clickOnEditEvent();
        editEventForm.setTagsField(tag);
        editEventForm.clickAddTag();
        editEventForm.clickSave();

        //Create a topic
        DiscussionsPage discussionsPage = siteDashboardPage.getSiteNav().selectDiscussionsPage().render();
        discussionsPage.createTopic(testName + "discussion", testName + "discussion", tag).render();

        //Create a link
        LinksPage linksPage = siteDashboardPage.getSiteNav().selectLinksPage().render();
        String url = getRandomString(7);
        linksPage.createLink(testName + "link", url, testName + "link", tag).render();

        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14658() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String fileName = testName + ".doc";
        String tag = testName + 2;


        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Configure Saved search with tag name
        List<SiteSearchItem> items = ShareUserDashboard.searchSavedSearchDashlet(drone, tag);
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "wiki"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "blog"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "event"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "discussion"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, testName + "link"));
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14659() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String fileName = testName + ".doc";
        String[] fileInfo = {fileName, DOCLIB};

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.uploadFileInFolder(drone, fileInfo);
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14659() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String fileName = testName + ".doc";
        String title = "Saved Search";

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Configure Saved search with file name
        List<SiteSearchItem> items = ShareUserDashboard.searchSavedSearchDashlet(drone, testName);
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));

        // Configure Saved search with file extension ".doc"
        ShareUserDashboard.configureSavedSearch(drone, "doc", title, SearchLimit.FIFTY);
        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        items = searchDashlet.getSearchItems();
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14660() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String fileName = testName;
        String[] dots = {".pdf", ".xml", ".html", ".txt", ".eml", ".odp", ".ods", ".odt", ".xls", ".xlsx", ".xsl", ".doc", ".docx", ".ppt",
                ".pptx", ".pot", ".xsd", ".js", ".java", ".css", ".rtf", ".msg"};


        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        for (String dot : dots)
        {
            String[] fileInfo = {fileName + dot, DOCLIB};
            ShareUser.uploadFileInFolder(drone, fileInfo);
        }
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14660() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String fileName = testName;
        String[] dots = {".pdf", ".xml", ".html", ".txt", ".eml", ".odp", ".ods", ".odt", ".xls", ".xlsx", ".xsl", ".doc", ".docx", ".ppt",
                ".pptx", ".pot", ".xsd", ".js", ".java", ".css", ".rtf", ".msg"};
        String title = "Saved Search";

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Configure Saved search with file name
        ShareUserDashboard.configureSavedSearch(drone, testName, title, SearchLimit.FIFTY);
        SavedSearchDashlet searchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        List<SiteSearchItem> items = searchDashlet.getSearchItems();


        for (String dot : dots)
        {
            Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName + dot));
        }

    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14661() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = "12345" + getSiteName(testName);
        String folderName = testName + "-Folder";

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUserSitePage.createFolder(drone, folderName, folderName);
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14661() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String folderName = testName + "-Folder";

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        // Configure Saved search with Folder Name
        List<SiteSearchItem> items = ShareUserDashboard.searchSavedSearchDashlet(drone, folderName);
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, folderName));
        ShareUser.logout(drone);
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14662() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        // Add Saved Search Dashlet
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14662() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String fileName = getFileName(testName) + System.currentTimeMillis() + "_1.1.odt";
        String[] fileInfo = {fileName, DOCLIB};

        ContentDetails contentDetails = new ContentDetails();
        contentDetails.setName(fileName);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        // Create site and upload document
        ShareUser.openSiteDashboard(drone, siteName);
        DocumentLibraryPage documentLibraryPage = ShareUser.uploadFileInFolder(drone, fileInfo);
        documentLibraryPage.getSiteNav().selectSiteDashBoard().render();

        // Wait till index
        Assert.assertTrue(ShareUser.searchSiteDashBoardWithRetry(drone, SITE_CONTENT_DASHLET, fileName, true, siteName));
        ShareUser.openUserDashboard(drone);
        ShareUserDashboard.configureSavedSearch(drone, ".odt");
        SavedSearchDashlet savedSearchDashlet = ShareUserDashboard.getSavedSearchDashlet(drone);
        Assert.assertTrue(savedSearchDashlet.isItemFound(fileName));

        List<SiteSearchItem> items = ShareUserDashboard.searchSavedSearchDashlet(drone, fileName);
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));

        DocumentDetailsPage detailsPage = items.get(0).getItemName().click().render();
        Assert.assertTrue(detailsPage instanceof DocumentDetailsPage);

        String newFileName = new StringBuffer(fileName).insert(4, '_').toString();
        EditDocumentPropertiesPage editDocumentPropertiesPage = detailsPage.selectEditProperties().render();
        editDocumentPropertiesPage.setName(newFileName);
        editDocumentPropertiesPage.selectSave().render();

        ShareUser.openUserDashboard(drone);

        items = ShareUserDashboard.searchSavedSearchDashlet(drone, ".odt", SearchLimit.HUNDRED);
        Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, newFileName));
    }

    @Test(groups = {"DataPrepEnterpriseOnly"})
    public void dataPrep_AONE_14663() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String siteName = getSiteName(testName);
        String fileName = getFileName(testName) + ".txt";
        String[] fileInfo = {fileName, DOCLIB, "Les études de l'admissibilité de la solution dans cette société financière."};

        String[] testUserInfo = new String[]{testUser};
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        ShareUserDashboard.addDashlet(drone, Dashlets.SAVED_SEARCH);
        ShareUser.createSite(drone, siteName, SITE_VISIBILITY_PUBLIC);
        ShareUser.uploadFileInFolder(drone, fileInfo);
        ShareUser.logout(drone);
    }

    @Test(groups = {"EnterpriseOnly"})
    public void AONE_14663() throws Exception {
        String testName = getTestName();
        String testUser = getUserNameForDomain(testName, siteDomain);
        String fileName = getFileName(testName) + ".txt";
        String[] searchTerms = {"l'admissibilité", "l'admissibilite", "études", "etudes", "financière", "financiere"};

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        List<SiteSearchItem> items;
        // Loop through the search terms and verify the item is found
        for (String searchTerm : searchTerms)
        {
            items = ShareUserDashboard.searchSavedSearchDashlet(drone, searchTerm, SearchLimit.TWENTY_FIVE);
            Assert.assertTrue(ShareUserDashboard.isContentDisplayedInSearchResults(items, fileName));
        }
    }
}
