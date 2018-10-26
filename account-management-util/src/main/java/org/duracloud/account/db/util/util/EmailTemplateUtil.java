/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.duracloud.account.db.model.EmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dbernstein
 */
public class EmailTemplateUtil {

    private EmailTemplateUtil() {
    }

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateUtil.class);

    /**
     * @param template
     */
    public static EmailTemplate loadDefault(EmailTemplate.Templates template) {
        try (BufferedReader reader = getTemplate(template)) {
            String subject = null;
            String body = null;
            final String subjectLinePrefix = "email-subject:";
            String line = null;
            while ((line = reader.readLine()) != null) {

                if (line.toLowerCase().startsWith(subjectLinePrefix)) {
                    subject = line.substring(subjectLinePrefix.length());
                    break;
                }
            }

            final String bodyPrefix = "email-body:";
            final StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().startsWith(bodyPrefix)) {
                    builder.append(line.substring(bodyPrefix.length()));
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {
                builder.append("\n" + line);
            }

            body = builder.toString();

            EmailTemplate emailTemplate = new EmailTemplate();
            emailTemplate.setTemplate(template);
            emailTemplate.setSubject(subject);
            emailTemplate.setBody(body);
            return emailTemplate;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedReader getTemplate(EmailTemplate.Templates template) {
        return new BufferedReader(new InputStreamReader(
            EmailTemplateUtil.class.getResourceAsStream("/templates/" + template.getTemplateName() + ".txt")));
    }

    public static String format(Map<String, String> parameters, String outputString) {
        for (String key : parameters.keySet()) {
            outputString = outputString.replace("${" + key + "}", parameters.get(key));
        }

        return outputString;
    }

}

