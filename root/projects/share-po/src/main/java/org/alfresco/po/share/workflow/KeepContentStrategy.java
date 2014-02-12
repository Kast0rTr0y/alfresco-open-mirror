/*
 * Copyright (C) 2005-2013s Alfresco Software Limited.
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

package org.alfresco.po.share.workflow;

/**
 * @author Abhijeet Bharade
 * 
 */
public enum KeepContentStrategy
{
    
    KEEPCONTENT("Keep content synced on cloud"), 
    KEEPCONTENTREMOVESYNC("Keep content on cloud and remove sync"), 
    DELETECONTENT("Delete content on cloud and remove sync");

    private String strategy;

    KeepContentStrategy(String strategy)
    {
        this.strategy = strategy;
    }

    public String getStrategy()
    {
        return strategy;
    }

}
