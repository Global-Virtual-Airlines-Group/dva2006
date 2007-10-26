// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.util.*;

/**
 * An Authenticator to authenticate users using an Apache-style password file. This authenticator only supports SHA
 * hashing of the password, not MD5 or crypt().
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApacheFileAuthenticator implements Authenticator {

	private static final Logger log = Logger.getLogger(ApacheFileAuthenticator.class);
	private static final String SHA_HDR = "{SHA}";

	private final Properties _props = new Properties();
	private File _pwdFile;
	private final Map<String, String> _pwdInfo = new TreeMap<String, String>();

	/**
	 * Create a new Apache File Authenticator and populate its user list from a file.
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {

		_props.clear();
		try {
			_props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading " + propsFile + " - " + ie.getMessage());
			throw new SecurityException(ie.getMessage());
		}
		
		// Check for the file
		_pwdFile = new File(_props.getProperty("apachefile.name"));
		if (!_pwdFile.exists()) {
			log.warn("Cannot open " + _pwdFile.getPath() + " - not found");
			return;
		}

		// Load the data
		try {
			LineNumberReader br = new LineNumberReader(new FileReader(_pwdFile));
			while (br.ready()) {
				StringTokenizer tkns = new StringTokenizer(br.readLine(), ":");
				if (tkns.countTokens() != 2)
					log.warn("Invalid token count on Line " + br.getLineNumber() + " tokens=" + tkns.countTokens());
				else {
					String userID = tkns.nextToken();
					String pwdInfo = tkns.nextToken();
					if (!pwdInfo.startsWith(SHA_HDR))
						log.warn("Invalid (non-SHA-1) password type on Line " + br.getLineNumber());
					else {
						_pwdInfo.put(userID, pwdInfo);
						log.debug("Loaded user " + userID);
					}
				}
			}

			br.close();
		} catch (IOException ie) {
			log.warn("Error loading " + _props.getProperty("apachefile.name") + " - " + ie.getMessage());
		}
	}

	/**
	 * Authenticates the user by searching for the directory name and then comparing the existing password on file to
	 * the one specified.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {
		
		// Get the user
		String userID = getID(usr);
		if (userID == null)
			throw new SecurityException("Cannot authenticate " + userID + " - User not found");

		// Create the password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String hash = SHA_HDR + Base64.encode(md.digest(pwd.getBytes()));
		
		// Get the existing password data
		String existingHash = _pwdInfo.get(userID);
		if (!hash.equals(existingHash))
			throw new SecurityException("Cannot authenticate " + userID + " - Invalid Credentials");
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the User bean
	 * @return TRUE if the user exists, otherwise FALSE
	 */
	public boolean contains(Person usr) throws SecurityException {
		String userID = getID(usr);
		return (userID != null) && _pwdInfo.containsKey(userID);
	}
	
	/**
	 * Checks if this Authenticator will accept a particular user.
	 * @return TRUE if the user is a Pilot and has the LDAPName property set, otherwise FALSE
	 */
	public boolean accepts(Person usr) {
		return (getID(usr) != null);
	}

	/**
	 * Adds a user to the Directory.
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if an error occurs
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {

		// Get the ID to update
		String userID = getID(usr);
		if (userID == null)
			return;
		else if (!_pwdInfo.containsKey(userID))
			throw new SecurityException(userID + " not found");

		// Create the new password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String hash = SHA_HDR + Base64.encode(md.digest(pwd.getBytes()));
		_pwdInfo.put(userID, hash);
		save();
	}

	/**
	 * Updates a User's password.
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs
	 */
	public void add(Person usr, String pwd) throws SecurityException {

		// Get the ID to add
		String userID = getID(usr);
		if (userID == null)
			return;
		else if (_pwdInfo.containsKey(userID))
			throw new SecurityException(userID + " already exists");
		
		// Create the new password hash
		MessageDigester md = new MessageDigester("SHA-1");
		String hash = SHA_HDR + Base64.encode(md.digest(pwd.getBytes()));
		_pwdInfo.put(userID, hash);
		save();
	}

	/**
	 * Renames a user in the Directory.
	 * @param usr the User bean
	 * @param newName the new Directory name
	 * @throws SecurityException if an error occurs
	 */
	public void rename(Person usr, String newName) throws SecurityException {

		// Get the ID to rename
		String userID = getID(usr);
		if (userID == null)
			return;
		else if (!_pwdInfo.containsKey(userID))
			throw new SecurityException(userID + " not found");
		else if (_pwdInfo.containsKey(newName))
			throw new SecurityException(newName + " already present");

		// Update the password
		_pwdInfo.put(newName, _pwdInfo.get(userID));
		_pwdInfo.remove(userID);
		save();
	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the User bean
	 * @throws SecurityException if an error occurs
	 */
	public void remove(Person usr) throws SecurityException {

		// Get the ID to delete
		String userID = getID(usr);
		if ((userID == null) || (!_pwdInfo.containsKey(userID)))
			return;

		// Remove the ID and update the
		_pwdInfo.remove(userID);
		save();
	}
	
	/**
	 * Disables a user's account. <i>This deletes the account.</i>
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs
	 */
	public void disable(Person usr) throws SecurityException {
		remove(usr);
	}

	/**
	 * Helper method to retrieve a user's alias.
	 */
	private String getID(Person usr) {
		boolean useAlias = Boolean.valueOf(_props.getProperty("apachefile.alias")).booleanValue();
		if ((useAlias) && (usr instanceof Applicant))
			return null;
		
		// Determine wether we use the LDAP name
		boolean useLDAP = (usr instanceof Pilot) && useAlias; 
		return useLDAP ? ((Pilot) usr).getLDAPName() : usr.getDN();
	}
	
	/**
	 * Updates the password file.
	 */
	private void save() throws SecurityException {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(_pwdFile));
			for (Iterator<String> i = _pwdInfo.keySet().iterator(); i.hasNext(); ) {
				String userID = i.next();
				out.println(userID + ":" + _pwdInfo.get(userID));
			}
		
			// Close the file
			out.close();
			log.debug("Saved " + _pwdInfo.size() + " entries");
		} catch (IOException ie) {
			throw new SecurityException(ie);
		}
	}
}