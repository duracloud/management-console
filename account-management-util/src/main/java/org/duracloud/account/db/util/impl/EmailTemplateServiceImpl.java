/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;

import org.duracloud.account.db.model.EmailTemplate;
import org.duracloud.account.db.repo.EmailTemplateRepo;
import org.duracloud.account.db.util.EmailTemplateService;
import org.duracloud.account.db.util.util.EmailTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author dbernstein
 */
@Component("emailTemplateService")
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private Logger log = LoggerFactory.getLogger(EmailTemplateServiceImpl.class);

    @Autowired
    private EmailTemplateRepo repo;

    @PostConstruct
    public void init() {
        log.info("Initializing email templates...");
        //if no templates are present, load from defaults.
        if (list().size() == EmailTemplate.Templates.values().length) {
            log.info("Templates initialized.");
        } else {
            Arrays.asList(EmailTemplate.Templates.values()).stream().forEach(t -> {
                EmailTemplate emailTemplate = this.repo.findByTemplate(t);
                if (emailTemplate == null) {
                    this.repo.save(EmailTemplateUtil.loadDefault(t));
                }
            });
        }
    }

    @Override
    public EmailTemplate getTemplate(EmailTemplate.Templates template) {
        return this.repo.findByTemplate(template);
    }

    @Override
    public EmailTemplate getTemplate(Long templateId) {
        return this.repo.findOne(templateId);
    }

    @Override
    public List<EmailTemplate> list() {
        return this.repo.findAll();
    }

    @Override
    public EmailTemplate update(Long templateId, String subject, String body) {
        EmailTemplate emailTemplate = getTemplate(templateId);
        emailTemplate.setSubject(subject);
        emailTemplate.setBody(body);
        emailTemplate.setModified(new Date());
        return this.repo.save(emailTemplate);
    }
}
