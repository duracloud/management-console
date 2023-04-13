/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.util.AccountManagerService;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.DuracloudUserService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.duracloud.account.db.util.error.DBNotFoundException;
import org.duracloud.account.util.StorageProviderTypeUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * The default view for this application
 *
 * @contributor dbernstein
 */
@Lazy
@RequestMapping(AbstractAccountController.ACCOUNTS_PATH)
public abstract class AbstractAccountController extends AbstractController {
    public static final String ACCOUNTS_PATH = "/accounts";
    public static final String ACCOUNT_PATH = "/byid/{accountId}";
    public static final String EDIT_PATH = "/edit";
    public static final String STATEMENT_PATH = ACCOUNT_PATH + "/statement";
    public static final String ACCOUNT_INFO_KEY = "accountInfo";
    public static final String DC_VERSIONS_KEY = "dcVersions";
    public static final String ACTION_STATUS = "actionStatus";

    @Autowired(required = true)
    protected AccountManagerService accountManagerService;

    @Autowired(required = true)
    protected DuracloudUserService userService;
    public static final String NEW_ACCOUNT_VIEW = "account-new";
    public static final String ACCOUNT_HOME = "account-home";

    public AccountManagerService getAccountManagerService() {
        return accountManagerService;
    }

    public void setAccountManagerService(
        AccountManagerService accountManagerService) {
        this.accountManagerService = accountManagerService;
    }

    public DuracloudUserService getUserService() {
        return userService;
    }

    public void setUserService(DuracloudUserService userService) {
        this.userService = userService;
    }

    protected void addAccountInfoToModel(AccountInfo info, Model model) {
        model.addAttribute(ACCOUNT_INFO_KEY, info);
    }

    protected void addAccountOwnersToModel(List<DuracloudUser> owners, Model model)
        throws AccountNotFoundException {
        model.addAttribute("accountOwners", owners);
    }

    /**
     * @param accountId
     * @param model
     * @throws AccountNotFoundException
     */
    protected AccountInfo loadAccountInfo(Long accountId, Model model)
        throws AccountNotFoundException {
        AccountService accountService =
            accountManagerService.getAccount(accountId);
        return loadAccountInfo(accountService, model);
    }

    protected AccountInfo loadAccountInfo(AccountService accountService,
                                          Model model) {
        AccountInfo accountInfo = accountService.retrieveAccountInfo();
        addAccountInfoToModel(accountInfo, model);
        return accountInfo;
    }

    /**
     * @param accountId
     * @param suffix
     * @return
     */
    protected ModelAndView createAccountRedirectModelAndView(Long accountId, String suffix) {
        return new ModelAndView(createAccountRedirectView(accountId, suffix));
    }

    /**
     * @param accountId
     * @param suffix
     * @return
     */
    protected View createAccountRedirectView(Long accountId, String suffix) {
        String url = MessageFormat.format("{0}{1}{2}", ACCOUNTS_PATH,
                                          ACCOUNT_PATH.replace("{accountId}", String.valueOf(accountId)),
                                          suffix);

        RedirectView redirectView = new RedirectView(url, true);
        redirectView.setExposeModelAttributes(false);

        return redirectView;
    }

    protected void loadBillingInfo(Long accountId, Model model) {
        //TODO LoadBillingInfo
        //model.addAttribute("billingInfo", new CreditCardPaymentInfo());
    }

    protected void loadProviderInfo(Long accountId, Model model)
        throws AccountNotFoundException {
        AccountService accountService =
            accountManagerService.getAccount(accountId);

        StorageProviderAccount primarySP =
            accountService.getPrimaryStorageProvider();
        model.addAttribute("primaryProvider", primarySP);

        Set<StorageProviderAccount> secondarySPs =
            accountService.getSecondaryStorageProviders();
        model.addAttribute("secondaryProviders", secondarySPs);

        // Get available providers for account
        ProviderForm providerForm = new ProviderForm();
        List<StorageProviderType> availableProviderTypes = StorageProviderTypeUtil.getAvailableSecondaryTypes();

        Set<StorageProviderType> usedTypes = new HashSet<StorageProviderType>();
        for (StorageProviderAccount secondaryAcct : secondarySPs) {
            usedTypes.add(secondaryAcct.getProviderType());
        }

        // Also remove the primary type from the available secondary types
        usedTypes.add(primarySP.getProviderType());

        availableProviderTypes.removeAll(usedTypes);

        if (availableProviderTypes.size() > 0) {
            providerForm.setStorageProviders(availableProviderTypes);
        } else {
            providerForm.setStorageProviders(null);
        }

        model.addAttribute("providerForm", providerForm);
    }

    protected void addUserToModel(Model model) throws DBNotFoundException {
        model.addAttribute(UserController.USER_KEY, getUser());
    }

    /**
     * @return
     */
    protected DuracloudUser getUser() throws DBNotFoundException {
        SecurityContext securityContext = SecurityContextHolder.getContextHolderStrategy().getContext();
        Authentication authentication = securityContext.getAuthentication();
        String username = authentication.getName();
        return this.userService.loadDuracloudUserByUsername(username);
    }

}
