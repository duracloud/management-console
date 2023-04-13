/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @contributor "Daniel Bernstein (dbernstein@duraspace.org)"
 */
@Controller
@Lazy
public class AccountController extends AbstractAccountController {

    public static final String NEW_ACCOUNT_FORM_KEY = "newAccountForm";

    @Autowired
    private AuthenticationManager authenticationManager;

    @RequestMapping(value = {ACCOUNT_PATH}, method = RequestMethod.GET)
    public String getHome(@PathVariable Long accountId, Model model)
        throws AccountNotFoundException {
        loadAccountInfo(accountId, model);
        return ACCOUNT_HOME;
    }

    @RequestMapping(value = {STATEMENT_PATH}, method = RequestMethod.GET)
    public String getStatement(@PathVariable Long accountId, Model model)
        throws AccountNotFoundException {
        loadAccountInfo(accountId, model);
        return "account-statement";
    }

    @RequestMapping(value = {ACCOUNT_PATH + "/activate"}, method = RequestMethod.POST)
    @Transactional
    public ModelAndView activate(@PathVariable Long accountId)
        throws AccountNotFoundException {
        AccountService accountService = accountManagerService.getAccount(
            accountId);
        accountService.storeAccountStatus(AccountInfo.AccountStatus.ACTIVE);

        String username =
            SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getName();
        return createUserRedirectModelAndView(username);
    }

    @RequestMapping(value = {ACCOUNT_PATH + "/deactivate"}, method = RequestMethod.POST)
    @Transactional
    public ModelAndView deactivate(@PathVariable Long accountId,
                                   Model model)
        throws AccountNotFoundException {
        AccountService accountService = accountManagerService.getAccount(accountId);
        accountService.storeAccountStatus(AccountInfo.AccountStatus.INACTIVE);

        String username =
            SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getName();
        return createUserRedirectModelAndView(username);
    }

    private ModelAndView createUserRedirectModelAndView(String username) {
        RedirectView view = UserController.formatUserRedirect(username);
        return new ModelAndView(view);
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(
        AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

}
