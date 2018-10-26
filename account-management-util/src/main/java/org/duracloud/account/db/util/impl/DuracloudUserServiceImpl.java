/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.duracloud.account.config.AmaEndpoint;
import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.AccountRights;
import org.duracloud.account.db.model.DuracloudGroup;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.model.UserInvitation;
import org.duracloud.account.db.repo.DuracloudGroupRepo;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.repo.DuracloudRightsRepo;
import org.duracloud.account.db.repo.DuracloudUserInvitationRepo;
import org.duracloud.account.db.repo.DuracloudUserRepo;
import org.duracloud.account.db.util.DuracloudUserService;
import org.duracloud.account.db.util.EmailTemplateService;
import org.duracloud.account.db.util.error.DBNotFoundException;
import org.duracloud.account.db.util.error.InvalidPasswordException;
import org.duracloud.account.db.util.error.InvalidRedemptionCodeException;
import org.duracloud.account.db.util.error.InvalidUsernameException;
import org.duracloud.account.db.util.error.ReservedPrefixException;
import org.duracloud.account.db.util.error.ReservedUsernameException;
import org.duracloud.account.db.util.error.UnsentEmailException;
import org.duracloud.account.db.util.error.UserAlreadyExistsException;
import org.duracloud.account.db.util.notification.NotificationMgr;
import org.duracloud.account.db.util.notification.Notifier;
import org.duracloud.common.sns.AccountChangeNotifier;
import org.duracloud.common.util.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @author Andrew Woods
 * Date: Oct 9, 2010
 */
@Component("duracloudUserService")
public class DuracloudUserServiceImpl implements DuracloudUserService, UserDetailsService {

    private Logger log = LoggerFactory.getLogger(DuracloudUserServiceImpl.class);

    private DuracloudRepoMgr repoMgr;
    private NotificationMgr notificationMgr;
    private Notifier notifier;
    private AmaEndpoint amaEndpoint;
    private EmailTemplateService emailTemplateService;
    private AccountChangeNotifier accountChangeNotifier;

    @Autowired
    public DuracloudUserServiceImpl(DuracloudRepoMgr duracloudRepoMgr,
                                    NotificationMgr notificationMgr,
                                    AmaEndpoint amaEndpoint,
                                    AccountChangeNotifier accountChangeNotifier,
                                    EmailTemplateService emailTemplateService) {
        this.repoMgr = duracloudRepoMgr;
        this.notificationMgr = notificationMgr;
        this.amaEndpoint = amaEndpoint;
        this.accountChangeNotifier = accountChangeNotifier;
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    public void checkUsername(String username)
        throws InvalidUsernameException, UserAlreadyExistsException {

        if (!isValidUsername(username)) {
            throw new InvalidUsernameException(username);
        }

        if (isReservedPrefix(username)) {
            throw new ReservedPrefixException(username);
        }

        if (isReservedName(username)) {
            throw new ReservedUsernameException(username);
        }

        DuracloudUser user = repoMgr.getUserRepo().findByUsername(username);
        if (user != null) {
            throw new UserAlreadyExistsException(username);
        }
    }

    private boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }

        return username.matches("\\A(?![_.\\-])[a-z0-9_.\\-]+(?<![_.\\-])\\Z");
    }

    private boolean isReservedName(String username) {
        return false;
    }

    private boolean isReservedPrefix(String username) {
        if (username.startsWith(DuracloudGroup.PREFIX)) {
            return true;
        }

        return false;
    }

    @Override
    public DuracloudUser createNewUser(String username,
                                       String password,
                                       String firstName,
                                       String lastName,
                                       String email,
                                       String securityQuestion,
                                       String securityAnswer)
        throws UserAlreadyExistsException, InvalidUsernameException {

        checkUsername(username);

        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.SHA_256);

        DuracloudUser user = new DuracloudUser();
        user.setUsername(username);
        user.setPassword(util.generateChecksum(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswer(securityAnswer);
        repoMgr.getUserRepo().save(user);

        log.info("New user created with username {}", username);
        getNotifier().sendNotificationCreateNewUser(user);

        if (user.isRoot()) {
            this.accountChangeNotifier.rootUsersChanged();
        }

        return user;
    }

    @Override
    public boolean setUserRights(Long acctId, Long userId, Role... roles) {
        return setUserRightsInternal(acctId, userId, roles);
    }

    private boolean setUserRightsInternal(Long acctId, Long userId, Role... roles) {

        Set<Role> roleSet = new HashSet<Role>();
        for (Role role : roles) {
            roleSet.add(role);
        }

        log.info("Updating user rights for user {} on account {} to roles " +
                 asString(roleSet), userId, acctId);

        boolean result = doSetUserRights(acctId, userId, roleSet);
        if (result) {
            notifyAccountChanged(acctId);
        }
        return result;
    }

    private void notifyAccountChanged(Long acctId) {
        AccountInfo account = this.repoMgr.getAccountRepo().getOne(acctId);
        String accountId = account.getSubdomain();
        this.accountChangeNotifier.accountChanged(accountId);
    }

    private boolean doSetUserRights(Long acctId, Long userId, Set<Role> roles) {
        if (null == roles) {
            throw new IllegalArgumentException("Role may not be null");
        }

        Set<Role> oldRoles = null;
        Set<Role> newRoles = new HashSet<Role>();
        for (Role role : roles) {
            newRoles.addAll(role.getRoleHierarchy());
        }

        DuracloudRightsRepo rightsRepo = repoMgr.getRightsRepo();
        AccountRights rights = rightsRepo.findByAccountIdAndUserId(acctId, userId);
        if (rights != null) {
            oldRoles = rights.getRoles();
        } else {
            log.info("New rights will be added for user {} on account {}",
                     userId, acctId);
        }

        boolean updatedNeeded = !newRoles.equals(oldRoles);
        if (updatedNeeded) {
            saveRights(acctId, userId, newRoles, rights);
        }
        return updatedNeeded;
    }

    private String asString(Set<Role> roles) {
        StringBuilder sb = new StringBuilder();
        for (Role role : roles) {
            sb.append(role.name());
            sb.append(",");
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private void saveRights(Long acctId,
                            Long userId,
                            Set<Role> roles,
                            AccountRights rights) {
        if (null == rights) {
            AccountInfo account = repoMgr.getAccountRepo().findOne(acctId);
            DuracloudUser user = repoMgr.getUserRepo().findOne(userId);

            rights = new AccountRights();
            rights.setAccount(account);
            rights.setUser(user);
            rights.setRoles(roles);
        } else {
            rights.setRoles(roles);
        }

        repoMgr.getRightsRepo().save(rights);
    }

    @Override
    public void revokeUserRights(Long acctId, Long userId) {
        log.info("Revoking rights for user {} on account {}", userId, acctId);

        doRevokeUserRights(acctId, userId);
        removeUserFromAccountGroups(acctId, userId);
        notifyAccountChanged(acctId);
    }

    private void doRevokeUserRights(Long acctId, Long userId) {
        DuracloudRightsRepo rightsRepo = repoMgr.getRightsRepo();
        AccountRights rights =
            rightsRepo.findByAccountIdAndUserId(acctId, userId);
        if (rights != null) {
            DuracloudUserRepo userRepo = repoMgr.getUserRepo();
            DuracloudUser user = userRepo.findOne(userId);
            user.getAccountRights().remove(rights);
            userRepo.saveAndFlush(user);
            rightsRepo.delete(rights.getId());
        }
    }

    private void removeUserFromAccountGroups(Long acctId, Long userId) {
        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        DuracloudGroupRepo groupRepo = repoMgr.getGroupRepo();
        List<DuracloudGroup> acctGroups = groupRepo.findByAccountId(acctId);
        for (DuracloudGroup group : acctGroups) {
            Set<DuracloudUser> groupUsers = group.getUsers();
            if (groupUsers.contains(user)) {
                groupUsers.remove(user);
                groupRepo.save(group);
            }
        }
    }

    @Override
    public void changePassword(Long userId,
                               String oldPassword,
                               boolean oldPasswordEncoded,
                               String newPassword)
        throws DBNotFoundException, InvalidPasswordException {
        if (null != newPassword && !newPassword.equals(oldPassword)) {
            log.info("Changing password for user with ID {}", userId);

            ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.SHA_256);

            DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
            if (user == null) {
                throw new DBNotFoundException("User with ID: " + userId + " does not exist.");
            }
            if (!oldPasswordEncoded) {
                oldPassword = util.generateChecksum(oldPassword);
            }
            if (!user.getPassword().equals(oldPassword)) {
                throw new InvalidPasswordException(userId);
            }

            user.setPassword(util.generateChecksum(newPassword));
            repoMgr.getUserRepo().save(user);

            propagateUserUpdate(userId);
        }
    }

    @Override
    public void changePasswordInternal(Long userId,
                                       String oldPassword,
                                       boolean oldPasswordEncoded,
                                       String newPassword)
        throws DBNotFoundException, InvalidPasswordException {
        changePassword(userId, oldPassword, oldPasswordEncoded, newPassword);
    }

    @Override
    public void redeemPasswordChangeRequest(Long userId, String redemptionCode)
        throws InvalidRedemptionCodeException {

        log.info("Redeeming change request for user with ID {}", userId);

        DuracloudUserInvitationRepo invRepo = repoMgr.getUserInvitationRepo();

        // Retrieve the invitation
        UserInvitation invitation = invRepo.findByRedemptionCode(redemptionCode);
        if (invitation == null) {
            throw new InvalidRedemptionCodeException(redemptionCode);
        }

        // Delete the invitation
        invRepo.delete(invitation.getId());
    }

    private void propagateUserUpdate(Long userId) {
        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        // Propagate changes for each of the user's accounts
        if (!user.isRoot()) { // Do no propagate if user is root

            List<AccountRights> rightsList =
                repoMgr.getRightsRepo().findByUserId(userId);

            for (AccountRights rights : rightsList) {
                this.accountChangeNotifier.userStoreChanged(rights.getAccount().getSubdomain());
            }
        } else {
            this.accountChangeNotifier.rootUsersChanged();
        }
    }

    @Override
    public void forgotPassword(String username,
                               String securityQuestion,
                               String securityAnswer)
        throws DBNotFoundException, InvalidPasswordException, UnsentEmailException {
        log.info("Resolving forgotten password for user {}", username);

        DuracloudUser user = loadDuracloudUserByUsernameInternal(username);

        if (!user.getSecurityQuestion().equalsIgnoreCase(securityQuestion) ||
            !user.getSecurityAnswer().equalsIgnoreCase(securityAnswer)) {
            throw new InvalidPasswordException(user.getId());
        }

        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String code = username + System.currentTimeMillis();
        String redemptionCode = cksumUtil.generateChecksum(code);
        int expirationDays = 14;
        UserInvitation userInvitation = new UserInvitation(null,
                                                           null,
                                                           "n/a",
                                                           "n/a",
                                                           "n/a",
                                                           "n/a",
                                                           username,
                                                           user.getEmail(),
                                                           expirationDays,
                                                           redemptionCode);

        this.repoMgr.getUserInvitationRepo().save(userInvitation);

        getNotifier().sendNotificationPasswordReset(user,
                                                    redemptionCode,
                                                    userInvitation.getExpirationDate());
    }

    @Override
    public UserInvitation retrievePassordChangeInvitation(String redemptionCode)
        throws DBNotFoundException {
        UserInvitation invite = this.repoMgr.getUserInvitationRepo()
                                            .findByRedemptionCode(redemptionCode);
        if (invite == null) {
            throw new DBNotFoundException("Change password invitation with" +
                                          " redemption code: " + redemptionCode + " does not exist");
        }
        if (invite.getExpirationDate().getTime() < System.currentTimeMillis()) {
            log.info("invitation {} has expired. Deleting from repo...", invite);
            this.repoMgr.getUserInvitationRepo().delete(invite.getId());
            throw new DBNotFoundException("Invitation has expired: " + invite);
        } else {
            return invite;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
        try {
            return loadDuracloudUserByUsername(username);
        } catch (DBNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    @Override
    public DuracloudUser loadDuracloudUserByUsername(String username)
        throws DBNotFoundException {
        return loadDuracloudUserByUsernameInternal(username);
    }

    @Override
    public DuracloudUser loadDuracloudUserByUsernameInternal(String username)
        throws DBNotFoundException {
        DuracloudUser user = repoMgr.getUserRepo().findByUsername(username);
        if (user == null) {
            throw new DBNotFoundException("User with username: " + username + " does not exist");
        }
        return user;
    }

    @Override
    public DuracloudUser loadDuracloudUserByIdInternal(Long userId)
        throws DBNotFoundException {
        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        if (user == null) {
            throw new DBNotFoundException("User with ID: " + userId + " does not exist");
        }
        return user;
    }

    @Override
    public Long redeemAccountInvitation(Long userId, String redemptionCode)
        throws InvalidRedemptionCodeException {
        log.info("Redeeming account invitation for user with ID {}", userId);

        DuracloudUserInvitationRepo invRepo = repoMgr.getUserInvitationRepo();

        // Retrieve the invitation
        UserInvitation invitation = invRepo.findByRedemptionCode(redemptionCode);
        if (invitation == null) {
            throw new InvalidRedemptionCodeException(redemptionCode);
        }

        // Add the user to the account if they are not already a member
        if (!userHasAccountRights(userId, invitation.getAccount().getId())) {
            setUserRights(invitation.getAccount().getId(), userId, Role.ROLE_USER);
        }

        // Delete the invitation
        invRepo.delete(invitation.getId());

        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        DuracloudUser adminUser =
            repoMgr.getUserRepo().findByUsername(invitation.getAdminUsername());
        if (adminUser == null) {
            String msg = "Exception encountered attempting to send admin user " +
                         invitation.getAdminUsername() + " notice that user with id " +
                         userId + " accepted their account invitation";
            DBNotFoundException e = new DBNotFoundException("Admin user with" +
                                                            " username: " + invitation.getAdminUsername() +
                                                            "does not exist");
            throw new UnsentEmailException(msg, e);
        }
        getNotifier().sendNotificationRedeemedInvitation(user, adminUser.getEmail());

        //return accountId
        return invitation.getAccount().getId();
    }

    private boolean userHasAccountRights(Long userId, Long accountId) {
        DuracloudRightsRepo rightsRepo = repoMgr.getRightsRepo();
        AccountRights rights = rightsRepo.findByAccountIdAndUserId(accountId, userId);
        return rights != null;
    }

    @Override
    public void storeUserDetails(Long userId,
                                 String firstName,
                                 String lastName,
                                 String email,
                                 String securityQuestion,
                                 String securityAnswer,
                                 String allowableIPAddressRange)
        throws DBNotFoundException {
        log.info("Updating user details for user with ID {}", userId);

        DuracloudUser user = repoMgr.getUserRepo().findOne(userId);
        if (user == null) {
            throw new DBNotFoundException("User with ID: " + userId + " does not exist");
        }
        boolean emailUpdate = !user.getEmail().equals(email);
        boolean ipAddressUpdate = !Objects.equals(user.getAllowableIPAddressRange(),
                                                  allowableIPAddressRange);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswer(securityAnswer);
        user.setAllowableIPAddressRange(allowableIPAddressRange);
        repoMgr.getUserRepo().save(user);

        if (emailUpdate || ipAddressUpdate) {
            propagateUserUpdate(userId);
        }
    }

    private Notifier getNotifier() {
        if (null == notifier) {
            notifier = new Notifier(notificationMgr.getEmailer(), amaEndpoint, emailTemplateService);
        }
        return notifier;
    }

    @Override
    public boolean addUserToAccount(Long acctId, Long userId) throws DBNotFoundException {
        boolean added = setUserRightsInternal(acctId, userId, Role.ROLE_USER);
        if (added) {
            DuracloudUser user = loadDuracloudUserByIdInternal(userId);
            AccountInfo accountInfo =
                repoMgr.getAccountRepo().findOne(acctId);
            if (accountInfo == null) {
                throw new DBNotFoundException("Account with ID: " + acctId + " does not exist");
            }
            getNotifier().sendNotificationUserAddedToAccount(user, accountInfo);
        }

        return added;
    }

}
