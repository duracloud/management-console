/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.util.HashSet;
import java.util.Set;

import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @contributor "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public class AccountDetailsControllerTest extends AmaControllerTestBase {
    private AccountDetailsController controller;

    @Before
    public void before() throws Exception {
        super.before();
        setupGenericAccountAndUserServiceMocks(TEST_ACCOUNT_ID);
        controller = new AccountDetailsController();
        controller.setAccountManagerService(this.accountManagerService);
        controller.setUserService(this.userService);
    }

    @Test
    public void testRemoveProvider() throws Exception {
        Set<StorageProviderAccount> spa = new HashSet<StorageProviderAccount>();
        StorageProviderAccount storage = new StorageProviderAccount();
        storage.setId(0L);
        storage.setProviderType(StorageProviderType.AMAZON_S3);
        storage.setUsername("test");
        storage.setPassword("test");
        spa.add(storage);
        EasyMock.expect(this.accountService.getSecondaryStorageProviders()).andReturn(spa);
        this.accountService.removeStorageProvider(EasyMock.anyLong());
        addFlashAttribute();
        replayMocks();
        this.controller.removeProvider(TEST_ACCOUNT_ID, StorageProviderType.AMAZON_S3.toString(), redirectAttributes);
    }

    @Test
    public void testChangePrimaryStorageProvider() throws Exception {
        Set<StorageProviderAccount> spa = new HashSet<StorageProviderAccount>();
        StorageProviderAccount storage = new StorageProviderAccount();
        storage.setId(0L);
        storage.setProviderType(StorageProviderType.AMAZON_S3);
        storage.setUsername("test");
        storage.setPassword("test");
        spa.add(storage);
        EasyMock.expect(this.accountService.getSecondaryStorageProviders()).andReturn(spa);
        this.accountService.changePrimaryStorageProvider(EasyMock.anyLong());
        addFlashAttribute();
        replayMocks();
        this.controller.makePrimary(TEST_ACCOUNT_ID, StorageProviderType.AMAZON_S3.toString(), redirectAttributes);
    }

}
