/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.error.AccessDeniedException;
import org.duracloud.account.db.util.security.AnnotationParser;
import org.duracloud.account.db.util.security.impl.MethodInvocationImpl;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;

/**
 * This class wraps another AccountService implementation with
 * AccessDecisionVoter security checks based on the AccountService annotations.
 *
 * @author Andrew Woods
 * Date: 4/7/11
 */
public class AccountServiceSecuredImpl implements AccountService {

    private Logger log = LoggerFactory.getLogger(AccountServiceSecuredImpl.class);

    private AccountService accountService;
    private Authentication authentication;
    private AccessDecisionVoter voter;

    private Map<String, Object[]> methodMap;

    public AccountServiceSecuredImpl(AccountService accountService,
                                     Authentication authentication,
                                     AccessDecisionVoter voter,
                                     AnnotationParser annotationParser) {
        this.accountService = accountService;
        this.authentication = authentication;
        this.voter = voter;
        this.methodMap = annotationParser.getMethodAnnotationsForClass(Secured.class,
                                                                       this.getClass());
    }

    private void throwIfAccessDenied(Object... args) {
        String methodName = getCurrentMethodName();

        Set<ConfigAttribute> configAtts = new HashSet<ConfigAttribute>();
        for (Object obj : methodMap.get(methodName)) {
            configAtts.add(new SecurityConfig((String) obj));
        }

        MethodInvocation invocation = new MethodInvocationImpl(this,
                                                               methodName,
                                                               args);
        int decision = voter.vote(authentication, invocation, configAtts);
        if (decision != AccessDecisionVoter.ACCESS_GRANTED) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    @Override
    public Long getAccountId() {
        throwIfAccessDenied();
        return accountService.getAccountId();
    }

    @Override
    public AccountInfo retrieveAccountInfo() {
        throwIfAccessDenied();
        return accountService.retrieveAccountInfo();
    }

    @Override
    public void storeAccountInfo(String acctName,
                                 String orgName,
                                 String department) {
        throwIfAccessDenied(acctName, orgName, department);
        accountService.storeAccountInfo(acctName, orgName, department);
    }

    @Override
    public void storeAccountStatus(AccountInfo.AccountStatus status) {
        throwIfAccessDenied(status);
        accountService.storeAccountStatus(status);
    }

    @Override
    public String getSubdomain() {
        throwIfAccessDenied();
        return accountService.getSubdomain();
    }

    @Override
    public StorageProviderAccount getPrimaryStorageProvider() {
        throwIfAccessDenied();
        return accountService.getPrimaryStorageProvider();
    }

    @Override
    public Set<StorageProviderAccount> getSecondaryStorageProviders() {
        throwIfAccessDenied();
        return accountService.getSecondaryStorageProviders();
    }

    @Override
    public void addStorageProvider(StorageProviderType storageProviderType) {
        throwIfAccessDenied(storageProviderType);
        accountService.addStorageProvider(storageProviderType);
    }

    @Override
    public void removeStorageProvider(Long storageProviderId) {
        throwIfAccessDenied(storageProviderId);
        accountService.removeStorageProvider(storageProviderId);
    }

    @Override
    public void changePrimaryStorageProvider(Long storageProviderId) {
        throwIfAccessDenied(storageProviderId);
        accountService.changePrimaryStorageProvider(storageProviderId);
    }

    @Override
    public Set<DuracloudUser> getUsers() {
        throwIfAccessDenied();
        return accountService.getUsers();
    }

    @Override
    public UserInvitation inviteUser(String emailAddress, String adminUsername) {
        throwIfAccessDenied(emailAddress, adminUsername);
        return accountService.inviteUser(emailAddress, adminUsername);
    }

    @Override
    public Set<UserInvitation> getPendingInvitations() {
        throwIfAccessDenied();
        return accountService.getPendingInvitations();
    }

    @Override
    public void deleteUserInvitation(Long invitationId) {
        throwIfAccessDenied(invitationId);
        accountService.deleteUserInvitation(invitationId);
    }

}
