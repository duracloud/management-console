/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util;

import org.duracloud.account.db.model.RabbitmqConfig;
import org.springframework.security.access.annotation.Secured;

/**
 * RabbitMQ Configuration management.
 *
 * @author Andy Foster
 * Date: 2021/05/06
 */
public interface RabbitmqConfigService {

    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public RabbitmqConfig get(Long id);

    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public void set(Long id,
                    String host,
                    Integer port,
                    String vhost,
                    String username,
                    String password);

}
