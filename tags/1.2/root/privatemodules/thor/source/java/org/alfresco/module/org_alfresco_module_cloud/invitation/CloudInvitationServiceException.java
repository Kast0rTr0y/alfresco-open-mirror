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
package org.alfresco.module.org_alfresco_module_cloud.invitation;

import org.alfresco.error.AlfrescoRuntimeException;

public class CloudInvitationServiceException extends AlfrescoRuntimeException 
{
    private static final long serialVersionUID = -1272265215297353993L;

    public CloudInvitationServiceException(String msgId)
    {
        super(msgId);
    }

    public CloudInvitationServiceException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public CloudInvitationServiceException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public CloudInvitationServiceException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}