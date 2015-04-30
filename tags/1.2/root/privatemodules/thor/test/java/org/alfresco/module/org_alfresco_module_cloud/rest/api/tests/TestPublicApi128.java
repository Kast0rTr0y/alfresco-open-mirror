package org.alfresco.module.org_alfresco_module_cloud.rest.api.tests;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.PersonInfo;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.test_category.PublicApiTestsCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Case sensitivity public api tests.
 * 
 * @author steveglover
 *
 */
@Category(PublicApiTestsCategory.class)
public class TestPublicApi128 extends CloudTestApi
{
	private long time;

	private static final String userId = "bOb.Jones";
	private static final String networkPrefix = "MiXeDCasE";
	private static final String networkDomain = "coM";

	private String networkId;
	private TestNetwork mixedCaseNetwork;
	
	private String getNetworkId(String networkIdPrefix, String networkIdDomain)
	{
		return networkIdPrefix + time + "." + networkIdDomain;
	}
	
	private String getUserName(String username, String networkIdPrefix, String networkIdDomain)
	{
		return username + "@" + getNetworkId(networkIdPrefix, networkIdDomain);
	}

	@Before
	public void setup()
	{
		// mixed case network
		this.time = System.currentTimeMillis();
		this.networkId = getNetworkId(networkPrefix, networkDomain);

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
        	@SuppressWarnings("synthetic-access")
        	public Void execute() throws Throwable
        	{
        		AuthenticationUtil.pushAuthentication();
        		try
        		{
	    	        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
	
	    	        TestPublicApi128.this.mixedCaseNetwork = repoService.createNetwork(TestPublicApi128.this.networkId, true);
	    	        TestPublicApi128.this.mixedCaseNetwork.create();
	
					// mixed case user
					PersonInfo personInfo = new CloudPersonInfo("Bob", "Jones", TestPublicApi128.userId, "password", false, null, null, null, null, null, null, null);
					mixedCaseNetwork.createUser(personInfo);
        		}
        		finally
        		{
        			AuthenticationUtil.popAuthentication();
        		}

				return null;
        	}
        }, false, true);
	}

	@Test
	public void testPublicApi128() throws Exception
	{
		People peopleProxy = publicApiClient.people();

		// try to get person activities given a mixture of cases for networkId and user id
		{
			publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", networkPrefix, networkDomain)));

			int skipCount = 0;
			int maxItems = 10;
			Paging paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities("-me-", createParams(paging, null));

			publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", "MIXEDCASE", "COM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities("-me-", createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("BOB.JONES", "MIXEDCASE", "COM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities("-me-", createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("Bob.JONES", "MIXEDCASE", "COM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities("-me-", createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", networkPrefix, networkDomain)));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("BOB.JONES", networkPrefix, networkDomain), createParams(paging, null));

			publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", "MIXEDCASE", "CoM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("BOB.JONES", "MIXEDCASE", networkDomain), createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("BOB.JONES", "MIXEDCASE", "coM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("BOB.JONES", "MIXEDCASE", "CoM"), createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("Bob.JONES", "MIXEDCASE", "COM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("Bob.JONES", "MIXEDCASE", networkDomain), createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", networkPrefix, networkDomain)));
			
			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("Bob.JONES", networkPrefix, networkDomain), createParams(paging, null));

			publicApiClient.setRequestContext(new RequestContext(networkId, getUserName("BOB.JONES", "MIXEDCASE", "COm")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("BOB.JONES", "MixEDCASE", networkDomain), createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("BOB.JONES", "MIXEDCASE", "COM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("BOB.joNES", "MIXEDcasE", networkDomain), createParams(paging, null));
			
			publicApiClient.setRequestContext(new RequestContext(networkId.toUpperCase(), getUserName("Bob.JONES", "MIXEDCASE", "coM")));

			skipCount = 0;
			maxItems = 10;
			paging = getPaging(skipCount, maxItems);
			peopleProxy.getActivities(getUserName("BOB.joNES", networkPrefix, "cOM"), createParams(paging, null));
		}		
	}
}
