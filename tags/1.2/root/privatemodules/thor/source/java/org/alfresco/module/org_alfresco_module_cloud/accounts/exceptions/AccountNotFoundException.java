/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_cloud.accounts.exceptions;

/**
 * This exception is thrown when an attempt is made to use an account that does not exist
 * 
 * @author Neil Mc Erlean
 * @since Alfresco Cloud Module 0.1 (Thor)
 */
public class AccountNotFoundException extends AccountServiceException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor to create new AccountNotFoundException with
     * a specified message
     * 
     * @param message The error message
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor to create new AccountNotFoundException with a
     * specified message and error
     *
     * @param message   The error message
     * @param error     The error with the stack trace
     */
    public AccountNotFoundException(String message, Throwable error) {
        super(message, error);
    }
}