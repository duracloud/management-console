/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import static org.easymock.EasyMock.expect;

import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.repo.DuracloudUserRepo;
import org.duracloud.account.db.util.EmailTemplateService;
import org.duracloud.account.db.util.error.InvalidUsernameException;
import org.duracloud.account.db.util.error.ReservedPrefixException;
import org.duracloud.account.db.util.error.UserAlreadyExistsException;
import org.duracloud.account.db.util.notification.NotificationMgr;
import org.duracloud.common.sns.AccountChangeNotifier;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A test class for DuracloudUserServiceImpl.java
 *
 * @author Danny Bernstein
 */
@RunWith(EasyMockRunner.class)
public class DuracloudUserServiceImplTest extends EasyMockSupport {

    @Mock
    private DuracloudRepoMgr duracloudRepoMgr;

    @Mock
    private NotificationMgr notificationMgr;

    @Mock
    private AmaEndpoint endpoint;

    @Mock
    private AccountChangeNotifier notifier;

    @Mock
    private DuracloudUserRepo userRepo;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private DuracloudUser user;

    private DuracloudUserServiceImpl service;

    @Before
    public void setup() {
        service = getDuracloudUserService();
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testCheckUsernameSucceedsWithDash() throws Exception {
        testCheckUsernameSucceeds("test-user");
    }

    @Test
    public void testCheckUsernameSucceedsWithDot() throws Exception {
        testCheckUsernameSucceeds("test.user");
    }

    @Test
    public void testCheckUsernameSucceedsWithUnderscore() throws Exception {
        testCheckUsernameSucceeds("test_user");
    }

    @Test
    public void testCheckUsernameSucceedsWithNumbers() throws Exception {
        testCheckUsernameSucceeds("test1234");
    }

    protected void testCheckUsernameSucceeds(String username) throws Exception {
        expect(duracloudRepoMgr.getUserRepo()).andReturn(userRepo);
        expect(userRepo.findByUsername(username)).andReturn(null);
        replayAll();
        service.checkUsername(username);
    }

    @Test(expected = InvalidUsernameException.class)
    public void testCheckUsernameFailsWithEmailAddress() throws Exception {
        replayAll();
        service.checkUsername("test@example.com");
    }

    @Test(expected = InvalidUsernameException.class)
    public void testCheckUsernameFailsWithCaps() throws Exception {
        replayAll();
        service.checkUsername("TEST");
    }

    @Test(expected = InvalidUsernameException.class)
    public void testCheckUsernameWithNonAZChars() throws Exception {
        replayAll();
        service.checkUsername("अनिच्चा");
    }

    @Test(expected = ReservedPrefixException.class)
    public void test() throws Exception {
        replayAll();
        service.checkUsername("group-test");
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void testCheckUsernameExists() throws Exception {
        expect(duracloudRepoMgr.getUserRepo()).andReturn(userRepo);
        expect(userRepo.findByUsername("user")).andReturn(user);
        replayAll();
        service.checkUsername("user");
    }

    private DuracloudUserServiceImpl getDuracloudUserService() {
        return new DuracloudUserServiceImpl(duracloudRepoMgr, notificationMgr, endpoint, notifier, emailTemplateService);
    }
}
