/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.integration;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.account.db.model.AccountInfo.AccountStatus;
import org.duracloud.account.flow.createaccount.CreateAccountFlowHandler;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Daniel Bernstein
 * Date: Mar 6, 2012
 */
public class TestNewAccountWizard extends AbstractIntegrationTest {

    private RootBot rootBot;

    @Override
    public void before() throws Exception {
        super.before();
        this.rootBot = new RootBot(sc);
    }

    private void assertNewAccountFormIsPresent() {
        Assert.assertTrue(isElementPresent("id=newAccountForm"));
    }

    private void fillForm(Map<String, String> fields) {
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (e.getValue().startsWith("value=")) {
                sc.select("id=" + e.getKey(), e.getValue());
            } else {
                sc.type("id=" + e.getKey(), e.getValue());
            }
        }
    }

    private void assertAccountInList(String accountName) {
        Assert.assertTrue(isElementPresent(formatAccountListSelector(accountName)));
    }

    private String formatAccountListSelector(String accountName) {
        return MessageFormat.format("css=[data-name=\"{0}\"]", accountName);
    }

    private void assertInfoMessagePresent() {
        Assert.assertTrue(isElementPresent("css=.message.info"));
    }

    private Map<String, String> createNewAccountFormInput(String accountName) {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("acctName", accountName);
        fields.put("orgName", "test organization");
        fields.put("department", "test department");
        fields.put("subdomain", accountName);
        return fields;
    }

    private void deleteAccount(String accountName) {
        String accountListSelector = formatAccountListSelector(accountName);
        clickAndWait(accountListSelector + " .delete");
        assertInfoMessagePresent();
        Assert.assertTrue(!isElementPresent(accountListSelector));
    }

    @Test
    public void testCreateFullAccount() throws Exception {
        rootBot.login();
        UrlHelper.openRelative(sc, "/" + CreateAccountFlowHandler.FLOW_ID);
        assertNewAccountFormIsPresent();
        String accountName = "test" + System.currentTimeMillis();
        Map<String, String> fields = createNewAccountFormInput(accountName);
        fillForm(fields);

        clickAndWait("id=next");
        Assert.assertTrue(isElementPresent("id=fullAccountForm"));
        //check S3 box
        sc.check("css=input[value='AMAZON_S3']");

        clickAndWait("id=finish");
        assertInfoMessagePresent();
        assertAccountInList(accountName);

        //click configure account.
        clickAndWait("css=tr[data-name='" + accountName + "'] .configure");

        //enter insufficient data
        sc.type("id=primaryStorageCredentialsusername", "username");
        sc.type("id=primaryStorageCredentialspassword", "password");

        //click ok
        clickAndWait("id=ok");

        //verify errors
        Assert.assertTrue(
            isElementPresent("css=input[id='secondaryStorageCredentailsList0.username'][class~='error']"));

        //complete form
        Assert.assertTrue(isTextPresent(StorageProviderType.AMAZON_S3.name()));

        sc.type("id=secondaryStorageCredentailsList0.username", "username");
        sc.type("id=secondaryStorageCredentailsList0.password", "password");

        sc.type("id=computeUsername", "username");
        sc.type("id=computePassword", "password");
        sc.type("id=computeElasticIP", "elasticIP");
        sc.type("id=computeKeypair", "keypair");
        sc.type("id=computeSecurityGroup", "security");

        clickAndWait("id=ok");
        //verify info message
        assertInfoMessagePresent();

        //verify activated.
        String status = sc.getText("css=tr[data-name='" + accountName + "'] .status");
        Assert.assertEquals(AccountStatus.ACTIVE, AccountStatus.valueOf(status));

        //delete account
        deleteAccount(accountName);
    }

}
