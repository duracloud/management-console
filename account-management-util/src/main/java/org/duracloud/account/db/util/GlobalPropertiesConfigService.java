/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util;

import org.duracloud.account.db.model.GlobalProperties;
import org.springframework.security.access.annotation.Secured;

/**
 * Duracloud Mill Configuration Management
 *
 * @author: Daniel Bernstein
 */
public interface GlobalPropertiesConfigService {

    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public GlobalProperties get();

    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public void set(String notifierType,
                    Long rabbitmqConfigId,
                    String rabbitmqExchange,
                    String instanceNotificationTopicArn,
                    String cloudFrontAccountId,
                    String cloudFrontKeyId,
                    String cloudFrontKeyPath);

}
