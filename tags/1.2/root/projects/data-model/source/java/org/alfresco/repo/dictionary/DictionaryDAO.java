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

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Dictionary Data Access
 * 
 * @author David Caruana, sglover
 */
public interface DictionaryDAO extends ModelQuery
{
    /**
     * get DictionaryListener registered by calls to registerListener
     * 
     * @see org.alfresco.repo.dictionary.DictionaryListener
     * @return read only list of dictionary listeners
     */
    List<DictionaryListener> getDictionaryListeners();

    DictionaryRegistry getDictionaryRegistry(String tenantDomain);

    boolean isContextRefreshed();

    /**
     * @return the models known by the dictionary
     */
    Collection<QName> getModels(boolean includeInherited);

    Collection<QName> getTypes(boolean includeInherited);

    Collection<QName> getAssociations(boolean includeInherited);

    Collection<QName> getAspects(boolean includeInherited);

    /**
     * @param name
     *            the model to retrieve
     * @return the named model definition
     */
    ModelDefinition getModel(QName name);

    /**
     * @param model
     *            the model to retrieve property types for
     * @return the property types of the model
     */
    Collection<DataTypeDefinition> getDataTypes(QName model);

    /**
     * @param model
     *            the model to retrieve types for
     * @return the types of the model
     */
    Collection<TypeDefinition> getTypes(QName model);

    /**
     * @param superType
     * @param follow
     *            true => follow up the super-class hierarchy, false =>
     *            immediate sub types only
     * @return
     */
    Collection<QName> getSubTypes(QName superType, boolean follow);

    /**
     * @param model
     *            the model to retrieve aspects for
     * @return the aspects of the model
     */
    Collection<AspectDefinition> getAspects(QName model);

    /**
     * @param model
     *            the model to retrieve associations for
     * @return the associations of the model
     */
    Collection<AssociationDefinition> getAssociations(QName model);

    /**
     * @param superAspect
     * @param follow
     *            true => follow up the super-class hierarchy, false =>
     *            immediate sub aspects only
     * @return
     */
    Collection<QName> getSubAspects(QName superAspect, boolean follow);

    /**
     * @param model
     *            the model for which to get properties for
     * @return the properties of the model
     */
    Collection<PropertyDefinition> getProperties(QName model);

    /**
     * Construct an anonymous type that combines a primary type definition and
     * and one or more aspects
     * 
     * @param type
     *            the primary type
     * @param aspects
     *            the aspects to combine
     * @return the anonymous type definition
     */
    TypeDefinition getAnonymousType(QName type, Collection<QName> aspects);

    /**
     * Adds a model to the dictionary. The model is compiled and validated.
     * 
     * @param model
     *            the model to add
     * @return QName name of model
     */
    QName putModel(M2Model model);

    // QName putCustomModel(M2Model model);

    /**
     * Adds a model to the dictionary. The model is compiled and validated.
     * Constraints are not loaded.
     * 
     * This method should only be used to load models where the enforcement of
     * constraints is never required. For example, SOLR read only use of the
     * index where contraints are not required and thier definitions may not be
     * available.
     * 
     * @param model
     *            the model to add
     * @return QName name of model
     */
    QName putModelIgnoringConstraints(M2Model model);

    /**
     * Removes a model from the dictionary. The types and aspect in the model
     * will no longer be available.
     * 
     * @param model
     *            the qname of the model to remove
     */
    void removeModel(QName model);

    /**
     * Get all properties for the model and that are of the given data type. If
     * dataType is null then the all properties will be returned.
     * 
     * @param modelName
     *            the name of the model
     * @param dataType
     *            <tt>null</tt> to get all properties
     * @return the properties associated with the model
     */
    Collection<PropertyDefinition> getProperties(QName modelName, QName dataType);

    /**
     * Get all properties for all models of the given data type.
     * 
     * @param dataType
     * @return
     */
    Collection<PropertyDefinition> getPropertiesOfDataType(QName dataType);

    /**
     * @param modelName
     *            the model to retrieve namespaces for
     * @return the namespaces of the model
     */
    Collection<NamespaceDefinition> getNamespaces(QName modelName);

    /**
     * @param model
     *            the model to retrieve constraint defs (including property
     *            constaint refs)
     * @return the constraints of the model
     */
    Collection<ConstraintDefinition> getConstraints(QName model);

    /**
     * @param model
     *            the model to retrieve constraint defs (optionally only
     *            referenceable constraints)
     * @return the constraints of the model
     */
    Collection<ConstraintDefinition> getConstraints(QName model,
            boolean referenceableDefsOnly);

    /**
     * Return diffs between input model and model in the Dictionary.
     * 
     * If the input model does not exist in the Dictionary then no diffs will be
     * returned.
     * 
     * @param model
     * @return model diffs (if any)
     */
    List<M2ModelDiff> diffModel(M2Model model);

    List<M2ModelDiff> diffModelIgnoringConstraints(M2Model model);

    /**
     * 
     * Register listener with the Dictionary
     * 
     * @param dictionaryListener
     */
    void registerListener(DictionaryListener dictionaryListener);

    /**
     * Reset the Dictionary - destroy & re-initialise
     */
    void reset();

    /**
     * Initialise the Dictionary
     */
    void init();

    /**
     * Destroy the Dictionary
     */
    void destroy();

    // MT-specific
    boolean isModelInherited(QName name);

    /**
     * @return
     */
    String getDefaultAnalyserResourceBundleName();

    /**
     * @return
     */
    ClassLoader getResourceClassLoader();

    /**
     * @param resourceClassLoader
     */
    void setResourceClassLoader(ClassLoader resourceClassLoader);
}
