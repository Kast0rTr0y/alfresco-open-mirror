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
package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.enums.Action;

public class CurrentVersionEvaluator extends AbstractActionEvaluator
{
    private CMISActionEvaluator currentVersionEvaluator;
    private boolean currentVersionValue;
    private boolean nonCurrentVersionValue;

    /**
     * Construct
     *
     * @param serviceRegistry ServiceRegistry
     * @param action Action
     * @param currentVersionValue boolean
     * @param nonCurrentVersionValue boolean
     */
    protected CurrentVersionEvaluator(ServiceRegistry serviceRegistry, Action action, boolean currentVersionValue,
            boolean nonCurrentVersionValue)
    {
        super(serviceRegistry, action);
        this.currentVersionValue = currentVersionValue;
        this.nonCurrentVersionValue = nonCurrentVersionValue;
    }

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    protected CurrentVersionEvaluator(ServiceRegistry serviceRegistry, CMISActionEvaluator currentVersionEvaluator,
            boolean nonCurrentVersionValue)
    {
        super(serviceRegistry, currentVersionEvaluator.getAction());
        this.currentVersionEvaluator = currentVersionEvaluator;
        this.nonCurrentVersionValue = nonCurrentVersionValue;
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if(nodeInfo.hasPWC())
        {
            if(!nodeInfo.isPWC())
            {
                return nonCurrentVersionValue;
            }
        }
        else
        {
            if (!nodeInfo.isCurrentVersion())
            {
                return nonCurrentVersionValue;
            }
        }
        

        return currentVersionEvaluator == null ? currentVersionValue : currentVersionEvaluator.isAllowed(nodeInfo);
    }
}
