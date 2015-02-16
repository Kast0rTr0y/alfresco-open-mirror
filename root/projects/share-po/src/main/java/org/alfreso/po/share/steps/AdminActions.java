package org.alfreso.po.share.steps;

import java.util.List;

import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.GroupsPage;
import org.alfresco.po.share.exception.UnexpectedSharePageException;
import org.alfresco.po.share.site.document.UserProfile;
import org.alfresco.webdrone.WebDrone;

public class AdminActions extends DashBoardActions
{
    /**
     * Navigate to Groups page
     * @param driver WebDriver Instance
     * @return Groups page
     */

    public GroupsPage navigateToGroup(WebDrone driver)
    {
        DashBoardPage dashBoard = openUserDashboard(driver);
        GroupsPage page = dashBoard.getNav().getGroupsPage();
        return page;
    }
    
    /**
     * Returns GroupsPage
     * @param driver WebDriver Instance
     * @return GroupsPage 
     * @throws UnexpectedSharePageException if not a Groups page instance
     */
    private GroupsPage getGroupsPage(WebDrone driver) throws UnexpectedSharePageException
    {
        try
        {
            GroupsPage page = (GroupsPage) getSharePage(driver);
            return page;
        }
        catch(ClassCastException c)
        {
            throw new UnexpectedSharePageException(GroupsPage.class, c);
        }        
    }

    /**
     * Click on browse button in Groups page. 
     * This method only proceeds when the user is on groups page
     * 
     * @param driver WebDriver Instance        
     * @return Groups page
     */
    public GroupsPage browseGroups(WebDrone driver)
    {
            GroupsPage page = getGroupsPage(driver);
            page = page.clickBrowse().render();
            return page;       
    }
    
    /**
     * Verify user is a member of group
     * 
     * @param driver WebDriver Instance
     * @param fname- User's first name
     * @param userName- check whether this user is in group
     * @param groupName - Check whether user in this specific group Name
     * @return Boolean
     */

    public Boolean isUserGroupMember(WebDrone driver, String fName, String uName, String groupName)
    {
        GroupsPage page = browseGroups(driver);
        GroupsPage groupspage = page.selectGroup(groupName).render();
        List<UserProfile> userProfiles = groupspage.getMembersList();

        for (UserProfile userProfile : userProfiles)
        {
            if (fName.equals(userProfile.getfName()))
            {
                // Verify user is present in the members list
                if (userProfile.getUsername().contains(uName))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
