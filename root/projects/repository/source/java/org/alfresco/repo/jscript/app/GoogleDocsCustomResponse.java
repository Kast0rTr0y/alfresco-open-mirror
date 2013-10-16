/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.jscript.app;

import org.alfresco.repo.googledocs.GoogleDocsService;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Return current status of Google Docs subsystem
 *
 * @author: mikeh
 */
public class GoogleDocsCustomResponse implements CustomResponse, ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(GoogleDocsCustomResponse.class);

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Populates the DocLib webscript response with custom metadata
     *
     * @return JSONObject or null
     */
    public Serializable populate()
    {
        try
        {
            ApplicationContextFactory subsystem = (ApplicationContextFactory)applicationContext.getBean("googledocs");
            ConfigurableApplicationContext childContext = (ConfigurableApplicationContext)subsystem.getApplicationContext();
            GoogleDocsService googleDocsService = (GoogleDocsService)childContext.getBean("googleDocsService");

            Map<String, Serializable> jsonObj = new LinkedHashMap<String, Serializable>(4);
            jsonObj.put("enabled", googleDocsService.isEnabled());

            return (Serializable)jsonObj;
        }
        catch (Exception e)
        {
            logger.warn("Could not add custom Google Docs status response to DocLib webscript");
        }
        return null;
    }
}
