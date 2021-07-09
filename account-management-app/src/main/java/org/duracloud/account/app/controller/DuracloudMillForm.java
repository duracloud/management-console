/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import javax.validation.constraints.NotNull;

/**
 * @author Daniel Bernstein
 */
public class DuracloudMillForm {
    @NotNull
    private String dbHost;
    @NotNull
    private Integer dbPort = 3306;
    @NotNull
    private String dbName;
    @NotNull
    private String dbUsername;
    @NotNull
    private String dbPassword;
    @NotNull
    private String auditQueue;
    @NotNull
    private String auditLogSpaceId;
    @NotNull
    private String queueType;
    private Boolean globalPropsRmqConfAvailable;
    private Boolean globalPropsRmqConf;
    private String rabbitmqHost;
    private Integer rabbitmqPort = 5672;
    private String rabbitmqVhost = "/";
    private String rabbitmqExchange;
    private String rabbitmqUsername;
    private String rabbitmqPassword;

    public Boolean getGlobalPropsRmqConfAvailable() {
        return globalPropsRmqConfAvailable;
    }

    public void setGlobalPropsRmqConfAvailable(Boolean globalPropsRmqConfAvailable) {
        this.globalPropsRmqConfAvailable = globalPropsRmqConfAvailable;
    }

    public Boolean getGlobalPropsRmqConf() {
        return globalPropsRmqConf;
    }

    public void setGlobalPropsRmqConf(Boolean globalPropsRmqConf) {
        this.globalPropsRmqConf = globalPropsRmqConf;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public Integer getDbPort() {
        return dbPort;
    }

    public void setDbPort(Integer dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getAuditQueue() {
        return auditQueue;
    }

    public void setAuditQueue(String auditQueue) {
        this.auditQueue = auditQueue;
    }

    public String getAuditLogSpaceId() {
        return auditLogSpaceId;
    }

    public void setAuditLogSpaceId(String auditLogSpaceId) {
        this.auditLogSpaceId = auditLogSpaceId;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public String getRabbitmqHost() {
        return rabbitmqHost;
    }

    public void setRabbitmqHost(String rabbitmqHost) {
        this.rabbitmqHost = rabbitmqHost;
    }

    public Integer getRabbitmqPort() {
        return rabbitmqPort;
    }

    public void setRabbitmqPort(Integer rabbitmqPort) {
        this.rabbitmqPort = rabbitmqPort;
    }

    public String getRabbitmqVhost() {
        return rabbitmqVhost;
    }

    public void setRabbitmqVhost(String rabbitmqVhost) {
        this.rabbitmqVhost = rabbitmqVhost;
    }

    public String getRabbitmqExchange() {
        return rabbitmqExchange;
    }

    public void setRabbitmqExchange(String rabbitmqExchange) {
        this.rabbitmqExchange = rabbitmqExchange;
    }

    public String getRabbitmqUsername() {
        return rabbitmqUsername;
    }

    public void setRabbitmqUsername(String rabbitmqUsername) {
        this.rabbitmqUsername = rabbitmqUsername;
    }

    public String getRabbitmqPassword() {
        return rabbitmqPassword;
    }

    public void setRabbitmqPassword(String rabbitmqPassword) {
        this.rabbitmqPassword = rabbitmqPassword;
    }
}
