package org.deltava.security;

/**
 * An interface for user authenticators. Authenticators are used only to validate user passwords, are are not used
 * for standard Directory operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Authenticator extends java.io.Serializable {
   
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
     * @param directoryName the fully-qualified directory name
     * @param pwd the password
     * @throws SecurityException if the authentication failed for any reason
     */
    public void authenticate(String directoryName, String pwd) throws SecurityException;
    
    /**
     * Checks if a particular name exists in the Directory.
     * @param directoryName the fully-qualified directory name
     * @return TRUE if the user exists, otherwise FALSE
     * @throws SecurityException if an error occurs
     */
    public boolean contains(String directoryName) throws SecurityException;
    
    /**
     * Updates a user's password.
     * @param directoryName the fully-qualified directory name
     * @param pwd the new password
     * @throws SecurityException if an error occurs
     */
    public void updatePassword(String directoryName, String pwd) throws SecurityException;

    /**
     * Adds a user to the Directory.
     * @param directoryName the fully-qualified directory name
     * @param pwd the user's password
     * @throws SecurityException if an error occurs
     * @see Authenticator#addUser(String, String, String)
     */
    public void addUser(String directoryName, String pwd) throws SecurityException;
    
    /**
     * Adds a user to the Directory.
     * @param directoryName the fully-qualified directory name
     * @param pwd the user's password
     * @param userID an alias for the user
     * @throws SecurityException if an error occurs
     * @see Authenticator#addUser(String, String)
     */
    public void addUser(String directoryName, String pwd, String userID) throws SecurityException;
    
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