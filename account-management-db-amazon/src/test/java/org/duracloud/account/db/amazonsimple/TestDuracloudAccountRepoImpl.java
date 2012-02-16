/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.db.amazonsimple;

import org.duracloud.account.common.domain.AccountInfo;
import org.duracloud.account.common.domain.AccountType;
import org.duracloud.account.db.error.DBConcurrentUpdateException;
import org.duracloud.account.db.error.DBNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Dec 3, 2010
 */
public class TestDuracloudAccountRepoImpl extends BaseTestDuracloudRepoImpl {

    private DuracloudAccountRepoImpl accountRepo;

    private static final String DOMAIN = "TEST_DURACLOUD_ACCOUNTS";

    private static final String subdomain0 = "subdomain0";
    private static final String subdomain1 = "subdomain1";
    private static final String subdomain2 = "subdomain2";
    private static final String acctName = "account-name";
    private static final String orgName = "org-name";
    private static final String department = "department";
    private static final int paymentInfoId = 100;
    private static final int serverDetailsId = 200;
    private static final int accountClusterId = 300;
    private static final AccountInfo.AccountStatus status =
        AccountInfo.AccountStatus.PENDING;
    private static final AccountType type = AccountType.FULL;

    @Before
    public void setUp() throws Exception {
        accountRepo = createAccountRepo();
    }

    private static DuracloudAccountRepoImpl createAccountRepo() throws Exception {
        return new DuracloudAccountRepoImpl(getDBManager(), DOMAIN);
    }

    @After
    public void tearDown() throws Exception {
        for(Integer itemId : accountRepo.getItemIds()) {
            accountRepo.delete(itemId);
        }
        verifyRepoSize(accountRepo, 0);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        createAccountRepo().removeDomain();
    }

    @Test
    public void testNotFound() {
        boolean thrown = false;
        try {
            accountRepo.findById(-100);
            Assert.fail("exception expected");

        } catch (DBNotFoundException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testGetIds() throws Exception {
        AccountInfo acct0 = createAccount(0, subdomain0);
        AccountInfo acct1 = createAccount(1, subdomain1);
        AccountInfo acct2 = createAccount(2, subdomain2);

        accountRepo.save(acct0);
        accountRepo.save(acct1);
        accountRepo.save(acct2);

        List<Integer> expectedIds = new ArrayList<Integer>();
        expectedIds.add(acct0.getId());
        expectedIds.add(acct1.getId());
        expectedIds.add(acct2.getId());

        new DBCaller<Integer>() {
            protected Integer doCall() throws Exception {
                return accountRepo.getIds().size();
            }
        }.call(expectedIds.size());

        verifyAccount(acct0);
        verifyAccount(acct1);
        verifyAccount(acct2);

        verifyAccountBySubdomain(subdomain0, acct0);
        verifyAccountBySubdomain(subdomain1, acct1);
        verifyAccountBySubdomain(subdomain2, acct2);

        // test concurrency
        verifyCounter(acct0, 1);

        AccountInfo acct = null;
        while (null == acct) {
            acct = accountRepo.findById(acct0.getId());
        }
        Assert.assertNotNull(acct);

        boolean thrown = false;
        try {
            accountRepo.save(acct);
            accountRepo.save(acct);
            accountRepo.save(acct);
            accountRepo.save(acct);

        } catch (DBConcurrentUpdateException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        verifyCounter(acct0, 2);
    }

    @Test
    public void testDelete() throws Exception {
        AccountInfo acct0 = createAccount(0, subdomain0);
        accountRepo.save(acct0);
        verifyRepoSize(accountRepo, 1);

        accountRepo.delete(acct0.getId());
        verifyRepoSize(accountRepo, 0);
    }

    private AccountInfo createAccount(int id, String subdomain) {
        return new AccountInfo(id,
                               subdomain,
                               acctName,
                               orgName,
                               department,
                               paymentInfoId,
                               serverDetailsId,
                               accountClusterId,
                               status,
                               type);
    }

    private void verifyAccount(final AccountInfo acct) {
        new DBCaller<AccountInfo>() {
            protected AccountInfo doCall() throws Exception {
                return accountRepo.findById(acct.getId());
            }
        }.call(acct);
    }

    private void verifyAccountBySubdomain(final String subdomain,
                                          final AccountInfo acct) {
        new DBCaller<AccountInfo>() {
            protected AccountInfo doCall() throws Exception {
                return accountRepo.findBySubdomain(subdomain);
            }
        }.call(acct);
    }

    private void verifyCounter(final AccountInfo acct, final int counter) {
        new DBCaller<Integer>() {
            protected Integer doCall() throws Exception {
                return accountRepo.findById(acct.getId()).getCounter();
            }
        }.call(counter);
    }

}
