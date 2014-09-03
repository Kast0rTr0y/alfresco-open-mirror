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
package org.alfresco.po.share.search;

import org.alfresco.webdrone.HtmlPage;

/**
 * Interface that represent search result row.
 * @author Michael Suzuki
 * @since 2.5
 *
 */
public interface SearchResult
{
    /**
     * Title of search result item.
     * 
     * @return String title
     */
     String getTitle();
     /**
      * Name of search result item.
      * 
      * @return String title
      */
     String getName();
    /**
     * Select the link of the search result item.
     * 
     * @return true if link found and selected
     */
     HtmlPage clickLink();
     /**
      * Verify if folder or not, true if search row represent
      * a folder.
      * 
      * @return boolean true if search result is of folder
      */
     boolean isFolder();
}
