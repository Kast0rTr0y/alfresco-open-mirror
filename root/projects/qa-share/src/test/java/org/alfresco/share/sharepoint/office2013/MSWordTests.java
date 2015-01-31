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
package org.alfresco.share.sharepoint.office2013;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.alfresco.application.windows.MicrosoftOffice2013;
import org.alfresco.po.share.enums.UserRole;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.FileDirectoryInfo;
import org.alfresco.po.share.site.document.TreeMenuNavigation;
import org.alfresco.test.FailedTestListener;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserMembers;
import org.alfresco.share.util.ShareUserSitePage;
import org.alfresco.share.util.WebDroneType;
import org.alfresco.share.util.api.CreateUserAPI;
import org.alfresco.utilities.Application;
import org.alfresco.webdrone.WebDrone;
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
public class MSWordTests extends AbstractUtils
{
        private String testName;
        private String testUser;
        private String siteName;

        private String docFileName_9808;
        private String docFileName_9809;
        private String docFileName_9810;
        private String docFileName_9811;
        private String docFileName_9812;
        private String docFileName_9813;
        private String docFileName_9814;
        private String docFileName_9815;
        private String docFileName_9816;
        private String docFileName_9824;
        private String docFileName_9825;
        private String docFileName_9826;
        private String docFileName_9827;

        private String fileType = ".docx";
        MicrosoftOffice2013 word = new MicrosoftOffice2013(Application.WORD, "2013");

        private static DocumentLibraryPage documentLibPage;
        private static final String SHAREPOINT = "sharepoint";
        public String sharepointPath;

        @Override
        @BeforeClass(alwaysRun = true)
        public void setup() throws Exception
        {
                super.setup();

                testName = this.getClass().getSimpleName();
                testUser = getUserNameFreeDomain(testName);
                siteName = getSiteName(testName);

                docFileName_9808 = "WSaveFileToShare";
                docFileName_9809 = "WInputSave";
                docFileName_9810 = "WInputSavechanges";
                docFileName_9811 = "WInputOpen";
                docFileName_9812 = "WCheckInAfterSaving";
                docFileName_9813 = "WInputCheckout";
                docFileName_9814 = "WInputCheckin";
                docFileName_9815 = "WInputKeepcheckout";
                docFileName_9816 = "WInputDiscard";
                docFileName_9824 = "WInputRefresh";
                docFileName_9825 = "WInputEmptycomm";
                docFileName_9826 = "WInputWildcardscomm";
                docFileName_9827 = "WInputCancel";

                Runtime.getRuntime().exec("taskkill /F /IM WINWORD.EXE");

                sharepointPath = word.getSharePointPath();
        }

        @AfterMethod(alwaysRun = true)
        public void teardownMethod() throws Exception
        {
                Runtime.getRuntime().exec("taskkill /F /IM WINWORD.EXE");
                Runtime.getRuntime().exec("taskkill /F /IM CobraWinLDTP.EXE");
        }

        @Test(groups = { "DataPrepWord" })
        public void dataPrep_AONE() throws Exception
        {
                // Create normal User
                String[] testUser1 = new String[] { testUser };
                CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUser1);

                // login with user
                ShareUser.login(drone, testUser);

                // Create public site
                ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                File file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9809 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9810 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9811 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9813 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9814 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9815 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9816 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9824 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9825 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9826 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

                file = new File(DATA_FOLDER + SLASH + SHAREPOINT + SLASH + docFileName_9827 + fileType);
                ShareUserSitePage.uploadFile(drone, file).render();

        }

        @Test(groups = "alfresco-one")
        public void AONE_9808() throws Exception
        {

                // MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();

                // Enter some content to the document;
                word.editOffice(l, "new input data");

                // Save as window is opened
                word.navigateToSaveAsSharePointBrowse(l);
                // word.operateOnSecurity(l, testUser, DEFAULT_PASSWORD);

                // 1. Type url into File name field (e.g. http://<host>:7070/alfresco);
                // 2. Enter the credentials;
                // 3. Select site Document Library where you would like to save the
                // document;
                // 4. Enter a workbook name;
                // 5. Click Save button;
                String path = getPathSharepoint(drone);
                word.operateOnSaveAsWithSharepoint(l, path, siteName, docFileName_9808, testUser, DEFAULT_PASSWORD);

                l=word.getAbstractUtil().setOnWindow(docFileName_9808);
                String actualName = word.getAbstractUtil().findWindowName(docFileName_9808);
                Assert.assertTrue(actualName.contains(docFileName_9808) && actualName.contains("Word"), "File not found");     
                
                // 1. Don't make any changes;
                // 2. Select File->Info;
                word.goToFile(l);
                word.getAbstractUtil().clickOnObject(l, "Info");
                
                // 3. Expand Versions menu and click Check Out;
                word.getAbstractUtil().clickOnObject(l, "ManageVersions");
                l.waitTime(2);
                l.keyPress("tab");
                l.keyPress("enter");
                l.click("Check Out");
                
                // 4. Click Check In;
                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9808);
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                word.getAbstractUtil().clickOnObject(l1, "CheckIn");
                
                // 5. Enter any comment and click OK button;
                String commentFromWord = "comment for 9643";
                word.operateOnCheckIn(l1, commentFromWord, false);

                String actualName2 = word.getAbstractUtil().findWindowName(docFileName_9808);
                Assert.assertTrue(actualName2.contains("[Read-Only]"), "Word is NOT opened in read only mode");

                // 6. Log into Share;
                ShareUser.login(drone, testUser, DEFAULT_PASSWORD);

                // 7. Go to site Document library where document was saved;
                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                // Expected results
                // 7. The saved document is present;
                Assert.assertTrue(documentLibPage.isFileVisible(docFileName_9808 + fileType), "The saved document is not displayed.");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9809() throws Exception
        {

                // MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                // 1. Enter the credentials;
                // word.operateOnSecurity(l, testUser, DEFAULT_PASSWORD);

                String path = getPathSharepoint(drone);

                // 2. Type url into File name field (e.g. http://<host>:7070/alfresco);
                // 3. Select the workbook from site Document Library you would like to
                // open;
                // 4. Click Open button;
                word.operateOnOpen(l, path, siteName, docFileName_9809, testUser, DEFAULT_PASSWORD);

                String actualName = word.getAbstractUtil().findWindowName(docFileName_9809);
                Assert.assertTrue(actualName.contains(docFileName_9809) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9809);

                // Checkout the document
                word.checkOutOffice(l1);                        

                Ldtp l2 = word.getAbstractUtil().setOnWindow(docFileName_9809);
                // edit document

                String newContent = testName;
                word.editOffice(l2, newContent);
                word.saveOffice(l2);

                // User login.
                ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9809 + fileType);
                Map<String, Object> properties = detailsPage.getProperties();
                Assert.assertEquals(properties.get("Name"), docFileName_9809 + " (Working Copy)" + fileType);
                String documentContent = detailsPage.getDocumentBody();
                Assert.assertTrue(documentContent.contains(newContent), "Changes are not present in the document.");

                // Excel Document is present in I'm Editing section;
                FileDirectoryInfo fileInfo = ShareUserSitePage.getFileDirectoryInfo(drone, docFileName_9809 + fileType);
                assertEquals(fileInfo.getContentInfo(), "This document is locked by you.", "File " + docFileName_9809 + " isn't locked");
                TreeMenuNavigation treeMenuNavigation = documentLibPage.getLeftMenus().render();
                treeMenuNavigation.selectDocumentNode(TreeMenuNavigation.DocumentsMenu.IM_EDITING).render();
                Assert.assertTrue(
                        ShareUserSitePage.getDocTreeMenuWithRetry(drone, docFileName_9809 + fileType, TreeMenuNavigation.DocumentsMenu.IM_EDITING, true),
                        docFileName_9809 + fileType + " cannot be found.");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9810() throws Exception
        {
                // 1. MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                // Open document window is opened;
                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the workbook from site Document Library you would like to
                // open;
                // Click Open button;

                String path = getPathSharepoint(drone);
                word.operateOnOpen(l, path, siteName, docFileName_9810, testUser, DEFAULT_PASSWORD);

                String actualName = word.getAbstractUtil().findWindowName(docFileName_9810);

                Assert.assertTrue(actualName.contains(docFileName_9810) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9810);
                // 1. Make some changes to the document;
                // 2. Click Save button;

                String newContent = testName;
                word.editOffice(l1, newContent);
                word.saveOffice(l1);

                // Click File ->Info -> Manage Versions;
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                word.getAbstractUtil().clickOnObject(l1, "ManageVersions");
                l1.waitTime(2);
                // 3. Click Refresh Versions list;
                l1.keyPress("tab");
                l1.keyPress("enter");
                l1.click("Refresh Server Versions List");
                // 3. New minor version is created
                String fileVersion = "1.1";
                String fileVersionObject = "btn" + fileVersion.replace(".", "");

                Assert.assertTrue(word.getAbstractUtil().isObjectDisplayed(l1, fileVersionObject), "Object with version " + fileVersion + " is not displayed");

                // 4. Log into Share;
                ShareUser.login(drone, testUser);

                // 5. Go to site Document Library and verify changes;
                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                // 5. Document version history is correctly dispalyed; Changes are applied;

                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9810 + fileType).render();

                Assert.assertTrue(detailsPage.isCheckedOut(), "The document is not checkout");
                Assert.assertEquals(detailsPage.getDocumentVersion(), fileVersion);
                String documentContent = detailsPage.getDocumentBody();
                Assert.assertTrue(documentContent.contains(newContent), "Changes are not present in the document.");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9811() throws Exception
        {
                // MS Office word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // 2. Type url into File name field (e.g. http://<host>:7070/alfresco);
                // 3. Select the workbook from site Document Library you would like to
                // open;
                // 4. Click Open button;
                word.operateOnOpen(l, path, siteName, docFileName_9811, testUser, DEFAULT_PASSWORD);

                // get the title name
                String actualName = word.getAbstractUtil().findWindowName(docFileName_9811);

                // verify word title
                Assert.assertTrue(actualName.contains(docFileName_9811) && actualName.contains("Word"), "File not found");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9812() throws Exception
        {

                // MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();

                // Enter some content to the document;
                String newContent = "new input data for 9812";
                word.editOffice(l, newContent);

                // Save as window is opened
                word.navigateToSaveAsSharePointBrowse(l);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Enter the credentials;
                // Select site Document Library where you would like to save the
                // document;
                // Enter a workbook name;
                // Click Save button;
                String path = getPathSharepoint(drone);
                word.operateOnSaveAsWithSharepoint(l, path, siteName, docFileName_9812, testUser, DEFAULT_PASSWORD);

                // verify the word title
                String actualName = word.getAbstractUtil().findWindowName(docFileName_9812);
                Assert.assertTrue(actualName.contains(docFileName_9812) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9812);

                // Checkout the document
                word.checkOutOffice(l1);

                // 3. User return to the document; (I) Checked Out This file has been
                // checked out to you. Check In this file to allow other users to see
                // your changes
                // and edit this file;
                Ldtp l2 = word.getAbstractUtil().setOnWindow(docFileName_9812);

                // 4. Click Check In;
                // 5. Enter any comment and click OK button;
                String commentFromWord = "comment for 9812";
                word.checkInOffice(l2, commentFromWord, false);

                String actualName2 = word.getAbstractUtil().findWindowName(docFileName_9812);
                Assert.assertTrue(actualName2.contains("[Read-Only]"), "Word is NOT opened in read only mode");

                // 6. Log into Share;
                ShareUser.login(drone, testUser);

                // 7. Go to site Document library;
                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();

                // 7. Locked document and working copy are present;
                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9812 + fileType);
                Assert.assertEquals(detailsPage.getContentInfo(), "This document is locked by you for offline editing.");
                Map<String, Object> properties = detailsPage.getProperties();
                Assert.assertEquals(properties.get("Name"), docFileName_9812 + " (Working Copy)" + fileType);

        }

        @Test(groups = "alfresco-one")
        public void AONE_9813() throws Exception
        {
                // 1. MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the workbook from site Document Library you would like to
                // open;
                // Click Open button;
                word.operateOnOpen(l, path, siteName, docFileName_9813, testUser, DEFAULT_PASSWORD);

                String actualName = word.getAbstractUtil().findWindowName(docFileName_9813);
                Assert.assertTrue(actualName.contains(docFileName_9813) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9813);

                // Click File ->Info;
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                Assert.assertFalse(word.getAbstractUtil().isObjectDisplayed(l1, "DiscardCheckOut"));
                Assert.assertFalse(word.getAbstractUtil().isObjectDisplayed(l1, "CheckIn"));
                // 2. Expand the Manage versions section;
                word.getAbstractUtil().clickOnObject(l1, "ManageVersions");
                l1.waitTime(2);
                // 3. Click Check Out action;
                l1.keyPress("tab");
                l1.keyPress("enter");
                l1.click("Check Out");

                Ldtp l2 = word.getAbstractUtil().setOnWindow(docFileName_9813);
                // 4. Click File ->Info;
                word.goToFile(l2);
                word.getAbstractUtil().clickOnObject(l2, "Info");

                // 5. Verify Checked out information is added to Info;
                // TODO: 5. Check In and Discard check out actions are available;
                // "No else can edit this document or view your changes until it is checked it"
                // message
                // is displayed at the pane;
                Assert.assertTrue(word.getAbstractUtil().isObjectDisplayed(l1, "DiscardCheckOut"), "Discard Check Out action is not available");
                Assert.assertTrue(word.getAbstractUtil().isObjectDisplayed(l1, "CheckIn"), "Check In action is not available");

                // 6. Log into Share;
                ShareUser.login(drone, testUser);

                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                FileDirectoryInfo fileInfo = ShareUserSitePage.getFileDirectoryInfo(drone, docFileName_9813 + fileType);

                // 7. Go to site Document Library and verify workbook is in locked
                assertEquals(fileInfo.getContentInfo(), "This document is locked by you for offline editing.", "File " + docFileName_9813 + " isn't locked");

                // 7. Excel Document is present in I'm Editing section;
                TreeMenuNavigation treeMenuNavigation = documentLibPage.getLeftMenus().render();
                treeMenuNavigation.selectDocumentNode(TreeMenuNavigation.DocumentsMenu.IM_EDITING).render();
                Assert.assertTrue(
                        ShareUserSitePage.getDocTreeMenuWithRetry(drone, docFileName_9813 + fileType, TreeMenuNavigation.DocumentsMenu.IM_EDITING, true),
                        docFileName_9813 + fileType + " cannot be found.");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9814() throws Exception
        {
                // 1. MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the workbook from site Document Library you would like to
                // open;
                // Click Open button;
                word.operateOnOpen(l, path, siteName, docFileName_9814, testUser, DEFAULT_PASSWORD);

                String actualName = word.getAbstractUtil().findWindowName(docFileName_9814);
                // the window name contains "frm" string at the begining
                // Workbook is opened in MS Office Word 2013;
                Assert.assertTrue(actualName.contains(docFileName_9814) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9814);

                // Checkout the document
                word.checkOutOffice(l1);

                // 1. Make some changes to the workbook;
                Ldtp l2 = word.getAbstractUtil().setOnWindow(docFileName_9814);
                String newContent = "new input data";
                word.editOffice(l2, newContent);

                // 2. Cick File->Info button;
                // 3. Click Check In button;
                // 3. Versions comment window pops up;
                // 4. Enter a comment for this version;
                // 5. Click OK button;
                String commentFromWord = "comment from word file check in";
                word.checkInOffice(l2, commentFromWord, false);

                // TODO: 5. Version window is closed; Information message (i) Server
                // read-only; This file was opened from server in read-only mode and to
                // buttons: Edit
                // Workbook and (X) close;

                String actualName2 = word.getAbstractUtil().findWindowName(docFileName_9814);
                Assert.assertTrue(actualName2.contains("[Read-Only]"), "Word is NOT opened in read only mode");

                // 6. Cick File->Info button;
                // word.goToFile(l);
                // word.clickOnObject(l, "Info");

                // TODO: 7. Verify the comment is displayed near the version (Put cursor
                // on comment icon to see it);

                // 8. Log into Share;
                ShareUser.login(drone, testUser);
                DocumentDetailsPage detailsPage;
                // 9. Navigate the workbook; Verify changes are applied; Version history
                // contains the entered comment;
                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
                detailsPage = documentLibPage.selectFile(docFileName_9814 + fileType).render();

                // 9. Document is checked in; Changes are applied; Comment is
                // successfully displayed in Versions history;
                Assert.assertTrue(detailsPage.isVersionHistoryPanelPresent(), "Version History section is not present");
                String commentFromShare = detailsPage.getCommentsOfLastCommit();
                Assert.assertEquals(commentFromShare, commentFromWord);
                
                String documentContent = detailsPage.getDocumentBody();
                Assert.assertTrue(documentContent.contains(newContent), "Changes are not present in the document.");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9815() throws Exception
        {

                // 1. MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the workbook from site Document Library you would like to
                // open;
                // Click Open button;

                word.operateOnOpen(l, path, siteName, docFileName_9815, testUser, DEFAULT_PASSWORD);

                String actualName = word.getAbstractUtil().findWindowName(docFileName_9815);
                Assert.assertTrue(actualName.contains(docFileName_9815) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9815);

                // Checkout the document
                word.checkOutOffice(l1);

                // 1. Make some changes to the workbook;
                String newContent = "new input data";
                word.editOffice(l1, newContent);

                // Save the document
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                word.getAbstractUtil().clickOnObject(l1, "mnuSave");

                // 2. Cick File->Info button;
                // 3. Click Check In button;
                // 4. Enter a comment for this version;
                // 5. Select Keep the word document checked out after checking in this
                // version;
                // 6. Click OK button;
                String commentFromWord = "comment from word file check in";
                word.checkInOffice(l1, commentFromWord, true);

                // 7. Log into Share;
                ShareUser.login(drone, testUser);

                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                FileDirectoryInfo fileInfo = ShareUserSitePage.getFileDirectoryInfo(drone, docFileName_9815 + fileType);

                assertEquals(fileInfo.getContentInfo(), "This document is locked by you for offline editing.", "File " + docFileName_9815 + " isn't locked");

                documentLibPage.getFileDirectoryInfo(docFileName_9815 + fileType).selectCancelEditing().render();

                // 8. Navigate the document;
                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9815 + fileType).render();

                String documentContent = detailsPage.getDocumentBody();
                Assert.assertTrue(documentContent.contains(newContent), "Changes are not present in the document.");

                // 8. Changes are applied to the original file; Version is increased to
                // new major one.
                Assert.assertFalse(detailsPage.isCheckedOut(), "The document is checkout");
                Assert.assertEquals(detailsPage.getDocumentVersion(), "2.0");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9816() throws Exception
        {

                // 1. MS Office word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the workbook from site Document Library you would like to
                // open;
                // Click Open button;

                word.operateOnOpen(l, path, siteName, docFileName_9816, testUser, DEFAULT_PASSWORD);

                String actualName = word.getAbstractUtil().findWindowName(docFileName_9816);
                Assert.assertTrue(actualName.contains(docFileName_9816) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9816);

                // Checkout the document
                word.checkOutOffice(l1);

                // 1. Make some changes to the file;
                String newContent = "new input data";
                word.editOffice(l1, newContent);

                // Save the document
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                word.getAbstractUtil().clickOnObject(l1, "mnuSave");

                // 2. Cick File->Info button;
                word.goToFile(l1);

                word.getAbstractUtil().clickOnObject(l1, "Info");

                // 3. Click Discard check out button;
                word.getAbstractUtil().clickOnObject(l1, "DiscardCheckOut");

                // 4. Click Yes button;
                word.getAbstractUtil().clickOnObject(l1, "Yes");

                // TODO: Verify ERROR CHECK OUT
                word.goToFile(l1);

                // 5. Log into Share;
                ShareUser.login(drone, testUser);

                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                // 6. Navigate the document;
                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9816 + fileType);

                // 6. Changes are not applied to the original file; Version is not
                // increased to new major one.
                String documentContent = detailsPage.getDocumentBody();
                Assert.assertFalse(documentContent.contains(newContent), "Changes are applied to the document.");
                String version = detailsPage.getDocumentVersion();
                if (!version.isEmpty())
                {
                        assertEquals(version, "1.0");
                }

        }

//        Keep manual - an Allow popup cannot be catch by WebDriver
//        @Test(groups = "alfresco-one")
//        public void AONE_9824() throws Exception
//        {
//                // 1. MS Office Word 2013 is opened;
//                Ldtp l = word.openOfficeApplication();
//                word.navigateToOpenSharePointBrowse(l);
//
//                String path = getPathSharepoint(drone);
//
//                // Type url into File name field (e.g. http://<host>:7070/alfresco);
//                // Select the workbook from site Document Library you would like to
//                // open;
//                // Click Open button;
//
//                word.operateOnOpen(l, path, siteName, docFileName_9824, testUser, DEFAULT_PASSWORD);
//
//                String actualName = word.getAbstractUtil().findWindowName(docFileName_9824);
//
//                Assert.assertTrue(actualName.contains(docFileName_9824) && actualName.contains("Word"), "File not found");
//
//                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9824);
//
//                // Checkout and check In the document
//                word.checkOutOffice(l1);
//
//                // Make some changes to the workbook;
//                word.editOffice(l1, "new input data");
//
//                // Save the document
//                word.goToFile(l1);
//                word.getAbstractUtil().clickOnObject(l1, "Info");
//                word.getAbstractUtil().clickOnObject(l1, "mnuSave");
//
//                // Cick File->Info button;
//                // Click Check In button;
//                String commentFromWord = "comment from word file check in";
//                word.checkInOffice(l1, commentFromWord, false);
//
//                // Word Document is opened in read-only mode;
//                actualName = word.getAbstractUtil().findWindowName(docFileName_9824);
//                Assert.assertTrue(actualName.contains("[Read-Only]"), "Word is NOT opened in read only mode");
//                word.closeOfficeApplication(docFileName_9824);
//
//                // 6. User logged into share;              
//                ShareUser.login(drone, testUser, DEFAULT_PASSWORD);
//                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);
//                Assert.assertTrue(documentLibPage.isFileVisible(docFileName_9824 + fileType), "The saved document is not displayed.");
//                
//                // 1. Update the document;
//                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9824 + fileType);
//                detailsPage.selectOnlineEdit().render();     
//                
//                Ldtp ldtp1 = word.getAbstractUtil().getLdtp();
//                word.operateOnSecurity(ldtp1, testUser, DEFAULT_PASSWORD);  
//                
//                ldtp1 = word.getAbstractUtil().setOnWindow(docFileName_9824);
//                word.editOffice(ldtp1, "edit via Share");
//                // Save the document
//                word.goToFile(ldtp1);
//                word.getAbstractUtil().clickOnObject(ldtp1, "Info");
//                word.getAbstractUtil().clickOnObject(ldtp1, "mnuSave");
//                              
//                // 2. In MS Word click File-INfo;
//                word.goToFile(ldtp1);
//                word.getAbstractUtil().clickOnObject(ldtp1, "Info");
//                
//                // 3. Expand Versions menu and select Refresh server versions list;
//                word.getAbstractUtil().clickOnObject(ldtp1, "ManageVersions");
//                ldtp1.waitTime(2);
//                ldtp1.keyPress("tab");
//                ldtp1.keyPress("enter");
//                ldtp1.click("Check Out");
//                ldtp1.click("Refresh Server Versions List");
//                
//                // 3. New version created via Share is added to the list;
//                String fileVersion = "1.1";
//                String fileVersionObject = "btn" + fileVersion.replace(".", "");
//
//                Assert.assertTrue(word.getAbstractUtil().isObjectDisplayed(ldtp1, fileVersionObject), "Object with version " + fileVersion + " is not displayed");
//
//                // 4. Click Edit Document button;  - Step not implemented because no Edit Document button is present either in Share or MS Word
//
//        }

        @Test(groups = "alfresco-one")
        public void AONE_9825() throws Exception
        {
                // 1. MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the file from site Document Library you would like to open;
                // Click Open button;

                word.operateOnOpen(l, path, siteName, docFileName_9825, testUser, DEFAULT_PASSWORD);
                String actualName = word.getAbstractUtil().findWindowName(docFileName_9825);

                Assert.assertTrue(actualName.contains(docFileName_9825) && actualName.contains("Word"), "File not found");
                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9825);

                // Checkout the document
                word.checkOutOffice(l1);

                // Make some changes to the file;
                String newValue =  "new input data";
                word.editOffice(l1, newValue);

                // Save the document
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                word.getAbstractUtil().clickOnObject(l1, "mnuSave");

                // Cick File->Info button;
                // 1.Click Check In button;
                // 2. Don't enter a comment for this version;
                // 3. Click OK button;
                String commentFromExcel = "";
                word.checkInOffice(l1, commentFromExcel, false);

                // 4. Log into Share;
                ShareUser.login(drone, testUser);

                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();

                // 5. Navigate the document; Verify changes are applied; Version history
                // contains is increased to major; no comment is displayed for the
                // version;
                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9825 + fileType).render();
                Assert.assertFalse(detailsPage.isCheckedOut(), "The document is checkout");
                String version = detailsPage.getDocumentVersion();
                Assert.assertEquals(version, "2.0");

                String documentContent = detailsPage.getDocumentBody();
                Assert.assertFalse(documentContent.contains(newValue), "Changes are  applied to the document.");

                String emptyComment = detailsPage.getCommentsOfLastCommit();
                Assert.assertEquals(emptyComment, "(No Comment)");

        }

        @Test(groups = "alfresco-one")
        public void AONE_9826() throws Exception
        {
                // 1. MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the file from site Document Library you would like to open;
                // Click Open button;

                word.operateOnOpen(l, path, siteName, docFileName_9826, testUser, DEFAULT_PASSWORD);
                String actualName = word.getAbstractUtil().findWindowName(docFileName_9826);
                Assert.assertTrue(actualName.contains(docFileName_9826) && actualName.contains("Word"), "File not found");

                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9826);

                // Checkout the document
                word.checkOutOffice(l1);

                // Make some changes to the file;
                String newValue = "new input data";
                word.editOffice(l1, newValue);

                // Save the document
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                word.getAbstractUtil().clickOnObject(l1, "mnuSave");

                // Cick File->Info button;
                // 1.Click Check In button;
                // 2. Don't enter a comment for this version;
                // 3. Click OK button;
                String commentFromW = "a(e.g. !@#$%^&*()_+|\\/?.,:;\"'`=-{}[]";
                word.checkInOffice(l1, commentFromW, false);

                // 4. Log into Share;
                ShareUser.login(drone, testUser);

                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName).render();

                // 5. Navigate the document; Verify changes are applied; Version history
                // contains is increased to major; no comment is displayed for the
                // version;
                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9826 + fileType).render();
                Assert.assertFalse(detailsPage.isCheckedOut(), "The document is checkout");
                String version = detailsPage.getDocumentVersion();
                Assert.assertEquals(version, "2.0");

                String documentContent = detailsPage.getDocumentBody();
                Assert.assertFalse(documentContent.contains(newValue), "Changes are applied to the document.");

                String fileComment = detailsPage.getCommentsOfLastCommit();
                Assert.assertEquals(fileComment, commentFromW);

        }

        @Test(groups = "alfresco-one")
        public void AONE_9827() throws Exception
        {
                // 1. MS Office Word 2013 is opened;
                Ldtp l = word.openOfficeApplication();
                word.navigateToOpenSharePointBrowse(l);

                String path = getPathSharepoint(drone);

                // Type url into File name field (e.g. http://<host>:7070/alfresco);
                // Select the workbook from site Document Library you would like to
                // open;
                // Click Open button;

                word.operateOnOpen(l, path, siteName, docFileName_9827, testUser, DEFAULT_PASSWORD);
                String actualName = word.getAbstractUtil().findWindowName(docFileName_9827);

                Assert.assertTrue(actualName.contains(docFileName_9827) && actualName.contains("Word"), "File not found");
                Ldtp l1 = word.getAbstractUtil().setOnWindow(docFileName_9827);

                // Checkout the document
                word.checkOutOffice(l1);

                // 1. Make some changes to the workbook;
                word.editOffice(l1, "new input data");

                // Save the document
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");
                word.getAbstractUtil().clickOnObject(l1, "mnuSave");

                // 2. Cick File->Info button;
                word.goToFile(l1);
                word.getAbstractUtil().clickOnObject(l1, "Info");

                // 3.Click Check In button;
                word.getAbstractUtil().clickOnObject(l1, "CheckIn");

                // 4. Enter a comment for this version;
                // 5. Click Cancel button;
                String commentFromW = "comment";
                l1.enterString("txtVersionComments", commentFromW);
                l1.mouseLeftClick("btnCancel");

                // 6. Log into Share;
                ShareUser.login(drone, testUser);

                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                // 7. Excel Document is still checked out; Changes are not applied;
                DocumentDetailsPage detailsPage = documentLibPage.selectFile(docFileName_9827 + fileType);
                assertEquals(detailsPage.getContentInfo(), "This document is locked by you for offline editing.", "File " + docFileName_9827 + " isn't locked");

                String documentContent = detailsPage.getDocumentBody();
                Assert.assertFalse(documentContent.contains(docFileName_9827), "Changes are applied to the document.");
        }

        @Test(groups = "alfresco-one")
        public void AONE_9828() throws Exception
        {
                String testName = getTestName() + "3";
                DocumentLibraryPage customDocumentLibPage;
                WebDrone thisDrone;
                DocumentDetailsPage detailsPage;
                DocumentDetailsPage customDetailsPage;

                setupCustomDrone(WebDroneType.HybridDrone);
                thisDrone = customDrone;

                // Create User
                String testUser2 = getUserNameFreeDomain(testName);
                String[] testUserInfo = new String[] { testUser2 };
                CreateUserAPI.CreateActivateUser(drone, ADMIN_USERNAME, testUserInfo);

                // 1. Connect to Share with user1 and browse to document library.
                ShareUser.login(drone, testUser);

                // User2 is invited to the site
                ShareUserMembers.inviteUserToSiteWithRole(drone, testUser, testUser2, siteName, UserRole.MANAGER);
                documentLibPage = ShareUser.openSitesDocumentLibrary(drone, siteName);

                // 2. Upload a word document in the document library and open its
                // details page.
                String fileName = getFileName(testName) + "3.txt";
                String[] fileInfo = { fileName, DOCLIB };
                documentLibPage = ShareUser.uploadFileInFolder(drone, fileInfo);

                detailsPage = documentLibPage.selectFile(fileName).render();

                // 3. In another browser or client machine, login to Share as user2, and
                // browse to the same document details page.
                ShareUser.login(thisDrone, testUser2, DEFAULT_PASSWORD);
                customDocumentLibPage = ShareUser.openSitesDocumentLibrary(thisDrone, siteName);
                customDetailsPage = customDocumentLibPage.selectFile(fileName).render();

                // 4. As user1, click "Edit Offline". Document should show up as locked
                // by user1 in user1's Share UI.
                detailsPage = ShareUser.openDocumentDetailPage(drone, fileName).render();
                detailsPage.selectEditOffLine(null).render();
                Assert.assertTrue(detailsPage.isCheckedOut());

                // 5. As user2, without refreshing the page, click "Edit Online", then
                // click OK.
                customDetailsPage = ShareUser.openDocumentDetailPage(thisDrone, fileName).render();
                customDetailsPage.selectEditOffLine();
                Assert.assertTrue(customDetailsPage.isErrorEditOfflineDocument(fileName));
        }

}