// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.Person;

import org.deltava.util.cache.Cacheable;

/**
 * A class to store cross-Airline User data. This is used to track locations of users in parts of the application (Water
 * Cooler, Online Events) that allow users from other airlines to write database entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserData extends DatabaseBean implements Cacheable {

	private String _aCode;
	private String _dbName;
	private String _tableName;
	private String _domainName;

	/**
	 * Creates a new UserData bean when a User is created.
	 * @param dbName
	 * @param table
	 * @param domain
	 */
	public UserData(String dbName, String table, String domain) {
		super();
		setDB(dbName);
		setAirlineCode(dbName);
		setTable(table);
		setDomain(domain);
	}

	/**
	 * Creates a new User Data bean for the specified User.
	 * @param id the User's database ID
	 */
	public UserData(int id) {
		super();
		setID(id);
	}

	/**
	 * Creates a new User Data bean for the specified Person.
	 * @param p the Person bean
	 */
	public UserData(Person p) {
		this(p.getID());
	}

	/**
	 * Returns the name of the database containing the User.
	 * @return the database name
	 * @see UserData#setDB(String)
	 */
	public String getDB() {
		return _dbName;
	}

	/**
	 * Returns the User's airline code.
	 * @return the airline code
	 * @see UserData#setAirlineCode(String)
	 */
	public String getAirlineCode() {
		return _aCode;
	}

	/**
	 * Returns the name of the table containing the User.
	 * @return the database table name
	 * @see UserData#setTable(String)
	 */
	public String getTable() {
		return _tableName;
	}

	/**
	 * Returns the domain name of the web site containing the User's profile.
	 * @return the domain name
	 * @see UserData#setDomain(String)
	 */
	public String getDomain() {
		return _domainName;
	}

	/**
	 * Returns if the User is an Applicant or a Pilot.
	 * @return TRUE if the user is an Applicant, otherwise FALSE 
	 */
	public boolean isApplicant() {
		return "APPLICANTS".equals(_tableName);
	}

	/**
	 * Updates the database name. This will be converted to lowercase.
	 * @param dbName the name of the database containing this User
	 * @throws NullPointerException if dbName is null
	 * @see UserData#getDB()
	 */
	public void setDB(String dbName) {
		_dbName = dbName.trim().toLowerCase();
	}

	/**
	 * Updates the User's airline code. This will be converted to uppercase.
	 * @param aCode the Airline code
	 * @throws NullPointerException if aCode is null
	 * @see UserData#getAirlineCode()
	 */
	public void setAirlineCode(String aCode) {
		_aCode = aCode.trim().toUpperCase();
	}

	/**
	 * Updates the table name. This will be converted to uppercase.
	 * @param tableName the name of the table containing this User
	 * @throws NullPointerException if tableName is null
	 * @see UserData#getTable()
	 */
	public void setTable(String tableName) {
		_tableName = tableName.trim().toUpperCase();
	}

	/**
	 * Sets the domain name of the database used to view the User's profile. This will be converted to lowercase.
	 * @param domainName the domain of the web site containing this user's Profile
	 * @throws NullPointerException if domainName is null
	 * @see UserData#getDomain()
	 */
	public void setDomain(String domainName) {
		_domainName = domainName.trim().toLowerCase();
	}

	/**
	 * Returns the hashcode of the database ID.
	 */
	public int hashCode() {
		return cacheKey().hashCode();
	}

	/**
	 * Returns the database ID.
	 */
	public Object cacheKey() {
		return new Integer(getID());
	}
}