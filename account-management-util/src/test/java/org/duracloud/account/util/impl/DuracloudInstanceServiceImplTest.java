/*
 * Copyright (c) 2009-2011 DuraSpace. All rights reserved.
 */
package org.duracloud.account.util.impl;

import org.duracloud.account.common.domain.AccountRights;
import org.duracloud.account.common.domain.DuracloudGroup;
import org.duracloud.account.common.domain.DuracloudInstance;
import org.duracloud.account.common.domain.DuracloudUser;
import org.duracloud.account.common.domain.Role;
import org.duracloud.account.common.domain.ServerImage;
import org.duracloud.account.common.domain.ServicePlan;
import org.duracloud.account.db.error.DBNotFoundException;
import org.duracloud.account.util.error.DuracloudInstanceUpdateException;
import org.duracloud.account.util.instance.InstanceUpdater;
import org.duracloud.appconfig.domain.DurabossConfig;
import org.duracloud.appconfig.domain.DuradminConfig;
import org.duracloud.appconfig.domain.DuraserviceConfig;
import org.duracloud.appconfig.domain.DurastoreConfig;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.security.domain.SecurityUserBean;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.duracloud.account.common.domain.ServicePlan.PROFESSIONAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 2/9/11
 */
public class DuracloudInstanceServiceImplTest
    extends DuracloudInstanceServiceTestBase {

    private final int NUM_USERS = 5;

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();

        EasyMock.expect(instance.getHostName())
            .andReturn("hostname")
            .anyTimes();
    }

    @Test
    public void testGetInstanceInfo() throws Exception {
        replayMocks();

        DuracloudInstance instanceInfo = service.getInstanceInfo();
        assertNotNull(instanceInfo);
        assertEquals(instance, instanceInfo);
    }

    @Test
    public void testGetStatus() throws Exception {
        String status = "status";
        EasyMock.expect(computeProvider.getStatus(EasyMock.isA(String.class)))
            .andReturn(status)
            .times(1);
        EasyMock.expect(instance.getProviderInstanceId())
            .andReturn("id")
            .times(1);

        replayMocks();

        String resultStatus = service.getStatus();
        assertNotNull(resultStatus);
        assertEquals(status, resultStatus);
    }

    @Test
    public void testGetStatusInternal() throws Exception {
        String status = "status";
        EasyMock.expect(computeProvider.getStatus(EasyMock.isA(String.class)))
            .andReturn(status)
            .times(1);
        EasyMock.expect(instance.getProviderInstanceId())
            .andReturn("id")
            .times(1);

        replayMocks();

        String resultStatus = service.getStatusInternal();
        assertNotNull(resultStatus);
        assertEquals(status, resultStatus);
    }

    @Test
    public void testStop() throws Exception {
        EasyMock.expect(instanceConfigUtil.getDurabossConfig())
                .andReturn(new DurabossConfig());

        EasyMock.expect(instance.getAccountId()).andReturn(accountId);
        EasyMock.expect(accountRepo.findById(accountId)).andReturn(account);
        EasyMock.expect(accountUtil.getServerDetails(account)).andReturn(
            serverDetails);
        EasyMock.expect(serverDetails.getServicePlan()).andReturn(PROFESSIONAL);

        durabossUpdater.stopDuraboss(EasyMock.isA(String.class),
                                     EasyMock.isA(DurabossConfig.class),
                                     EasyMock.isA(ServicePlan.class),
                                     EasyMock.isA(RestHttpHelper.class));
        EasyMock.expectLastCall();

        computeProvider.stop(EasyMock.isA(String.class));
        EasyMock.expectLastCall()
            .times(1);
        EasyMock.expect(instance.getProviderInstanceId())
            .andReturn("provider-id")
            .times(2);

        int instanceId = 42;
        instanceRepo.delete(instanceId);
        EasyMock.expectLastCall().times(1);

        EasyMock.expect(instance.getId())
            .andReturn(instanceId)
            .times(1);

        setUpServerImageMocks();
        replayMocks();

        service.stop();
    }

    @Test
    public void testInitialize() throws Exception {
        setUpInitializeMocks();
        replayMocks();
        service.initialize();
    }

    @Test
    public void testReInitializeUserRoles() throws Exception {
        setUpReInitializeUserRolesMocks();
        replayMocks();
        service.reInitializeUserRoles();
    }

    @Test
    public void testReInitializeInstance() throws Exception {
        setUpReInitializeMocks();
        replayMocks();
        service.reInitialize();
    }

    private void setUpReInitializeUserRolesMocks() throws Exception {
        int times = 1;
        setUpReInitializeCommonMocks(times);

        doSetUpReInitializeUserRolesMocks();
    }

    private void doSetUpReInitializeUserRolesMocks()
        throws DBNotFoundException {
        EasyMock.expect(instance.getAccountId()).andReturn(accountId).times(1);
        EasyMock.expect(accountRepo.findById(accountId))
            .andReturn(account)
            .times(1);

        Set<DuracloudUser> users = new HashSet<DuracloudUser>();
        users.add(createUser(22));
        users.add(createUser(33));
        EasyMock.expect(
            accountClusterUtil.getAccountClusterUsers(account))
                .andReturn(users);

        setUpGetClusterGroupsMocks();

        instanceUpdater.updateUserDetails(EasyMock.isA(String.class),
                                          EasyMock.isA(Set.class),
                                          EasyMock.isA(RestHttpHelper.class));
        EasyMock.expectLastCall();
    }

    private DuracloudUser createUser(int userId) {
        Set<AccountRights> accountRights = new HashSet<AccountRights>();
        Set<Role> roles = new HashSet<Role>();
        roles.add(Role.ROLE_USER);
        accountRights.add(new AccountRights(0, accountId, userId, roles));

        DuracloudUser user = new DuracloudUser(userId, "user", "pass", "first",
                                               "last", "email","question",
                                               "answer");
        user.setAccountRights(accountRights);
        return user;
    }

    private void setUpReInitializeMocks() throws Exception {
        int times = 3;
        setUpReInitializeCommonMocks(times);
        doSetUpReInitializeUserRolesMocks();

        DuradminConfig duradminConfig = new DuradminConfig();
        EasyMock.expect(instanceConfigUtil.getDuradminConfig()).andReturn(
            duradminConfig);
        EasyMock.expect(instanceConfigUtil.getDurastoreConfig())
            .andReturn(new DurastoreConfig());
        EasyMock.expect(instanceConfigUtil.getDuraserviceConfig())
            .andReturn(new DuraserviceConfig());
        EasyMock.expect(instanceConfigUtil.getDurabossConfig())
            .andReturn(new DurabossConfig());

        instanceUpdater.initializeInstance(EasyMock.isA(String.class),
                                           EasyMock.isA(DuradminConfig.class),
                                           EasyMock.isA(DurastoreConfig.class),
                                           EasyMock.isA(DuraserviceConfig.class),
                                           EasyMock.isA(DurabossConfig.class),
                                           EasyMock.isA(RestHttpHelper.class));
        EasyMock.expectLastCall();

        EasyMock.expect(accountUtil.getServerDetails(account)).andReturn(
            serverDetails);
        EasyMock.expect(serverDetails.getServicePlan()).andReturn(PROFESSIONAL);

        durabossUpdater.startDuraboss(EasyMock.isA(String.class),
                                      EasyMock.isA(DurabossConfig.class),
                                      EasyMock.isA(ServicePlan.class),
                                      EasyMock.isA(RestHttpHelper.class));
        EasyMock.expectLastCall();

        EasyMock.expect(instance.getId()).andReturn(1);

        EasyMock.expect(instanceRepo.findById(EasyMock.anyInt()))
            .andReturn(new DuracloudInstance(1,1,1,"host","provider",false));

        instanceRepo.save(EasyMock.isA(DuracloudInstance.class));
        EasyMock.expectLastCall();
    }

    private void setUpReInitializeCommonMocks(int times) throws Exception {
        // Set timeout to 0, to prevent waiting for instance availability
        service.setInitializeTimeout(0);
        setUpServerImageMocks();
    }

    @Test
    public void testRestart() throws Exception {
        computeProvider.restart(EasyMock.isA(String.class));
        EasyMock.expectLastCall()
            .times(1);
        EasyMock.expect(instance.getProviderInstanceId())
            .andReturn("id")
            .times(2);

        setUpInitializeMocks();
        replayMocks();

        service.restart();
    }

    private void setUpInitializeMocks() throws Exception {
        // Set timeout to 0, to prevent waiting for instance availability
        service.setInitializeTimeout(0);

        DuradminConfig duradminConfig = new DuradminConfig();
        EasyMock.expect(instanceConfigUtil.getDuradminConfig())
            .andReturn(duradminConfig)
            .times(1);
        EasyMock.expect(instanceConfigUtil.getDurastoreConfig())
            .andReturn(new DurastoreConfig())
            .times(1);
        EasyMock.expect(instanceConfigUtil.getDuraserviceConfig())
            .andReturn(new DuraserviceConfig())
            .times(1);
        EasyMock.expect(instanceConfigUtil.getDurabossConfig())
            .andReturn(new DurabossConfig())
            .times(1);

        EasyMock.expect(instance.getId()).andReturn(1);

        instanceUpdater.initializeInstance(EasyMock.isA(String.class),
                                           EasyMock.isA(DuradminConfig.class),
                                           EasyMock.isA(DurastoreConfig.class),
                                           EasyMock.isA(DuraserviceConfig.class),
                                           EasyMock.isA(DurabossConfig.class),
                                           EasyMock.isA(RestHttpHelper.class));
        EasyMock.expectLastCall()
            .times(1);

        EasyMock.expect(accountUtil.getServerDetails(account)).andReturn(
            serverDetails);
        EasyMock.expect(serverDetails.getServicePlan()).andReturn(PROFESSIONAL);

        durabossUpdater.startDuraboss(EasyMock.isA(String.class),
                                      EasyMock.isA(DurabossConfig.class),
                                      EasyMock.isA(ServicePlan.class),
                                      EasyMock.isA(RestHttpHelper.class));
        EasyMock.expectLastCall();

        EasyMock.expect(instanceRepo.findById(EasyMock.anyInt()))
            .andReturn(new DuracloudInstance(1,
                                             1,
                                             1,
                                             "host",
                                             "provider",
                                             false));

        instanceRepo.save(EasyMock.isA(DuracloudInstance.class));
        EasyMock.expectLastCall();

        doSetUpReInitializeUserRolesMocks();
        setUpServerImageMocks();
    }

    private void setUpServerImageMocks() throws Exception {
        int imageId = 7;
        String rootPassword = "rootpass";
        ServerImage serverImage = new ServerImage(imageId,
                                                  0,
                                                  "provider-image-id",
                                                  "version",
                                                  "description",
                                                  rootPassword,
                                                  false);

        EasyMock.expect(instance.getImageId())
            .andReturn(imageId)
            .times(1);
        EasyMock.expect(serverImageRepo.findById(imageId))
            .andReturn(serverImage)
            .times(1);
    }

    @Test
    public void testInitializeComputeProvider() throws Exception {
        setUpInitComputeProvider();
        replayMocks();

        service = new DuracloudInstanceServiceImpl(accountId,
                                                   instance,
                                                   repoMgr,
                                                   accountUtil,
                                                   accountClusterUtil,
                                                   computeProviderUtil,
                                                   notConfig);
    }

    @Test
    public void testGetInstanceVersion() throws Exception {
        setUpServerImageMocks();
        replayMocks();

        String instanceVersion = service.getInstanceVersion();
        assertEquals("version", instanceVersion);
    }

    @Test
    public void testSetUserRoles() throws Exception {
        int accountId = 13;
        setUpServerImageMocks();

        EasyMock.expect(instance.getAccountId()).andReturn(accountId);
        EasyMock.expect(accountRepo.findById(accountId)).andReturn(account);
        setUpGetClusterGroupsMocks();

        Capture<Set<SecurityUserBean>> capture =
            new Capture<Set<SecurityUserBean>>();
        setUpUserRoleMocks(capture);
        Set<DuracloudUser> users = createUsers();

        // This is the call under test.
        service.setUserRoles(users);

        Set<SecurityUserBean> beans = capture.getValue();
        assertNotNull(beans);

        verifyData(users, beans);
        verifyRoles(users, beans);
    }

    private void setUpGetClusterGroupsMocks() throws DBNotFoundException {
        Set<Integer> clusterAccountIds = new HashSet<Integer>();
        clusterAccountIds.add(accountId);
        int clusterAcctId = 14;
        clusterAccountIds.add(clusterAcctId);
        EasyMock.expect(accountClusterUtil.getClusterAccountIds(account))
            .andReturn(clusterAccountIds);

        Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();
        DuracloudGroup group = new DuracloudGroup(1, "group-test", accountId);
        group.addUserId(1);
        groups.add(group);

        EasyMock.expect(groupRepo.findByAccountId(accountId))
            .andReturn(groups);
        EasyMock.expect(groupRepo.findByAccountId(clusterAcctId))
            .andReturn(groups);
    }

    private void setUpUserRoleMocks(Capture<Set<SecurityUserBean>> capturedUsers) {
        instanceUpdater = createInstanceUpdaterExpectations(capturedUsers);

        replayMocks();
    }

    private InstanceUpdater createInstanceUpdaterExpectations(Capture<Set<SecurityUserBean>> capturedUsers) {
        instanceUpdater.updateUserDetails(EasyMock.isA(String.class),
                                          EasyMock.capture(capturedUsers),
                                          EasyMock.isA(RestHttpHelper.class));
        EasyMock.expectLastCall();
        return instanceUpdater;
    }

    private Set<DuracloudUser> createUsers() {
        Set<DuracloudUser> users = new HashSet<DuracloudUser>();
        for (int i = 0; i < NUM_USERS; ++i) {
            DuracloudUser user = newDuracloudUser(i, "user-" + i);

            Set<Role> roles = new HashSet<Role>();
            for(Role role : Role.values()[i].getRoleHierarchy()) {
                roles.add(role);
            }

            AccountRights accountRight =
                new AccountRights(-1, accountId, i, roles);
            Set<AccountRights> accountRights = new HashSet<AccountRights>();
            accountRights.add(accountRight);
            user.setAccountRights(accountRights);

            users.add(user);
        }
        return users;
    }

    private void verifyData(Set<DuracloudUser> users,
                            Set<SecurityUserBean> beans) {
        assertNotNull(users);
        assertNotNull(beans);

        for (DuracloudUser user: users)
        {
            try {
                // Ensure bean with username exists
                String username = user.getUsername();
                SecurityUserBean bean = getBeanWithName(username, beans);

                // Ensure data matches
                assertEquals(username,  bean.getUsername());
                assertEquals(user.getPassword(), bean.getPassword());
                assertEquals(user.getEmail(), bean.getEmail());
            } catch(RuntimeException e) {
                // Bean list should not include root users
                checkForRoot(user.getRolesByAcct(accountId), e);
            }
        }
    }

    private void verifyRoles(Set<DuracloudUser> users,
                             Set<SecurityUserBean> beans) {
        // Root user should be missing from list
        assertEquals(users.size() - 1, beans.size());

        for (DuracloudUser user: users)
        {
            Set<Role> roles = user.getRolesByAcct(accountId);
            assertNotNull(roles);
            assertTrue(roles.size() > 0);

            String username = user.getUsername();
            try {
                SecurityUserBean bean = getBeanWithName(username, beans);
                int numFound = 0;
                List<String> auths = bean.getGrantedAuthorities();
                assertNotNull(username, auths);

                assertEquals(roles.size(), auths.size());
                for (Role role : roles) {
                    for (String auth : auths) {
                        if (auth.equals(role.name())) {
                            numFound++;
                        }
                    }
                }
                assertEquals("username", roles.size(), numFound);
            } catch (RuntimeException e) {
                checkForRoot(roles, e);
            }
        }
    }

    private void checkForRoot(Set<Role> roles, RuntimeException e) {
        boolean isRoot = false;
        for(Role role : roles) {
            if(role.equals(Role.ROLE_ROOT)) {
                isRoot = true;
            }
        }
        if(!isRoot) {
            fail(e.getMessage());
        }
    }

    private SecurityUserBean getBeanWithName(String username,
                                             Set<SecurityUserBean> beans) {
        for (SecurityUserBean bean : beans) {
            if (username.equals(bean.getUsername())) {
                return bean;
            }
        }
        throw new RuntimeException("username not found in beans: " + username);
    }

    @Test
    public void testSetUserRolesEmpty() throws Exception {
        replayMocks();

        doTestSetUserRoles(new HashSet<DuracloudUser>());
        doTestSetUserRoles(null);
    }

    private void doTestSetUserRoles(HashSet<DuracloudUser> users) {
        boolean thrown = false;
        try {
            service.setUserRoles(users);
            fail("exception expected");

        } catch (DuracloudInstanceUpdateException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

}
