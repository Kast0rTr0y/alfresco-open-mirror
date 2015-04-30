/*
 * Copyright 2005-2012 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.enterprise.repo.management;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

/**
 * 
 * @since Odin
 *
 */
public interface ClusterNodeInfoMBean
{
    public String getId();
    public String getAddress();
    public TabularData getPeers() throws OpenDataException;
}
