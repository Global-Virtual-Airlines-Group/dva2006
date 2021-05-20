// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.assign.CharterRequest;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve Charter flight requests from the database. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class GetCharterRequests extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCharterRequests(Connection c) {
		super(c);
	}

	/**
	 * Retrieves a specific Charter request from the database.
	 * @param id the request database ID
	 * @return the CharterRequest, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public CharterRequest get(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM CHARTER_REQUESTS WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, id);
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all Charter requests from the database.
	 * @return a List of CharterRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CharterRequest> getAll() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM CHARTER_REQUESTS ORDER BY STATUS, ID DESC")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of pending Charter requests.
	 * @return the number of pending requests
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getPendingCount() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT COUNT(ID) FROM CHARTER_REQUESTS WHERE (STATUS=?)")) {
			ps.setInt(1, CharterRequest.RequestStatus.PENDING.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the database IDs of all Pilots who have created a Charter flight Request.
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getPilotIDs() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT AUTHOR_ID FROM CHARTER_REQUESTS")) {
			Collection<Integer> results = new HashSet<Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Retrieves all Charter requests for a specific Pilot from the database.
	 * @param pilotID the Pilot database ID
	 * @return a List of CharterRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<CharterRequest> getByPilot(int pilotID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM CHARTER_REQUESTS WHERE (AUTHOR_ID=?) ORDER BY STATUS, ID DESC")) {
			ps.setInt(1, pilotID);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse CharterRequest result sets.
	 */
	private static List<CharterRequest> execute(PreparedStatement ps) throws SQLException {
		List<CharterRequest> results = new ArrayList<CharterRequest>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				CharterRequest req = new CharterRequest();
				req.setID(rs.getInt(1));
				req.setAuthorID(rs.getInt(2));
				req.setCreatedOn(toInstant(rs.getTimestamp(3)));
				req.setAirportD(SystemData.getAirport(rs.getString(4)));
				req.setAirportA(SystemData.getAirport(rs.getString(5)));
				req.setAirline(SystemData.getAirline(rs.getString(6)));
				req.setEquipmentType(rs.getString(7));
				req.setDisposalID(rs.getInt(8));
				req.setDisposedOn(toInstant(rs.getTimestamp(9)));
				req.setStatus(CharterRequest.RequestStatus.values()[rs.getInt(10)]);
				req.setComments(rs.getString(11));
				results.add(req);
			}
		}
		
		return results;
	}
}