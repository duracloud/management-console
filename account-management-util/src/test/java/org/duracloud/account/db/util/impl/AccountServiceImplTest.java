/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.HashSet;
import java.util.Set;

import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.repo.DuracloudAccountRepo;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.repo.DuracloudStorageProviderAccountRepo;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.EmailTemplateService;
import org.duracloud.account.db.util.notification.NotificationMgr;
import org.duracloud.common.sns.AccountChangeNotifier;
import org.duracloud.notification.Emailer;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Bernstein
 */
@RunWith(EasyMockRunner.class)
public class AccountServiceImplTest extends EasyMockSupport {
    @Mock
    private DuracloudRepoMgr repoMgr;
    @Mock
    private AmaEndpoint amaEndpoint;
    @Mock
    private AccountInfo acct;
    @Mock
    private DuracloudAccountRepo accountRepo;
    @Mock
    private AccountChangeNotifier accountChangeNotifier;
    @Mock
    private NotificationMgr notificationMgr;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private Emailer emailer;

    @Before
    public void setup() {
        expect(notificationMgr.getEmailer()).andReturn(emailer);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testAddStorageProvider() {

        String subdomain = "test";
        expect(acct.getSubdomain()).andReturn(subdomain);

        expect(acct.getSecondaryStorageProviderAccounts())
            .andReturn(new HashSet<>());

        expect(repoMgr.getAccountRepo())
            .andReturn(accountRepo);
        expect(accountRepo.save(acct))
            .andReturn(null); // output is not used

        replayAll();

        AccountService service = createTestObject();
        service.addStorageProvider(StorageProviderType.AMAZON_S3);
    }

    private AccountService createTestObject() {
        return new AccountServiceImpl(amaEndpoint, acct, repoMgr, accountChangeNotifier, notificationMgr,
                                      emailTemplateService);
    }

    @Test
    public void testRemoveStorageProvider() {
        Long storageProviderId = 1000l;

        DuracloudStorageProviderAccountRepo providerAccountRepo = createMock(DuracloudStorageProviderAccountRepo.class);
        StorageProviderAccount providerAccount = createMock(StorageProviderAccount.class);

        String subdomain = "test";
        expect(acct.getSubdomain()).andReturn(subdomain);

        expect(repoMgr.getStorageProviderAccountRepo())
            .andReturn(providerAccountRepo)
            .times(2);
        expect(providerAccountRepo.findOne(storageProviderId))
            .andReturn(providerAccount);

        Set<StorageProviderAccount> providerAccounts = new HashSet<>();
        providerAccounts.add(providerAccount);
        expect(acct.getSecondaryStorageProviderAccounts())
            .andReturn(providerAccounts);

        expect(repoMgr.getAccountRepo())
            .andReturn(accountRepo);
        expect(accountRepo.save(acct))
            .andReturn(null); // output is not used

        providerAccountRepo.delete(storageProviderId);
        expectLastCall();

        accountChangeNotifier.storageProvidersChanged(subdomain);
        expectLastCall();

        replayAll();

        AccountService service = createTestObject();
        service.removeStorageProvider(storageProviderId);
    }

    @Test
    public void testChangePrimaryStorageProvider() {

        expect(repoMgr.getAccountRepo()).andReturn(accountRepo);
        expect(accountRepo.save(acct)).andReturn(acct);
        Long storageProviderId = 1l;
        StorageProviderAccount primary =
            createStorageProviderAccount(3l, StorageProviderType.AMAZON_S3);
        StorageProviderAccount secondary =
            createStorageProviderAccount(storageProviderId, StorageProviderType.DPN);
        Set<StorageProviderAccount> secondaryaccounts = new HashSet<>();
        secondaryaccounts.add(secondary);

        Set<StorageProviderAccount> result = new HashSet<>();
        result.add(primary);

        expect(acct.getSecondaryStorageProviderAccounts()).andReturn(secondaryaccounts);
        expect(acct.getPrimaryStorageProviderAccount()).andReturn(primary);
        acct.setPrimaryStorageProviderAccount(secondary);
        acct.setSecondaryStorageProviderAccounts(eq(result));
        expectLastCall();

        String subdomain = "test";
        expect(acct.getSubdomain()).andReturn(subdomain);

        accountChangeNotifier.storageProvidersChanged(subdomain);
        expectLastCall();

        replayAll();
        AccountService service = createTestObject();

        service.changePrimaryStorageProvider(storageProviderId);
    }

    private StorageProviderAccount createStorageProviderAccount(Long storageProviderId,
                                                                StorageProviderType providerType) {
        StorageProviderAccount account = new StorageProviderAccount();
        account.setId(storageProviderId);
        account.setProviderType(providerType);
        return account;
    }

}
