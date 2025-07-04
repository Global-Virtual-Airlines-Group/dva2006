// Copyright 2005, 2006, 2007, 2009, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.io.IOException;

import javax.naming.*;
import javax.naming.directory.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.util.*;

/**
 * An authenticator to validate users against an LDAP server.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class LDAPAuthenticator implements Authenticator {

	protected static final Logger log = LogManager.getLogger(LDAPAuthenticator.class);

	/**
	 * JNDI environment.
	 */
	protected final Hashtable<String, String> _env = new Hashtable<String, String>();

	/**
	 * Initializes the authenticator.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void init(String propsFile) throws SecurityException {

		Properties props = new Properties();
		try {
			props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading {} - {}", propsFile, ie.getMessage());
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
	@Override
	public void authenticate(Person usr, String pwd) throws SecurityException {
		// Create a new environment to connect to the LDAP server
		Hashtable<String, String> userEnv = new Hashtable<String, String>(_env);
		userEnv.put(Context.SECURITY_PRINCIPAL, usr.getDN());
		userEnv.put(Context.SECURITY_CREDENTIALS, pwd);

		// Do the bind and see what happens
		try {
			DirContext ctxt = new InitialDirContext(userEnv);
			ctxt.close();
			log.info("{} authenticated", usr.getName());
		} catch (NamingException ne) {
			log.warn("{} Authentication FAILURE - {}", usr.getDN(), ne.getMessage());
			throw new SecurityException("Authentication failure for " + usr.getDN(), ne);
		}
	}

	/**
	 * Updates a user's password.
	 * @param usr the user bean
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		log.debug("Updating password for {} in Directory", usr.getName());

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);
			try {
				SearchControls ctrls = new SearchControls();
				ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
			   ctxt.search(usr.getDN(), "(objectClass=person)", null);
			} catch (NameNotFoundException nnfe) {
				add(usr, pwd); // Add the user entry if not found
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
	@Override
	public void add(Person usr, String pwd) throws SecurityException {
		log.debug("Adding user {} to Directory", usr.getDN());

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);

			// Create the attributes for the password and other user data
			Attributes attrs = new BasicAttributes("userPassword", pwd);
			attrs.put("objectClass", "person");
			attrs.put("sn", usr.getLastName());
			if (usr instanceof Pilot p) {
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
	@Override
	public boolean contains(Person usr) throws SecurityException {
		try {
			DirContext ctxt = new InitialDirContext(_env);
			
			// Do the LDAP search
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
			NamingEnumeration<?> ne = ctxt.search(usr.getDN(), "(objectClass=person)", ctrls);
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
	@Override
	public boolean accepts(Person usr) {
		return ((usr != null) && (!StringUtils.isEmpty(usr.getDN())));
	}
	
	/**
	 * Disables a user's account. <i>This deletes the User.</i>
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void disable(Person usr) throws SecurityException {
		remove(usr);
	}

	/**
	 * Removes a User from the Directory.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void remove(Person usr) throws SecurityException {
		log.debug("Removing user {} from Directory", usr.getDN());
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
	@Override
	public void rename(Person usr, String newName) throws SecurityException {
		if (usr.getDN().equalsIgnoreCase(newName))
		   return;
		
	   log.debug("Renaming user {} to {}", usr.getDN(), newName);
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