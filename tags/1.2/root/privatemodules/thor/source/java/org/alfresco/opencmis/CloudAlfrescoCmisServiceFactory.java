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
package org.alfresco.opencmis;

import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.repo.tenant.TenantAdminService;

/**
 * Override factory for OpenCMIS service objects - for cloud network/tenant switching
 * 
 * @author janv
 * @since Alfresco Cloud Module
 */
public class CloudAlfrescoCmisServiceFactory extends AlfrescoCmisServiceFactory
{
    private TenantAdminService tenantAdminService;
    private NetworksService networksService;

    public void setNetworksService(NetworksService networksService)
    {
		this.networksService = networksService;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    @Override
    protected AlfrescoCmisService getCmisServiceTarget(CMISConnector connector)
    {
        return new CloudAlfrescoCmisService(connector, tenantAdminService, networksService);
    }
}
