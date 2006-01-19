// Copyright (c) 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import org.deltava.beans.Person;

/**
 * An Authenticator used to migrate user data from one Directory to another. When a user is sucessfully authenticated by
 * the first (&quot;source&quot;) authenticator, the directory name and password are written into the second
 * (&quot;destination&quot;) authenticator and deleted from the source.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MigrationAuthenticator extends MultiAuthenticator {

	/**
	 * Loads the Authenticator.
	 */
	public MigrationAuthenticator() {
		super(MigrationAuthenticator.class);
	}
	
	/**
	 * Initializes the Authenticator.
	 * @param propsFile the name of the proeprties file to load
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {
		init(propsFile, "migration");
	}

	/**
	 * Authenticates the user. This authenticator will check the destination Directory to see if it contains an entry
	 * for the user. If it does, then authentication proceeds normally. If not, then the credentials are authenticated
	 * against the source Directory. If authentication succeeds, they are used to add credentials to the destnation
	 * Directory.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails for any reason, contained within the cause of the exception.
	 * @see Authenticator#authenticate(Person, String)
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {

		// Figure out which authenticator to use
		Authenticator auth = _dst.contains(usr) ? _dst : _src;
		auth.authenticate(usr, pwd);

		// If we got this far, and we're not in the destination directory, then add us
		if (auth == _src)
			_dst.addUser(usr, pwd);
	}

	/**
	 * Checks if a particular name exists in either Directory.
	 * @param usr the user bean
	 * @return TRUE if the user exists in either Authenticator, otherwise FALSE
	 * @throws SecurityException if an error occurs
	 */
	public boolean contains(Person usr) throws SecurityException {
		return _src.contains(usr) || _dst.contains(usr);
	}

	/**
	 * Updates a user's password. This user must be present in the destination directory.
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs, or the user is not in the Destination directory
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		if (_dst.contains(usr)) {
			_dst.updatePassword(usr, pwd);
		} else {
			_dst.addUser(usr, pwd);
		}
	}
	
	/**
	 * Adds a User to the Destination Directory.
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(Person usr, String pwd) throws SecurityException {
		_dst.addUser(usr, pwd);
	}

	/**
	 * Removes a User from the Directory. This will remove from the first directory containing the user, first the
	 * Destination, then the Source.
	 * @param usr the user bean
	 * @throws SecurityException if an error occurs or the user does not exist
	 */
	public void removeUser(Person usr) throws SecurityException {
		if (_dst.contains(usr)) {
			_dst.removeUser(usr);
		} else if (_src.contains(usr)) {
			_src.removeUser(usr);
		} else {
			throw new SecurityException("Unknown User - " + usr.getDN());
		}
	}
	
   /**
    * Renames a user in the <i>Destination</i> Directory.
    * @param usr the user bean
    * @param newName the new fully-qualified directory 
    * @throws SecurityException if an error occurs
    */
	public void rename(Person usr, String newName) throws SecurityException {
		_dst.rename(usr, newName);
	}
}