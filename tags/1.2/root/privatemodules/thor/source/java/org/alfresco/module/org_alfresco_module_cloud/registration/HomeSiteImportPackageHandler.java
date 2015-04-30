/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_cloud.registration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterException;

/**
 * Handler for importing home site surf config
 */
public class HomeSiteImportPackageHandler implements ImportPackageHandler
{
    private final static String SITEID_PLACEHOLDER = "${siteId}";
    
    private HomeSiteSurfConfig config;
    private String siteId;

    public HomeSiteImportPackageHandler(HomeSiteSurfConfig config, String siteId)
    {
        this.config = config;
        this.siteId = siteId;
    }

    @Override
    public void startImport()
    {
    }

    @Override
    public Reader getDataStream()
    {
        return new StringReader(config.getImportView());
    }

    @Override
    public InputStream importStream(String contentPath)
    {
        String content = config.getImportContent(contentPath);
        if (content == null)
        {
            return null;
        }
        
        String siteContent = content.replace(SITEID_PLACEHOLDER, siteId);
        try
        {
            return new ByteArrayInputStream(siteContent.getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e)
        {
            throw new ImporterException("Failed to read content " + contentPath, e);
        }
    }

    @Override
    public void endImport()
    {
    }
}
