package org.deltava.security;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import org.deltava.util.ConfigLoader;

/**
 * An authenticator to validate users against a file repository. This should typically be used for testing or backup
 * purposes only.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class FileAuthenticator implements Authenticator {

	private static final Logger log = Logger.getLogger(FileAuthenticator.class);

	private Map _users = new HashMap();
	private Properties _props;

	private class UserInfo {

		private String _dn;
		private String _uid;
		private String _pwd;

		UserInfo(String rawInput) {
			StringTokenizer tokens = new StringTokenizer(rawInput, ",");
			_dn = tokens.nextToken();
			_pwd = tokens.nextToken();
			_uid = tokens.nextToken();
		}
		
		UserInfo(String dn, String pwd, String uid) {
		   this(dn + "," + pwd + "," + ((uid == null) ? "" : uid));
		}

		public String getDN() {
		   return _dn;
		}
		
		public String getUID() {
		   return _uid;
		}

		public String getPassword() {
		   return _pwd;
		}
		
		public void setPassword(String pwd) {
		   _pwd = pwd;
		}
	}

	/**
	 * Create a new File Authenticator and populate its user list from a file. User entries are stored internally in an
	 * unsorted Map. This file is comma-delimited with the fields in the following order: <i>FIRSTNAME, LASTNAME, PILOT
	 * ID, PASSWORD </i>
	 * @param propsFile the properties file to use
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {

		_props = new Properties();
		try {
			_props.load(ConfigLoader.getStream(propsFile));
		} catch (IOException ie) {
			log.error("Error loading " + propsFile + " - " + ie.getMessage());
			throw new SecurityException(ie.getMessage());
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(_props.getProperty("file.name"))));
			while (br.ready()) {
				UserInfo info = new UserInfo(br.readLine());
				_users.put(info.getDN(), info);
				log.debug("Loaded user " + info.getDN());
			}

			br.close();
		} catch (IOException ie) {
			log.warn("Error loading " + _props.getProperty("file.name") + " - " + ie.getMessage());
		}

		log.info("Loaded " + _users.size() + " user records");
	}

	/**
	 * Authenticates the user by searching for the directory name and then comparing the existing password on file to
	 * the one specified. The DN search is case-insensitive.
	 * @param dn the user's directory name
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails
	 * @see org.deltava.security.Authenticator#authenticate(java.lang.String, java.lang.String)
	 */
	public void authenticate(String dn, String pwd) throws SecurityException {
		UserInfo ui = (UserInfo) _users.get(dn);
		if (ui == null)
			throw new SecurityException(dn + " not found");

		if (!ui.getPassword().equals(pwd)) {
			log.warn(dn + " Authentication FAILURE - Invalid credentials");
			throw new SecurityException("Invalid Credentials");
		}

		log.info(dn + " authenticated");
	}

	/**
	 * Checks if a particular name exists in the Directory.
	 * @param directoryName the fully-qualified directory name
	 * @return TRUE if the user exists, otherwise FALSE
	 */
	public boolean contains(String directoryName) {
		return _users.containsKey(directoryName);
	}

	/**
	 * Writes the user list out to the data file.
	 */
	private void save() throws IOException {

	   // Create the file
	   PrintWriter pw = new PrintWriter(new FileWriter(new File(_props.getProperty("file.name"))));
	   for (Iterator i = _users.values().iterator(); i.hasNext(); ) {
	      UserInfo user = (UserInfo) i.next();
	      pw.print(user.getDN());
	      pw.print(',');
	      pw.println(user.getPassword());
	   }
	   
	   // Close the file
	   pw.flush();
	   pw.close();
	   
	   // Log success
	   log.info("Saved " + _users.size() + " user records");
	}

	/**
	 * Updates a user's password.
	 * @param directoryName the user's fully-qualified Directory name
	 * @param pwd the user's password
	 * @throws SecurityException if an error occurs
	 */
	public void updatePassword(String directoryName, String pwd) throws SecurityException {
	   
	   // Get the User
	   UserInfo usr = (UserInfo) _users.get(directoryName);
	   if (usr == null)
	      throw new SecurityException("User " + directoryName + " not found");
	   
	   // Update the password
	   usr.setPassword(pwd);
	   try {
	      save();
	   } catch (IOException ie) {
	      throw new SecurityException(ie.getMessage());
	   }
	}
	
	/**
	 * Adds a user to the Directory.
	 * @param directoryName the user's fully-qualified Directory name
	 * @param pwd the user's password
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(String directoryName, String pwd) throws SecurityException {
	   addUser(directoryName, pwd, null);
	}

	/**
	 * Adds a user to the Directory.
	 * @param directoryName the user's fully-qualified Directory name
	 * @param pwd the user's password
	 * @param userID the user's alias, or null if none
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(String directoryName, String pwd, String userID) throws SecurityException {
	   
	   // Check if the user exists
	   if (contains(directoryName))
	      throw new SecurityException("User " + directoryName + " already exists");

	   // Create the user object
	   UserInfo usr = new UserInfo(directoryName, pwd, userID);
	   _users.put(directoryName, usr);
	   
	   // Save the user list
	   try {
	      save();
	   } catch (IOException ie) {
	      throw new SecurityException(ie.getMessage());
	   }
	}

	/**
	 * Removes a user from the Directory.
	 * @param directoryName the user's fully-qualified Directory name
	 * @throws SecurityException if an error occurs
	 */
	public void removeUser(String directoryName) throws SecurityException {
	   
	   // Check for the user
	   if (!contains(directoryName))
	      throw new SecurityException("User " + directoryName + " not found");
	   
	   _users.remove(directoryName);
	   try {
	      save();
	   } catch (IOException ie) {
	      throw new SecurityException(ie.getMessage());
	   }
	}
	
	/**
	 * Renames a user in the Directory. <i>NOT IMPLEMENTED</i>
	 * @param oldName the old fullly-qualified Directory name
	 * @param newName the new fullly-qualified Directory name
	 * @throws UnsupportedOperationException always
	 */
	public void rename(String oldName, String newName) throws SecurityException {
	   throw new UnsupportedOperationException();
	}
}