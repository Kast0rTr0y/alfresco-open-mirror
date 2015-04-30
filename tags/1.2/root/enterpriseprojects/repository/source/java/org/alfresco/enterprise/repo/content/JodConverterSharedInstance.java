/*
 * Copyright 2005-2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.enterprise.repo.content;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Makes use of the {@link http://code.google.com/p/jodconverter/} library and an installed
 * OpenOffice application to perform OpenOffice-driven conversions.
 * 
 * @author Neil McErlean
 */
public class JodConverterSharedInstance implements InitializingBean, DisposableBean, JodConverter
{
    private static Log logger = LogFactory.getLog(JodConverterSharedInstance.class);

    private OfficeManager officeManager;
    private boolean isAvailable = false;
    
    // JodConverter's built-in configuration settings.
    //
    // These properties are set by Spring dependency injection at system startup in the usual way.
    // If the values are changed via the JMX console at runtime, then the subsystem will be stopped
    // and can be restarted with the new values - meaning that JodConverter will also be stopped and restarted.
    // Therefore there is no special handling required for changes to e.g. portNumbers which determines
    // the number of OOo instances there should be in the pool.
    //
    // Numeric parameters have to be handled as Strings, as that is what Spring gives us for missing values
    // e.g. if jodconverter.maxTasksPerProcess is not specified in the properties file, the value
    //      "${jodconverter.maxTasksPerProcess}" will be injected.

    private Integer maxTasksPerProcess;
    private String officeHome;
    private int[] portNumbers;
    private Long taskExecutionTimeout;
    private Long taskQueueTimeout;
    private File templateProfileDir;
    private Boolean enabled;
    private Long connectTimeout;
    
    public void setMaxTasksPerProcess(String maxTasksPerProcess)
    {
        Long l = parseStringForLong(maxTasksPerProcess.trim());
        if (l != null)
        {
            this.maxTasksPerProcess = l.intValue();
        }
    }
    
    public void setOfficeHome(String officeHome)
    {
        this.officeHome = officeHome.trim();
    }

    public void setPortNumbers(String s)
    {
        StringTokenizer tokenizer = new StringTokenizer(s.trim(), ",");
        int tokenCount = tokenizer.countTokens();
        portNumbers = new int[tokenCount];
        for (int i = 0 ; tokenizer.hasMoreTokens(); i++)
        {
            try
            {
                portNumbers[i] = Integer.parseInt(tokenizer.nextToken().trim());
            } catch (NumberFormatException e)
            {
                // Logging this as an error as this property would prevent JodConverter & therefore
                // OOo from starting as specified
                if (logger.isErrorEnabled())
                {
                    logger.error("Unparseable value for property 'jodconverter.portNumbers': " + s);
                }
                // We'll not rethrow the exception, instead allowing the problem to be picked up
                // when the OOoJodConverter subsystem is started.
            }
        }
    }
    
    public void setTaskExecutionTimeout(String taskExecutionTimeout)
    {
        this.taskExecutionTimeout = parseStringForLong(taskExecutionTimeout.trim());
    }

    public void setTemplateProfileDir(String templateProfileDir)
    {
        if (templateProfileDir == null || templateProfileDir.trim().length() == 0)
        {
            this.templateProfileDir = null;
        }
        else
        {
            File tmp = new File(templateProfileDir);
            if (!tmp.isDirectory())
            {
                throw new AlfrescoRuntimeException("OpenOffice template profile directory "+templateProfileDir+" does not exist.");
            }
            this.templateProfileDir = tmp;
        }
    }
    
    public void setTaskQueueTimeout(String taskQueueTimeout)
    {
        this.taskQueueTimeout = parseStringForLong(taskQueueTimeout.trim());
    }

    public void setConnectTimeout(String connectTimeout)
    {
    	this.connectTimeout = parseStringForLong(connectTimeout.trim());
    }

    public void setEnabled(String enabled)
    {
        this.enabled = Boolean.parseBoolean(enabled.trim());

        if (this.enabled == false)
        {
            this.isAvailable = false;
        }
    }

    private Long parseStringForLong(String string)
    {
        Long result = null;
        try
        {
            long l = Long.parseLong(string);
            result = new Long(l);
        }
        catch (NumberFormatException nfe)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Cannot parse numerical value from " + string);
            }
            // else intentionally empty
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.enterprise.repo.content.JodConverter#isAvailable()
     */
    public boolean isAvailable()
    {
        final boolean result = isAvailable && officeManager != null;
		return result;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
    	// isAvailable defaults to false afterPropertiesSet. It only becomes true on successful completion of this method.
    	this.isAvailable = false;
    	
        // Only start the JodConverter instance(s) if the subsystem is enabled.
        if (this.enabled == false)
        {
            return;
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("About to start JodConverter library with settings (null settings will be replaced by jodconverter defaults):");
            logger.debug("  jodconverter.officeHome = " + officeHome);
            StringBuilder portInfo = new StringBuilder();
            for (int i = 0; i < portNumbers.length ; i++)
            {
                portInfo.append(portNumbers[i]);
                if (i < portNumbers.length - 1)
                {
                    portInfo.append(", ");
                }
            }
            logger.debug("  jodconverter.portNumbers = " + portInfo);
            logger.debug("  jodconverter.maxTasksPerProcess = " + maxTasksPerProcess);
            logger.debug("  jodconverter.taskExecutionTimeout = " + taskExecutionTimeout);
            logger.debug("  jodconverter.taskQueueTimeout = " + taskQueueTimeout);
            logger.debug("  jodconverter.connectTimeout = " + connectTimeout);
        }

        logAllSofficeFilesUnderOfficeHome();
        
        try
        {
            DefaultOfficeManagerConfiguration defaultOfficeMgrConfig = new DefaultOfficeManagerConfiguration();
            if (maxTasksPerProcess != null && maxTasksPerProcess > 0)
            {
                defaultOfficeMgrConfig.setMaxTasksPerProcess(maxTasksPerProcess);
            }
            if (officeHome != null)
            {
                defaultOfficeMgrConfig.setOfficeHome(officeHome);
            }
            if (portNumbers != null && portNumbers.length != 0)
            {
                defaultOfficeMgrConfig.setPortNumbers(portNumbers);
            }
            if (taskExecutionTimeout != null && taskExecutionTimeout > 0)
            {
                defaultOfficeMgrConfig.setTaskExecutionTimeout(taskExecutionTimeout);
            }
            if (taskQueueTimeout != null && taskQueueTimeout > 0)
            {
                defaultOfficeMgrConfig.setTaskQueueTimeout(taskQueueTimeout);
            }
            if (templateProfileDir != null)
            {
                defaultOfficeMgrConfig.setTemplateProfileDir(templateProfileDir);
            }
            if (connectTimeout != null)
            {
            	defaultOfficeMgrConfig.setConnectTimeout(connectTimeout);
            }
            // Try to configure and start the JodConverter library.
            officeManager = defaultOfficeMgrConfig.buildOfficeManager();
            officeManager.start();
        }
        catch (IllegalStateException isx)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Unable to pre-initialise JodConverter library. "
                        + "The following error is shown for informational purposes only.", isx);
            }
            return;
        }
        catch (OfficeException ox)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Unable to start JodConverter library. "
                        + "The following error is shown for informational purposes only.", ox);
            }
            return;
        }
        catch (Exception x)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Unexpected error in configuring or starting the JodConverter library."
                                + "The following error is shown for informational purposes only.",x);
            }
            return;
        }

        // If any exceptions are thrown in the above code, then isAvailable
        // should remain false, hence the return statements.
        this.isAvailable = true;
    }
    
    private void logAllSofficeFilesUnderOfficeHome()
    {
    	if (logger.isDebugEnabled() == false)
    	{
    		return;
    	}
    	
    	File requestedOfficeHome = new File(this.officeHome);
    	
    	logger.debug("Some information on soffice* files and their permissions");

    	logFileInfo(requestedOfficeHome);
    	
    	for (File f : findSofficePrograms(requestedOfficeHome, new ArrayList<File>(), 2))
    	{
    		logFileInfo(f);
    	}
    }
    
    private List<File> findSofficePrograms(File searchRoot, List<File> results, int maxRecursionDepth)
    {
    	return this.findSofficePrograms(searchRoot, results, 0, maxRecursionDepth);
    }

    private List<File> findSofficePrograms(File searchRoot, List<File> results,
    		int currentRecursionDepth, int maxRecursionDepth)
    {
    	if (currentRecursionDepth >= maxRecursionDepth)
    	{
    		return results;
    	}
    	
    	File[] matchingFiles = searchRoot.listFiles(new FilenameFilter()
    	    {
    			@Override
	    		public boolean accept(File dir, String name)
    			{
		    		return name.startsWith("soffice");
	    		}
    		});
    	for (File f : matchingFiles)
    	{
    		results.add(f);
    	}
    	
    	for (File dir : searchRoot.listFiles(new FileFilter()
    	    {
				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}
    	    }))
    	{
    		findSofficePrograms(dir, results, currentRecursionDepth + 1, maxRecursionDepth);
    	}
    	
    	return results;
    }
    
    /**
     * Logs some information on the specified file, including name and r/w/x permissions.
     * @param f the file to log.
     */
    private void logFileInfo(File f)
    {
    	if (logger.isDebugEnabled() == false)
    	{
    		return;
    	}
    	
    	StringBuilder msg = new StringBuilder();
    	msg.append(f).append(" ");
    	if (f.exists())
    	{
    		if (f.canRead())
    		{
    			msg.append("(")
    			   .append(f.isDirectory() ? "d" : "-")
    			   .append(f.canRead() ? "r" : "-")
    			   .append(f.canWrite() ? "w" : "-")
    			   .append(f.canExecute() ? "x" : "-")
    			   .append(")");
    		}
    	}
    	else
    	{
    		msg.append("does not exist");
    	}
    	logger.debug(msg.toString());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
	public void destroy() throws Exception {
	    this.isAvailable = false;
	    if (officeManager != null)
	    {
	    	// If there is an OfficeException when stopping the officeManager below, then there is
	    	// little that can be done other than logging the exception and carrying on. The JodConverter-based
	    	// libraries will not be used in any case, as isAvailable is false.
	    	//
	    	// Any exception thrown out of this method will be logged and swallowed by Spring
	    	// (see javadoc for method declaration). Therefore there is no handling here for
	    	// exceptions from jodConverter.
	        officeManager.stop();
	    }
	}

    /* (non-Javadoc)
     * @see org.alfresco.enterprise.repo.content.JodConverterWorker#getOfficeManager()
     */
    public OfficeManager getOfficeManager()
    {
        return officeManager;
    }
}
