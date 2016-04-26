// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.testing.TestStatus;
import org.deltava.beans.hr.TransferRequest;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to read Pilot Transfer requests.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GetTransferRequest extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetTransferRequest(Connection c) {
		super(c);
	}

	/**
	 * Returns a Transfer Request for a particular Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return a TransferRequest bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TransferRequest get(int pilotID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM TXREQUESTS WHERE (ID=?) LIMIT 1");
			_ps.setInt(1, pilotID);

			// Execute the query, if empty return null
			List<TransferRequest> results = execute();
			loadCheckRides(results);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns whether the Pilot has a pending transfer request at any airline.
	 * @param pilotID the Pilot ID
	 * @return TRUE if the pilot has a transfer request, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public boolean hasTransfer(int pilotID) throws DAOException {
		try {
			Collection<String> dbNames = new LinkedHashSet<String>();
			prepareStatementWithoutLimits("SELECT DISTINCT DBNAME FROM common.AIRLINEINFO");
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					dbNames.add(rs.getString(1));
			}
			
			_ps.close();
			
			// Iterate through the databases to find out if they have a checkride
			boolean hasTX = false;
			for (Iterator<String> i = dbNames.iterator(); !hasTX && i.hasNext(); ) {
				String db = formatDBName(i.next());
				prepareStatementWithoutLimits("SELECT COUNT(*) FROM " + db + ".TXREQUESTS WHERE (ID=?)");
				_ps.setInt(1, pilotID);
				try (ResultSet rs = _ps.executeQuery()) {
					hasTX = rs.next() ? (rs.getInt(1) > 0) : false;
				}
					
				_ps.close();
			}
			
			return hasTX;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of pending Transfer Requests into a particular equipment program.
	 * @param eqType the Equipment program name, or null for all
	 * @return the number of Transfer Requests
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getCount(String eqType) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT COUNT(*) FROM TXREQUESTS");
		if (eqType != null)
			buf.append(" WHERE (EQTYPE=?)");
		
		try {
			prepareStatementWithoutLimits(buf.toString());
			if (eqType != null)
				_ps.setString(1, eqType);
			
			// Execute the query
			int result = 0;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					result = rs.getInt(1);
			}
			
			_ps.close();
			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the Transfer Request associated with a paritcular Check Ride.
	 * @param checkRideID the Check Ride database ID
	 * @return a TransferRequest bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public TransferRequest getByCheckRide(int checkRideID) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT TX.* FROM TXREQUESTS TX LEFT JOIN TXRIDES TC ON "
				+ "(TX.ID=TC.ID) WHERE (TC.CHECKRIDE_ID=?) LIMIT 1");
			_ps.setInt(1, checkRideID);

			// Execute the query, if empty return null
			List<TransferRequest> results = execute();
			loadCheckRides(results);
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Transfer Requests older than a certain age.
	 * @param minAge the number of days
	 * @return a Collection of TransferRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TransferRequest> getAged(int minAge) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT TX.* FROM TXREQUESTS TX LEFT JOIN TXRIDES TC ON (TX.ID=TC.ID) LEFT JOIN exams.CHECKRIDES CR ON (TC.CHECKRIDE_ID=CR.ID) WHERE "
				+ "(TX.CREATED < DATE_SUB(NOW(), INTERVAL ? DAY)) AND (TX.STATUS<>?) AND (CR.STATUS<>?) AND (CR.STATUS<>?) ORDER BY TX.CREATED");
			_ps.setInt(1, minAge);
			_ps.setInt(2, TransferRequest.OK);
			_ps.setInt(3, TestStatus.SUBMITTED.ordinal());
			_ps.setInt(4, TestStatus.SCORED.ordinal());
			List<TransferRequest> results = execute();
			loadCheckRides(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Transfer Requests.
	 * @param orderBy the sort order
	 * @return a List of TransferRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<TransferRequest> getAll(String orderBy) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT TX.* FROM TXREQUESTS TX LEFT JOIN TXRIDES TC ON (TX.ID=TC.ID) LEFT JOIN exams.CHECKRIDES CR ON (TC.CHECKRIDE_ID=CR.ID) ORDER BY ");
		sqlBuf.append((orderBy != null) ? orderBy : "TX.STATUS DESC, CR.STATUS DESC, TX.CREATED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			List<TransferRequest> results = execute();
			loadCheckRides(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Transfer Requests for a particular Equipment program.
	 * @param eqType the Equipment program name
	 * @param orderBy the sort order
	 * @return a List of TransferRequest beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<TransferRequest> getByEQ(String eqType, String orderBy) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT TX.* FROM TXREQUESTS TX LEFT JOIN TXRIDES TC ON "
			+ "(TX.ID=TC.ID) LEFT JOIN exams.CHECKRIDES CR ON (TC.CHECKRIDE_ID=CR.ID) WHERE (TX.EQTYPE=?) ORDER BY ");
		sqlBuf.append((orderBy != null) ? orderBy : "TX.STATUS DESC, CR.STATUS DESC, TX.CREATED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, eqType);
			List<TransferRequest> results = execute();
			loadCheckRides(results);
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/*
	 * Helper method to iterate through the result set.
	 */
	private List<TransferRequest> execute() throws SQLException {
		List<TransferRequest> results = new ArrayList<TransferRequest>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				TransferRequest txreq = new TransferRequest(rs.getInt(1), rs.getString(3));
				txreq.setStatus(rs.getInt(2));
				txreq.setDate(toInstant(rs.getTimestamp(4)));
				txreq.setRatingOnly(rs.getBoolean(5));
				results.add(txreq);
			}
		}

		_ps.close();
		return results;
	}
	
	/*
	 * Helper method to load checkride/transfer mappings.
	 */
	private void loadCheckRides(Collection<TransferRequest> txreqs) throws SQLException {
		Map<Integer, TransferRequest> reqs = CollectionUtils.createMap(txreqs, "ID");
		if (txreqs.isEmpty())
			return;
		
		StringBuilder sqlBuf = new StringBuilder("SELECT TC.ID, CR.ID, CR.STATUS FROM TXRIDES TC LEFT JOIN "
			+ "exams.CHECKRIDES CR ON (TC.CHECKRIDE_ID=CR.ID) WHERE TC.ID IN (");
		for (Iterator<TransferRequest> i = txreqs.iterator(); i.hasNext(); ) {
			TransferRequest tx = i.next();
			sqlBuf.append(tx.getID());
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append(") ORDER BY CR.ID");
		prepareStatement(sqlBuf.toString());
		
		// Execute the query
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				TransferRequest tx = reqs.get(Integer.valueOf(rs.getInt(1)));
				if (tx != null) {
					tx.addCheckRideID(rs.getInt(2));
					if (!tx.getCheckRideSubmitted())
						tx.setCheckRideSubmitted(rs.getInt(3) == TestStatus.SUBMITTED.ordinal());
				}
			}
		}
		
		_ps.close();
	}
}