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
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.duracloud.account.db.model.EmailTemplate;
import org.duracloud.account.db.util.util.EmailTemplateUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dbernstein
 */
public class EmailTemplateUtilTest {
    private Map<String, String> params = new HashMap<>();

    private final String user = "iamauser";
    private final String first = "John";
    private final String last = "Doe";
    private final String url = "http://myurl";
    private final String date = "01/01/2001";
    private final String subdomain = "mysub";
    private final String domain = "mydomain";
    private final String redemptionCode = "mycode";
    private final String orgName = "myorg";
    private final String accountName = "my account";
    private final String redemptionUrl = "http://myredemptionurl";
    private final String createUserProfileUrl = "http://createUserProfileUrl";

    @Before
    public void setup() {

        params.put("username", user);
        params.put("firstName", first);
        params.put("lastName", last);
        params.put("managementConsoleUrl", url);
        params.put("expirationDate", date);
        params.put("redemptionCode", redemptionCode);
        params.put("organizationName", orgName);
        params.put("domain", domain);
        params.put("subdomain", subdomain);
        params.put("accountName", accountName);
        params.put("redemptionUrl", redemptionUrl);
        params.put("createUserProfileUrl", createUserProfileUrl);

    }

    @Test
    public void testUserCreated() {
        test(USER_CREATED, null, new String[] {user, url});
    }

    @Test
    public void testInviteRedeemed() {
        test(INVITATION_REDEEMED, null, new String[] {user, url});
    }

    @Test
    public void testPasswordReset() {
        test(PASSWORD_RESET, null, new String[] {redemptionCode, date, url});
    }

    @Test
    public void testUserAddedToAccount() {
        test(USER_ADDED_TO_ACCOUNT, new String[] {orgName},
             new String[] {first, last, domain, subdomain, accountName, orgName});
    }

    @Test
    public void testUserInvitation() {
        test(USER_INVITATION, null,
             new String[] {subdomain, accountName, orgName, redemptionUrl, createUserProfileUrl});
    }

    private void test(EmailTemplate.Templates template, String[] subjectValues, String[] bodyValues) {
        EmailTemplate t = EmailTemplateUtil.loadDefault(template);

        String subject = EmailTemplateUtil.format(params, t.getSubject());
        if (subjectValues != null) {
            for (String value : subjectValues) {
                assertTrue(subject.contains(value));
            }
        }

        String body = EmailTemplateUtil.format(params, t.getBody());
        for (String value : bodyValues) {
            assertTrue(body.contains(value));
        }
    }
}
