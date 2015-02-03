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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.alfresco.application.windows.MicorsoftOffice2010;
import org.alfresco.po.share.CustomiseUserDashboardPage;
import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.dashlet.MyCalendarDashlet;
import org.alfresco.po.share.dashlet.SiteCalendarDashlet;
import org.alfresco.po.share.enums.Dashlets;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.SiteFinderPage;
import org.alfresco.po.share.site.calendar.CalendarPage;
import org.alfresco.po.share.site.calendar.CalendarPage.ActionEventVia;
import org.alfresco.po.share.site.calendar.InformationEventForm;
import org.alfresco.test.FailedTestListener;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.SiteUtil;
import org.alfresco.share.util.api.CreateUserAPI;
import org.alfresco.utilities.Application;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.cobra.ldtp.Ldtp;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

/**
 * @author bogdan.bocancea
 */

@Listeners(FailedTestListener.class)
public class MSOutlook2010EventsTests extends AbstractUtils
{
    MicorsoftOffice2010 outlook = new MicorsoftOffice2010(Application.OUTLOOK, "2010");

    private CustomiseUserDashboardPage customizeUserDash;
    private DashBoardPage dashBoard;
    private String sharePointPath;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        super.setup();

        Runtime.getRuntime().exec("taskkill /F /IM OUTLOOK.EXE");

        sharePointPath = outlook.getSharePointPath();
    }

    @AfterMethod(alwaysRun = true)
    public void teardownMethod() throws Exception
    {
        Runtime.getRuntime().exec("taskkill /F /IM OUTLOOK.EXE");
        Runtime.getRuntime().exec("taskkill /F /IM CobraWinLDTP.EXE");
    }

    /**
     * AONE-9704:Event info window
     */
    @Test(groups = "alfresco-one")
    public void AONE_9704() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName), "Event is missing");

        // ---- Step 1 ----
        // ---- Step Action -----
        // Click the event's name link;
        // Expected Result
        // Event Information window is opened;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();

        // ---- Step 2 ----
        // ---- Step Action -----
        // Verify event's information;
        // Expected Result
        // There are next fields are present:
        /**
         * Details part
         * - What field (marked as mandatory);
         * - Where text field;
         * - Description test field;
         * - Tags associated with the document;
         * Time part:
         * - Start Date ;
         * - End Date;
         * - Recurrence;
         */
        Assert.assertEquals(eventInfo.getWhatDetail(), siteName, "What field doesn't contain:" + siteName);
        Assert.assertEquals(eventInfo.getWhereDetail(), location, "Where detail doesn't contain " + location);
        Assert.assertTrue(eventInfo.getDescriptionDetail().isEmpty(), "Description is not empty");
        Assert.assertEquals(eventInfo.getTagName(), "(None)", "Tag name has data");
        Assert.assertFalse(eventInfo.getStartDateTime().isEmpty(), "Start date time is empty");
        Assert.assertFalse(eventInfo.getEndDateTime().isEmpty(), "End date time is empty");

    }

    /**
     * AONE-9705:Deleting event
     */
    @Test(groups = "alfresco-one")
    public void AONE_9705() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName_meeting = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // ---- Pre-conditions: -----
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);
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
        l4.enterString("txtLocation", location);
        l4.enterString("txtSubject", siteName_meeting);

        // Click "create" button;
        l4.click("btnCreate");
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);
        outlook.operateOnSecurity(security, testUser, DEFAULT_PASSWORD);

        // login to share
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName_meeting);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName_meeting), "Event is missing");

        // ---- Step 1 ----
        // ---- Step Action -----
        // Click the event's name;
        // ---- Expected result ----
        // Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName_meeting).render();

        // ---- Step 2 -----
        // ---- Step Action -----
        // Click Delete button;
        // ---- Expected result ----
        // Delete button is disabled; Event is not deleted;
        Assert.assertFalse(eventInfo.isDeleteButtonEnabled(), "Delete button is enabled");
        eventInfo.clickClose();

        // ---- Step 3 -----
        // ---- Step Action -----
        // Delete Appointment via Outlook;
        // ---- Expected result ----
        // Appointment is deleted successfully;
        Ldtp remove = outlook.getAbstractUtil().setOnWindow(siteName_meeting);
        remove.doubleClick("btnRemove");
        Ldtp l_error = outlook.getAbstractUtil().setOnWindow("Microsoft Outlook");
        l_error.click("btnYes");

        // login to share
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // ---- Step 4 -----
        // ---- Step Action -----
        // Open Calendar in Share client for the workspace;
        // ---- Expected result ----
        // Calendar is opened in Share;
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName_meeting);
        calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        calendarPage.chooseMonthTab().render();

        // ---- Step 5 -----
        // ---- Step Action -----
        // Verify the presence of recently deleted event ;
        // ---- Expected result ----
        // Event is absent in Calendar;
        Assert.assertFalse(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName_meeting), "Event is displayed "
                + siteName_meeting);
    }

    /**
     * AONE-9706:Editing event via Outlook. Send updates
     */
    @Test(groups = "alfresco-one")
    public void AONE_9706() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l3 = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l3, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        // login to share
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName), "Event is missing");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        String end_date = eventInfo.getEndDateTime();
        eventInfo.closeInformationForm();

        // ---- Step 1 -----
        // ---- Step Action -----
        // Expand\collapse event's duration;
        // ---- Expected result ----
        // Event's duration and time boundaries are changed; New value is set; Microsoft Outlook window pops up; It notifies about time of the meeteing has
        // changed and and offers sending updates or no.
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.enterString("txtTo", testUser);

        // add 1 day to current date
        SimpleDateFormat FormattedDATE = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        String tommorrow = (String) (FormattedDATE.format(c.getTime()));
        l1.deleteText("txtEnddate", 0);
        l1.enterString("txtEnddate", tommorrow);
        outlook.exitOfficeApplication(l1);

        // ---- Step 2,3,4 -----
        // ---- Step Action -----
        // Select Save Changes and Send update radio button and click OK button;
        // ---- Expected result ----
        // Save Changes and Save update radio button is selected; Microsoft Outlook windows closed;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow("Microsoft Outlook");
        l2.click("rbtnSavechangesandsendmeeting");
        l2.click("btnOK");

        // ---- Step 5, 6 -----
        // ---- Step Action -----
        // Log in Share as any user;Open Calendar tab for the workspace;
        // ---- Expected result ----
        // Calendar tab is opened for the workspace;
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);
        calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();

        // ---- Step 7 -----
        // ---- Step Action ----
        // Click the event's name and verify time and name were changed;
        // ---- Expected result ----
        // Event Info window is displayed; Event's details have changed;
        eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        String updated_endDate = eventInfo.getEndDateTime();
        Assert.assertNotEquals(updated_endDate, end_date, "End date isn't updated");
    }

    /**
     * AONE-9707:Editing event to recurrence via Outlook
     */
    @Test(groups = "alfresco-one")
    public void AONE_9707() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // login to share
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName), "Event is missing");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        Assert.assertFalse(eventInfo.isRecurrencePresent(), "Reccurence is displayed");
        eventInfo.closeInformationForm();

        calendarPage.chooseAgendaTab().render(maxWaitTime);
        int event_number = calendarPage.getTheNumOfEvents(ActionEventVia.AGENDA_TAB);
        Assert.assertEquals(1, event_number, "There is more than 1 event");

        // ---- Step 1 -----
        // ---- Step Action ----
        // Click Recurrence button in Ms Outlook;
        // ---- Expected result ----
        // Appointment recurrence window is opened;
        Ldtp site = outlook.getAbstractUtil().setOnWindow(siteName);
        site.doubleClick("btnRecurrence");

        // ---- Step 2 -----
        // ---- Step Action ----
        // Select any start and end time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Set any recurrence pattern;
        // ---- Expected result ----
        // Recurrence pattern is set;
        recurrence.click("rbtnDaily");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Set any range of recurrence;
        // ---- Expected result ----
        // Range of recurrence is set;
        recurrence.click("rbtnEndafter");
        recurrence.click("btnOK");

        // ---- Step 5 -----
        // ---- Step Action ----
        // // Select Save Changes (that is not matter with Send updates or no) and click OK button;
        // ---- Expected result ----
        // Changes are saved in Outlook;
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");

        // ---- Step 6, 7 -----
        // ---- Step Action ----
        // Log in Share;
        // Verify the created event is edited successfully;
        // ---- Expected result ----
        // My Calendar dashlet event is marked as recurring, recurring events are also displayed in calendar tab of the meeting workspace with correct start
        // time and duration, recurrence pattern and the range of recurrence;
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event " + siteName + " is not repeating");
        String eventName = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventName.contains("Repeating"), "Event is not repeating");

        // open site dashboard
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // navigate to Calendar
        calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.isRecurrencePresent(), "Recurrence is not displayed");
        eventInfo.closeInformationForm();

        // recurring events are also displayed in calendar tab
        calendarPage.chooseAgendaTab().render(maxWaitTime);
        int event_reccurence = calendarPage.getTheNumOfEvents(ActionEventVia.AGENDA_TAB);
        Assert.assertEquals(10, event_reccurence, "There are more than 10 events");
    }

    /**
     * AONE-9708:Editing recurrence event via Outlook
     */
    @Test(groups = "alfresco-one")
    public void AONE_9708() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // ---- Pre-conditions: ----
        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");
        recurrence.click("rbtnDaily");
        recurrence.click("rbtnEndafter");
        recurrence.deleteText("txtEndafterEditableTextoccurences", 0);
        recurrence.enterString("txtEndafterEditableTextoccurences", "10");
        recurrence.click("btnOK");
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");

        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName), "Event is not displayed");
        calendarPage.chooseAgendaTab().render(maxWaitTime);
        int event_number = calendarPage.getTheNumOfEvents(ActionEventVia.AGENDA_TAB);
        Assert.assertEquals(10, event_number, "There are more than 10 events");

        // ---- Step 1 -----
        // ---- Step Action ----
        // Click Recurrence button in Ms Outlook;
        // ---- Expected result ----
        // Appointment recurrence window is opened;
        Ldtp site = outlook.getAbstractUtil().setOnWindow(siteName);
        site.doubleClick("btnRecurrence");

        // ---- Step 2 -----
        // ---- Step Action ----
        // Select another start and end time ;
        // ---- Expected result ----
        // Time and duration are set;
        recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "6 hours");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Set any recurrence pattern;
        // ---- Expected result ----
        // Recurrence pattern is set;
        recurrence.click("rbtnDaily");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Set another range of recurrence;
        // ---- Expected result ----
        // Range of recurrence is set;
        recurrence.click("rbtnEndafter");
        recurrence.deleteText("txtEndafterEditableTextoccurences", 0);
        recurrence.enterString("txtEndafterEditableTextoccurences", "15");
        recurrence.click("btnOK");

        // ---- Step 5 -----
        // ---- Step Action ----
        // Select Save Changes (that is not matter with Send updates or no) and click OK button;
        // ---- Expected result ----
        // Changes are saved in Outlook;
        after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");

        // ---- Step 6, 7 -----
        // ---- Step Action ----
        // Verify the event is edited successfully;
        // ---- Expected result ----
        // My Calendar dashlet event is marked as recurring, recurring events are also displayed in calendar tab of the meeting workspace with chached start
        // time and duration, recurrence pattern and the range of recurrence;
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event " + siteName + " is not repeating");
        String eventName = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventName.contains("Repeating"), "Event " + siteName + " is not repeating");

        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);
        calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        calendarPage.chooseAgendaTab().render();
        int event_reccurence = calendarPage.getTheNumOfEvents(ActionEventVia.AGENDA_TAB);
        Assert.assertEquals(event_reccurence, 15, "There are more than 15 events");

        calendarPage.chooseMonthTab().render();
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.isRecurrencePresent(), "Recurrence is missing");
        eventInfo.closeInformationForm();
    }

    /**
     * AONE-9709:Editing event via Outlook. Don't save changes
     */
    @Test(groups = "alfresco-one")
    public void AONE_9709() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // ---- Pre-conditions: ----
        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        // login to share
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // navigate to site
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);

        // navigate to Calendar
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName), "Event is not displayed");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        String end_date = eventInfo.getEndDateTime();
        eventInfo.closeInformationForm();

        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.enterString("txtTo", testUser);

        // ---- Step 1 -----
        // ---- Step Action ----
        // Expand/collapse event's duration;
        // ---- Expected result ----
        // Event's duration and time boundaries are changed; New value is set; Microsoft Outlook window pops up; It notifies about time of the meeteing has
        // changed and and offers sending updates or no.
        SimpleDateFormat FormattedDATE = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        String tommorrow = (String) (FormattedDATE.format(c.getTime()));
        l1.click("btnSave");
        l1.deleteText("txtEnddate", 0);
        l1.enterString("txtEnddate", tommorrow);

        // ---- Step 2, 3, 4 -----
        // ---- Step Action ----
        // Select Dont't save changes radio button and click OK button;
        // ---- Expected result ----
        // Dont't save changes radio button is selected; Microsoft Outlook window is closed;
        outlook.exitOfficeApplication(l1);
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow("Microsoft Outlook");
        l2.click("rbtnDon'tsavechanges");
        l2.click("btnOK");

        // ---- Step 5, 6 -----
        // ---- Step Action ----
        // Log in Share as any user;
        // Open Calendar tab for the workspace;
        // ---- Expected result ----
        // Calendar tab is opened for the workspace;
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);
        calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();

        // ---- Step 7 -----
        // ---- Step Action ----
        // Log in Share as any user;
        // Open Calendar tab for the workspace;
        // ---- Expected result ----
        // Event Info window is displayed; Event's details have not changed;
        eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        String updated_endDate = eventInfo.getEndDateTime();
        Assert.assertEquals(updated_endDate, end_date, "End date is not updated");
    }

    /**
     * AONE-9710:Verify the events are founds without errors
     */
    @Test(groups = "alfresco-one")
    public void AONE_9710() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        // User login.
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // ---- Step 1 -----
        // ---- Step Action ----
        // Enter a name of the event created via MS Outlook;
        // ---- Expected result ----
        // Query is entered;
        SiteFinderPage siteFinderPage = SiteUtil.searchSiteWithRetry(drone, siteName, true).render();

        // ---- Step 2 -----
        // ---- Step Action ----
        // Click Search button or press Enter key;
        // ---- Expected result ----
        // Event is found and displayed at the Search result page without any errors;
        List<String> theSite = siteFinderPage.getSiteList();
        Assert.assertTrue(theSite.contains(siteName), "Site " + siteName + " is not displayed");
    }

    /**
     * AONE-9711:All day events created in Outlook are appearing in Calendar
     */
    @Test(groups = "alfresco-one")
    public void AONE_9711() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // create new meeting workspace
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        // login
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // open site dashboard
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // All day event is displayed in Calendar;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName), "Event " + siteName + " is missing");
    }

    /**
     * AONE-9712:Creating a recurrence event. Times and duration.
     */
    @Test(groups = "alfresco-one")
    public void AONE_9712() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // / ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "6 hours");
        recurrence.click("rbtnDaily");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));

        // select OK
        recurrence.click("btnOK");

        // Click Send button;
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 9 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

        // ---- Step 10 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New Appointment is created and displayed in the calendar of selected Meeting Workspace;
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName), "Event " + siteName + " is missing");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_ALL_DAY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Incorrect Start time");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "Incorrect Start time");
        eventInfo.closeInformationForm();

        // ---- Step 11 -----
        // ---- Step Action ----
        // Verify start and end date, duration of the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // The start and end date, duration of the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place are correctly
        // displayed;
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);

        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());

        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName));
        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay));

        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();

        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event " + siteName + " is not repeating");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Incorrect event details");
    }

    /**
     * AONE-9713:Creating a recurrence event. Daily Every () day
     */
    @Test(groups = "alfresco-one")
    public void AONE_9713() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 ----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // In Recurrence pattern set Daily radio button,
        // ---- Expected result ----
        // Any number is entered; Settings are saved;
        recurrence.click("rbtnDaily");
        recurrence.click("rbtnEvery");

        // ---- Step 6 -----
        // ---- Step Action ----
        // Fill Every day(s) with any day number and click ok button;
        // ---- Expected result ----
        // Any number is entered; Settings are saved;
        recurrence.deleteText("txtEveryEditableTextday(s)", 0);
        recurrence.enterString("txtEveryEditableTextday(s)", "5");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");

        // ---- Step 8 -----
        // ---- Step Action ----
        // Click Send button;
        // ---- Expected result ----
        // Appointment is sent;
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is not displayed");

        // Click the event's name -> Event Info window pops up;
        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Incorrect start time");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "Incorrect end time");
        String recDetail = eventInfo.getRecurrenceDetail();
        String infoRecCompare = "Occurs every 5 days effective " + infoWeekDay + " from " + startTime + " to " + endTime;
        Assert.assertTrue(recDetail.contains(infoRecCompare), "Incorrect recurrence on Event Information Form");
        eventInfo.closeInformationForm();

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify start and end date, duration of the event ar My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (it occurs
        // every <entered number> days effective start date from start time to end time);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end date are incorrect on Site Calendar dashlet");

        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard().render();

        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();
        myCalendar.render(maxWaitTime);

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event " + siteName + " is not repeating");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Star and end date are incorrect on My calendar dashlet");
    }

    /**
     * AONE-9714:Creating a recurrence event. Daily. Every Weekday
     */
    @Test(groups = "alfresco-one")
    public void AONE_9714() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // In Recurrence pattern set Daily radio button,
        // ---- Expected result ----
        // Radio button is selected; Settings are saved;
        recurrence.click("rbtnDaily");

        // ---- Step 6 -----
        // ---- Step Action ----
        // Select every weekday and click Ok button;
        // ---- Expected result ----
        // Radio button is selected; Settings are saved;
        recurrence.click("rbtnEveryweekday");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));

        // select OK
        recurrence.click("btnOK");

        // ---- Step 8 -----
        // ---- Step Action ----
        // Click Send button;
        // ---- Expected result ----
        // Appointment is sent;
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is not displayed");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Incorrect Start time");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "Incorrect End time");

        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());

        String compareDetail = "Occurs each week on Monday, Tuesday, Wednesday, Thursday, Friday, effective " + infoWeekDay + " from " + startTime + " to "
                + endTime;
        String recDetail = eventInfo.getRecurrenceDetail();
        Assert.assertTrue(recDetail.contains(compareDetail), "Recurrence detail is incorrect");
        eventInfo.closeInformationForm();

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify start and end date, duration of the event ar My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (it occurs
        // every weekday (from Monday till Friday) effective start date from start time to end time);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end date are incorrect on Site Calendar dashlet");

        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating on My Calendar dashlet");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Event details are incorrect");
    }

    /**
     * AONE-9715: Creating a recurrence event. Weekly. Every () week(s)
     */
    @Test(groups = "alfresco-one")
    public void AONE_9715() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // In Recurrence pattern set Weekly radio button,
        // ---- Expected result ----
        // Weekly radio button is selected;
        recurrence.click("rbtnWeekly");

        // ---- Step 6 -----
        // ---- Step Action ----
        // Enter any number into Recur every week(s) on, select any day(s) and click Ok button;
        // ---- Expected result ----
        // Any number is entered, day (s) are selected; Settings are saved;
        recurrence.deleteText("txtRecureveryEditableTextweek(s)", 0);
        recurrence.enterString("txtRecureveryEditableTextweek(s)", "3");

        String day;
        SimpleDateFormat format = new SimpleDateFormat("EEEE");
        Calendar cal = Calendar.getInstance();
        day = format.format(cal.getTime());
        recurrence.click("chk" + day);
        recurrence.click("chkMonday");
        recurrence.click("chkTuesday");
        recurrence.click("chkWednesday");
        recurrence.click("chkThursday");
        recurrence.click("chkFriday");
        recurrence.click("chkSaturday");
        recurrence.click("chkSunday");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");

        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is missing");

        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Incorrect start time");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "Incorrect start time");
        String recDetail = eventInfo.getRecurrenceDetail();
        String compareDetail = "Occurs every 3 weeks on Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, effective " + infoWeekDay + " from "
                + startTime + " to " + endTime;
        Assert.assertTrue(recDetail.contains(compareDetail), "Recurrence detail is incorrect");
        eventInfo.closeInformationForm();

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (Occurs every
        // <entered number> weeks on <selected day(s)> effective start date from start time to end time);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);

        Calendar calendar = Calendar.getInstance();
        // calendar.add(Calendar.DATE, 1);
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end time is incorrect");
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating");

        // The start and end date, duration of the event at My calendar dashlet
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Event details are incorrect");
    }

    /**
     * AONE-9716:Creating a recurrence event. Monthly. Every () of () month
     */
    @Test(groups = "alfresco-one")
    public void AONE_9716() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // In Recurrence pattern set Monthly radio button
        // ---- Expected result ----
        // Monthly radio button is seleceted;
        recurrence.click("rbtnMonthly");

        // ---- Step 6 -----
        // ---- Step Action ----
        // Select Day radio button, enter any number of day, enter a number of month(s) click Ok button;
        // ---- Expected result ----
        // Any numbers are entered, Day is selected; Settings are saved;
        recurrence.click("rbtnDay");
        recurrence.deleteText("txtDayEditableTextofeveryEditableTextmonth(s)1", 0);
        recurrence.enterString("txtDayEditableTextofeveryEditableTextmonth(s)1", "2");
        String day = recurrence.getTextValue("txtDayEditableTextofeveryEditableTextmonth(s)");
        String month = recurrence.getTextValue("txtDayEditableTextofeveryEditableTextmonth(s)1");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));

        recurrence.click("btnOK");
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName));

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Start time is incorrect");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "End time is incorrect");

        // Calendar tab of the meeting place are correctly displayed (Occurs every <entered number> weeks on <selected day(s)> effective start date from start
        // time to end time);
        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());
        String recDetail = eventInfo.getRecurrenceDetail();
        String compareRecDetail = "Occurs day " + day + " of every " + month + " month(s) effective " + infoWeekDay + " from " + startTime + " to " + endTime;
        Assert.assertTrue(recDetail.contains(compareRecDetail), "Recurrence detail is incorrect");
        eventInfo.closeInformationForm();

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (Occurs day
        // <number> of every <number> months effective start date from start time to end time);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end time are incorrect");
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Start and end time are incorrect on My calendar dashlet");
    }

    /**
     * AONE-9717: Creating a recurrence event. Monthly
     */
    @Test(groups = "alfresco-one")
    public void AONE_9717() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // In Recurrence pattern set Monthly radio button
        // ---- Expected result ----
        // Monthly radio button is selected;
        recurrence.click("rbtnMonthly");

        // ---- Step 6 -----
        // ---- Step Action ----
        // Select The radio button, select values form combo boxes, enter any number of month(s) click Ok button;
        // ---- Expected result ----
        // Any number is entered, some values are selected from combo boxes; Settings are saved;
        recurrence.click("rbtnThe");
        recurrence.selectItem("cboTheEditableTextEditableTextofeveryEditableTextmonth(s)", "last");
        recurrence.selectItem("cboTheEditableTextEditableTextofeveryEditableTextmonth(s)1", "Friday");
        recurrence.deleteText("txtTheEditableTextEditableTextofeveryEditableTextmonth(s)", 0);
        recurrence.enterString("txtTheEditableTextEditableTextofeveryEditableTextmonth(s)", "3");
        String months = recurrence.getTextValue("txtTheEditableTextEditableTextofeveryEditableTextmonth(s)");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName));

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Start time is incorrect");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "End time is incorrect");

        // Calendar tab of the meeting place are correctly displayed (Occurs every <entered number> weeks on <selected day(s)> effective start date from start
        // time to end time);
        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoCalendar.set(GregorianCalendar.DAY_OF_WEEK, Calendar.FRIDAY);
        infoCalendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, -1);
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());
        String recDetail = eventInfo.getRecurrenceDetail();
        String compareRecDetail = "Occurs the last Friday of every " + months + " month(s) effective " + infoWeekDay + " from " + startTime + " to " + endTime;
        Assert.assertTrue(recDetail.contains(compareRecDetail) && recDetail.contains("of every 3 month(s)"), "");
        eventInfo.closeInformationForm();

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (e.g. Occurs
        // the fourth Friday of every 2 months effective 11/26/2010 from 4:30 PM to 5:00 PM);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.set(GregorianCalendar.DAY_OF_WEEK, Calendar.FRIDAY);
        calendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, -1);
        weekDay = dayFormat.format(calendar.getTime());

        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeting");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end time is incorrect");
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Start and end date are incorrect");
    }

    /**
     * AONE-9718:Creating a recurrence event. Yearly. On the month
     */
    @Test(groups = "alfresco-one")
    public void AONE_9718() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // In Recurrence pattern set Yearly radio button
        // ---- Expected result ----
        // Yearly radio button is selected;
        recurrence.click("rbtnYearly");

        // ---- Step 6 -----
        // ---- Step Action ----
        // Fill Recur every with any correct value, select On radio button, select any month from combo box, enter any date and click Ok button;
        // ---- Expected result ----
        // Recur every field is filled, On radio button is selected, month is chosen, date is entered; Settings are saved;
        recurrence.deleteText("txtRecurevery", 0);
        recurrence.enterString("txtRecurevery", "3");
        recurrence.click("rbtnOn");
        String day = recurrence.getTextValue("txtEveryEditableTextEditableText");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");

        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName);

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is missing");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Incorrect start time");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "Incorrect end time");

        // Calendar tab of the meeting place are correctly displayed (Occurs every <entered number> weeks on <selected day(s)> effective start date from start
        // time to end time);
        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());
        String recDetail = eventInfo.getRecurrenceDetail();
        String compareRecDetail = "Occurs day " + day + " of every 36 month(s) effective " + infoWeekDay + " from " + startTime + " to " + endTime;
        Assert.assertTrue(recDetail.contains(compareRecDetail), "Recurrence detail is incorrect");
        eventInfo.closeInformationForm();

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (it occurs
        // every <entered year frequency> years on <selected month> <selected date> effective <start date> from <start time> to <end time>);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Star and end date are incorrect");
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating");

        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);

        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Start and end time from My Calendar dashlet are incorrect");
    }

    /**
     * AONE-9719:Creating a recurrence event. Yearly. Frequency
     */
    @Test(groups = "alfresco-one")
    public void AONE_9719() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // In Recurrence pattern set Yearly radio button
        // ---- Expected result ----
        // Yearly radio button is selected;
        recurrence.click("rbtnYearly");

        // ---- Step 6 -----
        // ---- Step Action ----
        // Fill Recur every with any correct value, select On the radio button, select any frequency, select any day from combobox, select any month from combox
        // and click Ok button;
        // ---- Expected result ----
        // Recur every field is filled, On radio button is selected, month is chosen, date is entered; Settings are saved;
        recurrence.deleteText("txtRecurevery", 0);
        recurrence.enterString("txtRecurevery", "3");
        recurrence.click("rbtnOnthe");
        String period = recurrence.getTextValue("cboTheEditableTextEditableTextofEditableText");
        String week_day = recurrence.getTextValue("cboTheEditableTextEditableTextofEditableText1");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");

        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName));

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Start time is incorrect");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "End time is incorrect");

        String recDetail = eventInfo.getRecurrenceDetail();
        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());
        String compareRecDetail = "Occurs the " + period + " " + week_day + " of every 36 month(s) effective " + infoWeekDay + " from " + startTime + " to "
                + endTime;
        Assert.assertTrue(recDetail.contains(compareRecDetail), "Recurrence detail is incorrect");
        eventInfo.closeInformationForm();

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly displayed (it occurs
        // every <entered year frequency> years on the <selected frequency> <selected day> of <selected month> effective <start date> from <start time> to <end
        // time>);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating on Site calendar dashlet");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end date are incorrect");

        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating on My calendar dashlet");

        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Start date and end date are not correct on My Calendar dashlet");
    }

    /**
     * AONE-9720:Creating a recurrence event. No end date
     */
    @Test(groups = "alfresco-one")
    public void AONE_9720() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // Set any recurrence pattern;
        // ---- Expected result ----
        // Any recurrence pattern is chosen;
        recurrence.click("rbtnDaily");
        recurrence.click("rbtnEveryweekday");

        // ---- Step 6 -----
        // ---- Step Action ----
        // In the Range of recurrence part select any start date and No end date radio button and click Ok button;
        // ---- Expected result ----
        // Start date is set,No end date radio button is selected; Settings are saved;
        recurrence.click("rbtnNoenddate");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");

        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is not displayed");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Start time is incorrect");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "End time is incorrect");

        String recDetail = eventInfo.getRecurrenceDetail();
        String infoWeekDay;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());
        String compareRecDetail = "Occurs each week on Monday, Tuesday, Wednesday, Thursday, Friday, effective " + infoWeekDay + " from " + startTime + " to "
                + endTime;
        Assert.assertTrue(recDetail.contains(compareRecDetail), "Recurrence detail is incorrect");
        eventInfo.closeInformationForm();
        calendarPage.chooseAgendaTab().render();
        int event_reccurence = calendarPage.getTheNumOfEvents(ActionEventVia.AGENDA_TAB);
        Assert.assertTrue(event_reccurence > 15, "Number of events must be > than 15");

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (start date is
        // correct, event repeats with no end date );
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end date are incorrect");

        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating in My calendar dashlet");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Details on My calendar dashlet are incorrect");
    }

    /**
     * AONE-9721:Creating a recurrence event. End after several occurrences
     */
    @Test(groups = "alfresco-one")
    public void AONE_9721() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // Set any recurrence pattern;
        // ---- Expected result ----
        // Any recurrence pattern is chosen;
        recurrence.click("rbtnDaily");
        recurrence.click("rbtnEveryweekday");

        // ---- Step 6 -----
        // ---- Step Action ----
        // In the Range of recurrence part select any start date, select End after, enter any value of occurrences and click Ok button;
        // ---- Expected result ----
        // Start date is set, End after radio button is selected, any occurrences nomber is entered; Settings are saved;s
        recurrence.click("rbtnEndafter");
        recurrence.deleteText("txtEndafterEditableTextoccurences", 0);
        recurrence.enterString("txtEndafterEditableTextoccurences", "9");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is not displayed");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Start time is incorrect");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "End time is incorrect");
        Assert.assertTrue(eventInfo.isRecurrencePresent(), "Recurrence is missing");

        String recDetail = eventInfo.getRecurrenceDetail();
        String infoWeekDay;
        String untilDate;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());

        infoCalendar.add(Calendar.DATE, 10);
        untilDate = infoDayFormat.format(infoCalendar.getTime());
        String compareRecDetail = "Occurs each week on Monday, Tuesday, Wednesday, Thursday, Friday, effective " + infoWeekDay + " until " + untilDate
                + " from " + startTime + " to " + endTime;
        Assert.assertTrue(recDetail.contains(compareRecDetail), "Recurrence detail is incorrect");

        eventInfo.closeInformationForm();
        calendarPage.chooseAgendaTab().render();
        int event_reccurence = calendarPage.getTheNumOfEvents(ActionEventVia.AGENDA_TAB);
        Assert.assertEquals(event_reccurence, 9, "Events are missing");

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (start date is
        // correct, event repeats <enter number of occurences in the 6 step> times );
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating on Site Calendar dashlet");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Event is not displayed");
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();

        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Details on My calendar dashlet are incorrect");
    }

    /**
     * AONE-9722:Creating a recurrence event. End by date
     */
    @Test(groups = "alfresco-one")
    public void AONE_9722() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // ---- Step 1, 2 -----
        // ---- Step Action ----
        // 1. Click on "New Appointment" link in "New" drop-down menu;
        // 2. Fill in Subject and location;
        // ---- Expected result ----
        // 1. Appointment form is opened;
        // 2. Information is entered successfully;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);
        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Click Recurrence button;
        // ---- Expected result ----
        // Appointment recurrence form is opened;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // ---- Step 4 -----
        // ---- Step Action ----
        // Select any Start time and End time (or set any duration);
        // ---- Expected result ----
        // Time and duration are set;
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // ---- Step 5 -----
        // ---- Step Action ----
        // Set any recurrence pattern;
        // ---- Expected result ----
        // Any recurrence pattern is chosen;
        recurrence.click("rbtnDaily");
        recurrence.click("rbtnEvery");

        // ---- Step 6 -----
        // ---- Step Action ----
        // In the Range of recurrnce part select any start date, select End by radio button and select any date, click Ok button;
        // ---- Expected result ----
        // Start date is set, End byr radio button is selected, any date is selected; Settings are saved;
        recurrence.click("rbtnEndby");
        recurrence.deleteText("txtEndby", 0);
        SimpleDateFormat FormattedDATE = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 2);
        String nextDays = (String) (FormattedDATE.format(c.getTime()));
        recurrence.enterString("txtEndby", nextDays);
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");

        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");
        after_rec.enterString("txtTo", testUser);
        after_rec.click("btnSend");

        // ---- Step 11 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // Alfresco Share is opened;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 12 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // New recurrence appointment is created and displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is not displayed");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Start time is incorrect");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "End time is incorrect");
        Assert.assertTrue(eventInfo.isRecurrencePresent(), "Recurrence is missing");

        String recDetail = eventInfo.getRecurrenceDetail();
        String infoWeekDay;
        String untilDate;
        SimpleDateFormat infoDayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Calendar infoCalendar = Calendar.getInstance();
        infoWeekDay = infoDayFormat.format(infoCalendar.getTime());
        infoCalendar.add(Calendar.DATE, 2);
        untilDate = infoDayFormat.format(infoCalendar.getTime());
        String compareRecDetail = "Occurs every day effective " + infoWeekDay + " until " + untilDate + " from " + startTime + " to " + endTime;
        Assert.assertTrue(recDetail.contains(compareRecDetail));

        eventInfo.closeInformationForm();
        calendarPage.chooseAgendaTab().render();
        int event_reccurence = calendarPage.getTheNumOfEvents(ActionEventVia.AGENDA_TAB);
        Assert.assertEquals(event_reccurence, 3, "Events are missing");

        // ---- Step 13 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (start date is
        // correct, event repeats till the end date selected in the 6 step);
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // The created appointment is displayed with correct date on the dashlet;
        Assert.assertTrue(siteCalendarDashlet.isEventsDisplayed(siteName), "The " + siteName + " isn't correctly displayed on calendar");
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating in Site calendar dashlet");

        // check start and end date, duration of the event at Site calendar dashlet
        Assert.assertTrue(siteCalendarDashlet.isEventsWithHeaderDisplayed(weekDay), "Start and end date are incorrect");

        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertTrue(repeating, "Event is not repeating in My calendar dashlet");
        String theTime = startTime + " - " + endTime;

        // The start and end date, duration of the event at My calendar dashlet
        dayFormat = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        String eventDetails = myCalendar.getEventDetails(siteName);
        Assert.assertTrue(eventDetails.contains(weekDay + " " + theTime), "Start and end date are incorrect in My Calendar dashlet");
    }

    /**
     * AONE-9723:Remove recurrence
     */
    @Test(groups = "alfresco-one")
    public void AONE_9723() throws Exception
    {
        String testName = getTestName() + System.currentTimeMillis();
        String location = testName + " - Room";
        String siteName = getSiteName(testName);
        String testUser = getUserNameFreeDomain(testName);

        // Create normal User
        String[] testUser1 = new String[] { testUser };
        CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

        // MS Outlook 2010 is opened;
        Ldtp l = outlook.openOfficeApplication();

        // Precondition: any recurrence event is created via MS Outlook;
        outlook.operateOnCreateNewMeetingWorkspace(l, sharePointPath, siteName, location, testUser, DEFAULT_PASSWORD, true, false);

        Ldtp l1 = outlook.getAbstractUtil().setOnWindow(siteName);
        l1.click("chkAlldayevent");

        // Click Recurrence button;
        Ldtp l2 = outlook.getAbstractUtil().setOnWindow(siteName);
        l2.doubleClick("btnRecurrence");

        // Select any start and end time (or set any duration);
        Ldtp recurrence = outlook.getAbstractUtil().setOnWindow("Appointment Recurrence");
        recurrence.deleteText("txtDuration", 0);
        recurrence.enterString("txtDuration", "4 hours");

        // Set any recurrence pattern;
        recurrence.click("rbtnDaily");
        recurrence.click("rbtnEvery");
        String startTime = convertHour(recurrence.getTextValue("txtStart"));
        String endTime = convertHour(recurrence.getTextValue("txtEnd"));
        recurrence.click("btnOK");

        // Select Save Changes
        Ldtp after_rec = outlook.getAbstractUtil().setOnWindow(siteName);
        after_rec.click("btnSave");

        // Go to Alfresco Share;
        ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
        SiteDashboardPage siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        CalendarPage calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName), "Event is not displayed");

        // Click the event's name -> Event Info window pops up;
        InformationEventForm eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertTrue(eventInfo.isRecurrencePresent(), "Recurrence is not displayed");
        eventInfo.closeInformationForm();

        // Verify the event at My calendar dashlet, Site calendar dashlet;
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        SiteCalendarDashlet siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // Site calendar dashlet is marked as recurrence
        Assert.assertTrue(siteCalendarDashlet.isRepeating(siteName), "Event is not repeating in Site calendar dashlet");

        // ---- Step 1 -----
        // ---- Step Action ----
        // Click Recurence button;
        // ---- Expected result ----
        // Apointment recurrence form is opened;
        after_rec.doubleClick("btnRecurrence");

        // ---- Step 2 -----
        // ---- Step Action ----
        // CLick Remove recurrence button;
        // ---- Expected result ----
        // Apointment recurrence form is closed; Reccurence rule is removed;
        recurrence.click("btnRemoveRecurrence");
        l2.click("btnSave");

        // ---- Step 3 -----
        // ---- Step Action ----
        // Go to Alfresco Share;
        // ---- Expected result ----
        // User logs in successfully;
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();

        // ---- Step 4 -----
        // ---- Step Action ----
        // Verify the created event is displayed in the calendar of selected Meeting Workspace;
        // ---- Expected result ----
        // Event is not recurrent; It correctly displayed in the calendar of selected Meeting Workspace;
        calendarPage = siteDashBoard.getSiteNav().selectCalendarPage().render();
        Assert.assertTrue(calendarPage.isEventPresent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName));
        eventInfo = calendarPage.clickOnEvent(CalendarPage.EventType.MONTH_TAB_MULTIPLY_EVENT, siteName).render();
        Assert.assertFalse(eventInfo.isRecurrencePresent(), "Recurrence is displayed");
        Assert.assertTrue(eventInfo.getStartDateTime().contains(startTime), "Start time is incorrect");
        Assert.assertTrue(eventInfo.getEndDateTime().contains(endTime), "End time is incorrect");
        eventInfo.closeInformationForm();

        // ---- Step 5 -----
        // ---- Step Action ----
        // Verify the event at My calendar dashlet, Site calendar dashlet and Calendar tab of the meeting place;
        // ---- Expected result ----
        // Event at My calendar dashlet, Site calendar dashlet is not marked as recurrence; Calendar tab of the meeting place are correctly dispalyed (start
        // date is correct, duration is correct); There is not event's reccurrences in future;
        siteDashBoard = SiteUtil.openSiteFromSearch(drone, siteName).render();
        siteCalendarDashlet = siteDashBoard.getDashlet("site-calendar").render();

        // Site calendar dashlet is marked as recurrence
        Assert.assertFalse(siteCalendarDashlet.isRepeating(siteName), "Event is repeating in Site calendar dashlet");
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        customizeUserDash = dashBoard.getNav().selectCustomizeUserDashboard();
        customizeUserDash.render();
        dashBoard = customizeUserDash.addDashlet(Dashlets.MY_CALENDAR, 1).render();
        MyCalendarDashlet myCalendar = dashBoard.getDashlet("my-calendar").render();

        // My Calendar dashlet event is marked as recurring
        Boolean repeating = myCalendar.isRepeating(siteName);
        Assert.assertFalse(repeating, "Event is repeating in My calendar dashlet");

    }

    private String convertHour(String hour)
    {
        SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
        SimpleDateFormat _12HourSDF = new SimpleDateFormat("h:mm a");
        String convertedHour = "";
        try
        {
            Date _24HourDt;
            _24HourDt = _24HourSDF.parse(hour);
            convertedHour = _12HourSDF.format(_24HourDt);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return convertedHour;
    }
}
