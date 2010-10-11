// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load flight routes from approved Flight Reports. 
 * @author Luke
 * @version 3.3
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
	 * Loads Flight Routes from Flight Reports in the current database.
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @return a Collection of FlightRoutes
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<? extends FlightRoute> getRoutes(Airport aD, Airport aA) throws DAOException {
		return getRoutes(aD, aA, SystemData.get("airline.db"));
	}
	
	/**
	 * Loads Flight Routes from Flight Reports.
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @param dbName the database name
	 * @return a Collection of FlightRoutes
	 * @throws DAOException if a JDBC error occurs
	 */	
	public Collection<? extends FlightRoute> getRoutes(Airport aD, Airport aA, String dbName) throws DAOException {
		
		// Build the SQL statement
		String db = formatDBName(dbName);
		StringBuilder sqlBuf = new StringBuilder("SELECT PRT.ROUTE, PR.SUBMITTED, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME) "
				+ "AS PNAME, APR.ACARS_ID, F.CRUISE_ALT FROM ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREP_ROUTE PRT, ");
		sqlBuf.append(db);
		sqlBuf.append(".PILOTS P, ");
		sqlBuf.append(db);
		sqlBuf.append(".PIREPS PR LEFT JOIN ");
		sqlBuf.append(db);
		sqlBuf.append(".ACARS_PIREPS APR ON (PR.ID=APR.ID) LEFT JOIN acars.FLIGHTS F ON (APR.ACARS_ID=F.ID) WHERE "
			+ "(PR.ID=PRT.ID) AND (P.ID=PR.PILOT_ID) AND (PR.AIRPORT_D=?) AND (PR.AIRPORT_A=?) AND (PR.STATUS=?) "
			+ "ORDER BY PR.SUBMITTED DESC");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, aD.getIATA());
			_ps.setString(2, aA.getIATA());
			_ps.setInt(3, FlightReport.OK);
			
			// Execute the query
			Collection<FlightRoute> results = new ArrayList<FlightRoute>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				ExternalRoute rt = new ExternalRoute();
				rt.setAirportD(aD);
				rt.setAirportA(aA);
				rt.setCreatedOn(rs.getTimestamp(2));
				rt.setSource("ACARS Flight #" + rs.getInt(4) + " flown by " + rs.getString(3) + " on " + 
						StringUtils.format(rt.getCreatedOn(), "dd-MMM-yyyy"));
				
				// Get the SID/STAR out of the route
				String rawRoute = rs.getString(1);
				List<String> wps = StringUtils.split(rawRoute, " ");
				if (wps.size() > 1) {
					if (TerminalRoute.isNameValid(wps.get(0))) {
						rt.setSID(wps.get(0) + "." + wps.get(1));
						wps.remove(0);
					}
					
					String last = wps.get(wps.size() - 1);
					if (TerminalRoute.isNameValid(last) && (wps.size() > 1)) {
						rt.setSTAR(last + "." + wps.get(wps.size() - 2));
						wps.remove(wps.size() - 1);
					}
					
					rt.setRoute(StringUtils.listConcat(wps, " "));
				} else
					rt.setRoute(rawRoute);

				results.add(rt);
			}
			
			// Clean up
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}