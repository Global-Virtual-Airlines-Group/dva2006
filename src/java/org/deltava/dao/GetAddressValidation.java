// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.system.AddressValidation;

/**
 * A Data Access Object to read e-mail address validation entries.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetAddressValidation extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAddressValidation(Connection c) {
		super(c);
	}

	/**
	 * Returns the Address Validation data for a particular Pilot/Applicant ID
	 * @param id the database ID
	 * @return the AddressValidation bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public AddressValidation get(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM EMAIL_VALIDATION WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, id);

			// Run the query, if empty return null
			List<AddressValidation> results = execute();
			return results.isEmpty() ? null : (AddressValidation) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns if a Pilot or Applicant's e-mail address is valid.
	 * @param pilotID the Pilot/Applicant ID
	 * @return TRUE if the address is valid (no record found), otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean isValid(int pilotID) throws DAOException {
		return (get(pilotID) == null);
	}

	/**
	 * Returns the Address Validation data matching a particular hash code.
	 * @param hashCode the hash code
	 * @return the AddressValidation bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public AddressValidation get(String hashCode) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM EMAIL_VALIDATION WHERE (HASH=?) LIMIT 1");
			_ps.setString(1, hashCode);

			// Run the query, if empty return null
			List<AddressValidation> results = execute();
			return results.isEmpty() ? null : (AddressValidation) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Address Validation data matching a particular e-mail address.
	 * @param eMail the e-mail address
	 * @return the AddressValidation bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public AddressValidation getAddress(String eMail) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM EMAIL_VALIDATION WHERE (EMAIL=?) LIMIT 1");
			_ps.setString(1, eMail);

			// Run the query, if empty return null
			List<AddressValidation> results = execute();
			return results.isEmpty() ? null : (AddressValidation) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to parse the result set.
	 */
	private List<AddressValidation> execute() throws SQLException {

		// Execute the query
		ResultSet rs = _ps.executeQuery();
		List<AddressValidation> results = new ArrayList<AddressValidation>();
		while (rs.next()) {
			AddressValidation addr = new AddressValidation(rs.getInt(1), rs.getString(2));
			addr.setHash(rs.getString(3));
			results.add(addr);
		}

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}