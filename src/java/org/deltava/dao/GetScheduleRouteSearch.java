// Copyright 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to create multi-leg routes between airports.
 * @author Luke
 * @version 5.1
 * @since 4.1
 */

@Deprecated
public class GetScheduleRouteSearch extends GetSchedule {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetScheduleRouteSearch(Connection c) {
		super(c);
	}
	
	/**
	 * Builds a route between two airports.
	 * @param rp the RoutePair
	 * @return a List of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Airport> findRoute(RoutePair rp) throws DAOException {
		return findRoute(rp, 1);
	}

	/**
	 * Builds a route between two airports.
	 * @param rp the RoutePair
	 * @param level the recursion level
	 * @return a List of ScheduleEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	private List<Airport> findRoute(RoutePair rp, int level) throws DAOException {
		try {
			prepareStatement("SELECT COUNT(*) FROM SCHEDULE WHERE (AIRPORT_A=?)");
			_ps.setString(1, rp.getAirportA().getIATA());
			boolean validDest = false;
			try (ResultSet rs = _ps.executeQuery()) {
				if (rs.next())
					validDest = (rs.getInt(1) > 0);
			}
			
			_ps.close();
			if (!validDest)
				return Collections.emptyList();
			
			prepareStatement("SELECT S.AIRPORT_A, COUNT(S.FLIGHT) AS CNT, (SELECT COUNT(FLIGHT) FROM "
				+ "SCHEDULE SS WHERE (SS.AIRPORT_A=?) AND (SS.AIRPORT_D=S.AIRPORT_A)) AS LEG2, "
				+ "IF(AIRPORT_A=?,0,1) AS ISDST FROM SCHEDULE S WHERE (AIRPORT_D=?) GROUP BY S.AIRPORT_A "
				+ "ORDER BY ISDST, LEG2 DESC, CNT DESC");
			_ps.setString(1, rp.getAirportA().getIATA());
			_ps.setString(2, rp.getAirportA().getIATA());
			_ps.setString(3, rp.getAirportD().getIATA());
			
			// See if we can find anything
			Collection<Airport> results = new LinkedHashSet<Airport>();
			Collection<RoutePair> legInfo = new LinkedHashSet<RoutePair>();
			results.add(rp.getAirportD()); boolean foundDest = false;
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					Airport a = SystemData.getAirport(rs.getString(1));
					int leg2 = rs.getInt(3); boolean isDest = (rs.getInt(4) == 0);
					if (isDest || (leg2 > 0)) {
						 foundDest = true;
						results.add(a);
						if (!isDest)
							 results.add(rp.getAirportA());
						
						break;
					}
					
					legInfo.add(new ScheduleRoute(a, rp.getAirportA()));
				}
			}
			
			_ps.close();
			
			// If we got back some results, return them
			if (foundDest)
				return new ArrayList<Airport>(results);
			else if (level > 4)
				return Collections.emptyList();
			
			// Iterate through the list and find something
			for (RoutePair lp : legInfo) {
				List<Airport> rcResults = findRoute(lp, level + 1);
				if (rcResults.size() > 0) {
					results.addAll(rcResults);
					return new ArrayList<Airport>(results);
				}
			}
			
			return Collections.emptyList();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}