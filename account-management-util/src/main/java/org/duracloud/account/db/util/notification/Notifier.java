/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.notification;

import static org.duracloud.account.db.util.notification.EmailTemplate.Templates.INVITATION_REDEEMED;
import static org.duracloud.account.db.util.notification.EmailTemplate.Templates.PASSWORD_RESET;
import static org.duracloud.account.db.util.notification.EmailTemplate.Templates.USER_ADDED_TO_ACCOUNT;
import static org.duracloud.account.db.util.notification.EmailTemplate.Templates.USER_CREATED;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.util.error.UnsentEmailException;
import org.duracloud.notification.Emailer;

/**
 * @author: Bill Branan
 * Date: 8/1/11
 */
public class Notifier {

    private Emailer emailer;
    private AmaEndpoint amaEndpoint;

    public Notifier(Emailer emailer, AmaEndpoint amaEndpoint) {
        this.emailer = emailer;
        this.amaEndpoint = amaEndpoint;
    }

    private EmailTemplate getTemplate(EmailTemplate.Templates template) {
        return new EmailTemplate(template);
    }

    private Map<String, String> createParameters(DuracloudUser user, AmaEndpoint amaEndpoint) {
        Map<String,String> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("firstName", user.getFirstName());
        params.put("lastName", user.getLastName());
        params.put("managementConsoleUrl", amaEndpoint.getUrl());
        return params;
    }

    private void sendEmail(EmailTemplate template, Map<String, String> parameters, String recipientEmail) {
        sendEmail(template.formatSubject(parameters), template.formatBody(parameters), recipientEmail);
    }

    public void sendNotificationCreateNewUser(DuracloudUser user) {
        EmailTemplate template = getTemplate(USER_CREATED);
        sendEmail(template,  createParameters(user, amaEndpoint), user.getEmail());
    }

    public void sendNotificationPasswordReset(DuracloudUser user,
                                              String redemptionCode,
                                              Date date) {

        EmailTemplate template = getTemplate(PASSWORD_RESET);
        Map<String,String> parameters = createParameters(user, amaEndpoint);
        parameters.put("redemptionCode", redemptionCode);
        parameters.put("expirationDate", date.toString());
        sendEmail(template,  parameters, user.getEmail());
    }

    public void sendNotificationRedeemedInvitation(DuracloudUser user,
                                                   String adminEmail) {
        EmailTemplate template = getTemplate(INVITATION_REDEEMED);
        Map<String,String> parameters = createParameters(user, amaEndpoint);
        sendEmail(template,  parameters, adminEmail);
    }

    public void sendNotificationUserAddedToAccount(DuracloudUser user, AccountInfo accountInfo) {
        EmailTemplate template = getTemplate(USER_ADDED_TO_ACCOUNT);
        Map<String,String> parameters = createParameters(user, amaEndpoint);
        StringBuilder organizationName = new StringBuilder(accountInfo.getOrgName());
        if (StringUtils.isNotBlank(accountInfo.getDepartment())) {
            organizationName.append(", ");
            organizationName.append(accountInfo.getDepartment());
        }

        parameters.put("organizationName", organizationName.toString());
        parameters.put("subdomain", accountInfo.getSubdomain());
        parameters.put("domain", amaEndpoint.getDomain());
        parameters.put("accountName", accountInfo.getAcctName());

        sendEmail(template, parameters, user.getEmail());
    }

    private void sendEmail(String subject, String message, String emailAddr) {
        try {
            emailer.send(subject, message, emailAddr);
        } catch (Exception e) {
            String msg =
                "Error: Unable to send email with subject: " + subject +
                " to address: " + emailAddr;
            throw new UnsentEmailException(msg, e);
        }
    }

}
