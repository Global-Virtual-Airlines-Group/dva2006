// Copyright 2010, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.hr.*;

/**
 * A Data Access Object to read Job applications and profiles from the database.
 * @author Luke
 * @version 9.0
 * @since 3.4
 */

public class GetJobProfiles extends DAO {

	/**
	 * Initializes the Data Access Object
	 * @param c the JDBC connection to use
	 */
	public GetJobProfiles(Connection c) {
		super(c);
	}
	
	/**
	 * Loads an applicant profile.
	 * @param id the auhtor ID
	 * @return a Profile bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Profile getProfile(int id) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT JP.*, P.FIRSTNAME, P.LASTNAME FROM JOBAPROFILES JP, PILOTS P WHERE (JP.ID=P.ID) AND (JP.ID=?)")) {
			ps.setInt(1, id);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all applicant profile.
	 * @return a Collection of Profile beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Profile> getProfiles() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT JP.*, P.FIRSTNAME, P.LASTNAME FROM JOBAPROFILES JP, PILOTS P WHERE (JP.ID=P.ID) ORDER BY P.LASTNAME, P.FIRSTNAME")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method for Profile result sets.
	 */
	private static List<Profile> execute(PreparedStatement ps) throws SQLException {
		List<Profile> results = new ArrayList<Profile>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Profile p = new Profile(rs.getInt(1));
				p.setCreatedOn(rs.getTimestamp(2).toInstant());
				p.setAutoReuse(rs.getBoolean(3));
				p.setBody(rs.getString(4));
				p.setFirstName(rs.getString(5));
				p.setLastName(rs.getString(6));
				results.add(p);
			}
		}
		
		return results;
	}
}