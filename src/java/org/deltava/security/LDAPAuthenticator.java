// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.io.IOException;

import javax.naming.*;
import javax.naming.directory.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.util.*;

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
	protected final Hashtable<String, String> _env = new Hashtable<String, String>();

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
			throw new SecurityException("Error validating context - " + ne.getMessage(), ne);
		}
	}

	/**
	 * Authenticates the user by doing an LDAP bind operation and checking if it succeeded.
	 * @param usr the user bean
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails for any reason, contained within the cause of the exception.
	 * @see org.deltava.security.Authenticator#authenticate(Person, String)
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {
		// Create a new environment to connect to the LDAP server
		Hashtable<String, String> userEnv = new Hashtable<String, String>(_env);
		userEnv.put(Context.SECURITY_PRINCIPAL, usr.getDN());
		userEnv.put(Context.SECURITY_CREDENTIALS, pwd);

		// Do the bind and see what happens
		try {
			DirContext ctxt = new InitialDirContext(userEnv);
			ctxt.close();
			log.info(usr.getName() + " authenticated");
		} catch (NamingException ne) {
			log.warn(usr.getDN() + " Authentication FAILURE - " + ne.getMessage());
			throw new SecurityException("Authentication failure for " + usr.getDN(), ne);
		}
	}

	/**
	 * Updates a user's password.
	 * @param usr the user bean
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		log.debug("Updating password for " + usr.getName() + " in Directory");

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);
			try {
				SearchControls ctrls = new SearchControls();
				ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
			   ctxt.search(usr.getDN(), "(objectClass=person)", null);
			} catch (NameNotFoundException nnfe) {
				addUser(usr, pwd); // Add the user entry if not found
				return;
			}
			   
			// Create the modifed password
			Attribute attr = new BasicAttribute("userPassword", pwd);
			ModificationItem mod = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);

			// Modify the password
			ctxt.modifyAttributes(usr.getDN(), new ModificationItem[] { mod });
			ctxt.close();
		} catch (NamingException ne) {
			throw new SecurityException("Error updating password - " + ne.getMessage(), ne);
		}
	}

	/**
	 * Adds a User to the Directory.
	 * @param usr the user bean
	 * @param pwd the User's password
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(Person usr, String pwd) throws SecurityException {
		log.debug("Adding user " + usr.getDN() + " to Directory");

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);

			// Create the attributes for the password and other user data
			Attributes attrs = new BasicAttributes("userPassword", pwd);
			attrs.put("objectClass", "person");
			attrs.put("sn", usr.getLastName());
			if (usr instanceof Pilot) {
				Pilot p = (Pilot) usr;
				if (p.getLDAPName() != null)
					attrs.put("uid", p.getLDAPName());
			}

			// Add the user to the directory
			ctxt.bind(usr.getDN(), null, attrs);
			ctxt.close();
		} catch (NamingException ne) {
			throw new SecurityException("Error adding User " + usr.getDN() + " - " + ne.getMessage(), ne);
		}
	}

	/**
     * Checks if a particular name exists in the Directory.
     * @param usr the user bean
     * @return TRUE if the user exists, otherwise FALSE
     * @throws SecurityException if an error occurs
     */
	public boolean contains(Person usr) throws SecurityException {
		try {
			DirContext ctxt = new InitialDirContext(_env);
			
			// Do the LDAP search
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
			NamingEnumeration ne = ctxt.search(usr.getDN(), "(objectClass=person)", ctrls);
			boolean isOK = ne.hasMoreElements();
			
			// Close the context
			ctxt.close();
			return isOK;
		} catch (NameNotFoundException nfe) {
			return false;
		} catch (NamingException ne) {
			throw new SecurityException("Error searching for User " + usr.getDN(), ne);
		}
	}
	
	/**
	 * This Authenticator accepts all users with a valid Directory Name.
	 * @return TRUE if the user's DN property is not empty
	 */
	public boolean accepts(Person usr) {
		return ((usr != null) && (!StringUtils.isEmpty(usr.getDN())));
	}

	/**
	 * Removes a User from the Directory.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	public void removeUser(Person usr) throws SecurityException {
		log.debug("Removing user " + usr.getDN() + " from Directory");
		if (!contains(usr))
			throw new SecurityException(usr.getDN() + " not found");

		// Bind to the Directory and remove
		try {
			DirContext ctxt = new InitialDirContext(_env);
			ctxt.unbind(usr.getDN());
			ctxt.close();
		} catch (NamingException ne) {
			throw new SecurityException("Error removing User " + usr.getDN() + " - " + ne.getMessage(), ne);
		}
	}
	
   /**
    * Renames a user in the Directory.
    * @param usr the user bean
    * @param newName the new fully-qualified directory 
    * @throws SecurityException if an error occurs
    */
	public void rename(Person usr, String newName) throws SecurityException {
		if (usr.getDN().equalsIgnoreCase(newName))
		   return;
		
	   log.debug("Renaming user " + usr.getDN() + " to " + newName);
		if (!contains(usr))
			throw new SecurityException(usr.getDN() + " not found");

		// Bind to the Directory and rename
		try {
		   DirContext ctxt = new InitialDirContext(_env);
		   ctxt.rename(usr.getDN(), newName);
		   ctxt.close();
		} catch (NamingException ne) {
			throw new SecurityException("Error renaming User " + usr.getDN(), ne);
		}
	}
}