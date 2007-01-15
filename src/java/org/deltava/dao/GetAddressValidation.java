// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.sql.*;

import org.deltava.beans.Applicant;
import org.deltava.beans.system.AddressValidation;

/**
 * A Data Access Object to read e-mail address validation entries.
 * @author Luke
 * @version 1.0
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
			prepareStatement("SELECT * FROM EMAIL_VALIDATION WHERE (ID=?)");
			_ps.setInt(1, id);
			_ps.setMaxRows(1);

			// Run the query, if empty return null
			List results = execute();
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
			prepareStatement("SELECT * FROM EMAIL_VALIDATION WHERE (HASH=?)");
			_ps.setString(1, hashCode);
			_ps.setMaxRows(1);

			// Run the query, if empty return null
			List results = execute();
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
			prepareStatement("SELECT * FROM EMAIL_VALIDATION WHERE (EMAIL=?)");
			_ps.setString(1, eMail);
			_ps.setMaxRows(1);

			// Run the query, if empty return null
			List results = execute();
			return results.isEmpty() ? null : (AddressValidation) results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Address Validation data for Pilots.
	 * @return a List of AddressValidation beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AddressValidation> getPilots() throws DAOException {
		try {
			prepareStatement("SELECT * FROM EMAIL_VALIDATION WHERE (ID < ?)");
			_ps.setInt(1, Applicant.BASE_DB_ID);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Address Validation data for Applicants.
	 * @return a List of AddressValidation beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<AddressValidation> getApplicants() throws DAOException {
		try {
			prepareStatement("SELECT * FROM EMAIL_VALIDATION WHERE (ID >= ?)");
			_ps.setInt(1, Applicant.BASE_DB_ID);
			return execute();
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

		// Iterate through the results
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