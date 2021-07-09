/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import org.duracloud.account.db.model.RabbitmqConfig;
import org.duracloud.account.db.repo.RabbitmqConfigRepo;
import org.duracloud.account.db.util.RabbitmqConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A service for modifying RabbitMQ settings.
 *
 * @author Andy Foster
 * Date: 2021/05/06
 */
@Component("rabbitmqConfigService")
public class RabbitmqConfigServiceImpl implements RabbitmqConfigService {

    @Autowired
    private RabbitmqConfigRepo repo;

    public void setRepo(RabbitmqConfigRepo repo) {
        this.repo = repo;
    }

    public RabbitmqConfigRepo getRepo() {
        return repo;
    }

    @Override
    public RabbitmqConfig get(Long id) {
        RabbitmqConfig entity = repo.findOne(id);
        return entity;
    }

    @Override
    public void set(Long id,
                    String host,
                    Integer port,
                    String vhost,
                    String username,
                    String password) {
        RabbitmqConfig rmqc = get(id);
        if (null == rmqc) {
            rmqc = new RabbitmqConfig();
        }

        rmqc.setId(id);
        rmqc.setHost(host);
        rmqc.setPort(port);
        rmqc.setVhost(vhost);
        rmqc.setUsername(username);
        rmqc.setPassword(password);

        repo.save(rmqc);
    }
}
