// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Flight Reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetFlightReports extends DAO {

	private static final Logger log = Logger.getLogger(GetFlightReports.class);

	/**
	 * Initializes the DAO with a given JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReports(Connection c) {
		super(c);
	}

	/**
	 * Returns a PIREP with a particular database ID.
	 * @param id the database ID
	 * @return the Flight Report, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightReport get(int id) throws DAOException {
		return get(id, SystemData.get("airline.db"));
	}
	
	/**
	 * Returns a PIREP with a particular database ID.
	 * @param id the database ID
	 * @param dbName the database Name
	 * @return the Flight Report, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightReport get(int id, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".PILOTS P, ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) WHERE (PR.PILOT_ID=P.ID) AND (PR.ID=?)");
		
		try {
		   setQueryMax(1);
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, id);

			// Execute the query, if nothing returned then give back null
			List results = execute();
			if (results.size() == 0)
				return null;

			// Get the primary equipment types
			FlightReport fr = (FlightReport) results.get(0);
			fr.setCaptEQType(getCaptEQType(fr.getID()));
			return fr;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns an ACARS-logged PIREP with a particular ACARS Flight ID.
	 * @param dbName  the database Name
	 * @param acarsID the ACARS flight ID
	 * @return the ACARSFlightReport, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ACARSFlightReport getACARS(String dbName, int acarsID) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuilder sqlBuf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".PILOTS P, ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".PIREPS PR, ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".ACARS_PIREPS APR LEFT JOIN ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".PIREP_COMMENT PC ON (PR.ID=PC.ID) WHERE (APR.ID=PR.ID) AND (PR.PILOT_ID=P.ID) "
			   + "AND (APR.ACARS_ID=?)");
	   
	   try {
	      setQueryMax(1);
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, acarsID);
			
			// Execute the query, if nothing returned then give back null
			List results = execute();
			if (results.size() == 0)
				return null;

			// Get the primary equipment types
			ACARSFlightReport afr = (ACARSFlightReport) results.get(0);
			afr.setCaptEQType(getCaptEQType(afr.getID()));
			return afr;
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}

	/**
	 * Returns all Flight Reports in particular statuses.
	 * @param status a Collection of Integer status codes
	 * @return a List of FlightReports in the specified statuses
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByStatus(Collection<Integer> status) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* "
				+ "FROM PILOTS P, PIREPS PR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN "
				+ "ACARS_PIREPS APR ON (PR.ID=APR.ID) WHERE (PR.PILOT_ID=P.ID) AND (");
		for (Iterator<Integer> i = status.iterator(); i.hasNext(); ) {
			Integer st = i.next();
			sqlBuf.append("(PR.STATUS=");
			sqlBuf.append(st.toString());
			sqlBuf.append(')');
			if (i.hasNext())
				sqlBuf.append(" OR ");
		}
		
		sqlBuf.append(") ORDER BY PR.DATE, PR.ID");
		
		try {
			prepareStatement(sqlBuf.toString());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of Flight Reports awaiting disposition.
	 * @return the number Flight Reports in SUBMITTED or HOLD status
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getDisposalQueueSize() throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM PIREPS WHERE (STATUS=?) OR (STATUS=?)");
			_ps.setInt(1, FlightReport.SUBMITTED);
			_ps.setInt(2, FlightReport.HOLD);

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			int results = rs.next() ? rs.getInt(1) : 0;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Reports associated with a particular Flight Assignment.
	 * @param id the Flight Assignment database ID
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List getByAssignment(int id) throws DAOException {
		try {
			prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM PILOTS P, "
					+ "PIREPS PR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR ON "
					+ "(PR.ID=APR.ID) WHERE (PR.PILOT_ID=P.ID) AND (PR.ASSIGN_ID=?)");
			_ps.setInt(1, id);

			// Return the results
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Flight Reports associated with a particular Online Event.
	 * @param id the Online Event database ID
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByEvent(int id) throws DAOException {
		try {
			prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM PILOTS P, "
					+ "PIREPS PR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR ON "
					+ "(PR.ID=APR.ID) WHERE (PR.PILOT_ID=P.ID) AND (PR.EVENT_ID=?)");
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Reports flown on a certain date.
	 * @param dt the date
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByDate(java.util.Date dt) throws DAOException {
		try {
			prepareStatement("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM PILOTS P, "
				+ "PIREPS PR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR ON "
				+ "(PR.ID=APR.ID) WHERE (PR.PILOT_ID=P.ID) AND (PR.DATE=DATE(?))");
			_ps.setTimestamp(1, createTimestamp(dt));
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Flight Reports for a particular Pilot, using a sort column.
	 * @param id the Pilot database ID
	 * @param orderBy the sort column (or null)
	 * @return a List of FlightReports
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getByPilot(int id, String orderBy) throws DAOException {

		// Build the statement
		StringBuilder buf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.*, PC.COMMENTS, APR.* FROM "
				+ "PILOTS P, PIREPS PR LEFT JOIN PIREP_COMMENT PC ON (PR.ID=PC.ID) LEFT JOIN ACARS_PIREPS APR "
				+ "ON (PR.ID=APR.ID) WHERE (PR.PILOT_ID=P.ID) AND (P.ID=?)");
		if (orderBy != null) {
			buf.append(" ORDER BY PR.");
			buf.append(orderBy);
		}

		try {
			prepareStatement(buf.toString());
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Gets online legs/hours for a particular Pilot.
	 * @param p the Pilot to query for (the object will be updated)
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getOnlineTotals(Pilot p) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT COUNT(PR.FLIGHT_TIME), SUM(PR.FLIGHT_TIME) FROM PIREPS PR WHERE "
					+ "(PR.PILOT_ID=?) AND (PR.STATUS=?) AND ((PR.ATTR & ?) != 0)");
			_ps.setInt(1, p.getID());
			_ps.setInt(2, FlightReport.OK);
			_ps.setInt(3, FlightReport.ATTR_ONLINE_MASK);

			// Execute the query - update the pilot if data found
			ResultSet rs = _ps.executeQuery();
			if (rs.next()) {
				p.setOnlineLegs(rs.getInt(1));
				p.setOnlineHours(rs.getDouble(2));
			}

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns online legs/hours for a group of Pilots .
	 * @param pilots a Map of Pilot objects to populate with results
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getOnlineTotals(Map<Integer, Pilot> pilots, String dbName) throws DAOException {
	   
	   // Trim the database name if it's in DB.TABLE format
	   if (dbName.indexOf('.') != -1)
	      dbName = dbName.substring(0, dbName.indexOf('.'));

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT F.PILOT_ID, COUNT(F.FLIGHT_TIME), ROUND(SUM(F.FLIGHT_TIME), 1) FROM ");
		sqlBuf.append(dbName);
		sqlBuf.append('.');
		sqlBuf.append("PIREPS F WHERE ((F.ATTR & ?) != 0) AND (F.STATUS=?) AND F.PILOT_ID IN (");

		// Append the Pilot IDs
		int setSize = 0;
		for (Iterator<Pilot> i = pilots.values().iterator(); i.hasNext();) {
			Pilot p = i.next();
			if (p.getOnlineLegs() == 0) {
				setSize++;
				sqlBuf.append(String.valueOf(p.getID()));
				sqlBuf.append(',');
			}
		}

		// If we are not querying any pilots, abort
		log.debug("Uncached set size = " + setSize);
		if (setSize == 0)
			return;

		// Strip out trailing comma
		if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
			sqlBuf.setLength(sqlBuf.length() - 1);

		// Close the SQL statement
		sqlBuf.append(") GROUP BY F.PILOT_ID");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, FlightReport.ATTR_ONLINE_MASK);
			_ps.setInt(2, FlightReport.OK);

			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			while (rs.next()) {
				int pilotID = rs.getInt(1);
				Pilot p = pilots.get(new Integer(pilotID));
				if (p != null) {
					p.setOnlineLegs(rs.getInt(2));
					p.setOnlineHours(rs.getDouble(3));
				} else {
					log.warn("Pilot ID " + pilotID + " not in Map");
				}
			}

			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Draft Flight Reports for a particular Pilot (with optional city pair).
	 * @param pilotID the Pilot's Database ID
	 * @param airportD the departure Airport
	 * @param airportA the arrival Airport
	 * @param dbName the database Name
	 * @return a List of Draft FlightReports matching the above criteria
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<FlightReport> getDraftReports(int pilotID, Airport airportD, Airport airportA, String dbName) throws DAOException {

		// Build the prepared statement
		StringBuilder sqlBuf = new StringBuilder("SELECT P.FIRSTNAME, P.LASTNAME, PR.* FROM ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".PILOTS P, ");
		sqlBuf.append(dbName.toLowerCase());
		sqlBuf.append(".PIREPS PR WHERE (PR.PILOT_ID=P.ID) AND (P.ID=?) AND (PR.STATUS=?)");

		// Add departure/arrival airports if specified
		if ((airportD != null) && (airportA != null))
			sqlBuf.append(" AND (PR.AIRPORT_D=?) AND (PR.AIRPORT_A=?)");

		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, pilotID);
			_ps.setInt(2, FlightReport.DRAFT);
			if ((airportD != null) && (airportA != null)) {
				_ps.setString(3, airportD.getIATA());
				_ps.setString(4, airportA.getIATA());
			}

			// Execute the query
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of approved Flight Reports for a particular Pilot.
	 * @param pilotID the Pilot database ID
	 * @return the number of approved Flight Reports
	 * @throws DAOException if a JDBC error occuurs
	 */
	public int getCount(int pilotID) throws DAOException {
	   try {
	      setQueryMax(1);
	      prepareStatement("SELECT COUNT(DISTINCT ID) FROM PIREPS WHERE (PILOT_ID=?) AND (STATUS=?)");
	      _ps.setInt(1, pilotID);
	      _ps.setInt(2, FlightReport.OK);
	      
	      // Execute the query
	      ResultSet rs = _ps.executeQuery();
	      int result = (rs.next()) ? rs.getInt(1) : 0;
	      
	      // Clean up and return
	      rs.close();
	      _ps.close();
	      return result;
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}

	/**
	 * Helper method to load PIREP data.
	 */
	protected List<FlightReport> execute() throws SQLException {
		List<FlightReport> results = new ArrayList<FlightReport>();

		// Do the query and get metadata
		ResultSet rs = _ps.executeQuery();
		ResultSetMetaData md = rs.getMetaData();
		boolean hasACARS = (md.getColumnCount() >= 25);
		boolean hasComments = (md.getColumnCount() >= 24);

		// Iterate throught the results
		while (rs.next()) {
			boolean isACARS = (hasACARS) && (rs.getInt(25) != 0);

			// Build the PIREP as a standard one, or an ACARS pirep
			Airline a = SystemData.getAirline(rs.getString(8));
			FlightReport p = (isACARS) ? new ACARSFlightReport(a, rs.getInt(9), rs.getInt(10)) : new FlightReport(a, rs.getInt(9), rs
					.getInt(10));

			// Populate the data
			p.setFirstName(rs.getString(1));
			p.setLastName(rs.getString(2));
			p.setID(rs.getInt(3));
			p.setDatabaseID(FlightReport.DBID_PILOT, rs.getInt(4));
			p.setRank(rs.getString(5));
			p.setStatus(rs.getInt(6));
			p.setDate(expandDate(rs.getDate(7)));
			p.setAirportD(SystemData.getAirport(rs.getString(11)));
			p.setAirportA(SystemData.getAirport(rs.getString(12)));
			p.setEquipmentType(rs.getString(13));
			p.setFSVersion(rs.getInt(14));
			p.setAttributes(rs.getInt(15));
			// Skip column #16 - we calculate this in the flight report
			p.setLength(Math.round(rs.getFloat(17) * 10));
			p.setRemarks(rs.getString(18));
			p.setDatabaseID(FlightReport.DBID_DISPOSAL, rs.getInt(19));
			p.setSubmittedOn(rs.getTimestamp(20));
			p.setDisposedOn(rs.getTimestamp(21));
			p.setDatabaseID(FlightReport.DBID_EVENT, rs.getInt(22));
			p.setDatabaseID(FlightReport.DBID_ASSIGN, rs.getInt(23));
			if (hasComments)
				p.setComments(rs.getString(24));

			// Load ACARS pirep data
			if (isACARS) {
				ACARSFlightReport ap = (ACARSFlightReport) p;
				ap.setAttribute(FlightReport.ATTR_ACARS, true);
				ap.setDatabaseID(FlightReport.DBID_ACARS, rs.getInt(26));
				ap.setStartTime(rs.getTimestamp(27));
				ap.setTaxiTime(rs.getTimestamp(28));
				ap.setTaxiWeight(rs.getInt(29));
				ap.setTaxiFuel(rs.getInt(30));
				ap.setTakeoffTime(rs.getTimestamp(31));
				ap.setTakeoffDistance(rs.getInt(32));
				ap.setTakeoffSpeed(rs.getInt(33));
				ap.setTakeoffN1(rs.getDouble(34));
				ap.setTakeoffWeight(rs.getInt(35));
				ap.setTakeoffFuel(rs.getInt(36));
				ap.setLandingTime(rs.getTimestamp(37));
				ap.setLandingDistance(rs.getInt(38));
				ap.setLandingSpeed(rs.getInt(39));
				ap.setLandingVSpeed(rs.getInt(40));
				ap.setLandingN1(rs.getDouble(41));
				ap.setLandingWeight(rs.getInt(42));
				ap.setLandingFuel(rs.getInt(43));
				ap.setEndTime(rs.getTimestamp(44));
				ap.setGateWeight(rs.getInt(45));
				ap.setGateFuel(rs.getInt(46));
				ap.setTime1X(rs.getInt(47));
				ap.setTime2X(rs.getInt(48));
				ap.setTime4X(rs.getInt(49));
			}

			// Add the flight report to the results
			results.add(p);
		}

		// Clean up and return results
		rs.close();
		_ps.close();
		return results;
	}

	/**
	 * Populates the Equipment Types a collection of PIREPs counts towards promotion in.
	 * @param pireps a Collection of FlightReport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getCaptEQType(Collection<FlightReport> pireps) throws DAOException {
		
		// Do nothing if empty
		if (pireps.isEmpty())
			return;
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ID, EQTYPE FROM PROMO_EQ WHERE (ID IN (");
		for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext(); ) {
			FlightReport fr = i.next();
			sqlBuf.append(String.valueOf(fr.getID()));
			if (i.hasNext())
				sqlBuf.append(',');
		}
		
		sqlBuf.append("))");
		
		// Convert PIREPs to a Map for lookup
		Map pMap = CollectionUtils.createMap(pireps, "ID");
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());

			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				FlightReport fr = (FlightReport) pMap.get(new Integer(rs.getInt(1)));
				if (fr != null)
					fr.setCaptEQType(rs.getString(2));
			}
			
			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to load data for flights counting towards promotion.
	 */
	private Collection<String> getCaptEQType(int id) throws SQLException {

		// Build the prepared statement and execute the query
		prepareStatementWithoutLimits("SELECT EQTYPE FROM PROMO_EQ WHERE (ID=?) ORDER BY EQTYPE");
		_ps.setInt(1, id);
		ResultSet rs = _ps.executeQuery();

		// Iterate through the results
		Set<String> results = new LinkedHashSet<String>();
		while (rs.next())
			results.add(rs.getString(1));

		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}