/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.duracloud.account.db.model.DuracloudMill;
import org.duracloud.account.db.model.RabbitmqConfig;
import org.duracloud.account.db.util.DuracloudMillConfigService;
import org.duracloud.account.db.util.RabbitmqConfigService;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.binding.message.Message;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Daniel Bernstein
 */
@RunWith(EasyMockRunner.class)
public class DuracloudMillControllerTest extends EasyMockSupport {

    @Mock
    private DuracloudMillConfigService service;

    @Mock
    private RabbitmqConfigService rmqService;

    @TestSubject
    private DuracloudMillController controller = new DuracloudMillController();

    String dbHost = "host";
    Integer dbPort = 100;
    String dbName = "name";
    String dbUsername = "user";
    String dbPassword = "password";
    String auditQueue = "auditQueue";
    String auditLogSpaceId = "auditLogSpaceId";
    String queueType = "rabbitmq";
    String rabbitmqHost = "rmqhost";
    Integer rabbitmqPort = 5672;
    String rabbitmqVhost = "rmqvhost";
    String rabbitmqExchange = "rmqexchange";
    String rabbitmqUsername = "rmqusername";
    String rabbitmqPassword = "rmqpassword";
    RabbitmqConfig rmqConf = new RabbitmqConfig();

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGet() {
        replayAll();
        assertNotNull(this.controller.get());
    }

    @Test
    public void testForm() {

        rmqConf.setId(1L);
        rmqConf.setHost(rabbitmqHost);
        rmqConf.setPort(rabbitmqPort);
        rmqConf.setVhost(rabbitmqVhost);
        rmqConf.setUsername(rabbitmqUsername);
        rmqConf.setPassword(rabbitmqPassword);

        DuracloudMill mill = createMock(DuracloudMill.class);
        expect(mill.getDbHost()).andReturn(dbHost);
        expect(mill.getDbPort()).andReturn(dbPort);
        expect(mill.getDbName()).andReturn(dbName);
        expect(mill.getDbUsername()).andReturn(dbUsername);
        expect(mill.getDbPassword()).andReturn(dbPassword);
        expect(mill.getAuditQueue()).andReturn(auditQueue);
        expect(mill.getAuditLogSpaceId()).andReturn(auditLogSpaceId);
        expect(mill.getQueueType()).andReturn(queueType);
        expect(mill.getRabbitmqConfig()).andReturn(rmqConf);
        expect(mill.getRabbitmqExchange()).andReturn(rabbitmqExchange);

        expect(this.rmqService.get(1L)).andReturn(rmqConf);
        expect(this.service.get()).andReturn(mill);
        replayAll();
        DuracloudMillForm form = this.controller.form();
        assertNotNull(form);
        assertEquals(form.getDbHost(), dbHost);
        assertEquals(form.getDbPort(), dbPort);
        assertEquals(form.getDbName(), dbName);
        assertEquals(form.getDbUsername(), dbUsername);
        assertEquals(form.getDbPassword(), dbPassword);
        assertEquals(form.getAuditQueue(), auditQueue);
        assertEquals(form.getAuditLogSpaceId(), auditLogSpaceId);
        assertEquals(form.getQueueType(), queueType);
        assertEquals(form.getRabbitmqHost(), rabbitmqHost);
        assertEquals(form.getRabbitmqPort(), rabbitmqPort);
        assertEquals(form.getRabbitmqVhost(), rabbitmqVhost);
        assertEquals(form.getRabbitmqExchange(), rabbitmqExchange);
        assertEquals(form.getRabbitmqUsername(), rabbitmqUsername);
        assertEquals(form.getRabbitmqPassword(), rabbitmqPassword);

    }

    @Test
    public void testEdit() {
        replayAll();
        assertNotNull(this.controller.edit());
    }

    @Test
    public void testUpdateUsingGlobalPropertiesRmqConf() {
        DuracloudMillForm form = createMock(DuracloudMillForm.class);
        BindingResult result = createMock(BindingResult.class);
        Model model = createMock(Model.class);
        RedirectAttributes redirectAttributes = createMock(RedirectAttributes.class);

        expect(result.hasErrors()).andReturn(false);

        expect(form.getGlobalPropsRmqConf()).andReturn(true);
        expect(form.getDbHost()).andReturn(dbHost);
        expect(form.getDbPort()).andReturn(dbPort);
        expect(form.getDbName()).andReturn(dbName);
        expect(form.getDbUsername()).andReturn(dbUsername);
        expect(form.getDbPassword()).andReturn(dbPassword);
        expect(form.getAuditQueue()).andReturn(auditQueue);
        expect(form.getAuditLogSpaceId()).andReturn(auditLogSpaceId);
        expect(form.getQueueType()).andReturn(queueType).times(2);
        expect(form.getRabbitmqExchange()).andReturn(rabbitmqExchange);

        this.service.set(dbHost, dbPort, dbName, dbUsername, dbPassword,
                         auditQueue, auditLogSpaceId, queueType,
                         1L, rabbitmqExchange);
        expectLastCall();

        expect(
            redirectAttributes.addFlashAttribute(isA(String.class),
                                                 isA(Message.class))).andReturn(redirectAttributes);
        expectLastCall();
        replayAll();
        this.controller.update(form, result, model, redirectAttributes);
    }

    @Test
    public void testUpdateUniqueRmqConf() {
        DuracloudMillForm form = createMock(DuracloudMillForm.class);
        BindingResult result = createMock(BindingResult.class);
        Model model = createMock(Model.class);
        RedirectAttributes redirectAttributes = createMock(RedirectAttributes.class);

        expect(result.hasErrors()).andReturn(false);

        expect(form.getGlobalPropsRmqConf()).andReturn(false);
        expect(form.getDbHost()).andReturn(dbHost);
        expect(form.getDbPort()).andReturn(dbPort);
        expect(form.getDbName()).andReturn(dbName);
        expect(form.getDbUsername()).andReturn(dbUsername);
        expect(form.getDbPassword()).andReturn(dbPassword);
        expect(form.getAuditQueue()).andReturn(auditQueue);
        expect(form.getAuditLogSpaceId()).andReturn(auditLogSpaceId);
        expect(form.getQueueType()).andReturn(queueType).times(2);
        expect(form.getRabbitmqExchange()).andReturn(rabbitmqExchange);
        expect(form.getRabbitmqHost()).andReturn(rabbitmqHost);
        expect(form.getRabbitmqPort()).andReturn(rabbitmqPort);
        expect(form.getRabbitmqVhost()).andReturn(rabbitmqVhost);
        expect(form.getRabbitmqUsername()).andReturn(rabbitmqUsername);
        expect(form.getRabbitmqPassword()).andReturn(rabbitmqPassword);

        this.rmqService.set(2L, rabbitmqHost,
                            rabbitmqPort, rabbitmqVhost,
                            rabbitmqUsername, rabbitmqPassword);
        expectLastCall();

        this.service.set(dbHost, dbPort, dbName, dbUsername, dbPassword,
                         auditQueue, auditLogSpaceId, queueType,
                         2L, rabbitmqExchange);
        expectLastCall();

        expect(
            redirectAttributes.addFlashAttribute(isA(String.class),
                                                 isA(Message.class))).andReturn(redirectAttributes);
        expectLastCall();
        replayAll();
        this.controller.update(form, result, model, redirectAttributes);
    }
}
