/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_cloud.webscripts;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.auth.AuthenticationListener;
import org.alfresco.repo.web.auth.WebCredentials;
import org.alfresco.repo.web.scripts.TenantWebScriptServletRequest;
import org.alfresco.repo.web.scripts.servlet.BasicHttpAuthenticatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * HTTP Basic Authentication for MT
 * 
 * @author davidc
 */
public class TenantBasicHTTPAuthenticatorFactory extends BasicHttpAuthenticatorFactory
{
    private static Log logger = LogFactory.getLog(TenantBasicHTTPAuthenticatorFactory.class);
    
    public static final String DEFAULT_AUTHENTICATOR_KEY_HEADER = "X-Alfresco-Authenticator-Key"; 
    public static final String DEFAULT_REMOTE_USER_HEADER = "X-Alfresco-Remote-User"; 
    
    private String authenticatorKeyHeader = DEFAULT_AUTHENTICATOR_KEY_HEADER;
    private String remoteUserHeader = DEFAULT_REMOTE_USER_HEADER;
    private RetryingTransactionHelper retryingTransactionHelper;
    private CloudTenantAuthentication tenantAuthentication;
    private Set<String> validAuthenticatorKeys = Collections.emptySet();
    private Set<String> outboundHeaderNames;
    
    public void setAuthenticatorKeyHeader(String authenticatorKeyHeader)
    {
        this.authenticatorKeyHeader = authenticatorKeyHeader;
    }
    
    /**
     * Set the headers passed to the gateway for authentication.
     * 
     * @param outboundHeaders
     */
    public void setOutboundHeaders(Set<String> outboundHeaders)
    {
        if (outboundHeaders != null)
        {
            Set<String> trimmed = new HashSet<String>();
            for (String value : outboundHeaders)
            {
                trimmed.add(value.toLowerCase(Locale.ENGLISH).trim());
            }
            outboundHeaders = trimmed;
        }
        
        this.outboundHeaderNames = outboundHeaders;
    }

    
    public void setRemoteUserHeader(String remoteUserHeader)
    {
        this.remoteUserHeader = remoteUserHeader;
    }
    
    public void setTenantAuthentication(CloudTenantAuthentication service)
    {
        this.tenantAuthentication = service;
    }
    
    public void setTransactionHelper(RetryingTransactionHelper service)
    {
        this.retryingTransactionHelper = service;
    }
    
    public void setValidAuthentictorKeys(Set<String> validKeys)
    {
        if (validKeys != null)
        {
            Set<String> trimmedKeys = new HashSet<String>();
            for (String key : validKeys)
            {
                trimmedKeys.add(key.trim());
            }
            validKeys = trimmedKeys;
        }
        this.validAuthenticatorKeys = validKeys;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.servlet.ServletAuthenticatorFactory#create(org.alfresco.web.scripts.servlet.WebScriptServletRequest, org.alfresco.web.scripts.servlet.WebScriptServletResponse)
     */
    public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
    {
        return new TenantBasicHttpAuthenticator(req, res, new ProxyListener());
    }
    
    private Map<String, String[]> getOutboundHeaders(TenantWebScriptServletRequest req) 
    {
        Map<String, String[]> outboundHeaders = new HashMap<String, String[]>();
        for (String headerName : outboundHeaderNames)
        {
            String[] headerValues = req.getHeaderValues(headerName);
            if (headerValues != null && headerValues.length > 0)
            {
                outboundHeaders.put(headerName, headerValues);
            }
        }
        return outboundHeaders;
    }
    
    /**
     * HTTP Basic Authentication with additional tenant applicability check
     */
    public class TenantBasicHttpAuthenticator extends BasicHttpAuthenticator
    {
        // dependencies
        private TenantWebScriptServletRequest servletReq;
        private WebScriptServletResponse servletRes;
        
        // Proxy listener used to receive initial authentication events from the base BasicHttpAuthenticator
        private ProxyListener proxyListener;
        
        /**
         * Construct
         * 
         * @param authenticationService
         * @param req
         * @param res
         */
        public TenantBasicHttpAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res, ProxyListener proxyListener)
        {
            super(req, res, proxyListener);
            if (!(req instanceof TenantWebScriptServletRequest))
            {
                throw new WebScriptException("Request is not a tenant aware request");
            }
            servletReq = (TenantWebScriptServletRequest)req;
            servletRes = res;
            this.proxyListener = proxyListener; 
        }
    
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#authenticate(org.alfresco.web.scripts.Description.RequiredAuthentication, boolean)
         */
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            boolean authorized = false;
            try
            {
                String authenticatorKey = servletReq.getHeader(authenticatorKeyHeader);
                String remoteUser = servletReq.getHeader(remoteUserHeader);
                if (authenticatorKey != null && 
                    remoteUser != null)
                {
                    // Trusted auth. Validate key and setup authentication context.
                    authorized = authenticateViaGateway(required, isGuest, authenticatorKey, remoteUser);
                }
                else
                {
                    // Fallback to standard BasicHttpAutheticator
                    try
                    {
                        authorized = super.authenticate(required, isGuest);
                    }
                    catch (AuthenticationException ae)
                    {
                        // eg. guest
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("TenantBasicHttpAuthenticator: required="+required+", isGuest="+isGuest+" - "+ae.getMessage());
                        }
                    }
                }
                if (!authorized)
                {
                    // not authorized, no point continuing
                    return authorized;
                }

                // check tenant validity
                final String tenant = servletReq.getTenant();
                final String email = AuthenticationUtil.getFullyAuthenticatedUser();
                try
                {
                    authorized = retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Boolean>()
                    {
                        public Boolean execute() throws Exception
                        {
                            return tenantAuthentication.authenticateTenant(email, tenant);
                        }
                    }, true, false);
                }
                finally
                {
                    if (!authorized)
                    {
                        proxyListener.authenticationFailed(new TenantCredentials(tenant, email, proxyListener.getOrignalCredentials()));
                        AuthenticationUtil.clearCurrentSecurityContext();
                    }
                    else
                    {
                        proxyListener.userAuthenticated(new TenantCredentials(tenant, email, proxyListener.getOrignalCredentials()));
                    }
                }
                
                return authorized;
            }
            finally
            {
                if (!authorized)
                {
                    servletRes.setStatus(401);
                    servletRes.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco " + servletReq.getTenant() + " tenant\"");
                }
            }
        }

        private boolean authenticateViaGateway(RequiredAuthentication required, boolean isGuest, String authenticatorKey, String remoteUser)
        {
            // Validate the authenticator key, and if valid set the fully authenticated user.
            if (validAuthenticatorKeys.contains(authenticatorKey))
            {
                AuthenticationUtil.setFullyAuthenticatedUser(remoteUser);
                proxyListener.userAuthenticated(new PublicApiCredentials(authenticatorKey, remoteUser, getOutboundHeaders(servletReq)));
                return true;
            }
            else
            {
                logger.error("Invalid authetnicator key:- " + authenticatorKey);
                proxyListener.authenticationFailed(new PublicApiCredentials(authenticatorKey, remoteUser, getOutboundHeaders(servletReq)));
                return false;
            }
        }
        
    }
    
    private class ProxyListener implements AuthenticationListener
    {
        private WebCredentials originalCredentials;

        @Override
        public void userAuthenticated(WebCredentials credentials)
        {
            listener.userAuthenticated(credentials);
            this.originalCredentials = credentials;
        }
        
        @Override
        public void authenticationFailed(WebCredentials credentials)
        {
            listener.authenticationFailed(credentials);
        }

        @Override
        public void authenticationFailed(WebCredentials credentials, Exception ex)
        {
            listener.authenticationFailed(credentials, ex);
        }
        
        public WebCredentials getOrignalCredentials()
        {
            return this.originalCredentials;
        }
    }
}