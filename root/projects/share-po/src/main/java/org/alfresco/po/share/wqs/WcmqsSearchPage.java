package org.alfresco.po.share.wqs;

import org.alfresco.po.share.site.document.PaginationForm;
import org.alfresco.webdrone.RenderTime;
import org.alfresco.webdrone.RenderWebElement;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.exception.PageException;
import org.alfresco.webdrone.exception.PageOperationException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucian Tuca on 11/18/2014.
 */
public class WcmqsSearchPage extends WcmqsAbstractPage
{
    @RenderWebElement
    private final By TAG_SEARCH_RESULT_TITLES = By.cssSelector(".newslist-wrapper>li>h4>a");

    private final By NO_OF_SEARCH_RESULTS = By.cssSelector("p.intheader-paragraph");
    private final By LATEST_BLOG_ARTICLES = By.cssSelector("div[id='right']>div[class='latest-news']");
    private final By PAGINATION = By.xpath("//div[@class='pagination']");
    

    /**
     * Constructor.
     * 
     * @param drone WebDriver to access page
     */
    public WcmqsSearchPage(WebDrone drone)
    {
        super(drone);
    }

    @SuppressWarnings("unchecked")
    @Override
    public WcmqsSearchPage render(RenderTime timer)
    {
        webElementRender(timer);
        return this;

    }

    @SuppressWarnings("unchecked")
    @Override
    public WcmqsSearchPage render()
    {
        return render(new RenderTime(maxPageLoadingTime));
    }

    @SuppressWarnings("unchecked")
    @Override
    public WcmqsSearchPage render(final long time)
    {
        return render(new RenderTime(time));
    }

    /**
     * Method to get tag search result titles
     */
    public ArrayList<String> getTagSearchResults()

    {
        ArrayList<String> results = new ArrayList<String>();
        try
        {
            List<WebElement> links = drone.findAndWaitForElements(TAG_SEARCH_RESULT_TITLES);
            for (WebElement div : links)
            {
                results.add(div.getText());
            }
        }
        catch (NoSuchElementException nse)
        {
            throw new PageException("Unable to access search results site data", nse);
        }

        return results;
    }

    /**
     * Method verifies the number of search results
     */
    public boolean verifyNumberOfSearchResultsHeader(int noOfResults, String searchedText)
    {
        String resultsText = String.format("Showing %d of %d results for \"%s\" within the website...", noOfResults, noOfResults, searchedText);
        return resultsText.equals(drone.findAndWait(NO_OF_SEARCH_RESULTS).getText());
    }

    /**
     * Method returns if the Latest Blog Articles block is displayed
     */
    public boolean isLatestBlogArticlesDisplayed()
    {
        try
        {
            drone.findAndWait(LATEST_BLOG_ARTICLES);
            return true;
        }
        catch (TimeoutException e)
        {
            throw new PageOperationException("Exceeded time to find Latest Blog Articles block. " + e.toString());
        }
    }

    /**
     * Method returns the pagination text
     * @return
     */
    public String getWcmqsSearchPagePagination()
    {
        try
        {
            return drone.findAndWait(PAGINATION).getText();
        }
        catch (TimeoutException e)
        {
            throw new PageOperationException("Exceeded time to find Pagination. " + e.toString());
        }
    }
    
    /**
     * Method to get latest blog articles titles
     */
    public ArrayList<String> getLatestBlogArticles()

    {
        ArrayList<String> blogArticles = new ArrayList<String>();
        try
        {
            List<WebElement> links = drone.findAndWaitForElements(LATEST_BLOG_ARTICLES);
            for (WebElement div : links)
            {
                blogArticles.add(div.getText());
            }
        }
        catch (NoSuchElementException nse)
        {
            throw new PageException("Unable to find Latest Blog Articles", nse);
        }

        return blogArticles;
    }
    
    /**
     * Method to click a news title
     * @param blogArticleTitle - the title of the blog article in wcmqs site
     */
    public void clickLatestBlogArticle(String blogArticleTitle)
    {
        try
        {
            drone.findAndWait(By.xpath(String.format("//a[text()=\"%s\"]", blogArticleTitle))).click();
        }
        catch (TimeoutException e)
        {
            throw new PageOperationException("Exceeded time to find news link. " + e.toString());
        }
    }
}
