/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.util.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.duracloud.account.db.model.AccountInfo;
import org.duracloud.account.db.model.DuracloudGroup;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.util.DuracloudGroupService;
import org.duracloud.account.db.util.error.AccountNotFoundException;
import org.duracloud.account.db.util.error.DuracloudGroupAlreadyExistsException;
import org.duracloud.account.db.util.error.DuracloudGroupNotFoundException;
import org.duracloud.account.db.util.error.InvalidGroupNameException;
import org.duracloud.common.changenotifier.AccountChangeNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Bernstein
 * Date: Nov 11, 2011
 */
@Component("duracloudGroupService")
public class DuracloudGroupServiceImpl implements DuracloudGroupService {

    private Logger log =
        LoggerFactory.getLogger(DuracloudGroupServiceImpl.class);

    private DuracloudRepoMgr repoMgr;

    private AccountChangeNotifier accountChangeNotifier;

    @Autowired
    public DuracloudGroupServiceImpl(DuracloudRepoMgr duracloudRepoMgr, AccountChangeNotifier accountChangeNotifier) {
        this.repoMgr = duracloudRepoMgr;
        this.accountChangeNotifier = accountChangeNotifier;
    }

    @Override
    public Set<DuracloudGroup> getGroups(Long acctId) {
        List<DuracloudGroup> listGroups = repoMgr.getGroupRepo().findByAccountId(acctId);
        Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();
        groups.addAll(listGroups);
        return Collections.unmodifiableSet(groups);
    }

    @Override
    public DuracloudGroup getGroup(String name, Long acctId) {
        return repoMgr.getGroupRepo().findByNameAndAccountId(name, acctId);
    }

    @Override
    public DuracloudGroup createGroup(String name, Long acctId)
        throws DuracloudGroupAlreadyExistsException, InvalidGroupNameException {
        if (!isGroupNameValid(name)) {
            throw new InvalidGroupNameException(name);
        }

        if (groupExistsInAccount(name, acctId)) {
            throw new DuracloudGroupAlreadyExistsException(name);
        }

        // The account id exists per the previous calls
        AccountInfo accountInfo = repoMgr.getAccountRepo().findById(acctId).get();

        DuracloudGroup group = new DuracloudGroup();
        group.setName(name);
        group.setAccount(accountInfo);
        group = repoMgr.getGroupRepo().save(group);
        return group;
    }

    /**
     * This method is 'protected' for testing purposes only.
     */
    protected final boolean isGroupNameValid(String name) {
        if (name == null) {
            return false;
        }

        if (!name.startsWith(DuracloudGroup.PREFIX)) {
            return false;
        }

        if (DuracloudGroup.PUBLIC_GROUP_NAME.equalsIgnoreCase(name)) {
            return false;
        }

        return name.substring(DuracloudGroup.PREFIX.length()).matches(
            "\\A(?![_.@\\-])[a-z0-9_.@\\-]+(?<![_.@\\-])\\Z");
    }

    private boolean groupExistsInAccount(String name, Long acctId) {
        DuracloudGroup group = getGroup(name, acctId);
        return group != null;
    }

    @Override
    public void deleteGroup(DuracloudGroup group, Long acctId) {
        if (null == group) {
            log.warn("Arg group is null.");
            return;
        }
        repoMgr.getGroupRepo().deleteById(group.getId());
        propagateUpdate(acctId);
    }

    @Override
    public void updateGroupUsers(DuracloudGroup group,
                                 Set<DuracloudUser> users,
                                 Long acctId)
        throws DuracloudGroupNotFoundException {
        group.setUsers(users);
        repoMgr.getGroupRepo().save(group);
        propagateUpdate(acctId);
    }

    private void propagateUpdate(Long acctId) {
        try {
            final var account = this.repoMgr.getAccountRepo().findById(acctId)
                .orElseThrow(() -> new AccountNotFoundException(acctId));
            this.accountChangeNotifier.userStoreChanged(account.getSubdomain());
        } catch (Exception ex) {
            log.error("failed to notify of change to account " + acctId, ex);
        }
    }
}
