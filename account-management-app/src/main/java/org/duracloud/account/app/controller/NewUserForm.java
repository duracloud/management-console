/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.controller;

import org.duracloud.account.annotation.UsernameConstraint;
import org.duracloud.common.annotation.FieldMatch;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @contributor "Daniel Bernstein (dbernstein@duraspace.org)"
 */
@FieldMatch(first = "password", second = "passwordConfirm", message = "Password confirmation does not match.")
public class NewUserForm {
    @Length(min = 4, max = 20, message = "Username must be between 4 and 20 characters in length.")
    @NotBlank(message = "Username is empty.")
    @UsernameConstraint
    private String username;

    @NotEmpty(message = "Password must be specified.")
    private String password;

    private String passwordConfirm;

    @NotBlank(message = "First name is empty.")
    private String firstName;

    @NotBlank(message = "Last name is empty.")
    private String lastName;

    @NotEmpty(message = "Email must be specified.")
    @Email(message = "Email is invalid.")
    private String email;

    @NotBlank(message = "Security question is empty.")
    private String securityQuestion;

    @NotBlank(message = "Security answer is empty.")
    private String securityAnswer;

    private String redemptionCode;
    private String recaptchaResponse;
    public String getRecaptchaResponse() {
        return recaptchaResponse;
    }

    public void setRecaptchaResponse(String recaptchaResponse) {
        this.recaptchaResponse = recaptchaResponse;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setRedemptionCode(String redemptionCode) {
        this.redemptionCode = redemptionCode;
    }

    public String getRedemptionCode() {
        return redemptionCode;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }
}
