// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.util.*;

/**
 * An Authenticator to read/write from FSD certificate files.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FSDAuthenticator implements Authenticator {
	
	private static final Logger log = Logger.getLogger(FSDAuthenticator.class);
	
	private final Properties _props = new Properties();
	private File _certFile;
	private final Map<Integer, FSDCert> _certs = Collections.synchronizedMap(new LinkedHashMap<Integer, FSDCert>());
	private final Collection<FSDCert> _atcCerts = new LinkedHashSet<FSDCert>();
	private final Map<String, Integer> _roleLevels = new HashMap<String, Integer>();
	
	private static final int MAX_CERT_LEVEL = 12;
	protected static final String[] CERT_TYPES = {"P", "A"};
	private static final String[] LEVEL_NAMES = {"Disabled", "Pilot", "Student 1", "Student 2", "Student 3", "Controller 1", "Controller 2",
		"Controller 3", "Instructor 1", "Instructor 2", "Instructor 3", "Supervisor", "Administrator" };
	
	private static final int PILOT = 0;
	private static final int ATC = 1;
	
	private class FSDCert extends DatabaseBean {
		
		private int _type;
		private String _userID;
		private String _pwd;
		private int _level;
		
		FSDCert(int type, String userID, String pwd, int level) {
			super();
			_type = type;
			_userID = userID.toUpperCase();
			_pwd = pwd;
			_level = Math.min(Math.max(1, level), MAX_CERT_LEVEL);
		}
		
		public String getUserID() {
			return _userID;
		}
		
		public String getPassword() {
			return _pwd;
		}
		
		public int getLevel() {
			return _level;
		}
		
		public void setPassword(String pwd) {
			_pwd = pwd;
		}
		
		public int hashCode() {
			return _userID.hashCode();
		}
		
		public String toString() {
			StringBuilder buf = new StringBuilder(CERT_TYPES[_type]);
			buf.append(' ');
			buf.append(_userID);
			buf.append(' ');
			buf.append(_pwd);
			buf.append(' ');
			buf.append(String.valueOf(_level));
			if (getID() > 0) {
				buf.append("; ");
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
		for (int x = 1; x <= MAX_CERT_LEVEL; x++) {
			String roleNames = _props.getProperty("fsd.level." + String.valueOf(x), "");
			Collection<String> roles = StringUtils.split(roleNames, ",");
			for (Iterator<String> i = roles.iterator(); i.hasNext(); )
				_roleLevels.put(i.next(), new Integer(x));
		}
		
		// Check for the certificate file
		_certFile = new File(_props.getProperty("fsd.cert"));
		if (!_certFile.exists()) {
			log.warn("Cannot open " + _certFile.getPath() + " - not found");
			return;
		}
		
		// Load the certificates
		try {
			LineNumberReader br = new LineNumberReader(new FileReader(_certFile));
			while (br.ready()) {
				StringTokenizer tkns = new StringTokenizer(br.readLine(), " ");
				if (tkns.countTokens() < 5)
					log.warn("Invalid token count on Line " + br.getLineNumber() + " tokens=" + tkns.countTokens());
				else {
					int certType = StringUtils.arrayIndexOf(CERT_TYPES, tkns.nextToken().toUpperCase());
					String userID = tkns.nextToken();
					try {
						FSDCert cert = new FSDCert(certType, userID, tkns.nextToken(), StringUtils.parse(tkns.nextToken(), 1));
						if (certType != ATC) {
							String dbID = tkns.nextToken().substring(1);
							cert.setID(StringUtils.parse(dbID, 0));
							_certs.put(new Integer(cert.getID()), cert);
						} else
							_atcCerts.add(cert);
					} catch (IllegalArgumentException iae) {
						log.warn("Invalid database ID for " + userID + " - " + iae.getMessage());
					}
				}
			}
			
			br.close();
		} catch (IOException ie) {
			log.warn("Error loading " + _props.getProperty("fsd.cert") + " - " + ie.getMessage());
		}
		
		log.info("Loaded " + _certs.size() + " user records");
	}

	/**
	 * Authenticates the user by searching for the directory name and then comparing the existing password on file to
	 * the one specified. Even if the credentials match, authentication will fail if the user's roles do not translate to at
	 * least Level 1.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {

		// Validate the roles
		int level = getUserLevel(usr);
		if (level == 0)
			throw new SecurityException(usr.getName() + " not authorized to access FSD");
		
		// Get the user
		FSDCert cert = _certs.get(new Integer(usr.getID()));
		if (cert == null)
			throw new SecurityException(usr.getName() + " not found!");

		// Validate the password
		if (!cert.getPassword().equals(pwd))
			throw new SecurityException("Cannot authenticate " + usr.getName() + " - Invalid Credentials");
	}
	
	/**
	 * Checks wether this Authenticator accepts a User. The user must be a member of a role that maps
	 * to at least Level 1.
	 * @param usr the user bean
	 * @return TRUE if the User is a member of a Role mapping to at least Level 1, otherwise FALSE
	 */
	public boolean accepts(Person usr) {
		return (getUserLevel(usr) > 0);
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param usr the User bean
	 * @return TRUE if the user exists, otherwise FALSE
	 */
	public boolean contains(Person usr) throws SecurityException {
		return _certs.containsKey(new Integer(usr.getID()));
	}

	/**
	 * Updates a User's password.
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		FSDCert cert = _certs.get(new Integer(usr.getID()));
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
	public void addUser(Person usr, String pwd) throws SecurityException {
		
		// Get the user's level; abort if they do not have access
		int level = getUserLevel(usr);
		if ((level == 0) || (!(usr instanceof Pilot)))
			return;
		
		// Add the user
		Pilot p = (Pilot) usr;
		FSDCert cert = new FSDCert(PILOT, p.getPilotCode(), pwd, level);
		cert.setID(usr.getID());
		_certs.put(new Integer(cert.getID()), cert);
		save();
	}

	/* (non-Javadoc)
	 * @see org.deltava.security.Authenticator#rename(org.deltava.beans.Person, java.lang.String)
	 */
	public void rename(Person usr, String newName) throws SecurityException {
		// TODO Auto-generated method stub

	}

	/**
	 * Removes a user from the Directory.
	 * @param usr the User bean
	 * @throws SecurityException if an error occurs
	 */
	public void removeUser(Person usr) throws SecurityException {
		if (!contains(usr))
			return;

		// Remove the user
		_certs.remove(new Integer(usr.getID()));
		save();
	}
	
	/**
	 * Helper method to return the user's access level based on their security roles.
	 */
	private int getUserLevel(Person usr) {
		if (usr == null)
			return 0;
		
		int result = 0;
		for (Iterator<String> i = usr.getRoles().iterator(); i.hasNext(); ) {
			String role = i.next();
			Integer roleLevel = _roleLevels.get(role);
			if (roleLevel != null)
				result = Math.max(result, roleLevel.intValue());
		}
		
		return result;
	}
	
	/**
	 * Helper method to save the certs to disk.
	 */
	private synchronized void save() throws SecurityException {
		Collection<FSDCert> certs = new LinkedHashSet<FSDCert>(_certs.values());
		try {
			PrintWriter pw = new PrintWriter(_certFile);
			
			// Write file header
			pw.println("; Auto-Generated FSD certs on " + new Date().toString());
			pw.println(";");
			pw.println("; User levels");
			for (int x = 0; x < LEVEL_NAMES.length; x++)
				pw.println("; " + String.valueOf(x) + " = " + LEVEL_NAMES[x]);
			
			// Write cert data
			pw.println(";");
			for (Iterator<FSDCert> i = certs.iterator(); i.hasNext(); ) {
				FSDCert cert = i.next();
				pw.println(cert.toString());
			}
			
			pw.close();
		} catch (IOException ie) {
			throw new SecurityException(ie);
		}
	}
}