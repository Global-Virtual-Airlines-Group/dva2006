// Copyright (c) 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.security;

import org.deltava.beans.Person;

/**
 * An interface for user authenticators.
 * @author Luke
 * @version 1.0
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
     * Checks if a particular name exists in the Directory.
     * @param directoryName the fully-qualified directory name
     * @return TRUE if the user exists, otherwise FALSE
     * @throws SecurityException if an error occurs
     */
    public boolean contains(String directoryName) throws SecurityException;
    
    /**
     * Updates a user's password.
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
    public void addUser(Person usr, String pwd) throws SecurityException;
    
    /**
     * Renames a user in the Directory.
     * @param oldName the old fully-qualified directory name
     * @param newName the new fully-qualified directory 
     * @throws SecurityException if an error occurs
     */
    public void rename(String oldName, String newName) throws SecurityException;
    
    /**
     * Removes a user from the Directory.
     * @param directoryName the fully-qualified directory name
     * @throws SecurityException if an error occurs
     */
    public void removeUser(String directoryName) throws SecurityException;
}