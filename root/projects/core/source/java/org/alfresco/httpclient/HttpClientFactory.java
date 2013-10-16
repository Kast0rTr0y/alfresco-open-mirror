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
package org.alfresco.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.AlfrescoKeyStoreImpl;
import org.alfresco.encryption.EncryptionUtils;
import org.alfresco.encryption.Encryptor;
import org.alfresco.encryption.KeyProvider;
import org.alfresco.encryption.KeyResourceLoader;
import org.alfresco.encryption.KeyStoreParameters;
import org.alfresco.encryption.ssl.AuthSSLProtocolSocketFactory;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A factory to create HttpClients and AlfrescoHttpClients based on the setting of the 'secureCommsType' property.
 * 
 * @since 4.0
 *
 */
public class HttpClientFactory
{
	public static enum SecureCommsType
	{
		HTTPS, NONE;
		
		public static SecureCommsType getType(String type)
		{
			if(type.equalsIgnoreCase("https"))
			{
				return HTTPS;
			}
			else if(type.equalsIgnoreCase("none"))
			{
				return NONE;
			}
			else
			{
				throw new IllegalArgumentException("Invalid communications type");
			}
		}
	};

    private static final Log logger = LogFactory.getLog(HttpClientFactory.class);

    private SSLEncryptionParameters sslEncryptionParameters;
    private KeyResourceLoader keyResourceLoader;
	private SecureCommsType secureCommsType;

	// for md5 http client (no longer used but kept for now)
    private KeyStoreParameters keyStoreParameters;
    private MD5EncryptionParameters encryptionParameters;

    private String host;
    private int port;
    private int sslPort;
    
    private AlfrescoKeyStore sslKeyStore;
    private AlfrescoKeyStore sslTrustStore;
    private ProtocolSocketFactory sslSocketFactory;

    private int maxTotalConnections = 40;

    private int maxHostConnections = 40;
    
    private int socketTimeout = 0;

    private int connectionTimeout = 0;
    
    public HttpClientFactory()
    {
    }

    public HttpClientFactory(SecureCommsType secureCommsType, SSLEncryptionParameters sslEncryptionParameters,
            KeyResourceLoader keyResourceLoader, KeyStoreParameters keyStoreParameters,
            MD5EncryptionParameters encryptionParameters, String host, int port, int sslPort, int maxTotalConnections,
            int maxHostConnections, int socketTimeout)
    {
    	this.secureCommsType = secureCommsType;
    	this.sslEncryptionParameters = sslEncryptionParameters;
    	this.keyResourceLoader = keyResourceLoader;
    	this.keyStoreParameters = keyStoreParameters;
    	this.encryptionParameters = encryptionParameters;
    	this.host = host;
    	this.port = port;
    	this.sslPort = sslPort;
    	this.maxTotalConnections = maxTotalConnections;
    	this.maxHostConnections = maxHostConnections;
    	this.socketTimeout = socketTimeout;
    	init();
    }

    public void init()
    {
		this.sslKeyStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getKeyStoreParameters(),  keyResourceLoader);
		this.sslTrustStore = new AlfrescoKeyStoreImpl(sslEncryptionParameters.getTrustStoreParameters(), keyResourceLoader);
    	this.sslSocketFactory = new AuthSSLProtocolSocketFactory(sslKeyStore, sslTrustStore, keyResourceLoader);    	
    }

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setSslPort(int sslPort)
	{
		this.sslPort = sslPort;
	}

	public boolean isSSL()
	{
		return secureCommsType == SecureCommsType.HTTPS;
	}

	public void setSecureCommsType(String type)
	{
		try
		{
			this.secureCommsType = SecureCommsType.getType(type);
		}
		catch(IllegalArgumentException e)
		{
			throw new AlfrescoRuntimeException("", e);
		}
	}
	
    public void setSSLEncryptionParameters(SSLEncryptionParameters sslEncryptionParameters)
	{
		this.sslEncryptionParameters = sslEncryptionParameters;
	}

	public void setKeyStoreParameters(KeyStoreParameters keyStoreParameters)
	{
		this.keyStoreParameters = keyStoreParameters;
	}

	public void setEncryptionParameters(MD5EncryptionParameters encryptionParameters)
	{
		this.encryptionParameters = encryptionParameters;
	}

	public void setKeyResourceLoader(KeyResourceLoader keyResourceLoader)
	{
		this.keyResourceLoader = keyResourceLoader;
	}
	
	/**
     * @return the maxTotalConnections
     */
    public int getMaxTotalConnections()
    {
        return maxTotalConnections;
    }

    /**
     * @param maxTotalConnections the maxTotalConnections to set
     */
    public void setMaxTotalConnections(int maxTotalConnections)
    {
        this.maxTotalConnections = maxTotalConnections;
    }

    /**
     * @return the maxHostConnections
     */
    public int getMaxHostConnections()
    {
        return maxHostConnections;
    }

    /**
     * @param maxHostConnections the maxHostConnections to set
     */
    public void setMaxHostConnections(int maxHostConnections)
    {
        this.maxHostConnections = maxHostConnections;
    }

    /**
     * Attempts to connect to a server will timeout after this period (millis).
     * Default is zero (the timeout is not used).
     * 
     * @param connectionTimeout time in millis.
     */
    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    protected HttpClient constructHttpClient()
	{
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpClient httpClient = new HttpClient(connectionManager);
        HttpClientParams params = httpClient.getParams();
        params.setBooleanParameter(HttpConnectionParams.TCP_NODELAY, true);
        params.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, true);
        params.setSoTimeout(socketTimeout);
        HttpConnectionManagerParams connectionManagerParams = httpClient.getHttpConnectionManager().getParams();
        connectionManagerParams.setMaxTotalConnections(maxTotalConnections);
        connectionManagerParams.setDefaultMaxConnectionsPerHost(maxHostConnections);
        connectionManagerParams.setConnectionTimeout(connectionTimeout);

        return httpClient;
	}
	
	protected HttpClient getHttpsClient()
	{
		// Configure a custom SSL socket factory that will enforce mutual authentication
		HttpClient httpClient = constructHttpClient();
        HttpHostFactory hostFactory = new HttpHostFactory(new Protocol("https", sslSocketFactory, sslPort));
        httpClient.setHostConfiguration(new HostConfigurationWithHostFactory(hostFactory));
        httpClient.getHostConfiguration().setHost(host, sslPort, "https");
        return httpClient;
	}

	protected HttpClient getDefaultHttpClient()
	{
		HttpClient httpClient = constructHttpClient();
        httpClient.getHostConfiguration().setHost(host, port);
        return httpClient;
	}
	
	protected AlfrescoHttpClient getAlfrescoHttpsClient()
	{
        AlfrescoHttpClient repoClient = new HttpsClient(getHttpsClient());
        return repoClient;
	}

    protected AlfrescoHttpClient getAlfrescoHttpClient()
	{
        AlfrescoHttpClient repoClient = new DefaultHttpClient(getDefaultHttpClient());
        return repoClient;
	}
    
	protected HttpClient getMD5HttpClient(String host, int port)
	{
		HttpClient httpClient = constructHttpClient();
        httpClient.getHostConfiguration().setHost(host, port);
        return httpClient;
	}
	
    protected AlfrescoHttpClient getAlfrescoMD5HttpClient(String host, int port)
	{
        AlfrescoHttpClient repoClient = new SecureHttpClient(getDefaultHttpClient(), keyResourceLoader, host, port,
        		keyStoreParameters, encryptionParameters);
        return repoClient;
	}
    
    /**
     * For testing.
     * 
     * @param host
     * @param port
     * @param encryptionService
     * @return
     */
    protected AlfrescoHttpClient getAlfrescoMD5HttpClient(String host, int port, EncryptionService encryptionService)
	{
        AlfrescoHttpClient repoClient = new SecureHttpClient(getDefaultHttpClient(), encryptionService);
        return repoClient;
	}
	
	public AlfrescoHttpClient getRepoClient(String host, int port)
    {
        AlfrescoHttpClient repoClient = null;

        if(secureCommsType == SecureCommsType.HTTPS)
        {
        	repoClient = getAlfrescoHttpsClient();
        }
        else if(secureCommsType == SecureCommsType.NONE)
        {
        	repoClient = getAlfrescoHttpClient();
        }
        else
        {
        	throw new AlfrescoRuntimeException("Invalid Solr secure communications type configured in alfresco.secureComms, should be 'ssl'or 'none'");
        }

        return repoClient;
    }
	
	public HttpClient getHttpClient()
    {
        HttpClient httpClient = null;

        if(secureCommsType == SecureCommsType.HTTPS)
        {
        	httpClient = getHttpsClient();
        }
        else if(secureCommsType == SecureCommsType.NONE)
        {
        	httpClient = getDefaultHttpClient();
        }
        else
        {
        	throw new AlfrescoRuntimeException("Invalid Solr secure communications type configured in alfresco.secureComms, should be 'ssl'or 'none'");
        }

        return httpClient;
    }
	

	
	/**
	 * A secure client connection to the repository.
	 * 
	 * @since 4.0
	 *
	 */
	class HttpsClient extends AbstractHttpClient
	{
	    public HttpsClient(HttpClient httpClient)
	    {
	    	super(httpClient);
	    }

	    /**
	     * Send Request to the repository
	     */
	    public Response sendRequest(Request req) throws AuthenticationException, IOException
	    {
	    	HttpMethod method = super.sendRemoteRequest(req);
	    	return new HttpMethodResponse(method);
	    }
	}
	
    /**
     * Simple HTTP client to connect to the Alfresco server. Simply wraps a HttpClient.
     * 
     * @since 4.0
     */
    class DefaultHttpClient extends AbstractHttpClient
    {        
        public DefaultHttpClient(HttpClient httpClient)
        {
        	super(httpClient);
        }

        /**
         * Send Request to the repository
         */
	    public Response sendRequest(Request req) throws AuthenticationException, IOException
	    {
	    	HttpMethod method = super.sendRemoteRequest(req);
	    	return new HttpMethodResponse(method);
	    }
    }
    
    /**
     * Simple HTTP client to connect to the Alfresco server.
     * 
     * @since 4.0
     */
    class SecureHttpClient extends AbstractHttpClient
    {
        private Encryptor encryptor;
        private EncryptionUtils encryptionUtils;
        private EncryptionService encryptionService;
        
        /**
         * For testing purposes.
         * 
         * @param solrResourceLoader
         * @param alfrescoHost
         * @param alfrescoPort
         * @param encryptionParameters
         */
        public SecureHttpClient(HttpClient httpClient, EncryptionService encryptionService)
        {
        	super(httpClient);
            this.encryptionUtils = encryptionService.getEncryptionUtils();
            this.encryptor = encryptionService.getEncryptor();
            this.encryptionService = encryptionService;
        }
        
        public SecureHttpClient(HttpClient httpClient, KeyResourceLoader keyResourceLoader, String host, int port,
        		KeyStoreParameters keyStoreParameters, MD5EncryptionParameters encryptionParameters)
        {
        	super(httpClient);
            this.encryptionService = new EncryptionService(host, port, keyResourceLoader, keyStoreParameters, encryptionParameters);
            this.encryptionUtils = encryptionService.getEncryptionUtils();
            this.encryptor = encryptionService.getEncryptor();
        }
        
        protected HttpMethod createMethod(Request req) throws IOException
        {
        	byte[] message = null;
        	HttpMethod method = super.createMethod(req);

        	if(req.getMethod().equalsIgnoreCase("POST"))
        	{
    	        message = req.getBody();
    	        // encrypt body
    	        Pair<byte[], AlgorithmParameters> encrypted = encryptor.encrypt(KeyProvider.ALIAS_SOLR, null, message);
    	        encryptionUtils.setRequestAlgorithmParameters(method, encrypted.getSecond());
    	        
    	        ((PostMethod)method).getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, encrypted.getFirst().length > DEFAULT_SAVEPOST_BUFFER);
    	        ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(encrypted.getFirst(), "application/octet-stream");
    	        ((PostMethod)method).setRequestEntity(requestEntity);
        	}

        	encryptionUtils.setRequestAuthentication(method, message);

        	return method;
    	}
        
        protected HttpMethod sendRemoteRequest(Request req) throws AuthenticationException, IOException
        {
        	HttpMethod method = super.sendRemoteRequest(req);

        	// check that the request returned with an ok status
        	if(method.getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
        	{
        		throw new AuthenticationException(method);
        	}
        	
        	return method;
        }

        /**
         * Send Request to the repository
         */
        public Response sendRequest(Request req) throws AuthenticationException, IOException
        {
        	HttpMethod method = super.sendRemoteRequest(req);
        	return new SecureHttpMethodResponse(method, httpClient.getHostConfiguration(), encryptionUtils);
        }
    }
    
    static class SecureHttpMethodResponse extends HttpMethodResponse
    {
    	protected HostConfiguration hostConfig;
        protected EncryptionUtils encryptionUtils;
		// Need to get as a byte array because we need to read the request twice, once for authentication
		// and again by the web service.
        protected byte[] decryptedBody;

        public SecureHttpMethodResponse(HttpMethod method, HostConfiguration hostConfig, 
        		EncryptionUtils encryptionUtils) throws AuthenticationException, IOException
        {
        	super(method);
        	this.hostConfig = hostConfig;
            this.encryptionUtils = encryptionUtils;

			if(method.getStatusCode() == HttpStatus.SC_OK)
			{
    			this.decryptedBody = encryptionUtils.decryptResponseBody(method);
				// authenticate the response
    			if(!authenticate())
    			{
    				throw new AuthenticationException(method);
    			}
			}
        }
        
        protected boolean authenticate() throws IOException
        {
        	return encryptionUtils.authenticateResponse(method, hostConfig.getHost(), decryptedBody);
        }
        
        public InputStream getContentAsStream() throws IOException
        {
        	if(decryptedBody != null)
        	{
        		return new ByteArrayInputStream(decryptedBody);
        	}
        	else
        	{
        		return null;
        	}
        }
    }

    private static class HttpHostFactory
    {
    	private Map<String, Protocol> protocols;

        public HttpHostFactory(Protocol httpsProtocol)
        {
        	protocols = new HashMap<String, Protocol>(2);
        	protocols.put("https", httpsProtocol);
        }
 
        /** Get a host for the given parameters. This method need not be thread-safe. */
        public HttpHost getHost(String host, int port, String scheme)
        {
        	if(scheme == null)
        	{
        		scheme = "http";
        	}
        	Protocol protocol = protocols.get(scheme);
        	if(protocol == null)
        	{
        		protocol = Protocol.getProtocol("http");
            	if(protocol == null)
            	{
            		throw new IllegalArgumentException("Unrecognised scheme parameter");
            	}
        	}

            return new HttpHost(host, port, protocol);
        }
    }
    
    private static class HostConfigurationWithHostFactory extends HostConfiguration
    {
        private final HttpHostFactory factory;

        public HostConfigurationWithHostFactory(HttpHostFactory factory)
        {
            this.factory = factory;
        }

        public synchronized void setHost(String host, int port, String scheme)
        {
            setHost(factory.getHost(host, port, scheme));
        }

        public synchronized void setHost(String host, int port)
        {
            setHost(factory.getHost(host, port, "http"));
        }
        
        @SuppressWarnings("unused")
		public synchronized void setHost(URI uri)
        {
            try {
                setHost(uri.getHost(), uri.getPort(), uri.getScheme());
            } catch(URIException e) {
                throw new IllegalArgumentException(e.toString());
            }
        }
    }

}
