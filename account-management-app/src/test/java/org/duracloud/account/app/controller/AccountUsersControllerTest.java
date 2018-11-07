/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.util.Arrays;
import java.util.HashSet;

import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.duracloud.account.db.util.error.DBNotFoundException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @contributor "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public class AccountUsersControllerTest extends AmaControllerTestBase {
    private AccountUsersController accountUsersController;

    @Before
    public void before() throws Exception {
        super.before();
        this.accountUsersController = new AccountUsersController();
        this.accountUsersController.setAccountManagerService(
            accountManagerService);
        this.accountUsersController.setUserService(userService);
    }

    @Test
    public void testAddUserByUsername() throws Exception {
        Long accountId = 2L;
        UsernameForm usernameForm = new UsernameForm();
        usernameForm.setUsername(TEST_USERNAME);
        Model model = new ExtendedModelMap();
        DuracloudUser user = createUser(TEST_USERNAME);

        EasyMock.expect(result.hasErrors()).andReturn(false);
        EasyMock.expect(userService.loadDuracloudUserByUsernameInternal(TEST_USERNAME))
                .andReturn(user)
                .anyTimes();

        EasyMock.expect(userService.addUserToAccount(accountId, user.getId()))
                .andReturn(true);

        addFlashAttribute();

        replayMocks();
        ModelAndView mav =
            this.accountUsersController.addUser(accountId,
                                                usernameForm,
                                                result,
                                                model,
                                                redirectAttributes);
        Assert.assertNotNull(mav);
        Assert.assertTrue(mav.getView() instanceof RedirectView);
    }

    @Test
    public void testSendInvitations() throws Exception {
        setupGenericAccountAndUserServiceMocks(TEST_ACCOUNT_ID);
        UserInvitation ui = createUserInvitation();

        EasyMock.expect(accountService.inviteUser(ui.getUserEmail(), ui.getAdminUsername()))
                .andReturn(ui);

        EasyMock.expect(result.hasErrors()).andReturn(false);
        addFlashAttribute();
        replayMocks();

        InvitationForm invitationForm = new InvitationForm();
        invitationForm.setEmailAddresses("test@duracloud.org");

        // call under test.
        this.accountUsersController.sendInvitations(TEST_ACCOUNT_ID,
                                                    invitationForm,
                                                    result,
                                                    model,
                                                    redirectAttributes);
    }

    /**
     * @return
     */
    private UserInvitation createUserInvitation() {
        UserInvitation ui = new UserInvitation(2L, createAccountInfo(TEST_ACCOUNT_ID),
                                               null, null, null, null, "testuser", "test@duracloud.org", 14, "xyz");
        return ui;
    }

    @Test
    public void testGet() throws Exception {
        setupGet(TEST_ACCOUNT_ID);
        EasyMock.expect(accountService.getPendingInvitations())
                .andReturn(new HashSet<UserInvitation>(Arrays.asList(new UserInvitation[0])))
                .times(1);
        replayMocks();
        accountUsersController.get(TEST_ACCOUNT_ID, model);
        verifyGet(model);

    }

    private void verifyGet(Model model) {
        Assert.assertTrue(model.containsAttribute("account"));
        Assert.assertTrue(model.containsAttribute(AccountUsersController.USERS_KEY));
    }

    private void setupGet(Long accountId)
        throws AccountNotFoundException, DBNotFoundException {
        EasyMock.expect(accountService.retrieveAccountInfo())
                .andReturn(createAccountInfo())
                .times(1);

        EasyMock.expect(accountService.getUsers())
                .andReturn(createUserSet())
                .times(1);

        EasyMock.expect(accountManagerService.getAccount(accountId))
                .andReturn(accountService)
                .once();

        EasyMock.expect(userService.loadDuracloudUserByUsername(TEST_USERNAME))
                .andReturn(createUser())
                .anyTimes();
    }

    @Test
    public void testDeleteUserInvitation() throws Exception {
        EasyMock.expect(accountManagerService.getAccount(TEST_ACCOUNT_ID))
                .andReturn(accountService);
        this.accountService.deleteUserInvitation(1L);
        EasyMock.expectLastCall();
        replayMocks();

        Model model = new ExtendedModelMap();
        this.accountUsersController.deleteUserInvitation(TEST_ACCOUNT_ID, 1L, model);
    }

    @Test
    public void testDeleteUser() throws Exception {
        userService.revokeUserRights(TEST_ACCOUNT_ID, 1L);
        EasyMock.expectLastCall();
        EasyMock.expect(userService.loadDuracloudUserByUsername(TEST_USERNAME))
                .andReturn(createUser())
                .anyTimes();

        replayMocks();
        this.accountUsersController.deleteUserFromAccount(TEST_ACCOUNT_ID,
                                                          1L,
                                                          model);
    }

    @Test
    public void testEditUser() throws Exception {
        // set up mocks, and args
        Long acctId = 7L;
        Long userId = 9L;

        AccountUserEditForm acctUserEditForm = new AccountUserEditForm();
        acctUserEditForm.setRole(Role.ROLE_ADMIN.name());
        EasyMock.expect(result.hasErrors()).andReturn(false);
        EasyMock.expect(userService.setUserRights(EasyMock.eq(acctId),
                                                  EasyMock.eq(userId),
                                                  EasyMock.isA(Role.class),
                                                  EasyMock.isA(Role.class),
                                                  EasyMock.isA(Role.class))).andReturn(true);

        addFlashAttribute();
        replayMocks();

        // method under test
        accountUsersController.editUser(acctId,
                                        userId,
                                        acctUserEditForm,
                                        result,
                                        model,
                                        redirectAttributes);

    }

}
