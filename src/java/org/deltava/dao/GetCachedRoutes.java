// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load cached external routes from the database.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class GetCachedRoutes extends DAO {

	private class CachedRoute extends FlightRoute implements ExternalFlightRoute {
		
		private String _source;
		
		CachedRoute(Airport aD, Airport aA) {
			super();
			setAirportD(aD);
			setAirportA(aA);
		}
		
		public String getSource() {
			return _source;
		}
		
		public void setSource(String src) {
			_source = src;
		}
		
		public String getComboAlias() {
			return getRoute();
		}

		public String getComboName() {
			return toString();
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
	}
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCachedRoutes(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves the average age of cached routes between two airports. 
	 * @param aD the departure Airport bean
	 * @param aA the arrival Airport bean
	 * @return the average age in days, or -1 if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getAverageAge(Airport aD, Airport aA) throws DAOException {
		try {
			prepareStatement("SELECT IFNULL(ROUND(AVG(DATEDIFF(NOW(), CREATED))), -1) FROM common.ROUTE_CACHE "
					+ "WHERE (AIRPORT_D=?) AND (AIRPORT_A=?)");
			_ps.setString(1, aD.getICAO());
			_ps.setString(2, aA.getICAO());
			ResultSet rs = _ps.executeQuery();
			int avgAge = rs.next() ? rs.getInt(1) : -1;
			rs.close();
			_ps.close();
			return avgAge;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all the cached routes between two airports.
	 * @param aD the departure Airport bean
	 * @param aA the arrival Airport bean
	 * @return a Collection of FlightRoute beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<? extends FlightRoute> getRoutes(Airport aD, Airport aA) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.ROUTE_CACHE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?) "
					+ "ORDER BY CREATED");
			_ps.setString(1, aD.getICAO());
			_ps.setString(2, aA.getICAO());
			
			// Execute the query
			Collection<FlightRoute> results = new ArrayList<FlightRoute>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				CachedRoute rt = new CachedRoute(SystemData.getAirport(rs.getString(1)), SystemData.getAirport(rs.getString(2)));
				rt.setCreatedOn(rs.getTimestamp(3));
				rt.setCruiseAltitude(rs.getString(4));
				rt.setSource(rs.getString(5));
				rt.setComments(rs.getString(6));
				
				// Get the SID/STAR out of the route
				String rawRoute = rs.getString(7);
				List<String> wps = StringUtils.split(rawRoute, " ");
				if (wps.size() > 2) {
					if (TerminalRoute.isNameValid(wps.get(0))) {
						rt.setSID(wps.get(0) + "." + wps.get(1));
						wps.remove(0);
					}
					
					String last = wps.get(wps.size() - 1);
					if (TerminalRoute.isNameValid(last)) {
						rt.setSTAR(last + "." + wps.get(wps.size() - 2));
						wps.remove(wps.size() - 1);
					}
					
					rt.setRoute(StringUtils.listConcat(wps, " "));
				} else
					rt.setRoute(rawRoute);
				
				results.add(rt);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}