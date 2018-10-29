/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.model;

import org.duracloud.account.db.model.Role;

public class Account implements Comparable<Account> {
    public Account(
            Long id, String accountName, String subdomain, Role role) {
        super();
        this.id = id;
        this.accountName = accountName;
        this.subdomain = subdomain;
        this.role = role;
    }

    private Long id;
    private String accountName;
    private String subdomain;
    private Role role;

    public Long getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public int compareTo(Account a) {
        return this.getAccountName().compareTo(a.getAccountName());
    }
}