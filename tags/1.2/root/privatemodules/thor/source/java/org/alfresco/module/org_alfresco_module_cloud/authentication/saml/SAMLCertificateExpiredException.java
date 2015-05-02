/*
 * Copyright 2005-2012 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.module.org_alfresco_module_cloud.authentication.saml;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * 
 * @author jkaabimofrad
 * @since Cloud SAML
 */
public class SAMLCertificateExpiredException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -3792234046427639045L;

    /**
     * Constructor
     * 
     * @param msgId
     *            the message id
     */
    public SAMLCertificateExpiredException(String msgId)
    {
        super(msgId);
    }

    /**
     * Constructor
     * 
     * @param msgId
     *            the message id
     * @param cause
     *            the exception cause
     */
    public SAMLCertificateExpiredException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

}