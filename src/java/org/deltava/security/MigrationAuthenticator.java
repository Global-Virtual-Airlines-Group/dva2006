// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.deltava.util.ConfigLoader;

/**
 * An Authenticator used to migrate user data from one Directory to another. When a user is sucessfully authenticated by
 * the first (&quot;source&quot;) authenticator, the directory name and password are written into the second
 * (&quot;destination&quot;) authenticator and deleted from the source.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MigrationAuthenticator implements Authenticator {

	private static final Logger log = Logger.getLogger(MigrationAuthenticator.class);

	private Authenticator _src;
	private Authenticator _dst;

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

		// Initialize the source authenticator
		try {
			Class sc = Class.forName(props.getProperty("migration.src"));
			_src = (Authenticator) sc.newInstance();
			_src.init(props.getProperty("migration.src.properties"));
		} catch (Exception e) {
			SecurityException se = new SecurityException("Error loading Source - " + e.getMessage());
			se.initCause(e);
			throw se;
		}

		// Initialize the destination authenticator
		try {
			Class dc = Class.forName(props.getProperty("migration.dst"));
			_dst = (Authenticator) dc.newInstance();
			_dst.init(props.getProperty("migration.dst.properties"));
		} catch (Exception e) {
			SecurityException se = new SecurityException("Error loading Destination - " + e.getMessage());
			se.initCause(e);
			throw se;
		}
	}

	/**
	 * Authenticates the user. This authenticator will check the destination Directory to see if it contains an entry
	 * for the user. If it does, then authentication proceeds normally. If not, then the credentials are authenticated
	 * against the source Directory. If authentication succeeds, they are used to add credentials to the destnation
	 * Directory.
	 * @param directoryName the user's directory name
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails for any reason, contained within the cause of the exception.
	 * @see Authenticator#authenticate(String, String)
	 */
	public void authenticate(String directoryName, String pwd) throws SecurityException {

		// Figure out which authenticator to use
		Authenticator auth = _dst.contains(directoryName) ? _dst : _src;
		auth.authenticate(directoryName, pwd);

		// If we got this far, and we're not in the destination directory, then add us
		if (auth == _src)
			_dst.addUser(directoryName, pwd);
	}

	/**
	 * Checks if a particular name exists in either Directory.
	 * @param directoryName the fully-qualified directory name
	 * @return TRUE if the user exists in either Authenticator, otherwise FALSE
	 * @throws SecurityException if an error occurs
	 */
	public boolean contains(String directoryName) throws SecurityException {
		return _src.contains(directoryName) || _dst.contains(directoryName);
	}

	/**
	 * Updates a user's password. This user must be present in the destination directory.
	 * @param directoryName the fully-qualified directory name
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs, or the user is not in the Destination directory
	 */
	public void updatePassword(String directoryName, String pwd) throws SecurityException {
		_dst.updatePassword(directoryName, pwd);
	}

	/**
	 * Adds a User to the Destination Directory.
	 * @param directoryName the User's fully-qualified Directory name
	 * @param pwd the User's password
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(String directoryName, String pwd) throws SecurityException {
		_dst.addUser(directoryName, pwd);
	}

	/**
	 * Removes a User from the Directory. This will remove from the first directory containing the user, first the
	 * Destination, then the Source.
	 * @param directoryName the User's fully-qualified Directory name
	 * @throws SecurityException if an error occurs or the user does not exist
	 */
	public void removeUser(String directoryName) throws SecurityException {
		if (_dst.contains(directoryName)) {
			_dst.removeUser(directoryName);
		} else if (_src.contains(directoryName)) {
			_src.removeUser(directoryName);
		} else {
			throw new SecurityException("Unknown User - " + directoryName);
		}
	}

	/**
	 * Returns the Source Authenticator.
	 * @return the Source Authenticator
	 */
	public Authenticator getSource() {
		return _src;
	}

	/**
	 * Returns the Destination Authenticator.
	 * @return the destination Authenticator
	 */
	public Authenticator getDestination() {
		return _dst;
	}
}