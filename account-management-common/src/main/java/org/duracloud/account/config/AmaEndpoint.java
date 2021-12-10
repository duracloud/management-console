/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.config;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrew Woods
 * Date: 3/21/11
 */
public class AmaEndpoint {
    private McConfig config;

    public AmaEndpoint(McConfig config) {
        this.config = config;
    }

    public String getHost() {
        return config.getMcHost();
    }

    public String getCtxt() {
        return config.getMcContext();
    }

    public String getDomain() {
        return config.getMcDomain();
    }

    public String getPort() {
        return config.getMcPort();
    }

    public String getUrl() {
        final var port = getPort();
        final var proto = port.equals("443") ? "https" : "http";
        final var portInUrl = port.equals("443") || port.equals("80") ? ""  : ":" + port;
        final var context = getCtxt();
        final var contextInUrl = context.startsWith("/") || StringUtils.isBlank(context) ? context : "/" + context;
        return proto + "://" + getHost() + portInUrl + contextInUrl;
    }

}
