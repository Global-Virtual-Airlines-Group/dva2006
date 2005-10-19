package org.deltava.security;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.*;
import javax.naming.directory.*;

import org.apache.log4j.Logger;

import org.deltava.util.ConfigLoader;

/**
 * An authenticator to validate users against an LDAP server.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LDAPAuthenticator implements Authenticator {

	protected static final Logger log = Logger.getLogger(LDAPAuthenticator.class);

	/**
	 * JNDI environment.
	 */
	protected Hashtable _env = new Hashtable();

	/**
	 * Initializes the authenticator.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {

		Properties props = new Properties();
		try {
			props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading " + propsFile + " - " + ie.getMessage());
			throw new SecurityException(ie.getMessage());
		}

		// Initialize the JNDI context
		_env.put(Context.PROVIDER_URL, props.getProperty("jndi.url"));
		_env.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty("jndi.driver"));
		_env.put(Context.SECURITY_PRINCIPAL, props.getProperty("jndi.user"));
		_env.put(Context.SECURITY_CREDENTIALS, props.getProperty("jndi.pwd"));

		// Do a test bind to the context
		try {
			DirContext ctxt = new InitialDirContext(_env);
			ctxt.close();
		} catch (NamingException ne) {
			SecurityException se = new SecurityException("Error validating context - " + ne.getMessage());
			se.initCause(ne);
			throw se;
		}
	}

	/**
	 * Authenticates the user by doing an LDAP bind operation and checking if it succeeded.
	 * @param dn the user's directory name
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails for any reason, contained within the cause of the exception.
	 * @see org.deltava.security.Authenticator#authenticate(java.lang.String, java.lang.String)
	 */
	public void authenticate(String dn, String pwd) throws SecurityException {
		// Create a new environment to connect to the LDAP server
		Hashtable userEnv = (Hashtable) _env.clone();
		userEnv.put(Context.SECURITY_PRINCIPAL, dn);
		userEnv.put(Context.SECURITY_CREDENTIALS, pwd);

		// Do the bind and see what happens
		try {
			DirContext ctxt = new InitialDirContext(userEnv);
			ctxt.close();
			log.info(dn + " authenticated");
		} catch (NamingException ne) {
			log.warn(dn + " Authentication FAILURE - " + ne.getMessage());
			SecurityException se = new SecurityException("Authentication failure for " + dn);
			se.initCause(ne);
			throw se;
		}
	}

	/**
	 * Updates a user's password.
	 * @param dName the fully-qualified directory name
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs
	 */
	public void updatePassword(String dName, String pwd) throws SecurityException {
		log.debug("Updating password for " + dName + " in Directory");

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);
			try {
				SearchControls ctrls = new SearchControls();
				ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
			   ctxt.search(dName, "(objectClass=person)", null);
			} catch (NameNotFoundException nnfe) {
				addUser(dName, pwd); // Add the user entry if not found
				return;
			}
			   
			// Create the modifed password
			Attribute attr = new BasicAttribute("userPassword", pwd);
			ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);

			// Modify the password
			ctxt.modifyAttributes(dName, new ModificationItem[] { mod });
			ctxt.close();
		} catch (NamingException ne) {
			SecurityException se = new SecurityException("Error updating LDAP password");
			se.initCause(ne);
			throw se;
		}
	}

	/**
	 * Adds a User to the Directory.
	 * @param dName the User's fully-qualified Directory name
	 * @param pwd the User's password
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(String dName, String pwd) throws SecurityException {
	   addUser(dName, pwd, null);
	}
	
	/**
	 * Adds a User to the Directory.
	 * @param dName the User's fully-qualified Directory name
	 * @param pwd the User's password
	 * @param userID an alias for the user, or null if none
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(String dName, String pwd, String userID) throws SecurityException {
		log.debug("Adding user " + dName + " to Directory");

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);

			// Create the attributes for the password and other user data
			Attributes attrs = new BasicAttributes("userPassword", pwd);
			attrs.put("objectClass", "person");
			attrs.put("sn", dName.substring(dName.indexOf(' ') + 1, dName.indexOf(',')));
			if (userID != null)
			   attrs.put("uid", userID);

			// Add the user to the directory
			ctxt.bind(dName, null, attrs);
			ctxt.close();
		} catch (NamingException ne) {
			SecurityException se = new SecurityException("Error adding User " + dName);
			se.initCause(ne);
			throw se;
		}
	}

	/**
     * Checks if a particular name exists in the Directory.
     * @param dName the fully-qualified directory name
     * @return TRUE if the user exists, otherwise FALSE
     * @throws SecurityException if an error occurs
     */
	public boolean contains(String dName) throws SecurityException {

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);
			
			// Do the LDAP search
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
			NamingEnumeration ne = ctxt.search(dName, "(objectClass=person)", ctrls);
			boolean isOK = ne.hasMoreElements();
			
			// Close the context
			ctxt.close();
			return isOK;
		} catch (NameNotFoundException nfe) {
			return false;
		} catch (NamingException ne) {
			SecurityException se = new SecurityException("Error searching for User " + dName);
			se.initCause(ne);
			throw se;
		}
	}

	/**
	 * Removes a User from the Directory.
	 * @param dName the User's fully-qualified Directory name
	 * @throws SecurityException if an error occurs
	 */
	public void removeUser(String dName) throws SecurityException {
		log.debug("Removing user " + dName + " from Directory");
		if (!contains(dName))
			throw new SecurityException(dName + " not found");

		// Bind to the Directory and remove
		try {
			DirContext ctxt = new InitialDirContext(_env);
			ctxt.unbind(dName);
			ctxt.close();
		} catch (NamingException ne) {
			SecurityException se = new SecurityException("Error removing User " + dName);
			se.initCause(ne);
			throw se;
		}
	}
	
   /**
    * Renames a user in the Directory.
    * @param oldName the old fully-qualified directory name
    * @param newName the new fully-qualified directory 
    * @throws SecurityException if an error occurs
    */
	public void rename(String oldName, String newName) throws SecurityException {
	   log.debug("Renaming user " + oldName + " to " + newName);
		if (!contains(oldName))
			throw new SecurityException(oldName + " not found");

		// Bind to the Directory and rename
		try {
		   DirContext ctxt = new InitialDirContext(_env);
		   ctxt.rename(oldName, newName);
		   ctxt.close();
		} catch (NamingException ne) {
			SecurityException se = new SecurityException("Error renaming User " + oldName);
			se.initCause(ne);
			throw se;
		}
	}
}