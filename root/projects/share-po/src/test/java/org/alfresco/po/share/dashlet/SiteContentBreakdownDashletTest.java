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

package org.alfresco.po.share.dashlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.enums.Dashlet;
import org.alfresco.po.share.site.CustomiseSiteDashboardPage;
import org.alfresco.po.share.site.SitePage;
import org.alfresco.po.share.site.UploadFilePage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.util.FailedTestListener;
import org.alfresco.po.share.util.SiteUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * SiteContentBreakdownDashlet test class for site content breakdown report dashlet page object
 * 
 * @author jcule
 */
@Test(groups = { "alfresco-one" })
@Listeners(FailedTestListener.class)
public class SiteContentBreakdownDashletTest extends AbstractSiteDashletTest
{
    private static final String SITE_CONTENT_REPORT = "site-content-report";
    private SiteContentBreakdownDashlet siteContentBreakdownDashlet = null;
    private CustomiseSiteDashboardPage customiseSiteDashBoard = null;
    
    private static final String JPEG_TYPE = "JPEG Image";
    private static final String TXT_TYPE =  "Plain Text"; 
    private static final String DOCX_TYPE = "Microsoft Word";
    private static final String HTML_TYPE = "HTML";
    private static final String PDF_TYPE =  "Adobe PDF Document";

    private static int numberOfTxtFiles = 5;
    private static int numberOfDocxFiles = 4;
    private static int numberOfHtmlFiles = 2;
    private static int numberOfJpgFiles = 3;
    private static int numberOfPdfFiles = 9;

    private DashBoardPage dashBoard;

    @BeforeTest
    public void prepare()
    {
        siteName = "sitecontentreportdashlettest" + System.currentTimeMillis();

    }

    @BeforeClass
    public void loadFiles() throws Exception
    {
        dashBoard = loginAs(username, password);
        SiteUtil.createSite(drone, siteName, "description", "Public");
        SitePage site = drone.getCurrentPage().render();
        DocumentLibraryPage docPage = site.getSiteNav().selectSiteDocumentLibrary().render();
        uploadFiles(docPage, numberOfTxtFiles, ".txt");
        uploadFiles(docPage, numberOfDocxFiles, ".docx");
        uploadFiles(docPage, numberOfJpgFiles, ".jpg");
        uploadFiles(docPage, numberOfHtmlFiles, ".html");
        uploadFiles(docPage, numberOfPdfFiles, ".pdf");

        navigateToSiteDashboard();

    }

    @AfterClass
    public void deleteSite()
    {
        SiteUtil.deleteSite(drone, siteName);
    }

    /**
     * 
     * Uploads files to site's document library
     * 
     * @param docPage
     * @param numberofFiles
     * @param extension
     * @throws IOException
     */
    private void uploadFiles(DocumentLibraryPage docPage, int numberofFiles, String extension) throws IOException
    {
        for (int i = 0; i < numberofFiles; i++)
        {
            String random = UUID.randomUUID().toString();
            File file = SiteUtil.prepareFile(random, random, extension);
            UploadFilePage upLoadPage = docPage.getNavigation().selectFileUpload().render();
            docPage = upLoadPage.uploadFile(file.getCanonicalPath()).render();

        }
    }

    /**
     * Drags and drops Site content report dashlet to site's dashboard
     * 
     */
    @Test
    public void instantiateDashlet()
    {
        customiseSiteDashBoard = siteDashBoard.getSiteNav().selectCustomizeDashboard();
        customiseSiteDashBoard.render();
        siteDashBoard = customiseSiteDashBoard.addDashlet(Dashlet.SITE_CONTENT_REPORT, 2).render();
        siteContentBreakdownDashlet = siteDashBoard.getDashlet(SITE_CONTENT_REPORT).render();
        Assert.assertNotNull(siteContentBreakdownDashlet);
    }

    /**
     * Checks mime types counts of the documents in the site's document library
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods = "instantiateDashlet")
    public void testMimeTypesAndCounts() throws Exception
    {
        List<String> mimeTypesCounts = siteContentBreakdownDashlet.getTypesAndCounts();
        for (String mimeTypesCount : mimeTypesCounts)
        {
            String count = mimeTypesCount.trim().substring(mimeTypesCount.indexOf("-")+2).trim();
            int mimeTypeCounts = Integer.parseInt(count);
                   
            Assert.assertEquals(mimeTypesCounts.size(), 5);
            
            if (mimeTypesCount.trim().startsWith(TXT_TYPE))
            {
                //System.out.println("TYPE-COUNT ++++ " + mimeTypesCount);
                //System.out.println("TXT COUNT **** " + mimeTypeCounts); 
                Assert.assertEquals(mimeTypeCounts, numberOfTxtFiles);
            }
            if (mimeTypesCount.trim().startsWith(JPEG_TYPE))
            {
                //System.out.println("TYPE-COUNT ++++ " + mimeTypesCount);
                //System.out.println("JPEG COUNT **** " + mimeTypeCounts);
                Assert.assertEquals(mimeTypeCounts, numberOfJpgFiles);
            }
            if (mimeTypesCount.trim().startsWith(DOCX_TYPE))
            {
                //System.out.println("TYPE-COUNT ++++ " + mimeTypesCount);
                //System.out.println("DOCX COUNT **** " + mimeTypeCounts);
                Assert.assertEquals(mimeTypeCounts, numberOfDocxFiles);
            }
            if (mimeTypesCount.trim().startsWith(PDF_TYPE))
            {
                //System.out.println("TYPE-COUNT ++++ " + mimeTypesCount);
                //System.out.println("PDF COUNT **** " + mimeTypeCounts);
                Assert.assertEquals(mimeTypeCounts, numberOfPdfFiles);
            }
            if (mimeTypesCount.trim().startsWith(HTML_TYPE))
            {
                //System.out.println("TYPE-COUNT ++++ " + mimeTypesCount);
                //System.out.println("HTML COUNT **** " + mimeTypeCounts);
                Assert.assertEquals(mimeTypeCounts, numberOfHtmlFiles);
            }
        }
    }
    
}
