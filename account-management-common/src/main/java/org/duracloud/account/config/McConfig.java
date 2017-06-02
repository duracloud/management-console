/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.config;

/**
 * Configuration properties for the Management Console
 *
 * @author Bill Branan
 *         Date: 6/18/14
 */
public class McConfig {

    private String mcHost;
    private String mcPort;
    private String mcContext;
    private String mcDomain;
    
    private String notificationUser;
    private String notificationPass;
    private String notificationFromAddress;
    private String notificationAdminAddress;

    private String dbHost;
    private String dbPort;
    private String dbName;
    private String dbUser;
    private String dbPass;

    public McConfig(String mcHost,
                    String mcPort,
                    String mcContext,
                    String mcDomain,
                    String notificationUser,
                    String notificationPass,
                    String notificationFromAddress,
                    String notificationAdminAddress,
                    String dbHost,
                    String dbPort,
                    String dbName,
                    String dbUser,
                    String dbPass) {
        this.mcHost = mcHost;
        this.mcPort = mcPort;
        this.mcContext = mcContext;
        this.mcDomain = mcDomain;
        this.notificationUser = notificationUser;
        this.notificationPass = notificationPass;
        this.notificationFromAddress = notificationFromAddress;
        this.notificationAdminAddress = notificationAdminAddress;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    public String getMcHost() {
        return mcHost;
    }

    public String getMcPort() {
        return mcPort;
    }

    public String getMcContext() {
        return mcContext;
    }

    public String getMcDomain() {
        return mcDomain;
    }
    
    public String getNotificationUser() {
        return notificationUser;
    }

    public String getNotificationPass() {
        return notificationPass;
    }

    public String getNotificationFromAddress() {
        return notificationFromAddress;
    }

    public String getNotificationAdminAddress() {
        return notificationAdminAddress;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

}
