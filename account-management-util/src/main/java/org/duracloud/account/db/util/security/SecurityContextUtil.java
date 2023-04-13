/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.security;

import org.duracloud.common.error.NoUserLoggedInException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This class returns the Credential of the user currently logged into the
 * session.
 *
 * @author Andrew Woods
 * Date: 4/7/11
 */
public class SecurityContextUtil {

    private final Logger log = LoggerFactory.getLogger(SecurityContextUtil.class);

    public Authentication getAuthentication() throws NoUserLoggedInException {
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().getContext();
        Authentication auth = context.getAuthentication();
        if (null == auth) {
            log.debug("no user-auth found.");
            throw new NoUserLoggedInException();
        }
        return auth;
    }
}

