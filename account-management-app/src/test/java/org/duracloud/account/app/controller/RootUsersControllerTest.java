/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.util.HashSet;

import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.util.RootAccountManagerService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Nicholas Woodward
 */
public class RootUsersControllerTest extends AmaControllerTestBase {
    private RootUsersController rootUsersController;
    private RootAccountManagerService rootAccountManagerService;

    @Before
    public void before() throws Exception {
        super.before();

        setupGenericAccountAndUserServiceMocks(TEST_ACCOUNT_ID);
        rootAccountManagerService =
            createMock(RootAccountManagerService.class);
        rootUsersController = new RootUsersController();
        rootUsersController.setRootAccountManagerService(rootAccountManagerService);
        rootUsersController.setAccountManagerService(this.accountManagerService);
        rootUsersController.setUserService(this.userService);
    }

    /**
     * Test method for org.duracloud.account.app.controller.rootUsersController
     *
     * @throws AccountNotFoundException
     */
    @Test
    public void testGet() throws AccountNotFoundException {
        EasyMock.expect(rootAccountManagerService.listAllRootUsers(null)).andReturn(new HashSet<DuracloudUser>());
        replayMocks();
        rootUsersController.get();
    }

    @Test
    public void testSetRoot() throws Exception {
        UsernameForm usernameForm = new UsernameForm();
        usernameForm.setUsername(TEST_USERNAME);
        DuracloudUser user = createUser(TEST_USERNAME);

        EasyMock.expect(result.hasErrors()).andReturn(false);
        EasyMock.expect(userService.loadDuracloudUserByUsernameInternal(TEST_USERNAME))
                .andReturn(user)
                .anyTimes();

        this.rootAccountManagerService.setRootUser(user.getId());
        EasyMock.expectLastCall();
        replayMocks();

        ModelAndView mav =
                this.rootUsersController.setRootUser(
                        usernameForm,
                        result,
                        redirectAttributes);
        Assert.assertNotNull(mav);
        Assert.assertTrue(mav.getView() instanceof RedirectView);
    }

    @Test
    public void testUnsetRoot() throws Exception {
        this.rootAccountManagerService.unsetRootUser(0L);
        EasyMock.expectLastCall();
        addFlashAttribute();

        replayMocks();
        rootUsersController.unsetRootUser(0L, redirectAttributes);
    }

}
