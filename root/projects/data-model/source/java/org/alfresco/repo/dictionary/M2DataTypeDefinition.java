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
package org.alfresco.repo.dictionary;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;


/**
 * Compiled Property Type Definition
 * 
 * @author David Caruana
 *
 */
/*package*/ class M2DataTypeDefinition implements DataTypeDefinition
{
    private ModelDefinition model;
    private QName name;
    private M2DataType dataType;
    private String  analyserResourceBundleName;
    private transient MessageLookup staticMessageLookup = new StaticMessageLookup();
    
    
    /*package*/ M2DataTypeDefinition(ModelDefinition model, M2DataType propertyType, NamespacePrefixResolver resolver)
    {
        this.model = model;
        this.name = QName.createQName(propertyType.getName(), resolver);
        if (!model.isNamespaceDefined(name.getNamespaceURI()))
        {
            throw new DictionaryException("Cannot define data type " + name.toPrefixString() + " as namespace " + name.getNamespaceURI() + " is not defined by model " + model.getName().toPrefixString());
        }
        this.dataType = propertyType;
        this.analyserResourceBundleName = dataType.getAnalyserResourceBundleName();
    }


    /*package*/ void resolveDependencies(ModelQuery query)
    {
        // Ensure java class has been specified
        String javaClass = dataType.getJavaClassName();
        if (javaClass == null)
        {
            throw new DictionaryException("Java class of data type " + name.toPrefixString() + " must be specified");
        }
        
        // Ensure java class is valid and referenceable
        try
        {
            Class.forName(javaClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new DictionaryException("Java class " + javaClass + " of data type " + name.toPrefixString() + " is invalid", e);
        }
    }
    
    /**
     * @see #getName()
     */
    @Override
    public String toString()
    {
        return getName().toString();
    }
    
    @Override
    public ModelDefinition getModel()
    {
        return model;
    }

    @Override
    public QName getName()
    {
        return name;
    }

    @Override
    public String getTitle()
    {
        return getTitle(staticMessageLookup);
    }
    
    @Override
    public String getDescription()
    {
        return getDescription(staticMessageLookup);
    }

    @Override
    public String getTitle(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(model, messageLookup, "datatype", name, "title"); 
        if (value == null)
        {
            value = dataType.getTitle();
        }
        return value;
    }
    
    @Override
    public String getDescription(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(model, messageLookup, "datatype", name, "description"); 
        if (value == null)
        {
            value = dataType.getDescription();
        }
        return value;
    }
   
    @Override
    public String getDefaultAnalyserClassName()
    {
        return dataType.getDefaultAnalyserClassName();
    }

    @Override
    public String getJavaClassName()
    {
        return dataType.getJavaClassName();
    }

    @Override
    public String getAnalyserResourceBundleName()
    {
        return analyserResourceBundleName;
    }
    
    @Override
    public String resolveAnalyserClassName()
    { 
        return resolveAnalyserClassName(I18NUtil.getLocale());
    }
    
    /**
     * @param dataType
     * @param locale
     * @param resourceBundleClassLoader
     * @return
     */
    @Override
    public String resolveAnalyserClassName(Locale locale)
    {
        ClassLoader resourceBundleClassLoader = getModel().getDictionaryDAO().getResourceClassLoader();
        if(resourceBundleClassLoader == null)
        {
            resourceBundleClassLoader = this.getClass().getClassLoader();
        }
        
        StringBuilder keyBuilder = new StringBuilder(64);
        keyBuilder.append(getModel().getName().toPrefixString());
        keyBuilder.append(".datatype");
        keyBuilder.append(".").append(getName().toPrefixString());
        keyBuilder.append(".analyzer");
        String key = StringUtils.replace(keyBuilder.toString(), ":", "_");
        
        String analyserClassName = null;
        
        String defaultAnalyserResourceBundleName = this.getModel().getDictionaryDAO().getDefaultAnalyserResourceBundleName();
        if(defaultAnalyserResourceBundleName != null)
        {
            ResourceBundle bundle = ResourceBundle.getBundle(defaultAnalyserResourceBundleName, locale, resourceBundleClassLoader);
            if(bundle.containsKey(key))
            {
                analyserClassName = bundle.getString(key);
            }
        }
        
        String analyserResourceBundleName;
        if(analyserClassName == null)
        {
            analyserResourceBundleName = dataType.getAnalyserResourceBundleName();
            if(analyserResourceBundleName != null)
            {
                ResourceBundle bundle = ResourceBundle.getBundle(analyserResourceBundleName, locale, resourceBundleClassLoader);
                if(bundle.containsKey(key))
                {
                    analyserClassName = bundle.getString(key);
                }
            }
        }
        
        if(analyserClassName == null)
        {
            analyserResourceBundleName = getModel().getAnalyserResourceBundleName();
            if(analyserResourceBundleName != null)
            {
                ResourceBundle bundle = ResourceBundle.getBundle(analyserResourceBundleName, locale, resourceBundleClassLoader);
                if(bundle.containsKey(key))
                {
                    analyserClassName = bundle.getString(key);
                }
            }
        }
        
        if(analyserClassName == null)
        {
            // MLTEXT should fall back to TEXT for analysis 
            if(name.equals(DataTypeDefinition.MLTEXT))
            {
                analyserClassName = model.getDictionaryDAO().getDataType(DataTypeDefinition.TEXT).resolveAnalyserClassName(locale);
                if(analyserClassName == null)
                {
                    analyserClassName = dataType.getDefaultAnalyserClassName();
                }
            }
            else
            {
                analyserClassName = dataType.getDefaultAnalyserClassName();
            }
        }
        
        return analyserClassName;
    }

    
}
