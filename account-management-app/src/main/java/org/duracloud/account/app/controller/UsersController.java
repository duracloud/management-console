/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.duracloud.account.app.model.Account;
import org.duracloud.account.app.model.User;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.AccountRights;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.util.DuracloudUserService;
import org.duracloud.common.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Daniel Bernstein
 * Date: Feb 17, 2012
 */
@Controller
@RequestMapping(UsersController.BASE_MAPPING)
public class UsersController extends AbstractRootController {
    public static final String BASE_MAPPING = RootConsoleHomeController.BASE_MAPPING + "/users";
    private static final String BASE_VIEW = BASE_MAPPING;
    private static final String CREATE_LIST_VIEW = "create-list";
    public static final String EDIT_ACCOUNT_USERS_FORM_KEY = "accountUsersEditForm";

    @Autowired
    private DuracloudUserService userService;

    @RequestMapping("")
    public ModelAndView get() {
        List<User> u = new ArrayList<User>();
        Set<DuracloudUser> users = getRootAccountManagerService().listAllUsers(null);
        for (DuracloudUser user : users) {
            Set<Account> accounts = new HashSet<Account>();
            if (user.getAccountRights() != null) {
                for (AccountRights account : user.getAccountRights()) {

                    AccountInfo accountInfo = account.getAccount();

                    accounts.add(new Account(account.getAccount().getId(),
                                             accountInfo.getAcctName(),
                                             accountInfo.getSubdomain(),
                                             user.getRoleByAcct(account.getAccount().getId())));
                }
            }

            u.add(new User(user.getId(),
                           user.getUsername(),
                           user.getFirstName(),
                           user.getLastName(),
                           user.getEmail(),
                           user.getAllowableIPAddressRange(),
                           accounts,
                           user.isRoot()));
        }

        Collections.sort(u);

        ModelAndView mav = new ModelAndView(BASE_VIEW);
        mav.addObject("users", u);
        mav.addObject(EDIT_ACCOUNT_USERS_FORM_KEY, new AccountUserEditForm());

        return mav;
    }

    @Transactional
    @RequestMapping(value = {BY_ID_MAPPING + "/reset"}, method = RequestMethod.POST)
    public ModelAndView resetUsersPassword(
        @PathVariable Long id, RedirectAttributes redirectAttributes)
        throws Exception {
        log.debug("resetting user {}'s password.", id);
        getRootAccountManagerService().resetUsersPassword(id);
        DuracloudUser user = getUserService().loadDuracloudUserByIdInternal(id);
        String message = MessageFormat.format("{0}'s password has been reset.", user.getUsername());
        setSuccessFeedback(message, redirectAttributes);
        return createRedirectMav(BASE_VIEW);
    }

    @Transactional
    @RequestMapping(value = BY_ID_DELETE_MAPPING, method = RequestMethod.POST)
    public ModelAndView deleteUser(
        @PathVariable Long id, RedirectAttributes redirectAttributes)
        throws Exception {
        log.info("delete user {}", id);

        //delete user
        getRootAccountManagerService().deleteUser(id);
        setSuccessFeedback("Successfully deleted user.", redirectAttributes);
        return createRedirectMav(BASE_VIEW);
    }

    @Transactional
    @RequestMapping(value = BY_ID_MAPPING + "/revoke", method = RequestMethod.POST)
    public ModelAndView revokeUserRightsFromAccount(@PathVariable("id") Long userId,
                                                    @RequestParam(required = true) Long accountId,
                                                    RedirectAttributes redirectAttributes)
        throws Exception {

        log.info("revoking user {}'s rights from account {}", userId, accountId);
        String username = getUserService().loadDuracloudUserByIdInternal(userId).getUsername();
        String accountName = getAccountManagerService().getAccount(accountId)
                                                       .retrieveAccountInfo()
                                                       .getAcctName();

        getUserService().revokeUserRights(accountId, userId);
        String message = MessageFormat.format("Removed {0} from {1}", username, accountName);
        setSuccessFeedback(message, redirectAttributes);
        return createRedirectMav(BASE_MAPPING);
    }

    @Transactional
    @RequestMapping(value = BY_ID_MAPPING + "/changerole", method = RequestMethod.POST)
    public ModelAndView changeUserRole(@PathVariable("id") Long userId,
                                       @ModelAttribute @Valid AccountUserEditForm accountUserEditForm,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes) throws Exception {
        Long accountId = accountUserEditForm.getAccountId();
        log.debug("editUser account {}", accountId);

        boolean hasErrors = result.hasErrors();
        if (!hasErrors) {
            Role role = Role.valueOf(accountUserEditForm.getRole());
            log.info("New role: {}", role);
            setUserRights(userService, accountId, userId, role);
            setSuccessFeedback("Successfully changed user role.", redirectAttributes);
        } else {
            setFailureFeedback("Unable to change the user role.", redirectAttributes);
        }

        return createRedirectMav(BASE_VIEW);
    }

    @RequestMapping(value = CREATE_LIST_VIEW, method = RequestMethod.GET)
    public String createList(HttpServletResponse response) throws Exception {
        log.info("Creating DuraCloud users list.");
        String csvHeaders = "First Name,Last Name,Username,Email,Accounts & Roles\n";

        StringBuffer contentDisposition = new StringBuffer();
        contentDisposition.append("attachment;");
        SimpleDateFormat dateFormat =
            new SimpleDateFormat(DateUtil.DateFormat.PLAIN_FORMAT.getPattern());
        contentDisposition.append("filename=\"duracloud-users-" + dateFormat.format(new Date()) + ".csv\"");
        response.setHeader("Content-Disposition", contentDisposition.toString());

        List<String[]> users = this.buildUsersList();
        log.info("Found {} users.", users.size());

        try {
            response.getOutputStream().print(csvHeaders);
            ListIterator<String[]> itr = users.listIterator();
            while (itr.hasNext()) {
                String user = StringUtils.join(itr.next(), ",");
                response.getOutputStream().print(user + "\n");
            }
        } catch (IOException ex) {
            log.error("Error occurred while downloading DuraCloud users list: " + ex.getMessage(), ex);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    private List<String[]> buildUsersList() {
        List<String[]> userDataLines = new ArrayList<>();
        List<String> userData;

        // Get all DuraCloud users
        Set<DuracloudUser> users = getRootAccountManagerService().listAllUsers(null);
        for (DuracloudUser user : users) {
            userData = new ArrayList<String>();

            // Add user metadata
            userData.add(user.getFirstName());
            userData.add(user.getLastName());
            userData.add(user.getUsername());
            userData.add(user.getEmail());

            // Create list of account:role pairs
            List<String> accountRolesList = new ArrayList<String>();
            String accountRoles = "";

            if (user.getAccountRights() != null) {
                for (AccountRights account : user.getAccountRights()) {
                    AccountInfo accountInfo = account.getAccount();
                    accountRolesList.add(accountInfo.getAcctName() + ":" +
                                         user.getRoleByAcct(account.getAccount().getId()));
                }
                String[] accountRolesArray = accountRolesList.toArray(new String[0]);
                accountRoles = StringUtils.join(accountRolesArray, " || ");
            }
            userData.add(accountRoles);

            String[] userDataArray = userData.toArray(new String[0]);
            userDataLines.add(userDataArray);
        }

        return userDataLines;
    }

    protected DuracloudUserService getUserService() {
        return userService;
    }

    public void setUserService(DuracloudUserService userService) {
        this.userService = userService;
    }
}
