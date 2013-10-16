package org.alfresco.repo.domain.solr;

/**
 * Interface for SOLR transaction objects.
 * 
 * @since 4.0
 */
public interface SOLRTransaction
{
    public Long getId();
    public Long getCommitTimeMs();
    public int getUpdates();
    public int getDeletes();
}
