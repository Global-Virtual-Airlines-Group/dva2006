// Copyright 2005, 2006, 2007, 2009, 2016, 2019, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.io.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.Person;

import org.deltava.util.*;

/**
 * An authenticator to validate users against a file repository. This should typically be used for testing or backup purposes only.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class FileAuthenticator implements Authenticator {

	private static final Logger log = LogManager.getLogger(FileAuthenticator.class);

	private final Map<String, UserInfo> _users = new HashMap<String, UserInfo>();
	private final Properties _props = new Properties();

	private class UserInfo {
		private final String _dn;
		private String _pwd;
		private boolean _enabled = true;

		UserInfo(String rawInput) {
			StringTokenizer tokens = new StringTokenizer(rawInput, ",");
			_dn = tokens.nextToken();
			_pwd = tokens.nextToken();
			tokens.nextToken();
			if (tokens.hasMoreTokens())
				_enabled = Boolean.parseBoolean(tokens.nextToken());
		}

		UserInfo(String dn, String pwd, String uid) {
			this(dn + "," + pwd + "," + ((uid == null) ? "" : uid));
		}

		public String getDN() {
			return _dn;
		}

		public String getPassword() {
			return _pwd;
		}
		
		public boolean isEnabled() {
			return _enabled;
		}
		
		public void setEnabled(boolean enabled) {
			_enabled = enabled;
		}

		public void setPassword(String pwd) {
			_pwd = pwd;
		}
	}

	/**
	 * Create a new File Authenticator and populate its user list from a file.
	 * User entries are stored internally in an unsorted Map. This file is
	 * comma-delimited with the fields in the following order: <i>FIRSTNAME,
	 * LASTNAME, PILOT ID, PASSWORD </i>
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void init(String propsFile) throws SecurityException {

		_props.clear();
		try {
			_props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading {} - {}", propsFile, ie.getMessage());
			throw new SecurityException(ie.getMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(new File(_props.getProperty("file.name"))))) {
			while (br.ready()) {
				UserInfo info = new UserInfo(br.readLine());
				_users.put(info.getDN(), info);
				log.debug("Loaded user {}", info.getDN());
			}
		} catch (IOException ie) {
			log.warn("Error loading {} = {}", _props.getProperty("file.name"), ie.getMessage());
		}

		log.info("Loaded {} user records", Integer.valueOf(_users.size()));
	}

	/**
	 * Authenticates the user by searching for the directory name and then
	 * comparing the existing password on file to the one specified. The DN
	 * search is case-insensitive.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails
	 * @see Authenticator#authenticate(Person, String)
	 */
	@Override
	public void authenticate(Person usr, String pwd) throws SecurityException {
		UserInfo ui = _users.get(usr.getDN());
		if (ui == null)
			throw new SecurityException(usr.getDN() + " not found");

		if (!ui.getPassword().equals(pwd)) {
			log.warn("{} Authentication FAILURE - Invalid credentials", usr.getDN());
			throw new SecurityException("Invalid Credentials");
		}

		log.info("{} authenticated", usr.getDN());
	}

	/**
	 * Checks whether this Authenticator will accept a User. The User must have a
	 * non-empty Directory name.
	 * @param usr the user bean
	 * @return TRUE if the Directory name is populated, otherwise FALSE
	 */
	@Override
	public boolean accepts(Person usr) {
		return ((usr != null) && !StringUtils.isEmpty(usr.getDN()));
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the User bean
	 * @return TRUE if the user exists, otherwise FALSE
	 */
	@Override
	public boolean contains(Person usr) {
		return _users.containsKey(usr.getDN());
	}

	/*
	 * Writes the user list out to the data file.
	 */
	private void save() throws SecurityException {
		try (PrintWriter pw = new PrintWriter(new FileWriter(new File(_props.getProperty("file.name"))))) {
			for (UserInfo user : _users.values()) {
				pw.print(user.getDN());
				pw.print(',');
				pw.println(user.getPassword());
				pw.print(',');
				pw.print(String.valueOf(user.isEnabled()));
			}

			pw.flush();
		} catch (IOException ie) {
			throw new SecurityException(ie);
		}

		log.info("Saved {} user records", Integer.valueOf(_users.size()));
	}

	/**
	 * Updates a user's password.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void updatePassword(Person usr, String pwd) throws SecurityException {

		// Get the User
		UserInfo usrInfo = _users.get(usr.getDN());
		if (usrInfo == null)
			throw new SecurityException("User " + usr.getDN() + " not found");

		// Update the password
		usrInfo.setPassword(pwd);
		usrInfo.setEnabled(true);
		save();
	}

	/**
	 * Adds a user to the Directory.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void add(Person usr, String pwd) throws SecurityException {

		// Check if the user exists
		if (contains(usr))
			throw new SecurityException("User " + usr.getDN() + " already exists");

		// Create the user object
		UserInfo usrInfo = new UserInfo(usr.getDN(), pwd, "usrID");
		_users.put(usr.getDN(), usrInfo);

		// Save the user list
		save();
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the User bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void remove(Person usr) throws SecurityException {
		if (!contains(usr))
			throw new SecurityException("User " + usr.getDN() + " not found");

		_users.remove(usr.getDN());
		save();
	}
	
	/**
	 * Disables a user's account.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	@Override
	public void disable(Person usr) throws SecurityException {
		UserInfo usrInfo = _users.get(usr.getDN());
		if ((usrInfo != null) && (usrInfo.isEnabled())) {
			usrInfo.setEnabled(false);
			save();
		} else if (usrInfo == null)
			throw new SecurityException("User " + usr.getDN() + " not found");
	}
	
	/**
	 * Renames a user in the Directory. <i>NOT IMPLEMENTED</i>
	 * @param usr the User bean
	 * @param newName the new fullly-qualified Directory name
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void rename(Person usr, String newName) throws SecurityException {
		throw new UnsupportedOperationException();
	}
}