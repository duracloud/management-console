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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;

import org.duracloud.account.app.model.Account;
import org.duracloud.account.app.model.User;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.util.DuracloudUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * This controller adds support for managing root users.
 *
 * @Author Nicholas Woodward
 */
@Controller
@Lazy
@RequestMapping(RootUsersController.BASE_MAPPING)
public class RootUsersController extends AbstractRootController {
    public static final String BASE_MAPPING = RootConsoleHomeController.BASE_MAPPING + "/rootusers";

    private static final String BASE_VIEW = BASE_MAPPING;

    private static final String USERNAME_FORM_KEY = "usernameForm";

    public static final String EDIT_ACCOUNT_ROOT_USERS_FORM_KEY = "accountRootUsersEditForm";

    @Autowired
    private DuracloudUserService userService;

    @ModelAttribute(USERNAME_FORM_KEY)
    public UsernameForm usernameForm() {
        return new UsernameForm();
    }

    @RequestMapping("")
    public ModelAndView get() {
        List<User> u = new ArrayList<User>();
        Set<DuracloudUser> root_users = getRootAccountManagerService().listAllRootUsers(null);
        for (DuracloudUser root_user : root_users) {
            Set<Account> accounts = new HashSet<Account>();
            u.add(new User(root_user.getId(),
                    root_user.getUsername(),
                    root_user.getFirstName(),
                    root_user.getLastName(),
                    root_user.getEmail(),
                    root_user.getAllowableIPAddressRange(),
                    accounts,
                    root_user.isRoot()));
        }

        Collections.sort(u);

        ModelAndView mav = new ModelAndView(BASE_VIEW);
        mav.addObject("rootusers", u);
        return mav;

    }

    @Transactional
    @RequestMapping(value = {"/set-root"}, method = RequestMethod.POST)
    public ModelAndView setRootUser(@ModelAttribute(USERNAME_FORM_KEY) @Valid UsernameForm usernameForm,
                                    BindingResult result, RedirectAttributes redirectAttributes)
            throws Exception {
        String username = usernameForm.getUsername();
        log.debug("set root on user {}.", username);
        if (result.hasErrors()) {
            return new ModelAndView(BASE_VIEW);
        }

        DuracloudUser user = userService.loadDuracloudUserByUsernameInternal(username);
        getRootAccountManagerService().setRootUser(user.getId());
        if (user.isRoot()) {
            log.info("user {} is now root", new Object[] {username});

            String message = MessageFormat.format("Successfully set root on user {0}.", username);
            setSuccessFeedback(message, redirectAttributes);
        }

        return createRedirectMav(BASE_VIEW);
    }

    @Transactional
    @RequestMapping(value = {BY_ID_MAPPING + "/unset-root"}, method = RequestMethod.POST)
    public ModelAndView unsetRootUser(@PathVariable Long id, RedirectAttributes redirectAttributes)
            throws Exception {
        log.debug("unset root on user {}.", id);
        getRootAccountManagerService().unsetRootUser(id);
        DuracloudUser user = getUserService().loadDuracloudUserByIdInternal(id);
        String message = MessageFormat.format("{0} is no longer root.", user.getUsername());
        setSuccessFeedback(message, redirectAttributes);
        return createRedirectMav(BASE_VIEW);
    }

    protected DuracloudUserService getUserService() {
        return userService;
    }

    public void setUserService(DuracloudUserService userService) {
        this.userService = userService;
    }

}