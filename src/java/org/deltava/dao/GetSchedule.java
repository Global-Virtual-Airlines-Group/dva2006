// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.time.temporal.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to search the Flight Schedule.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetSchedule extends DAO {
	
	private java.time.Instant _effDate = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant(ZoneOffset.UTC);
	
	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSchedule(Connection c) {
		super(c);
	}
	
	/**
	 * Sets the effective date of the Schedule, for DST calculations.
	 * @param dt the effective date/time or null for today
	 */
	public void setEffectiveDate(Instant dt) {
		if (dt != null)
			_effDate = dt.truncatedTo(ChronoUnit.DAYS);
	}

	/**
	 * Return a particular flight from the Schedule database.
	 * @param f the Flight to return, using the airline code, flight number and leg
	 * @param dbName the database name
	 * @return a ScheduleEntry matching the criteria, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if f is null
	 */
	public ScheduleEntry get(Flight f, String dbName) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".SCHEDULE WHERE (AIRLINE=?) AND (FLIGHT=?)");
		if (f.getLeg() != 0)
			sqlBuf.append("AND (LEG=?)");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, f.getAirline().getCode());
			ps.setInt(2, f.getFlightNumber());
			if (f.getLeg() != 0)
				ps.setInt(3, f.getLeg());

			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Return a particular flight from the Schedule database.
	 * @param f the Flight to return, using the airline code, flight number and leg
	 * @return a ScheduleEntry matching the criteria, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if f is null
	 */
	public ScheduleEntry get(Flight f) throws DAOException {
		return get(f, SystemData.get("airline.db"));
	}

	/**
	 * Returns all flights from a particular airport, sorted by Airline and Flight Number.
	 * @param a the origin Airport bean
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleEntry> getFlights(Airport a) throws DAOException {
		return getFlights(a, null);
	}
	
	/**
	 * Returns all flights from a particular airport with a particular airline, sorted by Airline and Flight Number.
	 * @param a the origin Airport bean
	 * @param al the Airline bean
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleEntry> getFlights(Airport a, Airline al) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT * FROM SCHEDULE WHERE (AIRPORT_D=?) ");
		if (al != null)
			sqlBuf.append("AND (AIRLINE=?)");
		sqlBuf.append("ORDER BY AIRLINE, FLIGHT, LEG");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, a.getIATA());
			if (al != null)
				ps.setString(2, al.getCode());
			
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Return a particular flight from the Schedule database.
	 * @param aCode the Airline Code
	 * @param flightNumber the Flight Number
	 * @param leg the Leg
	 * @return a ScheduleEntry matching the criteria, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public ScheduleEntry get(String aCode, int flightNumber, int leg) throws DAOException {
		return get(new ScheduleEntry(new Airline(aCode), flightNumber, leg), SystemData.get("airline.db"));
	}
	
	/**
	 * Returns the Airlines that provide service on a particular route.
	 * @param rp the RoutePair
	 * @return a Collection of Airline beans
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public Collection<Airline> getAirlines(RoutePair rp) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT AIRLINE FROM SCHEDULE WHERE ((AIRPORT_D=?) OR (AIRPORT_D=?)) AND ((AIRPORT_A=?) OR (AIRPORT_A=?)) AND (ACADEMY=?)")) {
			ps.setString(1, rp.getAirportD().getIATA());
			ps.setString(2, rp.getAirportD().getSupercededAirport());
			ps.setString(3, rp.getAirportA().getIATA());
			ps.setString(4, rp.getAirportA().getSupercededAirport());
			ps.setBoolean(5, false);
			
			// Execute the query
			Collection<Airline> results = new TreeSet<Airline>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(SystemData.getAirline(rs.getString(1)));	
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns Airports only serviced by Flight Academy flights.
	 * @return a Collection of Airports
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getAcademyAirports() throws DAOException {
		try {
			Collection<Airport> results = new TreeSet<Airport>();
			
			// Select departure airports
			try (PreparedStatement ps = prepareWithoutLimits("SELECT AIRPORT_D, SUM(1) AS CNT, SUM(ACADEMY) AS FACNT FROM SCHEDULE GROUP BY AIRPORT_D HAVING (CNT=FACNT)")) {
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						results.add(SystemData.getAirport(rs.getString(1)));	
				}
			}
			
			// Select arrival airports
			try (PreparedStatement ps = prepareWithoutLimits("SELECT AIRPORT_A, SUM(1) AS CNT, SUM(ACADEMY) AS FACNT FROM SCHEDULE GROUP BY AIRPORT_A HAVING (CNT=FACNT)")) {
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						results.add(SystemData.getAirport(rs.getString(1)));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the average flight time for all flights in the Schedule database between two airports.
	 * @param rp the RoutePair
	 * @param dbName the database name
	 * @return a FlightTime bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightTime getFlightTime(RoutePair rp, String dbName) throws DAOException {

		// Build the prepared statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AIRPORT_D, AIRPORT_A, IFNULL(ROUND(AVG(FLIGHT_TIME)), 0), SUM(1) AS CNT, SUM(HISTORIC) AS HST FROM ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".SCHEDULE WHERE ((AIRPORT_D=?) OR (AIRPORT_D=?)) AND ((AIRPORT_A=?) OR (AIRPORT_A=?)) AND (ACADEMY=?) GROUP BY AIRPORT_D, AIRPORT_A ORDER BY IF(AIRPORT_D=?, 0, 1), IF (AIRPORT_A=?, 0, 1)");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, rp.getAirportD().getIATA());
			ps.setString(2, rp.getAirportD().getSupercededAirport());
			ps.setString(3, rp.getAirportA().getIATA());
			ps.setString(4, rp.getAirportA().getSupercededAirport());
			ps.setBoolean(5, false);
			ps.setString(6, rp.getAirportD().getIATA());
			ps.setString(7, rp.getAirportA().getIATA());

			// Execute the Query
			FlightTime result = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					boolean hasHistoric = (rs.getInt(5) > 0); boolean hasCurrent = (rs.getInt(4) > rs.getInt(5));
					result = new FlightTime(rs.getInt(3), (hasHistoric && hasCurrent) ? RoutePairType.HYBRID : (hasHistoric ? RoutePairType.HISTORIC : RoutePairType.PRESENT));
				} else
					result = new FlightTime(0, RoutePairType.UNKNOWN);
			}

			return result;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the average flight time for all flights in the Schedule database between two airports.
	 * @param rp the RoutePair
	 * @return a FlightTime bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public FlightTime getFlightTime(RoutePair rp) throws DAOException {
		return getFlightTime(rp, SystemData.get("airline.db"));
	}
	
	/**
	 * Returns the most appropriate flight/leg number between two airports. 
	 * @param sr the ScheduleRoute
	 * @param hourOfDay the preferred hour of day (0-23)
	 * @param dbName the database name
	 * @return a ScheduleEntry bean with the Airline, Flight and Leg number or null if none found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public ScheduleEntry getFlightNumber(ScheduleRoute sr, int hourOfDay, String dbName) throws DAOException {

		// Build the SQL Statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT S.AIRLINE, S.FLIGHT, S.LEG, S.EQTYPE, S.TIME_D, S.TIME_A, A.HISTORIC, S.SRC FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".SCHEDULE S, common.AIRLINES A, common.AIRLINEINFO AI WHERE (AI.DBNAME=?) AND (S.AIRLINE=A.CODE) AND ((S.AIRPORT_D=?) OR (S.AIRPORT_D=?)) AND "
			+ "((S.AIRPORT_A=?) OR (S.AIRPORT_A=?)) AND (S.ACADEMY=0) ORDER BY IF(S.AIRPORT_D=?,0,1), IF(S.AIRPORT_A=?,0,1), IF(S.AIRLINE=?,0,IF(S.AIRLINE=AI.CODE,1,2)), "
			+ "IF(A.HISTORIC=1,0,1), ABS(HOUR(S.TIME_D)-?), S.FLIGHT LIMIT 1");

		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, db);
			ps.setString(2, sr.getAirportD().getIATA());
			ps.setString(3, sr.getAirportD().getSupercededAirport());
			ps.setString(4, sr.getAirportA().getIATA());
			ps.setString(5, sr.getAirportA().getSupercededAirport());
			ps.setString(6, sr.getAirportD().getIATA());
			ps.setString(7, sr.getAirportA().getIATA());
			ps.setString(8, sr.getAirline().getCode());
			ps.setInt(9, hourOfDay);
			
			// Execute the query
			ScheduleEntry se = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					se = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
					se.setEquipmentType(rs.getString(4));
					se.setAirportD(sr.getAirportD());
					se.setAirportA(sr.getAirportA());
					se.setTimeD(rs.getTimestamp(5).toLocalDateTime().plusSeconds(_effDate.getEpochSecond()));
					se.setTimeA(rs.getTimestamp(6).toLocalDateTime().plusSeconds(_effDate.getEpochSecond()));
					se.setSource(ScheduleSource.values()[rs.getInt(7)]);
				}
			}

			return se;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the lowest flight/leg number between two airports. 
	 * @param rp the RoutePair
	 * @param dbName the database name
	 * @return a ScheduleEntry bean with the Airline, Flight and Leg number or null if none found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if airportD or airportA are null
	 */
	public ScheduleEntry getFlightNumber(RoutePair rp, String dbName) throws DAOException {
		
		// Build the SQL Statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT S.AIRLINE, S.FLIGHT, S.LEG, S.EQTYPE, A.HISTORIC, AI.CODE FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".SCHEDULE S, common.AIRLINES A, common.AIRLINEINFO AI WHERE (AI.DBNAME=?) AND (S.AIRLINE=A.CODE) "
			+ "AND ((S.AIRPORT_D=?) OR (S.AIRPORT_D=?)) AND ((S.AIRPORT_A=?) OR (S.AIRPORT_A=?)) AND (S.ACADEMY=?) "
			+ "ORDER BY IF(S.AIRPORT_D=?,0,1), IF(S.AIRPORT_A=?,0,1), IF(S.AIRLINE=AI.CODE,1,0), IF(A.HISTORIC=1,0,1), S.FLIGHT LIMIT 1");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, db);
			ps.setString(2, rp.getAirportD().getIATA());
			ps.setString(3, rp.getAirportD().getSupercededAirport());
			ps.setString(4, rp.getAirportA().getIATA());
			ps.setString(5, rp.getAirportA().getSupercededAirport());
			ps.setBoolean(6, false);
			ps.setString(7, rp.getAirportD().getIATA());
			ps.setString(8, rp.getAirportA().getIATA());
			
			// Execute the query
			ScheduleEntry se = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					se = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
					se.setEquipmentType(rs.getString(4));
					se.setAirportD(rp.getAirportD());
					se.setAirportA(rp.getAirportA());
				}
			}
			
			return se;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Exports the entire Flight Schedule.
	 * @return a Collection of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleEntry> export() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM SCHEDULE ORDER BY AIRLINE, FLIGHT, LEG")) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Airports and the Airports they are directly connected to.
	 * @param includeHistoric an Inclusion to exclude/include Historic flights 
	 * @return a Collection of ScheduleRoutes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<ScheduleRoute> getRoutePairs(Inclusion includeHistoric) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT AIRPORT_D, AIRPORT_A, COUNT(FLIGHT), SUM(HISTORIC) FROM SCHEDULE ");
		if (includeHistoric != Inclusion.ALL)
			sqlBuf.append("WHERE (HISTORIC=?) ");
		
		sqlBuf.append("GROUP BY AIRPORT_D, AIRPORT_A");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) { 
			Collection<ScheduleRoute> results = new ArrayList<ScheduleRoute>();
			if (includeHistoric != Inclusion.ALL)
				ps.setBoolean(1, (includeHistoric == Inclusion.INCLUDE));
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ScheduleRoute rp = new ScheduleRoute(SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)));
					rp.setFlights(rs.getInt(3));
					boolean hasHistoric = (rs.getInt(4) > 1); boolean hasCurrent = (rs.getInt(3) > rs.getInt(4));
					rp.setType(hasHistoric && hasCurrent ? RoutePairType.HYBRID : (hasCurrent ? RoutePairType.PRESENT : RoutePairType.HISTORIC));
					results.add(rp);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to query the database.
	 * @param ps a PreparedStatement
	 * @return a List of ScheduleEntry beans
	 * @throws SQLException if an error occurs
	 */
	protected List<ScheduleEntry> execute(PreparedStatement ps) throws SQLException {
		List<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		try (ResultSet rs = ps.executeQuery()) {
			boolean hasDispatch = (rs.getMetaData().getColumnCount() > 15);
			boolean hasRouteCounts = (rs.getMetaData().getColumnCount() > 17);
			while (rs.next()) {
				ScheduleEntry entry = null;
				if (hasDispatch) {
					ScheduleSearchEntry sse = new ScheduleSearchEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
					sse.setDispatchRoutes(rs.getInt(16));
					if (hasRouteCounts) {
						sse.setFlightCount(rs.getInt(17));
						sse.setLastFlownOn(rs.getTimestamp(18));
					}
					
					entry = sse;
				} else
					entry = new ScheduleEntry(SystemData.getAirline(rs.getString(1)), rs.getInt(2), rs.getInt(3));
			
				entry.setAirportD(SystemData.getAirport(rs.getString(4)));
				entry.setAirportA(SystemData.getAirport(rs.getString(5)));
				entry.setEquipmentType(rs.getString(7));
				entry.setLength(rs.getInt(8));
				entry.setTimeD(rs.getTimestamp(9).toLocalDateTime().plusSeconds(_effDate.getEpochSecond()));
				entry.setTimeA(rs.getTimestamp(10).toLocalDateTime().plusSeconds(_effDate.getEpochSecond()));
				entry.setHistoric(rs.getBoolean(11));
				entry.setCanPurge(rs.getBoolean(12));
				entry.setAcademy(rs.getBoolean(13));
				entry.setSource(ScheduleSource.values()[rs.getInt(14)]);
				entry.setCodeShare(rs.getString(15));
				results.add(entry);
			}
		}

		return results;
	}
}