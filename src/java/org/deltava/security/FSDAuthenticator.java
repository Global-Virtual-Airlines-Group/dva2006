// Copyright 2007, 2009, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.servinfo.Rating;

import org.deltava.util.*;

/**
 * An Authenticator to read/write from FSD certificate files.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FSDAuthenticator implements Authenticator {

	private static final Logger log = Logger.getLogger(FSDAuthenticator.class);

	private final Properties _props = new Properties();
	private File _certFile;
	private final Map<Integer, FSDCert> _certs = Collections.synchronizedMap(new LinkedHashMap<Integer, FSDCert>());
	private final Map<String, Rating> _roleLevels = new HashMap<String, Rating>();

	private static final String[] LEVEL_NAMES = { "Disabled", "Pilot", "Student 1", "Student 2", "Student 3", "Controller 1", "Controller 2", "Controller 3", "Instructor 1", "Instructor 2", "Instructor 3", "Supervisor", "Administrator" };

	private class FSDCert extends DatabaseBean {

		private final String _userID;
		private String _pwd;
		private final Rating _level;

		FSDCert(String userID, String pwd, Rating level) {
			super();
			_userID = userID.toUpperCase();
			_pwd = pwd;
			_level = level;
		}

		public String getUserID() {
			return _userID;
		}

		public String getPassword() {
			return _pwd;
		}

		public Rating getLevel() {
			return _level;
		}

		public void setPassword(String pwd) {
			_pwd = pwd;
		}

		@Override
		public int hashCode() {
			return _userID.hashCode();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(getUserID());
			buf.append(' ');
			buf.append(_pwd);
			buf.append(' ');
			buf.append(getLevel());
			if (getID() > 0) {
				buf.append(" ;");
				buf.append(String.valueOf(getID()));
			}

			return buf.toString();
		}
	}

	/**
	 * Initialize the Authenticator and populate its user list from a file.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void init(String propsFile) throws SecurityException {

		// Load properties file
		_props.clear();
		try {
			_props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading " + propsFile + " - " + ie.getMessage());
			throw new SecurityException(ie.getMessage());
		}

		// Load the role mappings
		for (Rating r : Rating.values()) {
			if (r == Rating.DISABLED) continue;
			String roleNames = _props.getProperty("fsd.level." + String.valueOf(r.ordinal()), "");
			Collection<String> roles = StringUtils.split(roleNames, ",");
			for (Iterator<String> i = roles.iterator(); i.hasNext();)
				_roleLevels.put(i.next(), r);
		}

		// Check for the certificate file
		_certFile = new File(_props.getProperty("fsd.cert"));
		if (!_certFile.exists()) {
			log.warn("Cannot open " + _certFile.getPath() + " - not found");
			return;
		}

		// Load the certificates
		try (LineNumberReader br = new LineNumberReader(new FileReader(_certFile))) {
			while (br.ready()) {
				String data = br.readLine();
				if (!data.startsWith(";")) {
					StringTokenizer tkns = new StringTokenizer(data, " ");
					if (tkns.countTokens() < 4)
						log.warn("Invalid token count on Line " + br.getLineNumber() + " tokens=" + tkns.countTokens());
					else {
						String userID = tkns.nextToken();
						try {
							Rating rt = Rating.values()[StringUtils.parse(tkns.nextToken(), 1)];
							FSDCert cert = new FSDCert(userID, tkns.nextToken(), rt);
							String dbID = tkns.nextToken().substring(1);
							cert.setID(StringUtils.parse(dbID, 0));
							_certs.put(Integer.valueOf(cert.getID()), cert);
						} catch (IllegalArgumentException iae) {
							log.warn("Invalid database ID for " + userID + " - " + iae.getMessage());
						}
					}
				}
			}
		} catch (IOException ie) {
			log.warn("Error loading " + _props.getProperty("fsd.cert") + " - " + ie.getMessage());
		}

		log.info("Loaded " + _certs.size() + " user records");
	}

	/**
	 * Authenticates the user by searching for the directory name and then comparing the existing password on file to
	 * the one specified. Even if the credentials match, authentication will fail if the user's roles do not translate
	 * to at least Level 1.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails
	 */
	@Override
	public void authenticate(Person usr, String pwd) throws SecurityException {

		// Validate the roles
		Rating level = getUserLevel(usr);
		if (level == Rating.DISABLED)
			throw new SecurityException(usr.getName() + " not authorized to access FSD");

		// Get the user
		FSDCert cert = _certs.get(Integer.valueOf(usr.getID()));
		if (cert == null)
			throw new SecurityException(usr.getName() + " not found!");

		// Validate the password
		if (!cert.getPassword().equals(pwd))
			throw new SecurityException("Cannot authenticate " + usr.getName() + " - Invalid Credentials");
	}

	/**
	 * Checks whether this Authenticator accepts a User. The user must be a member of a role that maps to at least Level 1.
	 * @param usr the user bean
	 * @return TRUE if the User is a member of a Role mapping to at least Level 1, otherwise FALSE
	 */
	@Override
	public boolean accepts(Person usr) {
		return (getUserLevel(usr) != Rating.DISABLED) && (usr.getStatus() == Pilot.ACTIVE);
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the User bean
	 * @return TRUE if the user exists, otherwise FALSE
	 */
	@Override
	public boolean contains(Person usr) throws SecurityException {
		return _certs.containsKey(Integer.valueOf(usr.getID()));
	}

	/**
	 * Updates a User's password.
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		FSDCert cert = _certs.get(Integer.valueOf(usr.getID()));
		if (cert == null)
			throw new SecurityException(usr.getName() + " not found!");

		// Update the password and save
		cert.setPassword(pwd);
		save();
	}

	/**
	 * Adds a user to the Directory.
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void add(Person usr, String pwd) throws SecurityException {

		// Get the user's level; abort if they do not have access
		Rating level = getUserLevel(usr);
		if ((level == Rating.DISABLED) || (!(usr instanceof Pilot)))
			return;

		// Add the user
		Pilot p = (Pilot) usr;
		FSDCert cert = new FSDCert(p.getPilotCode(), pwd, level);
		cert.setID(usr.getID());
		_certs.put(Integer.valueOf(cert.getID()), cert);
		save();
	}

	/**
	 * Renames a user in the Directory. Since the FSD Authenticator relies upon database IDs and pilot codes,
	 * this is not implemented.
	 * @param usr the user bean
	 * @param newName the new fully-qualified directory
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void rename(Person usr, String newName) throws SecurityException {
		if (!contains(usr))
			throw new SecurityException(usr.getName() + " not found");
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the User bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void remove(Person usr) throws SecurityException {
		if (_certs.remove(Integer.valueOf(usr.getID())) != null)
			save();
	}
	
	/**
	 * Disables a user's account. <i>This removes the account.</i>
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void disable(Person usr) throws SecurityException {
		remove(usr);
	}

	/*
	 * Helper method to return the user's access level based on their security roles.
	 */
	private Rating getUserLevel(Person usr) {
		if (usr == null) return Rating.DISABLED;
		Rating result = Rating.DISABLED;
		for (Iterator<String> i = usr.getRoles().iterator(); i.hasNext();) {
			String role = i.next();
			Rating roleLevel = _roleLevels.get(role);
			if ((roleLevel != null) && (roleLevel.ordinal() > result.ordinal()))
				result = roleLevel;
		}

		return result;
	}

	/*
	 * Helper method to save the certs to disk.
	 */
	private synchronized void save() throws SecurityException {
		Collection<FSDCert> certs = new LinkedHashSet<FSDCert>(_certs.values());
		try (PrintWriter pw = new PrintWriter(_certFile)) {
			pw.println("; Auto-Generated FSD certs on " + Instant.now());
			pw.println(',');
			pw.println("; User levels");
			for (int x = 0; x < LEVEL_NAMES.length; x++)
				pw.println("; " + String.valueOf(x) + " = " + LEVEL_NAMES[x]);

			// Write cert data
			pw.println(',');
			for (Iterator<FSDCert> i = certs.iterator(); i.hasNext();) {
				FSDCert cert = i.next();
				pw.println(cert.toString());
			}

			pw.close();
		} catch (IOException ie) {
			throw new SecurityException(ie);
		}
	}
}