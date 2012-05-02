/*
 * Copyright (c) 2009-2012 DuraSpace. All rights reserved.
 */
package org.duracloud.account.util.error;

/**
 * 
 * @author Daniel Bernstein
 *         Date: January 22, 2012
 *
 */
public class ReservedPrefixException extends InvalidUsernameException{

    private static final long serialVersionUID = 1L;
    
    public ReservedPrefixException(String username) {
        super(username + " is a reserved prefix.");
    }
}