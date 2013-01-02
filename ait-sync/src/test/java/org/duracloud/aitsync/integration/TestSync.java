package org.duracloud.aitsync.integration;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Daniel Bernstein
 * Date:  12/24/2012
 *
 */
public class TestSync {
    private  RestTemplate restTemplate = new RestTemplate();
    private String host = "localhost";
    private String port = "8080";
    private String url="http://"+host+":"+port+"/ait-sync/{command}";
    @Before
    public void setUp() throws Exception {
        
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testStart() {
        ResponseEntity<String> response =
            restTemplate.postForEntity(url,
                                       null,
                                       String.class,
                                       "start");
        assertOk(response);

    }

    private void assertOk(ResponseEntity<String> response) {
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode().value());
    }

    @Test
    public void testStop() {
        ResponseEntity<String> response =
            restTemplate.postForEntity(url,
                                       null,
                                       String.class,
                                       "stop");
        assertOk(response);

    }

    @Test
    public void testStatus() {
        ResponseEntity<String> response =
            restTemplate.getForEntity(url, String.class,"status");
        assertOk(response);
    }

}