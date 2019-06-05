/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.AccountRights;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.repo.DuracloudRightsRepo;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.db.util.EmailTemplateService;
import org.duracloud.account.db.util.error.DuracloudProviderAccountNotAvailableException;
import org.duracloud.account.db.util.notification.NotificationMgr;
import org.duracloud.account.db.util.notification.Notifier;
import org.duracloud.common.sns.AccountChangeNotifier;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public class AccountServiceImpl implements AccountService {
    private Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    // The AccountInfo member is a read-cache. All 'getter' come from it, and
    // writes go to both it and the persistence layer.
    private AccountInfo account;
    private DuracloudRepoMgr repoMgr;
    private AccountChangeNotifier accountChangeNotifier;
    private Notifier notifier;

    /**
     * @param acct
     */
    public AccountServiceImpl(AmaEndpoint amaEndpoint,
                              AccountInfo acct,
                              DuracloudRepoMgr repoMgr,
                              AccountChangeNotifier accountChangeNotifier,
                              NotificationMgr notificationMgr,
                              EmailTemplateService emailTemplateService) {
        this.account = acct;
        this.repoMgr = repoMgr;
        this.accountChangeNotifier = accountChangeNotifier;
        this.notifier = new Notifier(notificationMgr.getEmailer(), amaEndpoint, emailTemplateService);
    }

    @Override
    public Set<DuracloudUser> getUsers() {
        DuracloudRightsRepo rightsRepo = repoMgr.getRightsRepo();

        Set<DuracloudUser> users = new HashSet<DuracloudUser>();
        List<AccountRights> rights =
            rightsRepo.findByAccountId(account.getId());

        for (AccountRights right : rights) {
            DuracloudUser user = right.getUser();
            user.getAccountRights().size();  // lazy load the user's accout rights
            users.add(user);
        }

        return users;
    }

    @Override
    public Long getAccountId() {
        return account.getId();
    }

    @Override
    public AccountInfo retrieveAccountInfo() {
        return account;
    }

    @Override
    public StorageProviderAccount getPrimaryStorageProvider() {
        return retrieveAccountInfo().getPrimaryStorageProviderAccount();
    }

    @Override
    public Set<StorageProviderAccount> getSecondaryStorageProviders() {
        return retrieveAccountInfo().getSecondaryStorageProviderAccounts();
    }

    @Override
    public void addStorageProvider(StorageProviderType storageProviderType) {
        String accountId = account.getSubdomain();
        log.info("Adding storage provider of type {} to account {}", storageProviderType, accountId);

        StorageProviderAccount storageProviderAccount = new StorageProviderAccount();
        storageProviderAccount.setProviderType(storageProviderType);

        AccountInfo account = retrieveAccountInfo();
        account.getSecondaryStorageProviderAccounts().add(storageProviderAccount);
        saveAccountInfo(account);

        // Note: This change is not propagated to DuraCloud as the StorageProvider is not yet
        // configured. The propagation occurs when the provider details are provided.
    }

    @Override
    public void removeStorageProvider(Long storageProviderId) {
        String accountId = account.getSubdomain();
        log.info("Removing storage provider with ID {} from account {}", storageProviderId, accountId);

        StorageProviderAccount storageProviderAccount =
            repoMgr.getStorageProviderAccountRepo().findOne(storageProviderId);
        AccountInfo accountInfo = retrieveAccountInfo();
        if (accountInfo.getSecondaryStorageProviderAccounts()
                       .remove(storageProviderAccount)) {
            saveAccountInfo(accountInfo);
            repoMgr.getStorageProviderAccountRepo().delete(storageProviderId);

            // Propagate changes to DuraCloud
            accountChangeNotifier.storageProvidersChanged(accountId);
        } else {
            throw new DuracloudProviderAccountNotAvailableException(
                "The storage provider account with ID " + storageProviderId +
                " is not associated with account with id " + account.getId() +
                " as a secondary storage provider.");
        }
    }

    private void saveAccountInfo(AccountInfo accountInfo) {
        this.repoMgr.getAccountRepo().save(accountInfo);
    }

    @Override
    public void changePrimaryStorageProvider(Long storageProviderId) {
        String accountId = account.getSubdomain();
        log.info("Changing primary storage provider to {} from account {}", storageProviderId, accountId);

        AccountInfo accountInfo = retrieveAccountInfo();
        Set<StorageProviderAccount> secondaryAccounts = accountInfo.getSecondaryStorageProviderAccounts();
        boolean primaryProviderUpdated = false;
        for (StorageProviderAccount secondary : secondaryAccounts) {
            if (secondary.getId().equals(storageProviderId)) {

                secondaryAccounts.remove(secondary);
                secondaryAccounts.add(accountInfo.getPrimaryStorageProviderAccount());
                accountInfo.setPrimaryStorageProviderAccount(secondary);
                accountInfo.setSecondaryStorageProviderAccounts(secondaryAccounts);
                saveAccountInfo(accountInfo);

                primaryProviderUpdated = true;
            }
        }

        if (primaryProviderUpdated) {
            // Propagate changes to DuraCloud
            accountChangeNotifier.storageProvidersChanged(accountId);
        } else {
            throw new DuracloudProviderAccountNotAvailableException(
                "The storage provider account with ID " + storageProviderId +
                " is not associated with account with id " + accountId +
                " as a secondary storage provider.");
        }
    }

    @Override
    public void storeAccountInfo(String acctName,
                                 String orgName,
                                 String department) {
        String[] logInfo =
            {account.getSubdomain(), acctName, orgName, department};
        log.info("Updating info for account {}. Account Name: {}, " +
                 "Org Name: {}, Department: {}", logInfo);

        account.setAcctName(acctName);
        account.setOrgName(orgName);
        account.setDepartment(department);
        repoMgr.getAccountRepo().save(account);
    }

    @Override
    public void storeAccountStatus(AccountInfo.AccountStatus status) {
        log.info("Updating account status to {} for account {}",
                 status.name(), account.getSubdomain());

        account.setStatus(status);
        repoMgr.getAccountRepo().save(account);
    }

    @Override
    public String getSubdomain() {
        return account.getSubdomain();
    }

    @Override
    public UserInvitation inviteUser(String emailAddress,
                                     String adminUsername) {
        log.info("Inviting user at address {} to account {}",
                 emailAddress, account.getSubdomain());

        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

        String code = emailAddress + System.currentTimeMillis();
        String redemptionCode = cksumUtil.generateChecksum(code);

        int expirationDays = 14;
        UserInvitation userInvitation = new UserInvitation(null,
                                                           account,
                                                           account.getAcctName(),
                                                           account.getOrgName(),
                                                           account.getDepartment(),
                                                           account.getSubdomain(),
                                                           adminUsername,
                                                           emailAddress,
                                                           expirationDays,
                                                           redemptionCode);
        repoMgr.getUserInvitationRepo().save(userInvitation);
        notifier.sendNotificationUserInvitation(userInvitation);

        return userInvitation;
    }

    @Override
    public Set<UserInvitation> getPendingInvitations() {
        List<UserInvitation> invitations =
            repoMgr.getUserInvitationRepo().findByAccountId(account.getId());

        Date now = new Date();
        Set<UserInvitation> pendingInvitations = new HashSet<UserInvitation>();

        for (UserInvitation ui : invitations) {
            if (ui.getExpirationDate().before(now)) {
                repoMgr.getUserInvitationRepo().delete(ui.getId());
            } else {
                pendingInvitations.add(ui);
            }
        }

        return pendingInvitations;
    }

    @Override
    public void deleteUserInvitation(Long invitationId) {
        log.info("Deleting user invitation with id {} from account {}",
                 invitationId, account.getSubdomain());

        repoMgr.getUserInvitationRepo().delete(invitationId);
    }

}
