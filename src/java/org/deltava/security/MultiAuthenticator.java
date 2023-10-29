// Copyright 2004, 2005, 2006, 2007, 2010, 2015, 2016, 2017, 2018, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.Person;
import org.deltava.util.*;

/**
 * An abstract Authenticator that supports multiple authenticators.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public abstract class MultiAuthenticator extends SQLAuthenticator {

	/**
	 * The &quot;source&quot; authenticator.
	 */
	protected Authenticator _src;

	/**
	 * The &quot;destination&quot; authenticators.
	 */
	protected final SequencedCollection<Authenticator> _dst = new LinkedHashSet<Authenticator>();

	/**
	 * The log4j logger.
	 */
	protected final Logger log;

	/**
	 * Initializes the Authentiactor.
	 * @param logClass the log4j log class name
	 */
	protected MultiAuthenticator(Class<?> logClass) {
		super();
		log = LogManager.getLogger(logClass);
	}

	/**
	 * Initializes the authenticator.
	 * @param propsFile the properties file to use
	 * @param authPrefix the property prefix
	 * @throws SecurityException if an error occurs
	 */
	protected void init(String propsFile, String authPrefix) throws SecurityException {

		Properties props = new Properties();
		try (InputStream in = ConfigLoader.getStream(propsFile)) {
			props.load(in);
		} catch (IOException ie) {
			throw new SecurityException(ie.getMessage());
		}

		// Initialize the source authenticator
		try {
			Class<?> sc = Class.forName(props.getProperty(authPrefix + ".src"));
			_src = (Authenticator) sc.getDeclaredConstructor().newInstance();
			_src.init(props.getProperty(authPrefix + ".src.properties"));
		} catch (Exception e) {
			throw new SecurityException("Error loading Source - " + e.getMessage(), e);
		}

		// Initialize the destination authenticators
		List<String> classes = StringUtils.split(props.getProperty(authPrefix + ".dst"), ",");
		for (String cName : classes) {
			try {
				Class<?> dc = Class.forName(cName);
				Authenticator auth = (Authenticator) dc.getDeclaredConstructor().newInstance();
				auth.init(props.getProperty(authPrefix + ".dst.properties"));
				_dst.add(auth);
			} catch (Exception e) {
				throw new SecurityException("Error loading Destination - " + e.getMessage(), e);
			}
		}
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
			if (auth instanceof SQLAuthenticator sa) {
				Connection con = getConnection();
				if (con != null)
					sa.setConnection(con);
			}
		}
	}

	/**
	 * Checks if authenticators are a {@link SQLAuthenticator} and if so clears the an explicit Connection to use.
	 * @param auths the Authenticators to check and update
	 */
	protected static void clearConnection(Authenticator... auths) {
		for (int x = 0; x < auths.length; x++) {
			auths[x].close();
		}
	}

	/**
	 * Synchronizes user information between the source and destination authenticators. If the supplied credentials
	 * cannot be used to authenticate against the destination authenticator, then they are called via an
	 * {@link Authenticator#add} or {@link Authenticator#updatePassword} call to syncrhonize the two authenticators.
	 * <i>This should only be called from a subclass' {@link Authenticator#authenticate} method since the credentials
	 * are presumed to be valid in the source authenticator.</i>
	 * @param usr the Person bean
	 * @param pwd the user's password
	 * @throws SecurityException if an error occurs
	 */
	protected void sync(Person usr, String pwd) throws SecurityException {
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext();) {
			Authenticator dst = i.next();
			String authName = dst.getClass().getSimpleName();
			setConnection(dst);
			if (dst.accepts(usr)) {
				try {
					if (log.isDebugEnabled())
						log.debug("Validating " + usr.getName() + " credentials in " + authName);

					dst.authenticate(usr, pwd);
				} catch (SecurityException se) {
					if (dst.contains(usr)) {
						log.warn("Updating password for " + usr.getName() + " in " + authName);
						dst.updatePassword(usr, pwd);
					} else {
						log.warn("Adding " + usr.getName() + " in " + authName);
						dst.add(usr, pwd);
					}
				}
			} else {
				try {
					if (dst.contains(usr)) {
						log.warn(authName + " contains " + usr.getName() + ", removing");
						dst.remove(usr);
					}
				} catch (SecurityException se) {
					log.warn("Error removing " + usr.getName() + " from " + authName + " - " + se.getMessage());
				}
			}

			clearConnection(dst);
		}
	}
	
	/**
	 * Removes the specified user from any destination Authenticators, while retaining the user in the source directory.
	 * @param usr the user bean
	 * @throws SecurityException if the user does not exist
	 */
	public void removeDestination(Person usr) throws SecurityException {
		for (Authenticator auth : _dst) {
			setConnection(auth);
			if (auth.contains(usr))
				auth.remove(usr);
			
			clearConnection(auth);
		}
	}

	/**
	 * Returns whether this Authenticator will accept a new User. This defaults to TRUE, although subclasses may override this default.
	 * @return TRUE always
	 */
	@Override
	public boolean accepts(Person usr) {
		return true;
	}
}