/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.security.vote;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.duracloud.account.db.model.AccountRights;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.repo.DuracloudRightsRepo;
import org.duracloud.account.security.domain.SecuredRule;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Andrew Woods
 * Date: 4/5/11
 */
public abstract class BaseAccessDecisionVoter implements AccessDecisionVoter<MethodInvocation> {

    protected Logger log = LoggerFactory.getLogger(BaseAccessDecisionVoter.class);

    private DuracloudRepoMgr repoMgr;

    public BaseAccessDecisionVoter(DuracloudRepoMgr repoMgr) {
        this.repoMgr = repoMgr;
    }

    /**
     * This abstract method returns the class for which this voter has interest.
     *
     * @return class of target service interface
     */
    protected abstract Class<?> getTargetService();

    @Override
    public boolean supports(ConfigAttribute attribute) {
        log.trace("supports attribute{}", attribute.getAttribute());
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        log.trace("supports {}", clazz.getName());
        return MethodInvocation.class.isAssignableFrom(clazz);
    }

    protected boolean supportsTarget(MethodInvocation invocation) {
        Class<?>[] interfaces = invocation.getThis().getClass().getInterfaces();
        if (null == interfaces || interfaces.length == 0) {
            return false;
        }

        for (Class<?> c : interfaces) {
            if (c.equals(getTargetService())) {
                return true;
            }
        }
        return false;
    }

    protected SecuredRule getRule(Collection<ConfigAttribute> atts) {
        if (null == atts || atts.size() != 1) {
            throw new DuraCloudRuntimeException("Invalid security att " + atts);
        }

        return new SecuredRule(atts.iterator().next().getAttribute());
    }

    protected Collection<String> getUserRoles(Authentication authentication) {
        Set<String> roles = new HashSet<String>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        return roles;
    }

    protected int voteHasRole(String role, Collection<String> userRoles) {
        return userRoles.contains(role) ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    protected int voteUserHasRoleOnAccount(DuracloudUser user,
                                           String role,
                                           Long acctId) {
        log.trace("Does user {} have role {} on acct {}?", user.getId(), role, acctId);

        AccountRights rights = getUserRightsForAcct(user.getId(), acctId);
        if (null == rights) {
            return ACCESS_DENIED;
        }

        Set<Role> acctRoles = rights.getRoles();
        log.trace("Roles found: {}", acctRoles);

        if (acctRoles != null && acctRoles.size() > 0) {
            for (Role acctRole : acctRoles) {
                if (role.equals(acctRole.authority().getAuthority())) {
                    return ACCESS_GRANTED;
                }
            }
        }
        return ACCESS_DENIED;
    }

    protected int voteUserHasRoleOnAcctToUpdateOthersRoles(Long userId,
                                                           Long acctId,
                                                           Long otherUserId,
                                                           Set<Role> otherRoles) {
        log.trace("Voting if user {} has roles on acct {} to manage {}.", userId, acctId, otherUserId);

        AccountRights rights = getUserRightsForAcct(userId, acctId);
        AccountRights other = getUserRightsForAcct(otherUserId, acctId);

        if (null == rights || null == other) {
            log.warn("No rights found for users {}, {} on acct {}", userId, otherUserId, acctId);
            return ACCESS_DENIED;
        }

        boolean existing = hasVote(voteRolesAreSufficientToUpdateOther(rights.getRoles(),
                                                                       other.getRoles()));

        boolean updates = hasVote(voteRolesAreSufficientToUpdateOther(rights.getRoles(),
                                                                      otherRoles));

        log.trace("Are {} sufficient to update both {} and {}?", rights.getRoles(), other.getRoles(), otherRoles);

        return existing && updates ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    protected int voteRolesAreSufficientToUpdateOther(Set<Role> roles,
                                                      Set<Role> other) {
        if (null == roles || null == other) {
            log.warn("Null roles one or more {}, {}", roles, other);
            return ACCESS_DENIED;
        }

        Role otherHighestRole = Role.highestRole(other);
        if (null == otherHighestRole) {
            log.warn("No highest role found for {}", other);
            return ACCESS_DENIED;
        }

        boolean userHasRole = roles.contains(otherHighestRole);
        log.trace("Roles {} has permission to manage other {}", roles, otherHighestRole);

        return userHasRole ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    protected boolean hasVote(int vote) {
        return vote == ACCESS_GRANTED;
    }

    protected int numUsersForAccount(Long acctId) {
        Set<AccountRights> rights =
            new HashSet<>(repoMgr.getRightsRepo().findByAccountId(acctId));
        return (null != rights) ? rights.size() : 0;
    }

    protected AccountRights getUserRightsForAcct(Long userId, Long acctId) {
        DuracloudRightsRepo rightsRepo = repoMgr.getRightsRepo();
        AccountRights rights = rightsRepo.findByAccountIdAndUserId(acctId, userId);
        return rights;
    }

    protected Set<AccountRights> getAllUserRightsForAcct(Long acctId) {
        DuracloudRightsRepo rightsRepo = repoMgr.getRightsRepo();
        Set<AccountRights> rights = null;
        return new HashSet<>(rightsRepo.findByAccountId(acctId));
    }

    protected int voteMyUserId(DuracloudUser user, Long userId) {
        return user.getId().equals(userId) ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    protected int voteMyUsername(DuracloudUser user, String username) {
        return user.getUsername().equals(username) ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    protected DuracloudUser getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            log.trace("Unknown user {}", principal);
            DuracloudUser user = new DuracloudUser();
            user.setUsername((String) principal);
            return user;
        } else {
            return (DuracloudUser) principal;
        }
    }

    protected String asString(int decision) {
        String s = "unknown";
        switch (decision) {
            case ACCESS_DENIED:
                return "ACCESS_DENIED";
            case ACCESS_ABSTAIN:
                return "ACCESS_ABSTAIN";
            case ACCESS_GRANTED:
                return "ACCESS_GRANTED";
            default:
                return s;
        }
    }

    @Override
    public final int vote(Authentication authentication,
                          MethodInvocation invocation,
                          Collection<ConfigAttribute> attributes) {

        if (!supportsTarget(invocation)) {
            return castVote(ACCESS_ABSTAIN, invocation);
        }

        // Collect target method arguments
        Object[] methodArgs = invocation.getArguments();

        // Collect user making the call.
        DuracloudUser user = getCurrentUser(authentication);

        if (user.isRootUser()) {
            return ACCESS_GRANTED;
        }

        // Collect security constraints on method.
        SecuredRule securedRule = getRule(attributes);
        String role = securedRule.getRole().name();
        SecuredRule.Scope scope = securedRule.getScope();
        return voteImpl(authentication, invocation, attributes, methodArgs, user, securedRule, role, scope);
    }

    protected int castVote(int decision, MethodInvocation invocation) {
        String methodName = invocation.getMethod().getName();
        String className = invocation.getThis().getClass().getSimpleName();
        log.trace("{}.{}() = {}", className, methodName, asString(decision));
        return decision;
    }

    protected abstract int voteImpl(Authentication authentication,
                                    MethodInvocation invocation,
                                    Collection<ConfigAttribute> attributes, Object[] methodArgs,
                                    DuracloudUser user, SecuredRule securedRule, String role,
                                    SecuredRule.Scope scope);

}
