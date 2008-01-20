// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;

import org.deltava.beans.*;

/**
 * A Data Access Object interface to check for uniqueness.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public interface PersonUniquenessDAO {

	/**
	 * Checks if a Person exists within a Particular database.
	 * @param usr the Person to check for
	 * @param dbName the database name
	 * @return a Collection of Database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkUnique(Person usr, String dbName) throws DAOException;

	/**
	 * Performs a soundex search on a Person's last name to detect possible matches. The soundex implementation is
	 * dependent on the capabilities of the underlying database engine, and is not guaranteed to be consistent (or even
	 * supported) across different database servers.
	 * @param usr the Person to check for
	 * @param dbName the database name
	 * @return a Collection of Database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> checkSoundex(Person usr, String dbName) throws DAOException;
	
	/**
	 * Returns Person objects which may be in another Airline's database.
	 * @param udm the UserDataMap bean containg the Person locations
	 * @return a Map of Persons indexed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, ? extends Person> get(UserDataMap udm) throws DAOException;
}