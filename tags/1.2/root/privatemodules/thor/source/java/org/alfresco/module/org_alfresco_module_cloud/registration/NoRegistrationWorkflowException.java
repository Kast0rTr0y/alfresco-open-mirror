/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

/**
 * This exception is thrown to indicate that a signup workflow instance could not be found.
 * 
 * @author Neil Mc Erlean
 */
public class NoRegistrationWorkflowException extends RegistrationServiceException 
{
    private static final long serialVersionUID = -1272265215297353993L;

    public NoRegistrationWorkflowException(String msgId)
    {
        super(msgId);
    }

    public NoRegistrationWorkflowException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public NoRegistrationWorkflowException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public NoRegistrationWorkflowException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
