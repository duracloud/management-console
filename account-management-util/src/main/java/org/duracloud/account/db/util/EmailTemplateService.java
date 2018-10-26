/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util;

import java.util.List;

import org.duracloud.account.db.model.EmailTemplate;
import org.springframework.security.access.annotation.Secured;

/**
 * @author dbernstein
 */
public interface EmailTemplateService {
    /**
     * Returns the template by id
     * @param templateId
     * @return
     */
    @Secured({"role:ROLE_ROOT, scope:ANY"})
    public EmailTemplate getTemplate(Long templateId);

    /**
     * Returns the template by enum value
     * @param template
     * @return
     */
    @Secured({"role:ROLE_ROOT, scope:ANY"})
    public EmailTemplate getTemplate(EmailTemplate.Templates template);

    /**
     * Updates the template and returns the updated object.
     * @param templateId
     * @param subject
     * @param body
     * @return
     */
    @Secured({"role:ROLE_ROOT, scope:ANY"})
    public EmailTemplate update(Long templateId, String subject, String body);

    /**
     * Lists all the email templates
     * @return
     */
    @Secured({"role:ROLE_ROOT, scope:ANY"})
    public List<EmailTemplate> list();
}
