/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.util.DuracloudUserService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;

public class UserControllerTest extends AmaControllerTestBase {
    private UserController userController;

    @Before
    public void before() throws Exception {
        super.before();
        setupGenericAccountAndUserServiceMocks(TEST_ACCOUNT_ID);
        userController = new UserController();
        userController.setUserService(userService);
        userController.setAccountManagerService(accountManagerService);

    }

    @Test
    public void testGetNewForm() {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);

        EasyMock.expect(session.getAttribute("redemptionCode"))
                .andReturn(null)
                .anyTimes();
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();

        replayMocks();
        EasyMock.replay(request, session);

        ModelAndView mv = userController.getNewForm(request);

        Assert.assertEquals(UserController.NEW_USER_VIEW, mv.getViewName());

        Map<String, Object> map = mv.getModel();
        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey(UserController.NEW_USER_FORM_KEY));

        Object obj = map.get(UserController.NEW_USER_FORM_KEY);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof NewUserForm);

        EasyMock.verify(request, session);
    }

    @Test
    public void testGetForgotPasswordForm() {
        replayMocks();

        ModelAndView mv = userController.getForgotPasswordForm(null);

        Assert.assertEquals(UserController.FORGOT_PASSWORD_VIEW,
                            mv.getViewName());

        Map<String, Object> map = mv.getModel();
        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey(UserController.FORGOT_PASSWORD_FORM_KEY));

        Object obj = map.get(UserController.FORGOT_PASSWORD_FORM_KEY);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof ForgotPasswordForm);
    }

    @Test
    public void testGetChangePasswordForm() throws Exception {
        replayMocks();
        String view = userController.changePassword(TEST_USERNAME, model);

        Assert.assertEquals(UserController.CHANGE_PASSWORD_VIEW, view);

        Map<String, Object> map = model.asMap();
        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey(UserController.CHANGE_PASSWORD_FORM_KEY));
        Assert.assertTrue(map.containsKey(UserController.USER_KEY));

        Object obj = map.get(UserController.CHANGE_PASSWORD_FORM_KEY);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof ChangePasswordForm);
        obj = map.get(UserController.USER_KEY);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof DuracloudUser);
    }

    @Test
    public void testGetUser() throws Exception {

        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpSession session = createMock(HttpSession.class);

        EasyMock.expect(session.getAttribute("redemptionCode"))
                .andReturn(null)
                .anyTimes();
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();

        DuracloudUser u = createUser();
        EasyMock.expect(userService.loadDuracloudUserByUsernameInternal(TEST_USERNAME))
                .andReturn(u)
                .anyTimes();

        Set<AccountInfo> accounts = createAccountSet();
        EasyMock.expect(accountManagerService.findAccountsByUserId(u.getId()))
                .andReturn(accounts);

        AmaEndpoint endpoint = EasyMock.createMock(AmaEndpoint.class);
        EasyMock.expect(endpoint.getDomain()).andReturn("test");
        userController.setAmaEndpoint(endpoint);
        replayMocks();

        ModelAndView mv = userController.getUser(TEST_USERNAME, request);
        Assert.assertEquals(UserController.USER_ACCOUNTS, mv.getViewName());

        Map<String, Object> map = mv.getModel();
        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey(UserController.USER_KEY));

        Object obj = map.get(UserController.USER_KEY);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof DuracloudUser);

        obj = map.get("activeAccounts");
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof List);

        obj = map.get("pendingAccounts");
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof List);

        obj = map.get("inactiveAccounts");
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof List);
    }

    private Set<AccountInfo> createAccountSet() {
        Set<AccountInfo> s = new HashSet<AccountInfo>();
        s.add(createAccountInfo());
        return s;
    }

    @Test
    public void testUpdateUser() throws Exception {
        initializeMockUserServiceStoreUser();
        UserProfileEditForm editForm = createMock(UserProfileEditForm.class);
        setupNoBindingResultErrors();
        EasyMock.expect(editForm.getFirstName()).andReturn("").anyTimes();
        EasyMock.expect(editForm.getLastName()).andReturn("").anyTimes();
        EasyMock.expect(editForm.getEmail()).andReturn("").anyTimes();
        EasyMock.expect(editForm.getAllowableIPAddressRange()).andReturn("").anyTimes();

        EasyMock.expect(editForm.getSecurityQuestion())
                .andReturn("")
                .anyTimes();
        EasyMock.expect(editForm.getSecurityAnswer()).andReturn("").anyTimes();
        replayMocks();
        ModelAndView mav = userController.update(TEST_USERNAME,
                                                 editForm,
                                                 result,
                                                 null);

        Assert.assertNotNull(mav);
    }

    @Test
    public void testUpdateUserErrors() throws Exception {
        setupHasBindingResultErrors(true);
        replayMocks();
        ModelAndView mav = userController.update(TEST_USERNAME,
                                                 null,
                                                 result,
                                                 new ExtendedModelMap());

        Assert.assertNotNull(mav);
        Assert.assertEquals(UserController.USER_EDIT_VIEW, mav.getViewName());
    }

    @Test
    public void testAddUser() throws Exception {
        EasyMock.expect(userService.createNewUser(EasyMock.isA(String.class),
                                                  EasyMock.isA(String.class),
                                                  EasyMock.isA(String.class),
                                                  EasyMock.isA(String.class),
                                                  EasyMock.isA(String.class),
                                                  EasyMock.isA(String.class),
                                                  EasyMock.isA(String.class)))
                .andReturn(createUser())
                .anyTimes();

        NewUserForm newUserForm = createMock(NewUserForm.class);
        setupNoBindingResultErrors();
        EasyMock.expect(newUserForm.getUsername())
                .andReturn(TEST_USERNAME)
                .anyTimes();
        EasyMock.expect(newUserForm.getPassword()).andReturn("").anyTimes();
        EasyMock.expect(newUserForm.getFirstName()).andReturn("").anyTimes();
        EasyMock.expect(newUserForm.getLastName()).andReturn("").anyTimes();
        EasyMock.expect(newUserForm.getEmail()).andReturn("").anyTimes();
        EasyMock.expect(newUserForm.getSecurityQuestion())
                .andReturn("")
                .anyTimes();
        EasyMock.expect(newUserForm.getSecurityAnswer())
                .andReturn("")
                .anyTimes();
        EasyMock.expect(newUserForm.getRedemptionCode())
                .andReturn("")
                .anyTimes();

        replayMocks();

        ModelAndView mav = userController.add(newUserForm, result, null, null);
        Assert.assertNotNull(mav);
    }

    @Test
    public void testAddUserErrors() throws Exception {
        setupHasBindingResultErrors(true);
        replayMocks();
        ModelAndView mav = userController.add(null, result, model, null);

        Assert.assertNotNull(mav);
        Assert.assertEquals(UserController.NEW_USER_VIEW, mav.getViewName());
    }

    @Test
    public void testForgotPassword() throws Exception {
        initializeForgotPasswordMockUserService();
        ForgotPasswordForm forgotPasswordForm = createMock(ForgotPasswordForm.class);
        setupNoBindingResultErrors();
        EasyMock.expect(forgotPasswordForm.getUsername())
                .andReturn(TEST_USERNAME)
                .anyTimes();
        EasyMock.expect(forgotPasswordForm.getSecurityQuestion())
                .andReturn("question")
                .anyTimes();

        forgotPasswordForm.setSecurityQuestion(EasyMock.isA(String.class));

        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(forgotPasswordForm.getSecurityAnswer())
                .andReturn("answer")
                .anyTimes();

        replayMocks();
        String view = userController.forgotPassword(forgotPasswordForm,
                                                    result,
                                                    model,
                                                    null);

        Assert.assertNotNull(view);
        Assert.assertEquals(UserController.FORGOT_PASSWORD_SUCCESS_VIEW, view);

    }

    @Test
    public void testChangePassword() throws Exception {
        initializeChangePasswordMockUserService();
        ChangePasswordForm cbangePasswordForm = createMock(ChangePasswordForm.class);
        setupNoBindingResultErrors();
        EasyMock.expect(cbangePasswordForm.getOldPassword())
                .andReturn("")
                .anyTimes();
        EasyMock.expect(cbangePasswordForm.getPassword())
                .andReturn("")
                .anyTimes();

        replayMocks();

        ModelAndView mav = userController.changePassword(TEST_USERNAME,
                                                         cbangePasswordForm,
                                                         result,
                                                         null);
        Assert.assertNotNull(mav);
    }

    @Test
    public void testAnonymousChangePassword() throws Exception {
        ChangePasswordForm cbangePasswordForm = createMock(ChangePasswordForm.class);
        setupNoBindingResultErrors();
        EasyMock.expect(cbangePasswordForm.getOldPassword())
                .andReturn("")
                .anyTimes();
        EasyMock.expect(cbangePasswordForm.getPassword())
                .andReturn("")
                .anyTimes();

        userService = EasyMock.createMock(DuracloudUserService.class);
        EasyMock.expect(userService.loadDuracloudUserByUsernameInternal(EasyMock.isA(String.class)))
                .andReturn(createUser())
                .anyTimes();
        userService.changePasswordInternal(EasyMock.anyLong(),
                                           EasyMock.isA(String.class),
                                           EasyMock.anyBoolean(),
                                           EasyMock.isA(String.class));
        EasyMock.expectLastCall();

        userService.redeemPasswordChangeRequest(EasyMock.anyLong(), EasyMock.isA(String.class));
        EasyMock.expectLastCall();

        EasyMock.expect(userService.retrievePassordChangeInvitation(EasyMock.isA(String.class)))
                .andReturn(new UserInvitation(1L, createAccountInfo(-1L), "n/a", "n/a", "n/a",
                                              "n/a", "username", "email", 1, "aaa"));

        AmaEndpoint endpoint = EasyMock.createMock(AmaEndpoint.class);
        EasyMock.expect(endpoint.getUrl()).andReturn("test");
        EasyMock.replay(userService);
        userController.setUserService(userService);

        userController.setAmaEndpoint(endpoint);
        replayMocks();

        String view = userController.anonymousPasswordChange("ABC",
                                                             cbangePasswordForm,
                                                             result,
                                                             model);
        Assert.assertNotNull(view);
    }

    @Test
    public void testForgotPasswordErrors() throws Exception {
        setupHasBindingResultErrors(true);
        ForgotPasswordForm form = new ForgotPasswordForm();
        form.setUsername(TEST_USERNAME);
        replayMocks();
        String view = userController.forgotPassword(form,
                                                    result,
                                                    model,
                                                    null);

        Assert.assertNotNull(view);
        Assert.assertEquals(UserController.FORGOT_PASSWORD_VIEW, view);
    }

    @Test
    public void testChangePasswordErrors() throws Exception {
        setupHasBindingResultErrors(true);
        replayMocks();
        ModelAndView mav = userController.changePassword(TEST_USERNAME,
                                                         null,
                                                         result,
                                                         model);

        Assert.assertNotNull(mav);
        Assert.assertEquals(UserController.USER_EDIT_VIEW, mav.getViewName());

        Map<String, Object> map = model.asMap();
        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey(UserController.USER_KEY));

        Object obj = map.get(UserController.USER_KEY);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof DuracloudUser);
    }

    private void initializeMockUserServiceStoreUser() throws Exception {
        userService.storeUserDetails(EasyMock.anyLong(),
                                     EasyMock.isA(String.class),
                                     EasyMock.isA(String.class),
                                     EasyMock.isA(String.class),
                                     EasyMock.isA(String.class),
                                     EasyMock.isA(String.class),
                                     EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();
    }

    private void initializeForgotPasswordMockUserService() throws Exception {

        userService.forgotPassword(EasyMock.eq(TEST_USERNAME),
                                   EasyMock.isA(String.class),
                                   EasyMock.isA(String.class));

        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(userService.loadDuracloudUserByUsernameInternal(TEST_USERNAME))
                .andReturn(createUser())
                .anyTimes();
    }

    private void initializeChangePasswordMockUserService() throws Exception {
        userService = EasyMock.createMock(DuracloudUserService.class);
        EasyMock.expect(userService.loadDuracloudUserByUsername(TEST_USERNAME))
                .andReturn(createUser())
                .anyTimes();
        userService.changePassword(EasyMock.anyLong(),
                                   EasyMock.isA(String.class),
                                   EasyMock.anyBoolean(),
                                   EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(userService);
        userController.setUserService(userService);
    }

    protected void setupAccountManagerService() throws AccountNotFoundException {
        Set set = EasyMock.createMock(Set.class);
        Iterator iterator = EasyMock.createMock(Iterator.class);

        EasyMock.expect(iterator.hasNext()).andReturn(false).anyTimes();
        EasyMock.expect(set.iterator()).andReturn(iterator).anyTimes();

        EasyMock.expect(accountManagerService.findAccountsByUserId(EasyMock.anyLong()))
                .andReturn(set)
                .anyTimes();
        EasyMock.replay(accountManagerService, set, iterator);
    }
}
