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
package org.alfresco.repo.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.search.impl.lucene.AVMLuceneIndexer;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Method interceptor for atomic indexing of AVM entries The properties can defined how stores are indexed based on type
 * (as set by Alfresco the Web site management UI) or based on the name of the store. Creates and deletes are indexed
 * synchronously. Updates may be asynchronous, synchronous or ignored by the index.
 * 
 * @author andyh
 */
public class AVMSnapShotTriggeredIndexingMethodInterceptorImpl implements AVMSnapShotTriggeredIndexingMethodInterceptor
{
    private static Log logger = LogFactory.getLog(AVMSnapShotTriggeredIndexingMethodInterceptorImpl.class);

    // Copy of store properties used to tag avm stores (a store propertry)

    private AVMService avmService;

    private IndexerAndSearcher indexerAndSearcher;

    private boolean enableIndexing = true;

    private IndexMode defaultMode = IndexMode.ASYNCHRONOUS;

    private Map<String, IndexMode> modeCache = new HashMap<String, IndexMode>();

    private List<IndexingDefinition> indexingDefinitions = new ArrayList<IndexingDefinition>();
    
    private SwitchableApplicationContextFactory searchApplicationContextFactory;

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        if (enableIndexing)
        {
            if (mi.getMethod().getName().equals("createSnapshot"))
            {
                // May cause any number of other stores to do snap shot under the covers via layering or do nothing
                // So we have to watch what actually changes

                Object returnValue = mi.proceed();

                Map<String, Integer> snapShots = (Map<String, Integer>) returnValue;

                // Index any stores that have moved on
                for (String store : snapShots.keySet())
                {
                    int after = snapShots.get(store).intValue();
                    indexSnapshot(store, after);
                }
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("purgeStore"))
            {
                String store = (String) mi.getArguments()[0];
                Object returnValue = mi.proceed();
                
                if (getIndexMode(store) != IndexMode.UNINDEXED)
                {
                    AVMLuceneIndexer avmIndexer = getIndexer(store);
                    if (avmIndexer != null)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("purgeStore " + store, new Exception("Stack Trace"));
                        }
                        avmIndexer.deleteIndex(store, IndexMode.SYNCHRONOUS);
                    }
                }
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("createStore"))
            {
                String store = (String) mi.getArguments()[0];
                Object returnValue = mi.proceed();
                if (getIndexMode(store) != IndexMode.UNINDEXED)
                {
                    createIndex(store);
                }
                return returnValue;
            }
            else if (mi.getMethod().getName().equals("renameStore"))
            {
                String from = (String) mi.getArguments()[0];
                String to = (String) mi.getArguments()[1];
                Object returnValue = mi.proceed();
                int after = avmService.getLatestSnapshotID(to);
                
                if (getIndexMode(from) != IndexMode.UNINDEXED)
                {
                    AVMLuceneIndexer avmIndexer = getIndexer(from);
                    if (avmIndexer != null)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("renameStore deleteIndex " + from, new Exception("Stack Trace"));
                        }
                        avmIndexer.deleteIndex(from, IndexMode.SYNCHRONOUS);
                    }
                }
                
                if (getIndexMode(to) != IndexMode.UNINDEXED)
                {
                    AVMLuceneIndexer avmIndexer = getIndexer(to);
                    if (avmIndexer != null)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("renameStore createIndex " + to + "(0, " + after + ")", new Exception("Stack Trace"));
                        }
                        avmIndexer.createIndex(to, IndexMode.SYNCHRONOUS);
                        avmIndexer.index(to, 0, after, getIndexMode(to));
                    }
                }
                return returnValue;
            }
            else
            {
                return mi.proceed();
            }
        }
        else
        {
            return mi.proceed();
        }
    }

    
    
    /**
     * @param searchApplicationContextFactory the searchApplicationContextFactory to set
     */
    public void setSearchApplicationContextFactory(SwitchableApplicationContextFactory searchApplicationContextFactory)
    {
        this.searchApplicationContextFactory = searchApplicationContextFactory;
    }



    /**
     * @return the searchApplicationContextFactory
     */
    public SwitchableApplicationContextFactory getSearchApplicationContextFactory()
    {
        return searchApplicationContextFactory;
    }



    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#setAvmService(org.alfresco.service.cmr.avm.AVMService)
     */
    @Override
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#setIndexerAndSearcher(org.alfresco.repo.search.IndexerAndSearcher)
     */
    @Override
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#setEnableIndexing(boolean)
     */
    @Override
    public void setEnableIndexing(boolean enableIndexing)
    {
        this.enableIndexing = enableIndexing;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#setIndexingDefinitions(java.util.List)
     */
    @Override
    public void setIndexingDefinitions(List<String> definitions)
    {
        indexingDefinitions.clear();
        for (String def : definitions)
        {
            IndexingDefinition id = new IndexingDefinition(def);
            indexingDefinitions.add(id);
        }
    }

    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#setDefaultMode(org.alfresco.repo.search.IndexMode)
     */
    @Override
    public void setDefaultMode(IndexMode defaultMode)
    {
        this.defaultMode = defaultMode;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#isIndexingEnabled()
     */
    @Override
    public boolean isIndexingEnabled()
    {
        return enableIndexing;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#indexSnapshot(java.lang.String, int, int)
     */
    @Override
    public void indexSnapshot(String store, int before, int after)
    {
        indexSnapshotImpl(store, before, after);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#indexSnapshot(java.lang.String, int)
     */
    @Override
    public void indexSnapshot(String store, int after)
    {
        indexSnapshotImpl(store, -1, after);
    }
    
    private void indexSnapshotImpl(String store, int before, int after)
    {
        if (getIndexMode(store) != IndexMode.UNINDEXED)
        {
            AVMLuceneIndexer avmIndexer = getIndexer(store);
            if (avmIndexer != null)
            {
                int last = getLastIndexedSnapshot(avmIndexer, store);
                
                if ((last == -1) && (! hasIndexBeenCreated(store)))
                {
                    createIndex(store);
                    // ALF-7845
                    last = getLastIndexedSnapshot(avmIndexer, store);
                }
                
                int from = before != -1 ? before : last;
                
                if (from > after)
                {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("skip indexSnapshotImpl " + store + " (" + (before == -1 ? "-1, " : "") + from +", " + after +")", new Exception("Stack Trace"));
                    }
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("indexSnapshotImpl " + store + " (" + (before == -1 ? "-1, " : "") + from +", " + after +")", new Exception("Stack Trace"));
                    }
                    avmIndexer.index(store, from, after, getIndexMode(store));
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#getLastIndexedSnapshot(java.lang.String)
     */
    @Override
    public int getLastIndexedSnapshot(String store)
    {
       
        AVMLuceneIndexer avmIndexer = getIndexer(store);
        if (avmIndexer != null)
        {
            return getLastIndexedSnapshot(avmIndexer, store);
        }
        return -1;
    }
    
    private int getLastIndexedSnapshot(AVMLuceneIndexer avmIndexer, String store)
    {
        return avmIndexer.getLastIndexedSnapshot(store);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#isSnapshotIndexed(java.lang.String, int)
     */
    @Override
    public boolean isSnapshotIndexed(String store, int id)
    {
        AVMLuceneIndexer avmIndexer = getIndexer(store);
        if (avmIndexer != null)
        {
            return avmIndexer.isSnapshotIndexed(store, id);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#isIndexUpToDateAndSearchable(java.lang.String)
     */
    @Override
    public boolean isIndexUpToDateAndSearchable(String store)
    {

        switch (getIndexMode(store))
        {
        case UNINDEXED:
            return false;
        case SYNCHRONOUS:
        case ASYNCHRONOUS:
            int last = avmService.getLatestSnapshotID(store);
            AVMLuceneIndexer avmIndexer = getIndexer(store);
            if (avmIndexer != null)
            {
                avmIndexer.flushPending();
                return avmIndexer.isSnapshotSearchable(store, last);
            }
            return false;
        default:
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#isIndexUpToDate(java.lang.String)
     */
    @Override
    public boolean isIndexUpToDate(String store)
    {
        switch (getIndexMode(store))
        {
        case UNINDEXED:
            return true;
        case SYNCHRONOUS:
        case ASYNCHRONOUS:
            int last = avmService.getLatestSnapshotID(store);
            AVMLuceneIndexer avmIndexer = getIndexer(store);
            if (avmIndexer != null)
            {
                avmIndexer.flushPending();
                return avmIndexer.isSnapshotIndexed(store, last);
            }
            return false;
        default:
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#getIndexMode(java.lang.String)
     */
    @Override
    public synchronized IndexMode getIndexMode(String store)
    {
        IndexMode mode = modeCache.get(store);
        if (mode == null)
        {
            for (IndexingDefinition def : indexingDefinitions)
            {
                if (def.definitionType == DefinitionType.NAME)
                {
                    if (def.pattern.matcher(store).matches())
                    {
                        mode = def.indexMode;
                        modeCache.put(store, mode);
                        break;
                    }
                }
                else
                {
                    AVMStoreDescriptor avmStoreDescriptor = avmService.getStore(store);
                    Map<QName, PropertyValue> storeProperties = null;
                    if (avmStoreDescriptor != null)
                    {
                        storeProperties = avmService.getStoreProperties(store);
                    }
                    String storeType = StoreType.getStoreType(store, avmStoreDescriptor, storeProperties).toString();
                    if (def.pattern.matcher(storeType).matches())
                    {
                        mode = def.indexMode;
                        modeCache.put(store, mode);
                        break;
                    }

                }
            }
        }
        // No definition
        if (mode == null)
        {
            mode = defaultMode;
            modeCache.put(store, mode);
        }
        return mode;
    }

    private class IndexingDefinition
    {
        IndexMode indexMode;

        DefinitionType definitionType;

        Pattern pattern;

        IndexingDefinition(String definition)
        {
            String[] split = definition.split(":", 3);
            if (split.length != 3)
            {
                throw new AlfrescoRuntimeException("Invalid index defintion. Must be of of the form IndexMode:DefinitionType:regular expression");
            }
            indexMode = IndexMode.valueOf(split[0].toUpperCase());
            definitionType = DefinitionType.valueOf(split[1].toUpperCase());
            pattern = Pattern.compile(split[2]);
        }
    }

    private enum DefinitionType
    {
        NAME, TYPE;
    }

    public enum StoreType
    {
        STAGING, STAGING_PREVIEW, AUTHOR, AUTHOR_PREVIEW, WORKFLOW, WORKFLOW_PREVIEW, AUTHOR_WORKFLOW, AUTHOR_WORKFLOW_PREVIEW, UNKNOWN;

        public static StoreType getStoreType(String name, AVMStoreDescriptor storeDescriptor, Map<QName, PropertyValue> storeProperties)
        {
            // if (avmService.getStore(name) != null)
            if (storeDescriptor != null)
            {
                // Map<QName, PropertyValue> storeProperties = avmService.getStoreProperties(name);
                if (storeProperties.containsKey(PROP_SANDBOX_STAGING_MAIN))
                {
                    return StoreType.STAGING;
                }
                else if (storeProperties.containsKey(PROP_SANDBOX_STAGING_PREVIEW))
                {
                    return StoreType.STAGING_PREVIEW;
                }
                else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_MAIN))
                {
                    return StoreType.AUTHOR;
                }
                else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_PREVIEW))
                {
                    return StoreType.AUTHOR_PREVIEW;
                }
                else if (storeProperties.containsKey(PROP_SANDBOX_WORKFLOW_MAIN))
                {
                    return StoreType.WORKFLOW;
                }
                else if (storeProperties.containsKey(PROP_SANDBOX_WORKFLOW_PREVIEW))
                {
                    return StoreType.WORKFLOW_PREVIEW;
                }
                else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN))
                {
                    return StoreType.AUTHOR_WORKFLOW;
                }
                else if (storeProperties.containsKey(PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW))
                {
                    return StoreType.AUTHOR_WORKFLOW_PREVIEW;
                }
                else
                {
                    return StoreType.UNKNOWN;
                }
            }
            else
            {
                return StoreType.UNKNOWN;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#hasIndexBeenCreated(java.lang.String)
     */
    @Override
    public boolean hasIndexBeenCreated(String store)
    {
        AVMLuceneIndexer avmIndexer = getIndexer(store);
        if (avmIndexer != null)
        {
            avmIndexer.flushPending();
            return avmIndexer.hasIndexBeenCreated(store);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#createIndex(java.lang.String)
     */
    @Override
    public void createIndex(String store)
    {
        AVMLuceneIndexer avmIndexer = getIndexer(store);
        if (avmIndexer != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("createIndex " + store, new Exception("Stack Trace"));
            }
            avmIndexer.createIndex(store, IndexMode.SYNCHRONOUS);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#getIndexer(java.lang.String)
     */
    @Override
    public AVMLuceneIndexer getIndexer(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            return avmIndexer;
        }
        else
        {
            if(searchApplicationContextFactory.getCurrentSourceBeanName().equals("solr"))
            {
                throw new AlfrescoRuntimeException("No AVM Indexer available (AVM is not supported with SOLR");
                //return null;
            }
            else
            {
                return null;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor#deleteIndex(java.lang.String)
     */
    @Override
    public void deleteIndex(String store)
    {
        StoreRef storeRef = AVMNodeConverter.ToStoreRef(store);
        Indexer indexer = indexerAndSearcher.getIndexer(storeRef);
        if (indexer instanceof AVMLuceneIndexer)
        {
            AVMLuceneIndexer avmIndexer = (AVMLuceneIndexer) indexer;
            avmIndexer.deleteIndex(storeRef);
        }
        else
        {
            if(searchApplicationContextFactory.getCurrentSourceBeanName().equals("solr"))
            {
                throw new AlfrescoRuntimeException("No AVM Indexer available (AVM is not supported with SOLR");
            }
            //else nothing to do
        }
    }
}
