/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * author dbernstein
 */
public class AmaEndpointTest {

    @Test
    public void testGetUrl() {
        var endpoint = createConfig("");
        assertEquals("http://localhost:8080", endpoint.getUrl());

        endpoint = createConfig("/context");
        assertEquals("http://localhost:8080/context", endpoint.getUrl());
    }

    private AmaEndpoint createConfig(final String context) {
        return new AmaEndpoint(new McConfig("localhost", "8080", context, "mydomain",
                "SES", null, null, null, "25",
                null, null, null, null, null, null, null));
    }
}
