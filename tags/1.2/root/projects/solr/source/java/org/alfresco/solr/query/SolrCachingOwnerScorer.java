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
package org.alfresco.solr.query;

import java.io.IOException;
import java.util.HashMap;

import org.alfresco.solr.AlfrescoSolrEventListener;
import org.alfresco.solr.AlfrescoSolrEventListener.CacheEntry;
import org.alfresco.solr.AlfrescoSolrEventListener.OwnerLookUp;
import org.alfresco.solr.ResizeableArrayList;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexReader;
import org.apache.solr.search.SolrIndexSearcher;

public class SolrCachingOwnerScorer extends AbstractSolrCachingScorer
{

    /**
     * @param similarity
     * @param in
     * @param solrIndexReader
     */
    SolrCachingOwnerScorer(Similarity similarity, DocSet in, SolrIndexReader solrIndexReader)
    {
        super(similarity, in, solrIndexReader);
        // TODO Auto-generated constructor stub
    }

    public static SolrCachingOwnerScorer createOwnerScorer(SolrIndexSearcher searcher, Similarity similarity, String authority, SolrIndexReader reader) throws IOException
    {
        // Get hold of solr top level searcher
        // Execute query with caching
        // translate reults to leaf docs
        // build ordered doc list

        BitDocSet authorityOwnedDocs = new BitDocSet(new OpenBitSet(searcher.getReader().maxDoc()));

        HashMap<String, OwnerLookUp> ownerLookUp = (HashMap<String, OwnerLookUp>) searcher.cacheLookup(AlfrescoSolrEventListener.ALFRESCO_CACHE,
                AlfrescoSolrEventListener.KEY_OWNER_LOOKUP);

        if (authority.contains("\\|"))
        {
            for (String current : authority.split("|"))
            {
                OwnerLookUp lookUp = ownerLookUp.get(current);
                if (lookUp != null)
                {
                    ResizeableArrayList<CacheEntry> indexedOderedByOwnerIdThenDoc = (ResizeableArrayList<CacheEntry>) searcher.cacheLookup(AlfrescoSolrEventListener.ALFRESCO_ARRAYLIST_CACHE,
                            AlfrescoSolrEventListener.KEY_DBID_LEAF_PATH_BY_OWNER_ID_THEN_LEAF);
                    for (int i = lookUp.getStart(); i < lookUp.getEnd(); i++)
                    {
                        authorityOwnedDocs.addUnique(indexedOderedByOwnerIdThenDoc.get(i).getLeaf());
                    }
                }
            }

        }
        else
        {
            OwnerLookUp lookUp = ownerLookUp.get(authority);
            if (lookUp != null)
            {
                ResizeableArrayList<CacheEntry> indexedOderedByOwnerIdThenDoc = (ResizeableArrayList<CacheEntry>) searcher.cacheLookup(AlfrescoSolrEventListener.ALFRESCO_ARRAYLIST_CACHE,
                        AlfrescoSolrEventListener.KEY_DBID_LEAF_PATH_BY_OWNER_ID_THEN_LEAF);
                for (int i = lookUp.getStart(); i < lookUp.getEnd(); i++)
                {
                    authorityOwnedDocs.addUnique(indexedOderedByOwnerIdThenDoc.get(i).getLeaf());
                }
            }
        }

        return new SolrCachingOwnerScorer(similarity, authorityOwnedDocs, reader);

    }

}
