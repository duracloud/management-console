/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.List;

import org.duracloud.account.db.model.DuracloudMill;
import org.duracloud.account.db.model.RabbitmqConfig;
import org.duracloud.account.db.repo.DuracloudMillRepo;
import org.duracloud.account.db.repo.RabbitmqConfigRepo;
import org.duracloud.account.db.util.DuracloudMillConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A service for modifying Mill configuration settings.
 *
 * @author Daniel Bernstein
 */
@Component("duracloudMillConfigService")
public class DuracloudMillConfigServiceImpl implements DuracloudMillConfigService {

    @Autowired
    private DuracloudMillRepo repo;

    public void setRepo(DuracloudMillRepo repo) {
        this.repo = repo;
    }

    public DuracloudMillRepo getRepo() {
        return repo;
    }

    @Autowired
    private RabbitmqConfigRepo rmqRepo;

    @Override
    public DuracloudMill get() {
        List<DuracloudMill> mill = repo.findAll();
        DuracloudMill entity = null;
        if (!mill.isEmpty()) {
            entity = mill.get(0);
        }
        return entity;
    }

    @Override
    public void set(String dbHost,
                    Integer dbPort,
                    String dbName,
                    String dbUsername,
                    String dbPassword,
                    String auditQueue,
                    String auditLogSpaceId,
                    String queueType,
                    Long rabbitmqConfigId,
                    String rabbitmqExchange) {
        DuracloudMill dm = get();
        if (null == dm) {
            dm = new DuracloudMill();
        }

        RabbitmqConfig rabbitmqConfig = null;
        if (null != rabbitmqConfigId) {
            rabbitmqConfig = rmqRepo.findOne(rabbitmqConfigId);
            if (null == rabbitmqConfig) {
                rabbitmqConfig = new RabbitmqConfig();
            }
        }

        dm.setDbHost(dbHost);
        dm.setDbPort(dbPort);
        dm.setDbName(dbName);
        dm.setDbUsername(dbUsername);
        dm.setDbPassword(dbPassword);
        dm.setAuditQueue(auditQueue);
        dm.setAuditLogSpaceId(auditLogSpaceId);
        dm.setQueueType(queueType);
        dm.setRabbitmqExchange(rabbitmqExchange);
        dm.setRabbitmqConfig(rabbitmqConfig);
        repo.save(dm);

    }
}
