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

package org.alfresco.po.share.user;

import org.alfresco.po.share.*;
import org.alfresco.po.share.util.FailedTestListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Integration test to verify Following Me page elements are in place.
 * Created by Olga Lokhach
 */
@Listeners(FailedTestListener.class)
public class FollowersPageTest extends AbstractTest
{
    private DashBoardPage dashBoard;
    private PeopleFinderPage peopleFinderPage;
    private MyProfilePage myProfilePage;
    private FollowersPage followersPage;
    private String userName1;
    private String userName2;


    @BeforeClass(groups = { "Enterprise-only" }, alwaysRun = true)
    public void setup() throws Exception
    {
        userName1 = "User_1_" + System.currentTimeMillis();
        userName2 = "User_2_" + System.currentTimeMillis();
        createEnterpriseUser(userName1);
        createEnterpriseUser(userName2);
        ShareUtil.loginAs(drone, shareUrl, userName1, UNAME_PASSWORD).render();
    }

    @Test(groups = { "Enterprise-only"})
    public void openFollowersPage()
    {
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        myProfilePage = dashBoard.getNav().selectMyProfile().render();
        followersPage = myProfilePage.getProfileNav().selectFollowers().render();
        assertNotNull(followersPage);
    }

    @Test(groups="Enterprise-only", dependsOnMethods = "openFollowersPage")
    public void isHeaderTitlePresent() throws Exception
    {
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        myProfilePage = dashBoard.getNav().selectMyProfile().render();
        followersPage = myProfilePage.getProfileNav().selectFollowers().render();
        assertTrue(followersPage.isTitlePresent("Followers"), "Title is incorrect");
    }

    @Test(groups="Enterprise-only", dependsOnMethods = "isHeaderTitlePresent")
    public void isNoFollowersMessagePresent() throws Exception
    {
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        myProfilePage = dashBoard.getNav().selectMyProfile().render();
        followersPage = myProfilePage.getProfileNav().selectFollowers().render();
        assertTrue(followersPage.isNoFollowersMessagePresent(), "No Followers message isn't displayed");
        assertEquals(followersPage.getFollowersCount(), "0");
        ShareUtil.logout(drone);
    }

    @Test(groups="Enterprise-only", dependsOnMethods = "isNoFollowersMessagePresent")
    public void isUserLinkPresent() throws Exception
    {
        ShareUtil.loginAs(drone, shareUrl, userName2, UNAME_PASSWORD).render();
        SharePage page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        peopleFinderPage = dashBoard.getNav().selectPeople().render();
        peopleFinderPage = peopleFinderPage.searchFor(userName1).render();
        List<ShareLink> searchLinks = peopleFinderPage.getResults();
        if (!searchLinks.isEmpty())
        {
            for (ShareLink result : searchLinks)
            {
                if (result.getDescription().contains(userName1))
                {
                    peopleFinderPage.selectFollowForUser(userName1);
                }
            }
        }
        else
        {
            fail(userName1 + " is not found");
        }
        assertEquals(peopleFinderPage.getTextForFollowButton(userName1), "Unfollow");
        ShareUtil.logout(drone);
        ShareUtil.loginAs(drone, shareUrl, userName1, UNAME_PASSWORD).render();
        page = drone.getCurrentPage().render();
        dashBoard = page.getNav().selectMyDashBoard();
        myProfilePage = dashBoard.getNav().selectMyProfile().render();
        followersPage = myProfilePage.getProfileNav().selectFollowers().render();
        assertTrue(followersPage.isUserLinkPresent(userName2), "Can't find " + userName2);
        assertEquals(followersPage.getFollowersCount(), "1");
    }
}
