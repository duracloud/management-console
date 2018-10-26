/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import javax.validation.Valid;

import org.duracloud.account.db.model.EmailTemplate;
import org.duracloud.account.db.util.EmailTemplateService;
import org.duracloud.account.util.UserFeedbackUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Daniel Bernstein
 * Date: 01/05/2015
 */
@Controller
@RequestMapping(NotificationsController.BASE_MAPPING)
public class NotificationsController {

    public static final String BASE_MAPPING = "/root/notifications";

    private static final String EMAIL_TEMPLATE_FORM = "emailTemplateForm";
    private static final String EMAIL_TEMPLATE = "emailTemplate";

    @Autowired
    private EmailTemplateService emailTemplateService;

    /**
     * @return
     */
    @RequestMapping("")
    public ModelAndView get() {
        ModelAndView mav = new ModelAndView(BASE_MAPPING);
        mav.addObject("emailTemplates", emailTemplateService.list());
        return mav;
    }

    @RequestMapping(value = "/edit/{templateId}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable Long templateId) {
        EmailTemplate emailTemplate = emailTemplateService.getTemplate(templateId);
        EmailTemplateForm form = new EmailTemplateForm();
        form.setSubject(emailTemplate.getSubject());
        form.setBody(emailTemplate.getBody());

        return
            new ModelAndView(BASE_MAPPING + "/edit").addObject(EMAIL_TEMPLATE_FORM, form)
                                                    .addObject(EMAIL_TEMPLATE, emailTemplate);
    }

    @RequestMapping(value = "/edit/{templateId}", method = RequestMethod.POST)
    @Transactional
    public ModelAndView update(@PathVariable long templateId,
                               @ModelAttribute(EMAIL_TEMPLATE_FORM) @Valid EmailTemplateForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        EmailTemplate template = emailTemplateService.getTemplate(templateId);
        boolean hasErrors = bindingResult.hasErrors();
        if (hasErrors) {
            return new ModelAndView(BASE_MAPPING + "/edit").addObject(EMAIL_TEMPLATE_FORM, form)
                                                           .addObject(EMAIL_TEMPLATE, template);
        }

        this.emailTemplateService.update(templateId, form.getSubject(), form.getBody());
        UserFeedbackUtil.addSuccessFlash("Successfully updated email template!", redirectAttributes);
        return new ModelAndView(new RedirectView(BASE_MAPPING, true));
    }

}
