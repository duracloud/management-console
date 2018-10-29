/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.model;

import java.util.Set;

public class User implements Comparable<User> {
    public User(
            Long id, String username, String firstName, String lastName,
            String email, String allowableIPAddressRange, Set<Account> accounts, boolean root) {
        super();
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.allowableIPAddressRange = allowableIPAddressRange;
        this.accounts = accounts;
        this.root = root;
    }

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Set<Account> accounts;
    private String allowableIPAddressRange;
    private boolean root = false;

    public Long getId() {
        return id;
    }

    public boolean isRoot() {
        return root;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    @Override
    public int compareTo(User o) {
        return this.getUsername().compareTo(o.getUsername());
    }

    public String getAllowableIPAddressRange() {
        return allowableIPAddressRange;
    }

}