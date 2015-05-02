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
package org.alfresco.module.org_alfresco_module_cloud.site;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.site.SiteService;

public interface CloudSiteService extends SiteService
{
    // filter list based on home tenant / site membership - note: maintain sort order
    public List<String> filterVisibleUsers(List<String> userNames, int maxItems);
    
    public boolean isSameHomeTenant(Long account1, Long account2);
    public Set<String> getAllSiteMemberships(String username);
}