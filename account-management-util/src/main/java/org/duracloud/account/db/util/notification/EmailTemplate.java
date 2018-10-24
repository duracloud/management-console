/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.notification;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.duracloud.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dbernstein
 */
public class EmailTemplate {

    public enum Templates {
        USER_CREATED("user-created"),
        PASSWORD_RESET("password-reset"),
        INVITATION_REDEEMED("invitation-redeemed"),
        USER_ADDED_TO_ACCOUNT("user-added-to-account");

        private String templateName;

        private Templates(String templateName) {
            this.templateName = templateName;
        }

        public String getTemplateName() {
            return templateName;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(EmailTemplate.class);
    private static final String TEMPLATE_DIR_SYSTEM_PROPERTY = "duracloud.template.dir";
    private String subject;
    private String body;

    /**
     * @param template
     */
    public EmailTemplate(Templates template) {
        try (BufferedReader is = new BufferedReader(
            new InputStreamReader(getTemplateInputStream(template.getTemplateName())))) {
            String line = null;

            final String subjectLinePrefix = "email-subject:";
            while ((line = is.readLine()) != null) {

                if (line.toLowerCase().startsWith(subjectLinePrefix)) {
                    subject = line.substring(subjectLinePrefix.length());
                    break;
                }
            }

            final String bodyPrefix = "email-body:";
            final StringBuilder builder = new StringBuilder();
            while ((line = is.readLine()) != null) {
                if (line.toLowerCase().startsWith(bodyPrefix)) {
                    builder.append(line.substring(bodyPrefix.length()));
                    break;
                }
            }

            while ((line = is.readLine()) != null) {
                builder.append("\n" + line);
            }

            this.body = builder.toString();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private InputStream getTemplateInputStream(String templateName) {
        final String templateDirPath = System.getProperty(TEMPLATE_DIR_SYSTEM_PROPERTY);
        final String fileName = templateName + ".txt";
        if (templateDirPath != null) {
            final File templateDirectory = new File(templateDirPath);
            if (templateDirectory.exists()) {
                final File file = new File(templateDirectory, fileName);
                if (file.exists()) {
                    log.debug(
                        "Loading template {} from {}...", templateName, templateDirPath);

                    return IOUtil.getFileStream(file);
                }
            } else {
                log.warn(
                    "The template directory you specified in the {} system property does not exist: {} .  Loading " +
                    "template {} from the classpath...",
                    TEMPLATE_DIR_SYSTEM_PROPERTY, templateDirPath, templateName);
            }
        }

        log.debug(
            "Loading template {} from the classpath...", templateName);

        return getClass().getResourceAsStream("/templates/" + fileName);
    }

    /**
     * @param parameters
     * @return
     */
    public String formatSubject(Map<String, String> parameters) {
        return formatString(parameters, this.subject);
    }

    private String formatString(Map<String, String> parameters, String outputString) {
        for (String key : parameters.keySet()) {
            outputString = outputString.replace("${" + key + "}", parameters.get(key));
        }

        return outputString;
    }

    /**
     * @param parameters
     * @return
     */
    public String formatBody(Map<String, String> parameters) {
        return formatString(parameters, this.body);
    }
}
