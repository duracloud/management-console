/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.notification;

import static org.duracloud.account.db.model.EmailTemplate.Templates.INVITATION_REDEEMED;
import static org.duracloud.account.db.model.EmailTemplate.Templates.PASSWORD_RESET;
import static org.duracloud.account.db.model.EmailTemplate.Templates.USER_ADDED_TO_ACCOUNT;
import static org.duracloud.account.db.model.EmailTemplate.Templates.USER_CREATED;
import static org.duracloud.account.db.model.EmailTemplate.Templates.USER_INVITATION;
import static org.duracloud.account.db.util.util.EmailTemplateUtil.format;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.EmailTemplate;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.util.EmailTemplateService;
import org.duracloud.account.db.util.error.UnsentEmailException;
import org.duracloud.notification.Emailer;

/**
 * @author: Bill Branan
 * Date: 8/1/11
 */
public class Notifier {

    private Emailer emailer;
    private AmaEndpoint amaEndpoint;
    private EmailTemplateService emailTemplateService;

    public Notifier(Emailer emailer, AmaEndpoint amaEndpoint, EmailTemplateService emailTemplateService) {
        this.emailer = emailer;
        this.amaEndpoint = amaEndpoint;
        this.emailTemplateService = emailTemplateService;
    }

    private Map<String, String> createParameters(DuracloudUser user, AmaEndpoint amaEndpoint) {
        Map<String,String> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("firstName", user.getFirstName());
        params.put("lastName", user.getLastName());
        params.putAll(createParameters(amaEndpoint));
        return params;
    }

    private Map<String, String> createParameters(AmaEndpoint amaEndpoint) {
        Map<String,String> params = new HashMap<>();
        params.put("managementConsoleUrl", amaEndpoint.getUrl());
        params.put("domain", amaEndpoint.getDomain());
        return params;
    }

    private void sendEmail(EmailTemplate template, Map<String, String> parameters, String recipientEmail) {
        sendEmail(format(parameters, template.getSubject()), format(parameters, template.getBody()), recipientEmail);
    }

    public void sendNotificationCreateNewUser(DuracloudUser user) {
        EmailTemplate template = emailTemplateService.getTemplate(USER_CREATED);
        sendEmail(template,  createParameters(user, amaEndpoint), user.getEmail());
    }

    public void sendNotificationPasswordReset(DuracloudUser user,
                                              String redemptionCode,
                                              Date date) {

        EmailTemplate template = emailTemplateService.getTemplate(PASSWORD_RESET);
        Map<String,String> parameters = createParameters(user, amaEndpoint);
        parameters.put("redemptionCode", redemptionCode);
        parameters.put("expirationDate", date.toString());
        sendEmail(template,  parameters, user.getEmail());
    }

    public void sendNotificationRedeemedInvitation(DuracloudUser user,
                                                   String adminEmail) {
        EmailTemplate template = emailTemplateService.getTemplate(INVITATION_REDEEMED);
        Map<String,String> parameters = createParameters(user, amaEndpoint);
        sendEmail(template,  parameters, adminEmail);
    }

    public void sendNotificationUserAddedToAccount(DuracloudUser user, AccountInfo accountInfo) {
        EmailTemplate template = emailTemplateService.getTemplate(USER_ADDED_TO_ACCOUNT);
        Map<String,String> parameters = createParameters(user, amaEndpoint);
        parameters.putAll(createParameters(accountInfo));

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

    public void sendNotificationUserInvitation(UserInvitation invitation) {
        final EmailTemplate template = emailTemplateService.getTemplate(USER_INVITATION);
        final Map<String,String> parameters = createParameters(amaEndpoint);
        parameters.putAll(createParameters(invitation.getAccount()));
        parameters.put("redemptionUrl",  amaEndpoint.getUrl() + "/users/redeem/" + invitation.getRedemptionCode());
        parameters.put("createUserProfileUrl", amaEndpoint.getUrl() + "/users/new");
        sendEmail(template, parameters, invitation.getUserEmail());
    }

    private Map<String,String> createParameters(AccountInfo accountInfo) {
        final Map<String,String> parameters = new HashMap<>();
        final StringBuilder organizationName = new StringBuilder(accountInfo.getOrgName());
        if (StringUtils.isNotBlank(accountInfo.getDepartment())) {
            organizationName.append(", ");
            organizationName.append(accountInfo.getDepartment());
        }

        parameters.put("organizationName", organizationName.toString());
        parameters.put("subdomain", accountInfo.getSubdomain());
        parameters.put("accountName", accountInfo.getAcctName());
        return parameters;
    }
}
