package org.duraspace.rest;

import junit.framework.TestCase;

import org.junit.Test;

import org.duraspace.rest.RestTestHelper.HttpResponse;

/**
 * Runtime test of service REST API. The orgwebapp
 * web application must be deployed and available at the
 * baseUrl location in order for these tests to pass.
 *
 * @author Bill Branan
 */
public class TestServiceRest
        extends TestCase {

    private static RestTestHelper restHelper = new RestTestHelper();
    private static String baseUrl = "http://localhost:8080/orgwebapp";

    @Test
    public void testGetServices() throws Exception {
        String url = baseUrl + "/service";
        HttpResponse response = restHelper.get(url);

        assertTrue(response.getStatusCode() == 200);
        assertTrue("<services />".equals(response.getResponseBody()));
    }

    @Test
    public void testGetServiceSubscriptions() throws Exception {
        String url = baseUrl + "/service/customer1";
        HttpResponse response = restHelper.get(url);

        assertTrue(response.getStatusCode() == 200);
        assertTrue("<serviceSubscriptions />".equals(response.getResponseBody()));
    }

    @Test
    public void testGetServiceConfiguration() throws Exception {
        String url = baseUrl + "/service/customer1/service1";
        HttpResponse response = restHelper.get(url);

        assertTrue(response.getStatusCode() == 200);
        assertTrue("<serviceConfiguration />".equals(response.getResponseBody()));
    }

    @Test
    public void testAddServiceSubscription() throws Exception {
        String url = baseUrl + "/service/customer1/service1";
        String serviceConfig = "<config />";
        HttpResponse response = restHelper.put(url, serviceConfig);

        assertTrue(response.getStatusCode() == 201);
    }

    @Test
    public void testUpdateServiceConfiguration() throws Exception {
        String url = baseUrl + "/service/customer1/service1";
        String serviceConfig = "<config />";
        HttpResponse response = restHelper.post(url, serviceConfig);

        assertTrue(response.getStatusCode() == 200);
        String responseText = response.getResponseBody();
        assertNotNull(responseText);
        assertTrue(responseText.contains("service1"));
        assertTrue(responseText.contains("updated"));
    }

    @Test
    public void testRemoveServiceSubscription() throws Exception {
        String url = baseUrl + "/service/customer1/service1";
        HttpResponse response = restHelper.delete(url);

        assertTrue(response.getStatusCode() == 200);
        String responseText = response.getResponseBody();
        assertNotNull(responseText);
        assertTrue(responseText.contains("service1"));
        assertTrue(responseText.contains("removed"));
    }
}