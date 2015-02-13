package org.alfreso.po.share.steps;

import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.site.SiteMembersPage;
import org.alfresco.webdrone.HtmlPage;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.exception.PageException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShareUser extends AbstractSteps
{

    private static Log logger = LogFactory.getLog(ShareUser.class);
    
    
    /**
     * Navigate to User DashBoard page and waits for the page render to
     * complete. Assumes User is logged in
     * 
     * @param driver WebDrone Instance
     * @return DashBoardPage
     */
    public static DashBoardPage refreshUserDashboard(WebDrone driver)
    {
        // Assumes User is logged in
        SharePage page = getSharePage(driver);

        page.getNav().selectMyDashBoard().render();
        logger.info("Opened User Dashboard");
        return new DashBoardPage(driver).render();
    }
    
    /**
     * Navigate to User DashBoard and waits for the page render to complete.
     * Assumes User is logged in
     * 
     * @param driver WebDrone Instance
     * @return DashBoardPage
     */
    public static DashBoardPage openUserDashboard(WebDrone driver)
    {
        // Assumes User is logged in
        SharePage page = getSharePage(driver);
        if (page.getPageTitle().contains(MY_DASHBOARD))
        {
            logger.info("User Dashboard already Open");
            return (DashBoardPage) page;
        }

        return refreshUserDashboard(driver);
    }
}
