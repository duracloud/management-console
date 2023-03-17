/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.model.Role;
import org.duracloud.account.db.util.DuracloudUserService;
import org.duracloud.account.util.UserFeedbackUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Base class for all controllers.
 *
 * @contributor "Daniel Bernstein (dbernstein@duraspace.org)"
 */

public abstract class AbstractController {

    protected Logger log = LoggerFactory.getLogger(AbstractController.class);

    @Value("${recaptcha.enabled:false}")
    private boolean recaptchaEnabled;

    @Value("${recaptcha.siteKey:placeholderSiteKey}")
    private String recaptchaSiteKey;

    @Value("${recaptcha.secret:placeholderSecret}")
    private String recaptchaSecret;

    public static final String USERS_MAPPING = "/users";
    public static final String USER_MAPPING = "/byid/{username:[a-z0-9.\\-_@]*}";

    public static final String NEW_MAPPING = "/new";
    public static final String EDIT_MAPPING = "/edit";

    public static final String BY_ID_MAPPING = "/byid/{id}";
    public static final String BY_ID_EDIT_MAPPING = BY_ID_MAPPING + EDIT_MAPPING;
    public static final String BY_ID_DELETE_MAPPING = BY_ID_MAPPING + "/delete";

    public void init() {
        log.info("initializing " + this.toString());
    }

    public void destroy() {
        log.info("destroying " + this.toString());
    }

    @ModelAttribute("ownerRole")
    public Role getOwnerRole() {
        return Role.ROLE_OWNER;
    }

    @ModelAttribute("adminRole")
    public Role getAdminRole() {
        return Role.ROLE_ADMIN;
    }

    @ModelAttribute("userRole")
    public Role getUserRole() {
        return Role.ROLE_USER;
    }

    @ModelAttribute("recaptchaSiteKey")
    public String getRecaptchaSiteKey() {
        return recaptchaSiteKey;
    }

    @ModelAttribute("recaptchaEnabled")
    protected boolean isRecaptchaEnabled() {
        return recaptchaEnabled;
    }

    public String getRecaptchaSecret() {
        return recaptchaSecret;
    }

    protected void setUserRights(DuracloudUserService userService,
                                 Long accountId, Long userId, Role role) {
        Set<Role> roles = role.getRoleHierarchy();

        userService.setUserRights(accountId,
                                  userId,
                                  roles.toArray(new Role[roles.size()]));
    }

    /**
     * @param users
     * @return
     */
    protected boolean accountHasMoreThanOneOwner(
        Set<DuracloudUser> users, Long accountId) {
        int ownerCount = 0;
        for (DuracloudUser u : users) {
            if (u.isOwnerForAcct(accountId) && !u.isRoot()) {
                ownerCount++;
                if (ownerCount > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        log.error(e.getMessage(), e);
        return new ModelAndView("exception", "ex", e);
    }

    /*
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        setError(e);
        String username =
            SecurityContextHolder.getContext().getAuthentication().getName();
        return createRedirectMav(UserController.formatUserUrl(username));
    }
    */

    protected static String formatUserUrl(String username) {
        String url = USERS_MAPPING + USER_MAPPING;
        url = url.replaceAll("\\{username.*\\}", username);
        return url;
    }

    protected void setError(Exception e) {
        setError(e, null);
    }

    protected void setError(Exception e, RedirectAttributes redirectAttributes) {
        log.error(e.getMessage(), e);
        UserFeedbackUtil.addFailureFlash(e.getMessage(), redirectAttributes);
    }

    protected ModelAndView createRedirectMav(String url) {
        RedirectView view = new RedirectView(url, true);
        view.setExposeModelAttributes(false);
        return new ModelAndView(view);
    }

    protected void setSuccessFeedback(String message, RedirectAttributes redirectAttributes) {
        UserFeedbackUtil.addSuccessFlash(message, redirectAttributes);
    }

    protected void setFailureFeedback(String message, RedirectAttributes redirectAttributes) {
        UserFeedbackUtil.addFailureFlash(message, redirectAttributes);
    }

    protected boolean validateRecaptcha(final String recaptchaResponse) throws IOException {
        log.info("recaptcha response from form = {}", recaptchaResponse);
        boolean success = false;
        final HttpPost httpPost = new HttpPost("https://www.google.com/recaptcha/api/siteverify");
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("response", recaptchaResponse));
        params.add(new BasicNameValuePair("secret", getRecaptchaSecret()));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = (CloseableHttpResponse) client
                     .execute(httpPost)) {
            var statusLine = response.getStatusLine();
            log.info("http status from recaptcha = {}", statusLine.toString());
            var jsonStr = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            log.info("http response body from recaptcha = {}", jsonStr);
            final int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                var map = new ObjectMapper().readValue(jsonStr, Map.class);
                success = (boolean) map.get("success");

            }
        }
        return success;
    }
}
