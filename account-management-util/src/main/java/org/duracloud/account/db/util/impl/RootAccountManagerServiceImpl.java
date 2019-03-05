/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.AccountRights;
import org.duracloud.account.db.model.DuracloudGroup;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.repo.DuracloudAccountRepo;
import org.duracloud.account.db.repo.DuracloudGroupRepo;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.repo.DuracloudRightsRepo;
import org.duracloud.account.db.repo.DuracloudStorageProviderAccountRepo;
import org.duracloud.account.db.repo.DuracloudUserInvitationRepo;
import org.duracloud.account.db.repo.DuracloudUserRepo;
import org.duracloud.account.db.util.DuracloudUserService;
import org.duracloud.account.db.util.RootAccountManagerService;
import org.duracloud.account.db.util.error.DBNotFoundException;
import org.duracloud.account.db.util.error.InvalidPasswordException;
import org.duracloud.account.db.util.error.UnsentEmailException;
import org.duracloud.common.sns.AccountChangeNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * @author Andrew Woods
 * Date: Oct 9, 2010
 */
@Component("rootAccountManagerService")
public class RootAccountManagerServiceImpl implements RootAccountManagerService {

    private Logger log = LoggerFactory.getLogger(RootAccountManagerServiceImpl.class);

    private DuracloudRepoMgr repoMgr;
    private DuracloudUserService userService;
    private AccountChangeNotifier accountChangeNotifier;

    @Autowired
    public RootAccountManagerServiceImpl(DuracloudRepoMgr duracloudRepoMgr,
                                         DuracloudUserService userService,
                                         AccountChangeNotifier accountChangeNotifier) {
        this.repoMgr = duracloudRepoMgr;
        this.userService = userService;
        this.accountChangeNotifier = accountChangeNotifier;
    }

    @Override
    public void resetUsersPassword(Long userId)
        throws DBNotFoundException, UnsentEmailException {
        log.info("Resetting password for user with ID {}", userId);

        DuracloudUser user = getUserRepo().findOne(userId);
        if (user == null) {
            throw new DBNotFoundException("User with ID: " + userId + " does not exist");
        }

        try {
            userService.forgotPassword(user.getUsername(), user.getSecurityQuestion(), user.getSecurityAnswer());
        } catch (InvalidPasswordException e) {
            log.error("This should never happen!", e);
        }
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID {}", userId);

        // Remove all user rights
        List<AccountRights> accountRights = getRightsRepo().findByUserId(userId);
        for (AccountRights right : accountRights) {
            this.userService.revokeUserRights(right.getAccount().getId(), userId);
        }

        // Remove user from all groups
        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        DuracloudGroupRepo groupRepo = getGroupRepo();
        List<DuracloudGroup> allGroups = groupRepo.findAll();
        for (DuracloudGroup group : allGroups) {
            Set<DuracloudUser> groupUsers = group.getUsers();
            if (groupUsers.contains(user)) {
                groupUsers.remove(user);
                groupRepo.save(group);
            }
        }

        // Remove the user
        getUserRepo().delete(userId);

        if (user.isRoot()) {
            notifyRootUsersChanged();
        } else {
            notifyUserChange(accountRights);
        }
    }

    @Override
    public void setRootUser(Long userId) {
        log.info("Setting root on user with ID {}", userId);

        // Adding root from the user
        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        user.setRoot(true);
        notifyRootUsersChanged();

    }

    @Override
    public void unsetRootUser(Long userId) {
        log.info("Unsetting root on user with ID {}", userId);

        // Remove root from the user
        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        user.setRoot(false);
        notifyRootUsersChanged();
    }

    private void notifyUserChange(List<AccountRights> accountRights) {
        for (AccountRights right : accountRights) {
            try {
                this.accountChangeNotifier.userStoreChanged(right.getAccount().getSubdomain());
            } catch (Exception ex) {
                log.error("failed to notify of user change: " + ex.getMessage(), ex);
            }
        }
    }

    private void notifyRootUsersChanged() {
        try {
            this.accountChangeNotifier.rootUsersChanged();
        } catch (Exception ex) {
            log.error("failed to notify of user change: " + ex.getMessage(), ex);
        }
    }

    private void notifyAccountChange(List<AccountRights> accountRights) {
        for (AccountRights right : accountRights) {
            try {
                this.accountChangeNotifier.accountChanged(right.getAccount().getSubdomain());
            } catch (Exception ex) {
                log.error("failed to notify of account change: " + ex.getMessage(), ex);
            }
        }

    }

    private AccountInfo getAccountByStorageProvider(Long providerId) {
        DuracloudAccountRepo accountRepo = this.repoMgr.getAccountRepo();
        AccountInfo account = accountRepo.findByPrimaryStorageProviderAccountId(providerId);
        if (account == null) {
            account = accountRepo.findBySecondaryStorageProviderAccountsId(providerId);
        }

        return account;
    }

    private void notifyStorageProviderChange(String accountId) {
        this.accountChangeNotifier.storageProvidersChanged(accountId);
    }

    @Override
    public void deleteAccount(Long accountId) {
        log.info("Deleting account with ID {}", accountId);

        // Delete the account rights
        List<AccountRights> rightsList = getRightsRepo().findByAccountId(accountId);
        for (AccountRights rights : rightsList) {
            DuracloudUser user = rights.getUser();
            user.getAccountRights().remove(rights);
            rights.getRoles().clear();
            getRightsRepo().save(rights);
        }

        getRightsRepo().deleteInBatch(rightsList);

        // Delete the groups associated with the account
        DuracloudGroupRepo groupRepo = repoMgr.getGroupRepo();
        List<DuracloudGroup> groups = groupRepo.findByAccountId(accountId);
        groupRepo.deleteInBatch(groups);

        // Delete any user invitations
        DuracloudUserInvitationRepo invRepo = repoMgr.getUserInvitationRepo();
        invRepo.deleteInBatch(invRepo.findByAccountId(accountId));

        // Delete account
        getAccountRepo().delete(accountId);

        notifyAccountChange(rightsList);
    }

    @Override
    public List<StorageProviderAccount> getSecondaryStorageProviders(Long accountId) {
        AccountInfo account = repoMgr.getAccountRepo().findOne(accountId);
        return new ArrayList(account.getSecondaryStorageProviderAccounts());
    }

    @Override
    public void setupStorageProvider(Long providerId,
                                     String username,
                                     String password,
                                     Map<String, String> properties,
                                     int storageLimit) {
        log.info("Setting up storage provider with ID {}", providerId);

        StorageProviderAccount storageProviderAccount = getStorageRepo().findOne(providerId);
        storageProviderAccount.setUsername(username);
        storageProviderAccount.setPassword(password);
        storageProviderAccount.getProperties().putAll(properties);
        storageProviderAccount.setStorageLimit(storageLimit);

        getStorageRepo().save(storageProviderAccount);
        notifyStorageProviderChange(getAccountByStorageProvider(providerId).getSubdomain());
    }

    @Override
    public AccountInfo getAccount(Long id) {
        return getAccountRepo().findOne(id);
    }

    @Override
    public void activateAccount(Long accountId) {
        log.info("Activating account with ID {}", accountId);
        AccountInfo accountInfo = getAccountRepo().findOne(accountId);
        accountInfo.setStatus(AccountInfo.AccountStatus.ACTIVE);
        getAccountRepo().save(accountInfo);
        this.accountChangeNotifier.accountChanged(accountInfo.getSubdomain());
    }

    @Override
    public Set<AccountInfo> listAllAccounts(String filter) {
        List<AccountInfo> accounts = getAccountRepo().findAll(new Sort("acctName"));
        Set<AccountInfo> accountInfos = new LinkedHashSet<AccountInfo>();
        for (AccountInfo acct : accounts) {
            if (filter == null || acct.getOrgName().startsWith(filter)) {
                accountInfos.add(acct);
            }
        }
        return accountInfos;
    }

    @Override
    public Set<DuracloudUser> listAllUsers(String filter) {
        List<DuracloudUser> usersList = getUserRepo().findAll(new Sort("username"));
        Set<DuracloudUser> users = new LinkedHashSet<DuracloudUser>();
        for (DuracloudUser user : usersList) {
            if (filter == null ||
                (user.getUsername().startsWith(filter)
                 || user.getFirstName().startsWith(filter)
                 || user.getLastName().startsWith(filter)
                 || user.getEmail().startsWith(filter))) {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public Set<DuracloudUser> listAllRootUsers(String filter) {
        List<DuracloudUser> usersList = getUserRepo().findAll(new Sort("username"));
        Set<DuracloudUser> users = new LinkedHashSet<DuracloudUser>();
        for (DuracloudUser user : usersList) {
            if (user.isRoot() && (filter == null ||
                    (user.getUsername().startsWith(filter)
                            || user.getFirstName().startsWith(filter)
                            || user.getLastName().startsWith(filter)
                            || user.getEmail().startsWith(filter)))) {
                users.add(user);
            }
        }
        return users;
    }

    private DuracloudUserRepo getUserRepo() {
        return repoMgr.getUserRepo();
    }

    private DuracloudGroupRepo getGroupRepo() {
        return repoMgr.getGroupRepo();
    }

    private DuracloudAccountRepo getAccountRepo() {
        return repoMgr.getAccountRepo();
    }

    private DuracloudRightsRepo getRightsRepo() {
        return repoMgr.getRightsRepo();
    }

    private DuracloudStorageProviderAccountRepo getStorageRepo() {
        return repoMgr.getStorageProviderAccountRepo();
    }
}
