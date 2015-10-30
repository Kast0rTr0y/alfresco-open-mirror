/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.invitation.site;

import java.util.Map;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets completed
 * along the "accept" transition
 * 
 * @author glen johnson at alfresco com
 * @author Nick Smith
 */
public class AcceptInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = 8133039174866049136L;

    /**
    * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> executionVariables = executionContext.getContextInstance().getVariables();
        String invitee = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String siteName = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarResourceName);
        String inviter = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviterUserName);
        String role = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarRole);
        
        invitationService.acceptNominatedInvitation(siteName, invitee, role, inviter);
    }
}
