/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.config;

/**
 * @author Andrew Woods
 *         Date: 3/21/11
 */
public class AmaEndpoint {
    private McConfig config;
    public AmaEndpoint(McConfig config){
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
    
    public  String getPort() {
        return config.getMcPort();
    }

    public  String getUrl() {
        String proto = getPort().equals("443") ? "https" : "http";
        return proto + "://" + getHost() + ":" + getPort() + "/" + getCtxt();
    }

}
