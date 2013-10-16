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

package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingQueuePost extends PublishingWebScript
{
    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String content = null;
        try
        {
            content = WebScriptUtil.getContent(req);
            if (content == null || content.isEmpty())
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "No publishing event was posted!");
            }
            String eventId = jsonParser.schedulePublishingEvent(publishingService, content);
            PublishingEvent event = publishingService.getPublishingEvent(eventId);
            Map<String, Object> eventModel = builder.buildPublishingEvent(event, channelService);
            return WebScriptUtil.createBaseModel(eventModel);
        }
        catch (WebScriptException we)
        {
            throw we;
        }
        catch (Exception e)
        {
            String msg = "Failed to schedule publishing event. POST body: " + content;
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, e);
        }
    }
}
