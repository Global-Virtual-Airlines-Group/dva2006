// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import org.deltava.beans.Person;

/**
 * An interface for user authenticators.
 * @author Luke
 * @version 2.0
 * @since 1.0
 */

public interface Authenticator {
   
   /**
    * Default properties file used for authenticator configuration options.
    */
   public static final String DEFAULT_PROPS_FILE = "/etc/auth.properties";
   
   /**
    * Initializes the authenticator.
    * @param propsFile the properties file to use
    * @throws SecurityException if an error occurs
    */
   public void init(String propsFile) throws SecurityException;

    /**
     * Authenticate a particular user given a set of credentials.
     * @param usr the user bean
     * @param pwd the password
     * @throws SecurityException if the authentication failed for any reason
     */
    public void authenticate(Person usr, String pwd) throws SecurityException;
    
    /**
     * Checks if a particular user exists in the Directory.
     * @param usr the user bean
     * @return TRUE if the user exists, otherwise FALSE
     * @throws SecurityException if an error occurs
     */
    public boolean contains(Person usr) throws SecurityException;
    
    /**
     * Checks if a particular user should exist within a Directory. Not all Authenticators will
     * include all users, and to avoid errors in {@link MultiAuthenticator} implementations,
     * this method is included to validate which authenticators credentials should be
     * cascaded to.
     * @param usr the user bean
     * @return TRUE if the user will be added to the Directory if requested, otherwise FALSE
     */
    public boolean accepts(Person usr);
    
    /**
     * Updates a user's password. If the authenticator supports the {@link Authenticator#accepts(Person)}
     * method, then the user account should be re-enabled.
     * @param usr the user bean
     * @param pwd the new password
     * @throws SecurityException if an error occurs
     */
    public void updatePassword(Person usr, String pwd) throws SecurityException;

    /**
     * Adds a user to the Directory.
     * @param usr the user bean
     * @param pwd the user's password
     * @throws SecurityException if an error occurs
     */
    public void add(Person usr, String pwd) throws SecurityException;
    
    /**
     * Disables a user's account in the Directory. This is an optional operation, if an implementation
     * does not support this operation it should call {@link Authenticator#remove(Person)} instead.
     * @param usr the user bean
     * @throws SecurityException if an error occurs
     */
    public void disable(Person usr) throws SecurityException;
    
    /**
     * Renames a user in the Directory.
     * @param usr the user bean
     * @param newName the new fully-qualified directory 
     * @throws SecurityException if an error occurs
     */
    public void rename(Person usr, String newName) throws SecurityException;
    
    /**
     * Removes a user from the Directory.
     * @param usr the user bean
     * @throws SecurityException if an error occurs
     */
    public void remove(Person usr) throws SecurityException;
}