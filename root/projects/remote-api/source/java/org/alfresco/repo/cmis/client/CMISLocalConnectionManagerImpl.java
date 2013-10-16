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
package org.alfresco.repo.cmis.client;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl;
import org.alfresco.opencmis.AlfrescoLocalCmisServiceFactory;
import org.alfresco.opencmis.CMISConnector;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

/**
 * CMIS Connection manager with a local default connection.
 */
public class CMISLocalConnectionManagerImpl extends AbstractCMISConnectionManagerImpl implements CMISConnectionManager
{
    /**
     * Sets the CMIS connector.
     */
    public void setCmisConnector(CMISConnector connector)
    {
        AlfrescoLocalCmisServiceFactory.setCmisConnector(connector);
    }

    @Override
    public CMISConnection createDefaultConnection(CMISServer server)
    {
        throw new IllegalStateException("Local connection cannot be changed!");
    }

    /**
     * Creates a local connection.
     */
    @Override
    public CMISConnection getConnection()
    {
        lock.writeLock().lock();
        try
        {
            CMISConnection connection = getConnection(DEFAULT_CONNECTION_ID);
            if (connection != null)
            {
                return connection;
            }

            String currentUser = authenticationService.getCurrentUserName();

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.LOCAL.value());
            parameters.put(SessionParameter.LOCAL_FACTORY, AlfrescoLocalCmisServiceFactory.class.getName());
            parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, AlfrescoObjectFactoryImpl.class.getName());
            parameters.put(SessionParameter.USER, currentUser);

            CMISServer server = createServerDefinition("default", parameters);
            Session session = createSession(server.getParameters());
            String userConnectionId = createUserConnectionId(currentUser, DEFAULT_CONNECTION_ID);

            connection = new CMISConnectionImpl(this, userConnectionId, session, server, currentUser, true, false);

            userConnections.put(userConnectionId, connection);

            return connection;
        } finally
        {
            lock.writeLock().unlock();
        }
    }
}
