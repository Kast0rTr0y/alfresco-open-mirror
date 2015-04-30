package org.alfresco.filesys.repo.rules;


import java.util.ArrayList;
import java.util.Date;

import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.commands.SoftRenameFileCommand;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.FileName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A rename create scenario is ...
 * 
 * a) Rename File to File~
 * b) Create File
 * 
 * This rule will kick in and copy the content and then switch the two file over. 
 * 
 */
class ScenarioRenameCreateShuffleInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioRenameShuffleInstance.class);
      
    private Date startTime = new Date();
    
    /**
     * Timeout in ms.  Default 30 seconds.
     */
    private long timeout = 30000;
    
    private boolean isComplete = false;
    
    private Ranking ranking = Ranking.HIGH;
    
    enum InternalState
    {
        NONE,
        INITIALISED,
        LOOK_FOR_DELETE
    } ;
    
    InternalState state = InternalState.NONE;
    
    String from;
    String to;
    
    /**
     * Evaluate the next operation
     * @param operation
     */
    public Command evaluate(Operation operation)
    {                
        /**
         * Anti-pattern : timeout
         */
        Date now = new Date();
        if(now.getTime() > startTime.getTime() + getTimeout())
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Instance timed out");
            }
            isComplete = true;
            return null;
        }
        
        switch (state)
        {
            case NONE:
                if(operation instanceof RenameFileOperation)
                {
                    logger.debug("New scenario initialised");
                    RenameFileOperation r = (RenameFileOperation)operation;
                    this.from = r.getFrom();
                    this.to = r.getTo();
                    state = InternalState.INITIALISED;
                    
                    SoftRenameFileCommand r1 = new SoftRenameFileCommand(from, to, r.getRootNodeRef(), r.getFromPath(), r.getToPath());
                    isComplete = true;
                    return r1;
                }
                break;
                
         }

             
        return null;
    }
    
    @Override
    public boolean isComplete()
    {
        return isComplete;
    }
    
    public String toString()
    {
        return "ScenarioRenameCreateShuffleInstance from:" + from + " to:" + to;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }
    
    @Override
    public Ranking getRanking()
    {
        return ranking;
    }
    
    public void setRanking(Ranking ranking)
    {
        this.ranking = ranking;
    }
}
