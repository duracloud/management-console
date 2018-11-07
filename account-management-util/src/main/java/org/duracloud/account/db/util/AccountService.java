/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util;

import java.util.Set;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.StorageProviderAccount;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.storage.domain.StorageProviderType;
import org.springframework.security.access.annotation.Secured;

/**
 * An interface for manipulating account data.
 *
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */
public interface AccountService {

    /**
     * This method returns the id of account
     *
     * @return account id
     */
    @Secured("role:ROLE_ANONYMOUS, scope:ANY")
    public Long getAccountId();

    /**
     * @return
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public AccountInfo retrieveAccountInfo();

    /**
     * @param acctName
     * @param orgName
     * @param department
     */
    @Secured({"role:ROLE_OWNER, scope:SELF_ACCT"})
    public void storeAccountInfo(String acctName,
                                 String orgName,
                                 String department);

    /**
     * @param status
     */
    @Secured({"role:ROLE_OWNER, scope:SELF_ACCT"})
    public void storeAccountStatus(AccountInfo.AccountStatus status);

    /**
     * This method returns the subdomain associated with this account.
     *
     * @return subdomain
     */
    @Secured({"role:ROLE_USER, scope:SELF_ACCT"})
    public String getSubdomain();

    /**
     * Retrieves the primary storage provider account info
     *
     * @return
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public StorageProviderAccount getPrimaryStorageProvider();

    /**
     * Retrieves the info for all secondary storage provider accounts
     *
     * @return
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public Set<StorageProviderAccount> getSecondaryStorageProviders();

    /**
     * Adds a new secondary storage provider to this account.
     *
     * @param storageProviderType
     */
    @Secured({"role:ROLE_OWNER, scope:SELF_ACCT"})
    public void addStorageProvider(StorageProviderType storageProviderType);

    /**
     * Removes a storage provider from the list of secondary storage providers
     * for this account. The primary storage provider cannot be removed.
     *
     * @param storageProviderId
     */
    @Secured({"role:ROLE_OWNER, scope:SELF_ACCT"})
    public void removeStorageProvider(Long storageProviderId);

    /**
     * @return empty list
     */
    @Secured({"role:ROLE_ANONYMOUS, scope:ANY"})
    public Set<DuracloudUser> getUsers();

    /**
     * Invites a user to join this account by sending a notification to the
     * provided email address. The invitation sent to the user is also
     * returned by this method, as it may be useful to an administrator.
     *
     * @param emailAddress address at which to invite user
     * @return UserInvitation
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public UserInvitation inviteUser(String emailAddress, String adminUsername);

    /**
     * Gets a listing of the user invitations which are associated with this
     * account.
     *
     * @return UserInvitation set
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public Set<UserInvitation> getPendingInvitations();

    /**
     * Deletes an invitation to this account.
     *
     * @param invitationId
     */
    @Secured({"role:ROLE_ADMIN, scope:SELF_ACCT"})
    public void deleteUserInvitation(Long invitationId);

    /**
     * Changes the primary storage provider to the one specified by the id.
     *
     * @param id of the new primary storage provider.
     */
    @Secured({"role:ROLE_OWNER, scope:SELF_ACCT"})
    public void changePrimaryStorageProvider(Long id);

}
