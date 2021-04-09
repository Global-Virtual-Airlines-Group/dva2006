// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.*;

import org.deltava.beans.TZInfo;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Airport data.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class GetAirport extends DAO {

	private String _appCode;

	/**
	 * Creates the DAO with a JDBC connection.
	 * @param c the JDBC connection to use
	 */
	public GetAirport(Connection c) {
		super(c);
		setAppCode(SystemData.get("airline.code"));
	}
	
	/**
	 * Overrides the application code (for use by the ACARS server).
	 * @param code the new application code
	 * @throws NullPointerException if code is null
	 */
	public void setAppCode(String code) {
		_appCode = code.toUpperCase();
	}

	/**
	 * Returns an airport object by its IATA or ICAO code.
	 * @param code the airport IATA or ICAO code
	 * @return an Airport object matching the requested code, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 */
	public Airport get(String code) throws DAOException {
		try {
			Airport a = null;
			try (PreparedStatement ps = prepareWithoutLimits("SELECT A.*, ND.ALTITUDE, ND.REGION, IFNULL(MAX(R.LENGTH), MAX(RND.ALTITUDE)), COUNT(DISTINCT GA.NAME) AS GCNT FROM common.AIRPORTS A "
				+ "LEFT JOIN common.NAVDATA ND ON ((ND.CODE=A.ICAO) AND (ND.ITEMTYPE=?)) LEFT JOIN common.RUNWAYS R ON (A.ICAO=R.ICAO) LEFT JOIN common.NAVDATA RND "
				+ "ON ((RND.CODE=A.ICAO) AND (RND.ITEMTYPE=?)) LEFT JOIN common.GATE_AIRLINES GA ON (GA.ICAO=A.ICAO) WHERE ((A.ICAO=?) OR (A.IATA=?)) GROUP BY A.IATA LIMIT 1")) {
				ps.setInt(1, Navaid.AIRPORT.ordinal());
				ps.setInt(2, Navaid.RUNWAY.ordinal());
				ps.setString(3, code.toUpperCase());
				ps.setString(4, code.toUpperCase());

				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
						a.setCountry(Country.get(rs.getString(5)));
						a.setTZ(TZInfo.get(rs.getString(3)));
						a.setLocation(rs.getDouble(6), rs.getDouble(7));
						a.setASDE(rs.getBoolean(8));
						a.setHasPFI(rs.getBoolean(9));
						a.setIsSchengen(rs.getBoolean(10));
						a.setSupercededAirport(rs.getString(11));
						a.setAltitude(rs.getInt(12));
						a.setRegion(rs.getString(13));
						a.setGateData(rs.getInt(15) > 0);
						int maxRunway = rs.getInt(14);
						a.setMaximumRunwayLength((maxRunway == 0) ? 2500 : maxRunway);
					}
				}
			}

			if (a == null) return null;

			// Pull in the airline data
			try (PreparedStatement ps = prepareWithoutLimits("SELECT CODE FROM common.AIRPORT_AIRLINE WHERE (IATA=?) AND (APPCODE=?)")) {
				ps.setString(1, a.getIATA());
				ps.setString(2, _appCode);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						a.addAirlineCode(rs.getString(1));
				}
			}

			return a;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all Airports served by a particular Airline.
	 * @param al the Airline to query with
	 * @param sortBy the SORT BY column
	 * @return a List of Airport objects
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if al is null
	 */
	public Collection<Airport> getByAirline(Airline al, String sortBy) throws DAOException {
		
		// Build SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT A.*, ND.ALTITUDE, ND.REGION, IFNULL(MAX(R.LENGTH), MAX(RND.ALTITUDE)), COUNT(DISTINCT GA.NAME) AS GCNT FROM common.AIRPORTS A "
			+ "LEFT JOIN common.AIRPORT_AIRLINE AA ON ((AA.APPCODE=?) AND (A.IATA=AA.IATA)) LEFT JOIN common.NAVDATA ND ON ((ND.CODE=A.ICAO) AND (ND.ITEMTYPE=?)) "
			+ "LEFT JOIN common.RUNWAYS R ON (A.ICAO=R.ICAO) LEFT JOIN common.NAVDATA RND ON ((RND.CODE=A.ICAO) AND (RND.ITEMTYPE=?)) LEFT JOIN common.GATE_AIRLINES GA ON (A.ICAO=GA.ICAO) WHERE ");
		sqlBuf.append((al == null) ? "(AA.CODE IS NULL)" : "(AA.CODE=?)");
		sqlBuf.append(" GROUP BY A.IATA");
		if (!StringUtils.isEmpty(sortBy)) {
			sqlBuf.append(" ORDER BY A.");
			sqlBuf.append(sortBy);
		}
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, _appCode);
			ps.setInt(2, Navaid.AIRPORT.ordinal());
			ps.setInt(3, Navaid.RUNWAY.ordinal());
			if (al != null)
				ps.setString(4, al.getCode());

			// Execute the query
			List<Airport> results = new ArrayList<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
					a.setCountry(Country.get(rs.getString(5)));
					a.setTZ(TZInfo.get(rs.getString(3)));
					a.setLocation(rs.getDouble(6), rs.getDouble(7));
					a.setASDE(rs.getBoolean(8));
					a.setHasPFI(rs.getBoolean(9));
					a.setIsSchengen(rs.getBoolean(10));
					a.setSupercededAirport(rs.getString(11));
					a.setAltitude(rs.getInt(12));
					a.setRegion(rs.getString(13));
					a.setGateData(rs.getInt(15) > 0);
					int maxRunway = rs.getInt(14);
					a.setMaximumRunwayLength((maxRunway == 0) ? 2500 : maxRunway);
					if (al != null)
						a.addAirlineCode(al.getCode());
					
					results.add(a);
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Airports visited by a particular Pilot.
	 * @param id the Pilot's database ID
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getByPilot(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT AIRPORT_D, AIRPORT_A FROM PIREPS P WHERE (PILOT_ID=?) ORDER BY ID")) {
			ps.setInt(1, id);
			
			Collection<Airport> results = new LinkedHashSet<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					results.add(SystemData.getAirport(rs.getString(1)));
					results.add(SystemData.getAirport(rs.getString(2)));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Airports with upcoming Online Events.
	 * @return a Collection of Airports
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getEventAirports() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT EA.AIRPORT_D, EA.AIRPORT_A FROM events.EVENT_AIRPORTS EA, events.EVENTS E WHERE (E.ID=EA.ID) AND (E.ENDTIME > NOW())")) {
			Collection<Airline> airlines = SystemData.getApps().stream().map(AirlineInformation::getCode).map(c -> SystemData.getAirline(c)).filter(Objects::nonNull).collect(Collectors.toSet());
			
			Collection<Airport> results = new LinkedHashSet<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airport aD = (Airport) SystemData.getAirport(rs.getString(1)).clone();
					airlines.forEach(al -> aD.addAirlineCode(al.getCode()));
					results.add(aD);
					Airport aA = (Airport) SystemData.getAirport(rs.getString(2)).clone();
					airlines.forEach(al -> aA.addAirlineCode(al.getCode()));
					results.add(aA);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Airports with Terminal Routes.
	 * @return a Collection of Airport beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getWithTerminalRoutes() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT ICAO FROM common.SID_STAR")) {
			Collection<Airport> results = new LinkedHashSet<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airport a = SystemData.getAirport(rs.getString(1));
					if (a != null)
						results.add(a);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all airports.
	 * @return a Map of Airports, keyed by IATA/ICAO codes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Airport> getAll() throws DAOException {
		Map<String, Airport> results = new HashMap<String, Airport>();
		try {
			try (PreparedStatement ps = prepareWithoutLimits("SELECT A.*, ND.ALTITUDE, ND.REGION, IFNULL(MAX(R.LENGTH), MAX(RND.ALTITUDE)), COUNT(DISTINCT GA.NAME) AS GCNT FROM common.AIRPORTS A "
					+ "LEFT JOIN common.NAVDATA ND ON ((ND.CODE=A.ICAO) AND (ND.ITEMTYPE=?)) LEFT JOIN common.NAVDATA RND ON ((RND.CODE=A.ICAO) AND (RND.ITEMTYPE=?)) "
					+ "LEFT JOIN common.RUNWAYS R ON (A.ICAO=R.ICAO) LEFT JOIN common.GATE_AIRLINES GA ON (GA.ICAO=A.ICAO) GROUP BY A.IATA")) {
				ps.setInt(1, Navaid.AIRPORT.ordinal());
				ps.setInt(2, Navaid.RUNWAY.ordinal());
			
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						Airport a = new Airport(rs.getString(1), rs.getString(2), rs.getString(4));
						a.setTZ(TZInfo.get(rs.getString(3)));
						a.setCountry(Country.get(rs.getString(5)));
						a.setLocation(rs.getDouble(6), rs.getDouble(7));
						a.setASDE(rs.getBoolean(8));
						a.setHasPFI(rs.getBoolean(9));
						a.setIsSchengen(rs.getBoolean(10));
						a.setSupercededAirport(rs.getString(11));
						a.setAltitude(rs.getInt(12));
						a.setRegion(rs.getString(13));
						a.setGateData(rs.getInt(15) > 0);
						int maxRunway = rs.getInt(14);
						a.setMaximumRunwayLength((maxRunway == 0) ? 2500 : maxRunway);
					
						// Save in the map
						results.put(a.getIATA(), a);
						if (!results.containsKey(a.getICAO()))
							results.put(a.getICAO(), a);
					}
				}
			}

			// Load the airlines for each airport and execute the query
			boolean isAll = "ALL".equalsIgnoreCase(_appCode);
			try (PreparedStatement ps = prepareWithoutLimits(isAll ? "SELECT * FROM common.AIRPORT_AIRLINE" : "SELECT * FROM common.AIRPORT_AIRLINE WHERE (APPCODE=?)")) {
				if (!isAll)
					ps.setString(1, _appCode);
				
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String code = rs.getString(2);
						Airport a = results.get(code);
						if (a != null)
							a.addAirlineCode(rs.getString(1));
					}
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the ICAO code for a particular airport from the DAFIF database.
	 * @param iata the IATA code
	 * @return the ICAO code, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getICAO(String iata) throws DAOException {
		if ((iata == null) || (iata.length() != 3)) return iata;
		
		try (PreparedStatement ps = prepareWithoutLimits("SELECT ICAO FROM common.AIRPORT_CODES WHERE (IATA=?) LIMIT 1")) {
			ps.setString(1, iata.toUpperCase());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString(1) : null;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the number of Airports served by each Airline.
	 * @return a Map of counts, keyed by Airline
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Airline, Integer> getAirportCounts() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT CODE, COUNT(DISTINCT IATA) AS CNT FROM common.AIRPORT_AIRLINE GROUP BY CODE ORDER BY CNT DESC")) {
			Map<Airline, Integer> results = new LinkedHashMap<Airline, Integer>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airline al = SystemData.getAirline(rs.getString(1));
					if (al != null)
						results.put(al, Integer.valueOf(rs.getInt(2)));
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}