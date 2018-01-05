/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.text.MessageFormat;
import java.util.List;

import javax.validation.Valid;

import org.duracloud.account.app.controller.AccountSetupForm.StorageProviderSettings;
import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.AccountInfo.AccountStatus;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.RootAccountManagerService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/**
 * 
 * @author Daniel Bernstein
 *         Date: Feb 27, 2012
 *
 */
@Controller
@RequestMapping(AccountsController.BASE_MAPPING)
public class AccountsController extends AbstractRootController{
    public static final String BASE_MAPPING = RootConsoleHomeController.BASE_MAPPING + "/accounts";
    private static final String BASE_VIEW = BASE_MAPPING;
    private static final String ACCOUNT_SETUP_VIEW = BASE_VIEW+"/setup";
    public static final String ACCOUNT_SETUP_MAPPING = BY_ID_MAPPING + "/setup";
    private static final String SETUP_ACCOUNT_FORM_KEY = "setupAccountForm";

    @Autowired
    private AmaEndpoint amaEndpoint;

    @RequestMapping("")
    public ModelAndView get() {
        ModelAndView mav = new ModelAndView(BASE_VIEW, "accounts",
                getRootAccountManagerService().listAllAccounts(null));
        mav.addObject("mcDomain", amaEndpoint.getDomain());
        return mav;
    }

    @RequestMapping(value = { BY_ID_DELETE_MAPPING}, method = RequestMethod.POST)
    @Transactional
    public ModelAndView delete(@PathVariable Long id, RedirectAttributes redirectAttributes)
        throws AccountNotFoundException {
        AccountService accountService = getAccountManagerService().getAccount(id);
        String accountName = accountService.retrieveAccountInfo().getAcctName();
        getRootAccountManagerService().deleteAccount(id);
        String message = MessageFormat.format("Successfully deleted account ({0}).", accountName);
        setSuccessFeedback(message, redirectAttributes);
        return createRedirectMav(BASE_MAPPING);
    }

    
    @RequestMapping(value = { BY_ID_MAPPING +"/activate"}, method = RequestMethod.POST)
    @Transactional
    public ModelAndView activate(@PathVariable Long id, RedirectAttributes redirectAttributes)
        throws AccountNotFoundException {
        AccountService accountService = getAccountManagerService().getAccount(id);
        accountService.storeAccountStatus(AccountInfo.AccountStatus.ACTIVE);
        String accountName = accountService.retrieveAccountInfo().getAcctName();
        String message = MessageFormat.format("Successfully activated account ({0}).", accountName);
        setSuccessFeedback(message, redirectAttributes);
        return createRedirectMav(BASE_MAPPING);
    }

    @RequestMapping(value = { BY_ID_MAPPING +"/deactivate"}, method = RequestMethod.POST)
    @Transactional
    public ModelAndView deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes)
        throws AccountNotFoundException {
        AccountService accountService = getAccountManagerService().getAccount(id);
        accountService.storeAccountStatus(AccountInfo.AccountStatus.INACTIVE);
        String accountName = accountService.retrieveAccountInfo().getAcctName();
        String message = MessageFormat.format("Successfully deactivated account ({0}).", accountName);
        setSuccessFeedback(message, redirectAttributes);
        return createRedirectMav(BASE_MAPPING);

    }

    @RequestMapping(value = ACCOUNT_SETUP_MAPPING, method = RequestMethod.GET)
    public String getSetupAccount(
        @PathVariable Long id, Model model)
        throws Exception {
        log.info("setup account {}", id);

        AccountService as = getAccountManagerService().getAccount(id);
        StorageProviderAccount primary = as.getPrimaryStorageProvider();

        RootAccountManagerService rams =  getRootAccountManagerService();
        AccountInfo info = rams.getAccount(id);
        List<StorageProviderAccount> secondary =
            rams.getSecondaryStorageProviders(id);
        
        AccountSetupForm form = new AccountSetupForm(primary, secondary);
        model.addAttribute(AbstractAccountController.ACCOUNT_INFO_KEY, info);
        model.addAttribute(SETUP_ACCOUNT_FORM_KEY, form);
        model.addAttribute("pending",
                           info.getStatus().equals(AccountStatus.PENDING));
        return ACCOUNT_SETUP_VIEW;
    }

    @RequestMapping(value = ACCOUNT_SETUP_MAPPING, method = RequestMethod.POST)
    @Transactional
    public ModelAndView setupAccount(
        @PathVariable Long id,
        @ModelAttribute(SETUP_ACCOUNT_FORM_KEY) @Valid AccountSetupForm accountSetupForm,
                       BindingResult result, Model model,
                       RedirectAttributes redirectAttributes)
        throws Exception {
        log.info("setup account {}", id);

        RootAccountManagerService rams = getRootAccountManagerService();
        AccountInfo info = rams.getAccount(id);

        model.addAttribute(AbstractAccountController.ACCOUNT_INFO_KEY, info);

        if(result.hasErrors()){
            model.addAttribute("pending",
                               info.getStatus().equals(AccountStatus.PENDING));
            return new ModelAndView(ACCOUNT_SETUP_VIEW, model.asMap());
        }

        //save primary
        saveStorageProvider(accountSetupForm.getPrimaryStorageProviderSettings());
        
        //save secondary
        for(StorageProviderSettings sp :  accountSetupForm.getSecondaryStorageProviderSettingsList()){
            saveStorageProvider(sp);
        }

        String message = MessageFormat
                .format("Successfully configured providers for {0}",info.getAcctName());

        AccountStatus status = info.getStatus();
        //activate only if pending
        if(AccountStatus.PENDING.equals(status)){
            getRootAccountManagerService().activateAccount(id);
            message += " and activated acccount";
        }

        setSuccessFeedback(message, redirectAttributes);

        return createRedirectMav(BASE_MAPPING);
    }

    private void
        saveStorageProvider(StorageProviderSettings storageProviderSettings) {
        getRootAccountManagerService().setupStorageProvider(storageProviderSettings.getId(),
                storageProviderSettings.getUsername(),
                storageProviderSettings.getPassword(),
                storageProviderSettings.getProperties(),
                Integer.parseInt(storageProviderSettings.getStorageLimit()));
        
    }

    public void setAmaEndpoint(AmaEndpoint amaEndpoint) {
        this.amaEndpoint = amaEndpoint;
    }
 }
