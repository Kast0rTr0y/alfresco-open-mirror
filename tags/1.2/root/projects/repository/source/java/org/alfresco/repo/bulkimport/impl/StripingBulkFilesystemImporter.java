/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.bulkimport.impl;

import java.io.File;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.NodeImporter;

/**
 * A multi threaded bulk importer that imports by striping across filesystem levels.
 * 
 * @since 4.0
 *
 */
public class StripingBulkFilesystemImporter extends MultiThreadedBulkFilesystemImporter
{
	/**
     * Method that does the work of importing a filesystem using the BatchProcessor.
     * 
     * @param bulkImportParameters  The bulk import parameters to apply to this bulk import.
     * @param nodeImporter          The node importer implementation that will import each node.
     * @param lockToken             The lock token to use during the bulk import.
     */
    @Override
    protected void bulkImportImpl(final BulkImportParameters bulkImportParameters, final NodeImporter nodeImporter, final String lockToken)
    {
        super.bulkImportImpl(bulkImportParameters, nodeImporter, lockToken);

    	final File sourceFolder = nodeImporter.getSourceFolder();
        final int batchSize = getBatchSize(bulkImportParameters);
        final int loggingInterval = getLoggingInterval(bulkImportParameters);
    	final StripingFilesystemTracker tracker = new StripingFilesystemTracker(directoryAnalyser, bulkImportParameters.getTarget(), sourceFolder, batchSize);
        final BatchProcessor<ImportableItem> batchProcessor = getBatchProcessor(bulkImportParameters, tracker.getWorkProvider(), loggingInterval);
        final BatchProcessor.BatchProcessWorker<ImportableItem> worker = getWorker(bulkImportParameters, lockToken, nodeImporter, tracker);

		do
		{
			batchProcessor.process(worker, true);
			if(batchProcessor.getLastError() != null)
			{
				throw new AlfrescoRuntimeException(batchProcessor.getLastError());
			}
		}
		while(tracker.moreLevels());
    }
}
