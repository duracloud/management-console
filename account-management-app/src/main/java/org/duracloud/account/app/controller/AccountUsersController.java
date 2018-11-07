/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.validation.Valid;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.model.util.DuracloudAccount;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.duracloud.account.db.util.error.UnsentEmailException;
import org.duracloud.account.util.EmailAddressesParser;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @contributor "Daniel Bernstein (dbernstein@duraspace.org)"
 */
@Controller
@Lazy
public class AccountUsersController extends AbstractAccountController {
    public static final String ACCOUNT_USERS_VIEW_ID = "account-users";
    public static final String ACCOUNT_USERS_EDIT_ID = "account-users-edit";

    public static final String EDIT_ACCOUNT_USERS_FORM_KEY = "accountUsersEditForm";
    private static final String USERNAME_FORM_KEY = "usernameForm";
    private static final String INVITATION_FORM_KEY = "invitationForm";

    public static final String ACCOUNT_USERS_PATH = "/users";
    public static final String ACCOUNT_USERS_MAPPING =
        ACCOUNT_PATH + ACCOUNT_USERS_PATH;
    public static final String USERS_INVITATIONS_DELETE_MAPPING =
        ACCOUNT_USERS_MAPPING + "/invitations/byid/{invitationId}/delete";
    public static final String USERS_DELETE_MAPPING =
        ACCOUNT_USERS_MAPPING + "/byid/{userId}/delete";
    public static final String USERS_EDIT_MAPPING =
        ACCOUNT_USERS_MAPPING + "/byid/{userId}/edit";

    public static final String USERS_KEY = "users";

    @ModelAttribute(INVITATION_FORM_KEY)
    public InvitationForm invitationForm() {
        return new InvitationForm();
    }

    @ModelAttribute(USERNAME_FORM_KEY)
    public UsernameForm usernameForm() {
        return new UsernameForm();
    }

    @ModelAttribute(EDIT_ACCOUNT_USERS_FORM_KEY)
    public AccountUserEditForm accountUserEditForm() {
        return new AccountUserEditForm();
    }

    /**
     * @param accountId
     * @param model
     * @return
     * @throws AccountNotFoundException
     */
    @RequestMapping(value = ACCOUNT_USERS_MAPPING, method = RequestMethod.GET)
    public String get(@PathVariable Long accountId, Model model)
        throws Exception {
        addUserToModel(model);
        return get(getAccountService(accountId), model);
    }

    @Transactional
    @RequestMapping(value = ACCOUNT_USERS_MAPPING + "/adduser", method = RequestMethod.POST)
    public ModelAndView addUser(@PathVariable Long accountId,
                                @ModelAttribute(USERNAME_FORM_KEY) @Valid UsernameForm usernameForm,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) throws Exception {
        String username = usernameForm.getUsername();
        log.debug("entering addUser: adding {} to account {}",
                  username,
                  accountId);
        if (result.hasErrors()) {
            addUserToModel(model);
            get(getAccountService(accountId), model);
            return new ModelAndView(ACCOUNT_USERS_VIEW_ID, model.asMap());
        }

        DuracloudUser user = userService.loadDuracloudUserByUsernameInternal(username);
        if (userService.addUserToAccount(accountId, user.getId())) {
            log.info("added user {} to account {}", new Object[] {username, accountId});

            String message = MessageFormat.format("Successfully added {0} to account.", username);
            setSuccessFeedback(message, redirectAttributes);
        }

        return createAccountRedirectModelAndView(accountId, ACCOUNT_USERS_PATH);
    }

    protected String get(AccountService accountService, Model model)
        throws Exception {
        loadAccountUsers(accountService, model);
        return ACCOUNT_USERS_VIEW_ID;
    }

    @Transactional
    @RequestMapping(value = ACCOUNT_USERS_MAPPING, method = RequestMethod.POST)
    public ModelAndView sendInvitations(@PathVariable Long accountId,
                                        @ModelAttribute(INVITATION_FORM_KEY) @Valid InvitationForm invitationForm,
                                        BindingResult result,
                                        Model model,
                                        RedirectAttributes redirectAttributes) throws Exception {
        log.info("sending invitations from account {}", accountId);
        boolean hasErrors = result.hasErrors();
        AccountService service = getAccountService(accountId);

        if (!hasErrors) {

            List<String> emailAddresses =
                EmailAddressesParser.parse(invitationForm.getEmailAddresses());

            List<String> failedEmailAddresses = new ArrayList<String>();
            String adminUsername = getUser().getUsername();

            for (String emailAddress : emailAddresses) {
                try {
                    UserInvitation ui =
                        service.inviteUser(emailAddress,
                                           adminUsername);
                    String template = "Successfully created user invitation on "
                                      + "account {0} for {1} expiring on {2}";
                    String message =
                        MessageFormat.format(template,
                                             ui.getAccount().getId(),
                                             ui.getUserEmail(),
                                             ui.getExpirationDate());
                    log.info(message);
                } catch (UnsentEmailException e) {
                    failedEmailAddresses.add(emailAddress);
                }
            }

            if (!failedEmailAddresses.isEmpty()) {
                String template = "Unable to send an email to the following recipients, "
                                  + "but the user has been added: {0}";
                String message = MessageFormat.format(template, failedEmailAddresses);

                result.addError(new ObjectError("emailAddresses", message));
                hasErrors = true;
            }
        }

        if (hasErrors) {
            addUserToModel(model);
            get(service, model);
            return new ModelAndView(ACCOUNT_USERS_VIEW_ID);
        }

        setSuccessFeedback("Successfully sent invitations.", redirectAttributes);
        return createAccountRedirectModelAndView(accountId, ACCOUNT_USERS_PATH);
    }

    @Transactional
    @RequestMapping(value = USERS_INVITATIONS_DELETE_MAPPING, method = RequestMethod.POST)
    public ModelAndView deleteUserInvitation(@PathVariable Long accountId,
                                             @PathVariable Long invitationId,
                                             Model model)
        throws Exception {
        log.info("remove invitation {} from account {}", invitationId, accountId);
        AccountService service = getAccountService(accountId);
        service.deleteUserInvitation(invitationId);

        return createAccountRedirectModelAndView(accountId, ACCOUNT_USERS_PATH);
    }

    @Transactional
    @RequestMapping(value = USERS_DELETE_MAPPING, method = RequestMethod.POST)
    public ModelAndView deleteUserFromAccount(@PathVariable Long accountId,
                                              @PathVariable Long userId,
                                              Model model)
        throws Exception {
        log.info("delete user {} from account {}", userId, accountId);
        userService.revokeUserRights(accountId, userId);
        DuracloudUser user = getUser();

        if (user.getId() == userId) {
            View redirect = UserController.formatUserRedirect(user.getUsername());
            return new ModelAndView(redirect);
        }
        return createAccountRedirectModelAndView(accountId, ACCOUNT_USERS_PATH);
    }

    @RequestMapping(value = USERS_EDIT_MAPPING, method = RequestMethod.GET)
    public String getEditUserForm(@PathVariable Long accountId,
                                  @PathVariable Long userId,
                                  Model model)
        throws Exception {
        log.info("getEditUserForm user {} account {}", userId, accountId);
        AccountService accountService = getAccountService(accountId);
        Set<DuracloudUser> users = accountService.getUsers();

        for (DuracloudUser u : users) {
            if (u.getId() == userId) {
                AccountUser au = new AccountUser(u.getId(),
                                                 u.getUsername(),
                                                 u.getFirstName(),
                                                 u.getLastName(),
                                                 u.getEmail(),
                                                 InvitationStatus.ACTIVE,
                                                 u.getRoleByAcct(accountId),
                                                 u.getAllowableIPAddressRange(),
                                                 false);
                //TODO set current role for select box - based on hierarchy?
                //editForm.setRole();
                model.addAttribute("user", au);
                break;
            }
        }

        loadAccountInfo(accountId, model);
        return ACCOUNT_USERS_EDIT_ID;
    }

    @Transactional
    @RequestMapping(value = USERS_EDIT_MAPPING, method = RequestMethod.POST)
    public ModelAndView editUser(@PathVariable Long accountId,
                                 @PathVariable Long userId,
                                 @ModelAttribute(EDIT_ACCOUNT_USERS_FORM_KEY) AccountUserEditForm accountUserEditForm,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) throws Exception {
        log.debug("editUser account {}", accountId);

        boolean hasErrors = result.hasErrors();
        if (!hasErrors) {
            Role role = Role.valueOf(accountUserEditForm.getRole());
            log.info("New role: {}", role);
            try {
                setUserRights(userService, accountId, userId, role);
                setSuccessFeedback("Successfully changed user role.", redirectAttributes);
            } catch (AccessDeniedException e) {
                result.addError(new ObjectError("role",
                                                "You are unauthorized to set the role for this user"));
                hasErrors = true;
            }
        }

        if (hasErrors) {
            addUserToModel(model);
            return new ModelAndView(ACCOUNT_USERS_VIEW_ID, model.asMap());
        }

        return createAccountRedirectModelAndView(accountId,
                                                 ACCOUNT_USERS_PATH);
    }

    /**
     * @param accountId
     * @return
     */
    private AccountService getAccountService(Long accountId)
        throws AccountNotFoundException {
        return this.accountManagerService.getAccount(accountId);
    }

    /**
     * @param accountService
     * @param model
     */
    private void loadAccountUsers(AccountService accountService, Model model)
        throws Exception {
        AccountInfo accountInfo = accountService.retrieveAccountInfo();
        model.addAttribute(ACCOUNT_INFO_KEY, accountInfo);
        Set<DuracloudUser> users = accountService.getUsers();
        Set<UserInvitation> pendingUserInvitations = accountService.getPendingInvitations();
        DuracloudUser caller = getUser();
        DuracloudAccount duracloudAccount = new DuracloudAccount();
        duracloudAccount.setAccountInfo(accountInfo);
        duracloudAccount.setUserRole(caller.getRoleByAcct(accountInfo.getId()));
        model.addAttribute("account", duracloudAccount);

        List<AccountUser> accountUsers =
            buildUserList(accountInfo.getId(), users, caller);
        Collections.sort(accountUsers);
        addInvitationsToModel(pendingUserInvitations, accountService, model);
        model.addAttribute(USERS_KEY, accountUsers);

    }

    /**
     * @param model
     * @param pendingUserInvitations
     * @param accountService
     */
    private void addInvitationsToModel(Set<UserInvitation> pendingUserInvitations,
                                       AccountService accountService,
                                       Model model)
        throws Exception {
        Set<PendingAccountUser> pendingUsers = new TreeSet<PendingAccountUser>();
        for (UserInvitation ui : pendingUserInvitations) {
            pendingUsers.add(new PendingAccountUser(ui, Role.ROLE_USER));
        }
        model.addAttribute("pendingUserInvitations", pendingUsers);
    }

    /**
     * @param accountId
     * @return
     */
    private List<AccountUser> buildUserList(
        Long accountId, Set<DuracloudUser> users, DuracloudUser caller) {
        List<AccountUser> list = new LinkedList<AccountUser>();
        for (DuracloudUser u : users) {
            Role role = u.getRoleByAcct(accountId);
            AccountUser au =
                new AccountUser(u.getId(),
                                u.getUsername(),
                                u.getFirstName(),
                                u.getLastName(),
                                u.getEmail(),
                                InvitationStatus.ACTIVE,
                                role,
                                u.getAllowableIPAddressRange(),
                                caller.isRoot()
                                || caller.isOwnerForAcct(accountId)
                                || (caller.isAdminForAcct(accountId)
                                    && (role.equals(Role.ROLE_USER)
                                        || role.equals(Role.ROLE_ADMIN)))
                );
            list.add(au);
        }

        return list;
    }

    /**
     * This class is a read only representation of a user from the perspective
     * of an account admin/owner.
     *
     * @contributor "Daniel Bernstein (dbernstein@duraspace.org)"
     */
    public class AccountUser implements Comparable<AccountUser> {

        public AccountUser(
            Long id, String username, String firstName, String lastName,
            String email, InvitationStatus status, Role role, String allowableIPAddressRange, boolean editable) {
            super();
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.status = status;
            this.role = role;
            this.editable = editable;
            this.allowableIPAddressRange = allowableIPAddressRange;
        }

        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private InvitationStatus status;
        private Role role;
        private boolean editable;
        private String allowableIPAddressRange;

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public InvitationStatus getStatus() {
            return status;
        }

        public Role getRole() {
            return role;
        }

        public boolean isEditable() {
            return this.editable;
        }

        @Override
        public int compareTo(AccountUser o) {
            return this.getUsername().compareTo(o.getUsername());
        }

        public String getAllowableIPAddressRange() {
            return allowableIPAddressRange;
        }

        public void setAllowableIPAddressRange(String allowableIPAddressRange) {
            this.allowableIPAddressRange = allowableIPAddressRange;
        }
    }

    public static enum InvitationStatus {
        ACTIVE, PENDING, EXPIRED
    }

    public class PendingAccountUser implements Comparable<PendingAccountUser> {

        private UserInvitation invitation;
        private Role role;

        public PendingAccountUser(UserInvitation ui, Role role) {
            this.invitation = ui;
            this.role = role;
        }

        public Long getInvitationId() {
            return this.invitation.getId();
        }

        public String getRedemptionCode() {
            return this.invitation.getRedemptionCode();
        }

        public String getEmail() {
            return this.invitation.getUserEmail();
        }

        public String getExpirationDate() {
            return this.invitation.getExpirationDate().toString();
        }

        @Override
        public int compareTo(PendingAccountUser o) {
            return this.getEmail().compareTo(o.getEmail());
        }

        public Role getRole() {
            return role;
        }
    }
}
