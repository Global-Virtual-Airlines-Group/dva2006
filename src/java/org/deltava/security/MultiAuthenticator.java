// Copyright 2004, 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Person;

import org.deltava.util.*;

/**
 * An abstract Authenticator that supports multiple authenticators.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class MultiAuthenticator implements SQLAuthenticator {

	/**
	 * The &quot;source&quot; authenticator.
	 */
	protected Authenticator _src;

	/**
	 * The &quot;destination&quot; authenticators.
	 */
	protected final Collection<Authenticator> _dst = new LinkedHashSet<Authenticator>();
	
	/**
	 * If either authenticator is a {@link SQLAuthenticator}, an explict connection can be passed down the authenticator
	 * chain to preserve transaction atomicity.
	 */
	protected final ThreadLocal<Connection> _cons = new ThreadLocal<Connection>(); 

	/**
	 * The log4j logger.
	 */
	protected Logger log;

	/**
	 * Initializes the Authentiactor.
	 * @param logClass the log4j log class name
	 */
	protected MultiAuthenticator(Class logClass) {
		super();
		log = Logger.getLogger(logClass);
	}

	/**
	 * Initializes the authenticator.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	protected void init(String propsFile, String authPrefix) throws SecurityException {

		Properties props = new Properties();
		try {
			props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading " + propsFile + " - " + ie.getMessage());
			throw new SecurityException(ie.getMessage());
		}

		// Initialize the source authenticator
		try {
			Class sc = Class.forName(props.getProperty(authPrefix + ".src"));
			_src = (Authenticator) sc.newInstance();
			_src.init(props.getProperty(authPrefix + ".src.properties"));
		} catch (Exception e) {
			SecurityException se = new SecurityException("Error loading Source - " + e.getMessage());
			se.initCause(e);
			throw se;
		}

		// Initialize the destination authenticators
		List<String> classes = StringUtils.split(props.getProperty(authPrefix + ".dst"), ",");
		for (Iterator<String> i = classes.iterator(); i.hasNext();) {
			String cName = i.next();
			try {
				Class dc = Class.forName(cName);
				Authenticator auth = (Authenticator) dc.newInstance();
				auth.init(props.getProperty(authPrefix + ".dst.properties"));
				_dst.add(auth);
			} catch (Exception e) {
				throw new SecurityException("Error loading Destination - " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Provides the JDBC connection for this Authenticator and its children to use.
	 * @param c the Connection to use
	 */
	public void setConnection(Connection c) {
		_cons.set(c);
	}
	
	/**
	 * Clears the explicit JDBC connection for an Authenticator to use, reverting to default behavior.
	 */
	public void clearConnection() {
		_cons.set(null);
	}

	/**
	 * Returns the Source Authenticator.
	 * @return the Source Authenticator
	 */
	public final Authenticator getSource() {
		return _src;
	}

	/**
	 * Returns the Destination Authenticators.
	 * @return the destination Authenticator
	 */
	public final Collection<Authenticator> getDestination() {
		return _dst;
	}
	
	/**
	 * Checks if authenticators are a {@link SQLAuthenticator} and if so passes in an explicit Connection to use.
	 * @param auths the Authenticators to check and update
	 */
	protected void setConnection(Authenticator... auths) {
		for (int x = 0; x < auths.length; x++) {
			Authenticator auth = auths[x];
			Connection con = _cons.get();
			if ((auth instanceof SQLAuthenticator) && (con != null))
				((SQLAuthenticator) auth).setConnection(con);
		}
	}
	
	/**
	 * Checks if authenticators are a {@link SQLAuthenticator} and if so clears the an explicit Connection to use.
	 * @param auths the Authenticators to check and update
	 */
	protected void clearConnection(Authenticator... auths) {
		for (int x = 0; x < auths.length; x++) {
			Authenticator auth = auths[x];
			if (auth instanceof SQLAuthenticator)
				((SQLAuthenticator) auth).clearConnection();
		}
	}

	/**
	 * Synchronizes user information between the source and destination authenticators. If the supplied credentials
	 * cannot be used to authenticate against the destination authenticator, then they are called via an
	 * {@link Authenticator#addUser} or {@link Authenticator#updatePassword} call to syncrhonize the two authenticators.
	 * <i>This should only be called from a subclass' {@link Authenticator#authenticate} method since the credentials
	 * are presumed to be valid in the source authenticator.</i>
	 * @param usr the Person bean
	 * @param pwd the user's password
	 * @throws SecurityException if an error occurs
	 */
	protected void sync(Person usr, String pwd) throws SecurityException {
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext();) {
			Authenticator dst = i.next();
			setConnection(dst);
			try {
				dst.authenticate(usr, pwd);
			} catch (SecurityException se) {
				if (_dst.contains(usr)) {
					log.warn("Updating password for " + usr.getName() + " in " + _dst.getClass().getSimpleName());
					dst.updatePassword(usr, pwd);
				} else {
					dst.addUser(usr, pwd);
				}
			}
			
			clearConnection(dst);
		}
	}
}