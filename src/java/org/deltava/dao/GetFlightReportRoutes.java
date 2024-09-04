// Copyright 2010, 2011, 2014, 2016, 2018, 2019, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to load flight routes from approved Flight Reports. 
 * @author Luke
 * @version 11.2
 * @since 3.3
 */

public class GetFlightReportRoutes extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFlightReportRoutes(Connection c) {
		super(c);
	}

	/**
	 * Loads Flight Routes from Flight Reports.
	 * @param rp the RoutePair
	 * @param dbName the database name
	 * @return a Collection of FlightRoutes
	 * @throws DAOException if a JDBC error occurs
	 */	
	public Collection<? extends FlightRoute> getRoutes(RoutePair rp, String dbName) throws DAOException {
		
		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PRT.ROUTE, MAX(PR.SUBMITTED) AS LF, F.CRUISE_ALT, COUNT(APR.ACARS_ID) AS CNT FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_ROUTE PRT, ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN acars.FLIGHTS F ON (APR.ACARS_ID=F.ID) WHERE (PR.ID=PRT.ID) AND (PR.AIRPORT_D=?) AND (PR.AIRPORT_A=?) AND (PR.STATUS=?) "
			+ "AND (F.CRUISE_ALT IS NOT NULL) GROUP BY PRT.ROUTE HAVING (LF>DATE_SUB(CURDATE(), INTERVAL 2 YEAR)) ORDER BY CNT DESC, LF DESC");
		
		try (PreparedStatement ps = prepare(sqlBuf.toString())) {
			ps.setString(1, rp.getAirportD().getIATA());
			ps.setString(2, rp.getAirportA().getIATA());
			ps.setInt(3, FlightStatus.OK.ordinal());
			
			// Execute the query
			int maxCount = 0; boolean doMore = true; int id = 0;
			Collection<FlightRoute> results = new ArrayList<FlightRoute>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next() && doMore) {
					ExternalRoute rt = new ExternalRoute("ACARS");
					rt.setID(++id);
					rt.setAirportD(rp.getAirportD());
					rt.setAirportA(rp.getAirportA());
					rt.setCreatedOn(rs.getTimestamp(2).toInstant());
					rt.setCruiseAltitude(rs.getString(3));
					rt.setUseCount(rs.getInt(4));
					rt.setComments(String.format("%d ACARS Flight%s, last on %s", Integer.valueOf(rt.getUseCount()), (rt.getUseCount() > 1) ? "s" : "", StringUtils.format(rt.getCreatedOn(), "dd-MMM-yyyy")));
					maxCount = Math.max(maxCount, rt.getUseCount());
				
					// Get the SID/STAR out of the route
					String rawRoute = rs.getString(1);
					List<String> wps = StringUtils.split(rawRoute, " ");
					if (wps.size() > 1) {
						if (TerminalRoute.isNameValid(wps.get(0))) {
							rt.setSID(wps.get(0) + "." + wps.get(1));
							wps.remove(0);
						}
					
						String last = wps.getLast();
						if (TerminalRoute.isNameValid(last) && (wps.size() > 1)) {
							rt.setSTAR(last + "." + wps.get(wps.size() - 2));
							wps.remove(wps.getLast());
						}
					
						rt.setRoute(StringUtils.listConcat(wps, " "));
					} else
						rt.setRoute(rawRoute);

					// Restrict to routes tht are at least 25% as popular as the most popular route, and we have at least 4
					doMore = (rt.getUseCount() >= (maxCount >> 2)) || (results.size() < 5);
					if (doMore)
						results.add(rt);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}