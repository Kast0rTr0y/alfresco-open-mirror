/*
 * Copyright 2005-2013 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.module.org_alfresco_module_cloud.authentication.saml.webscripts;

import java.io.StringWriter;
import java.util.Map;
import org.alfresco.module.org_alfresco_module_cloud.accounts.AccountType;
import org.alfresco.module.org_alfresco_module_cloud.authentication.saml.core.SAMLTestHelper;
import org.alfresco.module.org_alfresco_module_cloud.authentication.saml.core.SAMLUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test code for {@link SSOResponseACSPost} and {@link SSORequestGet}.
 * 
 * @author jkaabimofrad
 * @since Cloud SAML
 */
public class SSORestApiTest extends AbstractSAMLWebScriptTestHelper
{
    private static final String POST_SSO_PREFIX_URL = "/internal/saml/acs/";
    private static final String GET_SSO_REQUEST_PREFIX_URL = "/internal/saml/sso/";

    private String testTenant;
    private String testUser1;
    private String testUser2;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        testTenant = cloudContext.createTenantName("acme");
        testUser1 = cloudContext.createUserName("testuser1", testTenant);
        testUser2 = cloudContext.createUserName("testuser2", testTenant);

        // Set the current security context as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        createAccount(testTenant, AccountType.ENTERPRISE_NETWORK_ACCOUNT_TYPE, true);
        createUserAsNetworkAdmin(testUser1, "John", "Doe", "password");
    }

    @Override
    protected void tearDown() throws Exception
    {
        cleanup(testTenant);
        cloudContext.cleanup();
    }

    public void testSSOResponseInNotSamlEnabledNetwork() throws Exception
    {
        // Send SSO Response to a network, which isn't SAML-Enabled
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // The network is not SAML enabled
                Response resp = postSSOResponse(SAMLTestHelper.RESPONSE_OPENAM, null, 401);
                assertEquals(401, resp.getStatus());

                return null;
            }
        }, testUser1, testTenant);
    }

    public void testExpiredSSOResponse()
    {
        // Enable and configure the Network and send an expired SSO Response
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                setSamlConfig(testTenant, true, "http://localhost:8081/opensso/sso",
                    "http://localhost:8081/opensso/slo", "http://localhost:8081/opensso/slo",
                    SAMLTestHelper.getDefaultCertificate());

                // Send an expired SSO Response
                Response resp = postSSOResponse(SAMLTestHelper.RESPONSE_OPENAM, null, 400);
                assertEquals(400, resp.getStatus());

                return null;
            }
        }, testUser1, testTenant);
    }

    public void testMismatchEndpointSSOResponse()
    {
        // Send valid SSO Response
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                setSamlConfig(testTenant, true, "http://localhost:8081/opensso/sso",
                    "http://localhost:8081/opensso/slo", "http://localhost:8081/opensso/slo",
                    SAMLTestHelper.getDefaultCertificate());

                // Assume this response has been generated by an IdP.
                org.opensaml.saml2.core.Response response = SAMLTestHelper.buildValidTestResponse(testUser1);

                Endpoint endpoint = SAMLUtil.generateEndpoint(SingleSignOnService.DEFAULT_ELEMENT_NAME,
                    "http://sp-domain.com/acs", null);
                Map<String, String> encodedResponse = samlBinding.encodeSignSAMLMessage(response, endpoint,
                    SAMLTestHelper.getTestSigningCredential(), "veryCoolIssuer");

                // SAML message intended destination endpoint will not match the recipient endpoint.
                Response resp = postSSOResponse(encodedResponse.get("SAMLResponse"), null, 400);
                assertEquals(400, resp.getStatus());

                return null;
            }
        }, testUser1, testTenant);
    }

    public void testValidSSOResponse()
    {
        // Send valid SSO Response
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                try
                {
                    setSamlConfig(testTenant, true, "http://localhost:8081/opensso/sso",
                        "http://localhost:8081/opensso/slo", "http://localhost:8081/opensso/slo",
                        SAMLTestHelper.getDefaultCertificate());

                    // Assume this response has been generated by an IdP.
                    org.opensaml.saml2.core.Response samlResponse = SAMLTestHelper.buildValidTestResponse(testUser1);

                    Endpoint endpoint = SAMLUtil.generateEndpoint(SingleSignOnService.DEFAULT_ELEMENT_NAME,
                        authenticationService.getSpSsoURL(testTenant), null);
                    Map<String, String> encodedResponse = samlBinding.encodeSignSAMLMessage(samlResponse, endpoint,
                        SAMLTestHelper.getTestSigningCredential(), "veryCoolIssuer");

                    Response resp = postSSOResponse(encodedResponse.get("SAMLResponse"), null, 200);

                    String contentAsString = resp.getContentAsString();

                    JSONObject jsonRsp = (JSONObject)JSONValue.parse(contentAsString);
                    assertNotNull("Problem reading JSON", jsonRsp);

                    JSONObject json = (JSONObject)jsonRsp.get("data");
                    String userId = (String)json.get("userId");
                    String idpSessionIndex = (String)json.get("idpSessionIndex");
                    String ticket = (String)json.get("ticket");

                    assertEquals(testUser1, userId);
                    assertEquals(samlResponse.getAssertions().get(0).getAuthnStatements().get(0).getSessionIndex(),
                        idpSessionIndex);
                    assertNotNull("Ticket cannot be null.", ticket);
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                    fail("Valid SSO Response.");
                }

                return null;
            }
        }, testUser1, testTenant);
    }
    
    //Test case for Surfnet IdP
    public void testValidSSOResponseWithXSAnyType()
    {
        // Send valid SSO Response
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                try
                {
                    setSamlConfig(testTenant, true, "http://localhost:8081/opensso/sso",
                        "http://localhost:8081/opensso/slo", "http://localhost:8081/opensso/slo",
                        SAMLTestHelper.getDefaultCertificate());

                    // Assume this response has been generated by an IdP.
                    org.opensaml.saml2.core.Response samlResponse = SAMLTestHelper.buildValidTestResponseWithXSAnyType(testUser1);

                    Endpoint endpoint = SAMLUtil.generateEndpoint(SingleSignOnService.DEFAULT_ELEMENT_NAME,
                        authenticationService.getSpSsoURL(testTenant), null);
                    Map<String, String> encodedResponse = samlBinding.encodeSignSAMLMessage(samlResponse, endpoint,
                        SAMLTestHelper.getTestSigningCredential(), "veryCoolIssuer");

                    Response resp = postSSOResponse(encodedResponse.get("SAMLResponse"), null, 200);

                    String contentAsString = resp.getContentAsString();

                    JSONObject jsonRsp = (JSONObject)JSONValue.parse(contentAsString);
                    assertNotNull("Problem reading JSON", jsonRsp);

                    JSONObject json = (JSONObject)jsonRsp.get("data");
                    String userId = (String)json.get("userId");
                    String idpSessionIndex = (String)json.get("idpSessionIndex");
                    String ticket = (String)json.get("ticket");

                    assertEquals(testUser1, userId);
                    assertEquals(samlResponse.getAssertions().get(0).getAuthnStatements().get(0).getSessionIndex(),
                        idpSessionIndex);
                    assertNotNull("Ticket cannot be null.", ticket);
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                    fail("Valid SSO Response.");
                }

                return null;
            }
        }, testUser1, testTenant);
    }

    public void testValidSSOResponseNewUser()
    {
        // Send valid SSO Response
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                try
                {
                    setSamlConfig(testTenant, true, "http://localhost:8081/opensso/sso",
                        "http://localhost:8081/opensso/slo", "http://localhost:8081/opensso/slo",
                        SAMLTestHelper.getDefaultCertificate());

                    // Assume this response has been generated by an IdP.
                    // testUser2 is not registered
                    org.opensaml.saml2.core.Response samlResponse = SAMLTestHelper.buildValidTestResponse(testUser2);

                    Endpoint endpoint = SAMLUtil.generateEndpoint(SingleSignOnService.DEFAULT_ELEMENT_NAME,
                        authenticationService.getSpSsoURL(testTenant), null);
                    Map<String, String> encodedResponse = samlBinding.encodeSignSAMLMessage(samlResponse, endpoint,
                        SAMLTestHelper.getTestSigningCredential(), "veryCoolIssuer");

                    Response resp = postSSOResponse(encodedResponse.get("SAMLResponse"), null, 200);

                    String contentAsString = resp.getContentAsString();

                    JSONObject jsonRsp = (JSONObject)JSONValue.parse(contentAsString);
                    assertNotNull("Problem reading JSON", jsonRsp);

                    JSONObject json = (JSONObject)jsonRsp.get("data");
                    String userId = (String)json.get("userId");
                    String idpSessionIndex = (String)json.get("idpSessionIndex");
                    String ticket = (String)json.get("ticket");

                    JSONObject regObj = (JSONObject)json.get("registration");
                    String registrationId = (String)regObj.get("id");
                    String registrationKey = (String)regObj.get("key");
                    String registrationType = (String)regObj.get("type");

                    assertEquals(testUser2, userId);
                    assertEquals(samlResponse.getAssertions().get(0).getAuthnStatements().get(0).getSessionIndex(),
                        idpSessionIndex);
                    assertNull("Ticket must be null", ticket);
                    assertNotNull(registrationId);
                    assertNotNull(registrationKey);
                    assertNotNull(registrationType);
                }
                catch(Throwable t)
                {
                    fail("Valid SSO Response.");
                }

                return null;
            }
        }, testUser1, testTenant);
    }

    public void testValidSSORequest() throws Exception
    {
        // Get SSO Request
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // No need to set the certificate here, as we won't validate the IdP's Request/Response.
                setSamlConfig(testTenant, true, "http://localhost:8081/opensso/sso",
                    "http://localhost:8081/opensso/slo", "http://localhost:8081/opensso/slo", null);

                Response resp = getSSORequest(200);

                String contentAsString = resp.getContentAsString();

                JSONObject jsonRsp = (JSONObject)JSONValue.parse(contentAsString);
                assertNotNull("Problem reading JSON", jsonRsp);

                String samlRequest = (String)jsonRsp.get("SAMLRequest");
                String signature = (String)jsonRsp.get("Signature");
                String sigAlg = (String)jsonRsp.get("SigAlg");
                String keyInfo = (String)jsonRsp.get("KeyInfo");
                String action = (String)jsonRsp.get("action");

                assertNotNull(samlRequest);
                assertNotNull(signature);
                assertNotNull(sigAlg);
                assertNotNull(keyInfo);
                assertNotNull(action);

                return null;
            }
        }, testUser1, testTenant);
    }

    public void testSSORequestInNotSamlEnabledNetwork() throws Exception
    {
        // Get SSO Request. Network is not SAML enabled.
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // The network is not SAML enabled
                Response resp = getSSORequest(401);
                assertEquals(401, resp.getStatus());

                return null;
            }
        }, testUser1, testTenant);
    }

    public void testValidateSSOResponseWithWrongCertificate()
    {
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Set a wrong certificate for this network
                setSamlConfig(testTenant, true, "http://localhost:8081/opensso/sso",
                    "http://localhost:8081/opensso/slo", "http://localhost:8081/opensso/slo",
                    SAMLTestHelper.getUnknownCertificate());

                /*
                 * The "RESPONSE" is expired. But we don't care about that,
                 * as first you need to decode the message in order to validate its expiration.
                 * And as the certificate is wrong,it will fail in decoding.
                 */
                Response resp = postSSOResponse(SAMLTestHelper.RESPONSE_OPENAM, null, 400);
                assertEquals(400, resp.getStatus());

                return null;
            }
        }, testUser1, testTenant);
    }

    @SuppressWarnings("unchecked")
    private Response postSSOResponse(String base64EncodedResponse, String signature, int expectedStatus)
        throws Exception
    {
        JSONObject obj = new JSONObject();
        obj.put("SAMLResponse", base64EncodedResponse);
        obj.put("Signature", signature);

        StringWriter stringWriter = new StringWriter();
        obj.writeJSONString(stringWriter);
        String jsonString = stringWriter.toString();

        Response resp = sendRequest(new TestWebScriptServer.PostRequest(POST_SSO_PREFIX_URL + testTenant, jsonString,
            APPLICATION_JSON), expectedStatus);

        return resp;
    }

    private Response getSSORequest(int expectedStatus) throws Exception
    {
        Response resp = sendRequest(new TestWebScriptServer.GetRequest(GET_SSO_REQUEST_PREFIX_URL + testTenant),
            expectedStatus);

        return resp;
    }
}