package org.alfresco.po.share.wqs;

import static org.alfresco.webdrone.RenderElement.getVisibleRenderElement;

import org.alfresco.webdrone.RenderTime;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.exception.PageOperationException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

public abstract class WcmqsAbstractArticlePage extends WcmqsAbstractPage
{
    protected final By EDIT_LINK = By.cssSelector("a.alfresco-content-edit");
    protected final By CREATE_LINK = By.cssSelector("a.alfresco-content-new");
    protected final By DELETE_LINK = By.cssSelector("alfresco-content-delete");
    
    public WcmqsAbstractArticlePage(WebDrone drone)
    {
        super(drone);
    }

    @SuppressWarnings("unchecked")
    @Override
    public WcmqsAbstractArticlePage render(RenderTime renderTime)
    {
        elementRender(renderTime, getVisibleRenderElement(EDIT_LINK), getVisibleRenderElement(CREATE_LINK), getVisibleRenderElement(DELETE_LINK));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public WcmqsAbstractArticlePage render()
    {
        return render(new RenderTime(maxPageLoadingTime));
    }

    @SuppressWarnings("unchecked")
    @Override
    public WcmqsAbstractArticlePage render(final long time)
    {
        return render(new RenderTime(time));
    }
    
    public WcmqsEditPage clickEditButton()
    {
        try
        {
            drone.findAndWait(EDIT_LINK).click();
            return new WcmqsEditPage(drone);
        }
        catch (TimeoutException e)
        {
            throw new PageOperationException("Exceeded time to find edit button. " + e.toString());
        }
    }
}