/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of the Spring Surf Extension project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.extensions.surf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.WebFrameworkConfigElement;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.types.Theme;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;

/**
 * This extends the default {@link CssThemeHandler} and adds in support for LESS compilation via the LESS for
 * Java Library. This bean is a hybrid of two differing approaches:
 * <ul><li>The simple token substitution approach used for Alfresco Share 4.2</li>
 * <li>Using LESS for advanced CSS capabilities</li></ul>
 * This hybrid is required during the interim of modifying the widgets that were originally written to just use
 * the simple token substitution with LESS capabilities. This is <b>currently</b> the default Surf CSS Theme handler bean.
 * 
 * @author Dave Draper
 */
public class HybridCssThemeHandler extends CssThemeHandler
{
    private static final Log logger = LogFactory.getLog(HybridCssThemeHandler.class);
    
    /**
     * The engine to use for LESS processing.
     */
    private LessEngine engine;
    
    /**
     * Sets up a new {@link LessEngine} instance.
     */
    public HybridCssThemeHandler()
    {
        this.engine = new LessEngine();
    }

    /**
     * The default LESS configuration. This will be populated with the contents of a file referenced by the 
     * web-framework > defaults > dojo-pages > default-less-configuration.
     */
    private String defaultLessConfig = null;
    
    /**
     * Returns the current default LESS configuration. If it has not previously been retrieved then it will
     * attempt to load it.
     * 
     * @return A String containing the default LESS configuration variables.
     */
    public String getDefaultLessConfig()
    {
        if (this.defaultLessConfig == null)
        {
            WebFrameworkConfigElement wfce = this.getWebFrameworkConfigElement();
            if (wfce != null)
            {
                String defaultLessConfigPath = wfce.getDojoDefafultLessConfig();
                if (defaultLessConfigPath != null)
                {
                    try
                    {
                        InputStream in = this.getDependencyHandler().getResourceInputStream(defaultLessConfigPath);
                        if (in != null)
                        {
                            this.defaultLessConfig = this.getDependencyHandler().convertResourceToString(in);
                        }
                        else
                        {
                            if (logger.isErrorEnabled())
                            {
                                logger.error("Could not find the default LESS configuration at: " + defaultLessConfigPath);
                            }
                           
                            // Set the configuration as the empty string as it's not in the configured location
                            this.defaultLessConfig = "";
                        }
                    }
                    catch (IOException e)
                    {
                        if (logger.isErrorEnabled())
                        {
                            logger.error("An exception occurred retrieving the default LESS configuration from: " + defaultLessConfigPath, e);
                        }
                    }
                }
                else
                {
                    if (logger.isErrorEnabled())
                    {
                        logger.error("A location for the default LESS configuration file has not been specififed");
                    }
                    this.defaultLessConfig = "";
                }
            }
        }
        return this.defaultLessConfig;
    }

    /**
     * Looks for a CSS token called "LessVariables" which should contain the LESS style variables that 
     * can be applied to each CSS file.
     * 
     * @return
     */
    public String getLessVariables() {
        String variables = this.getDefaultLessConfig();
        Theme currentTheme = ThreadLocalRequestContext.getRequestContext().getTheme();
        if (currentTheme == null)
        {
            currentTheme = ThreadLocalRequestContext.getRequestContext().getObjectService().getTheme("default");
        }
        String themeVariables = currentTheme.getCssTokens().get(LessForJavaCssThemeHandler.LESS_TOKEN);
        if (themeVariables != null)
        {
            // Add a new line just to make sure the first theme specific variable isn't appended to 
            // the end of the last default variable!
            variables += "\n" + themeVariables;
        }
        return variables;
    }
    
    /**
     * Overrides the default implementation to add LESS processing capabilities.
     * 
     * @param path The path of the file being processed (used only for error output)
     * @param cssContents The CSS to process
     * @throws IOException when accessing file contents.
     */
    @Override
    public String processCssThemes(String path, StringBuilder cssContents) throws IOException
    {
        String compiledCss = null;
        String intialProcessResults = super.processCssThemes(path, cssContents);
        String lessVariables = this.getLessVariables();
        String fullCSS = lessVariables + intialProcessResults;
        try
        {
            compiledCss = this.engine.compile(fullCSS.toString());
        }
        catch (LessException e)
        {
            compiledCss = "/*" + LessForJavaCssThemeHandler.logLessException(e, path) + "*/\n\n " + intialProcessResults;
        }
        catch (ClassCastException e)
        {
            compiledCss = "/*" + LessForJavaCssThemeHandler.logLessException(e, path) + "*/\n\n " + intialProcessResults;
        }
        return compiledCss;
    }
}
