// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security;

import java.io.IOException;
import java.util.Properties;

import javax.naming.*;
import javax.naming.directory.*;

import org.deltava.util.ConfigLoader;

/**
 * An authenticator to validate users against a Domino LDAP server.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DominoAuthenticator extends LDAPAuthenticator {
	
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
		_env.put(Context.PROVIDER_URL, props.getProperty("jndi.domino.url"));
		_env.put(Context.INITIAL_CONTEXT_FACTORY, props.getProperty("jndi.domino.driver"));
		_env.put(Context.SECURITY_PRINCIPAL, props.getProperty("jndi.domino.user"));
		_env.put(Context.SECURITY_CREDENTIALS, props.getProperty("jndi.domino.pwd"));

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
     * Checks if a particular name exists in the Directory.
     * @param dName the fully-qualified directory name
     * @return TRUE if the user exists, otherwise FALSE
     * @throws SecurityException if an error occurs
     */
	public boolean contains(String dName) throws SecurityException {

		// Bind to the directory
		try {
			DirContext ctxt = new InitialDirContext(_env);
			
			// Implement the LDAP search
			NamingEnumeration ne = ctxt.search("", "(" + dName + ")", null);
			boolean isOK = ne.hasMoreElements();
			
			ctxt.close();
			return isOK;
		} catch (NamingException ne) {
			SecurityException se = new SecurityException("Error searching for User " + dName);
			se.initCause(ne);
			throw se;
		}
	}
}