// Copyright 2009, 2010, 2011, 2014, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 9.0
 * @since 2.6
 */

public class GetCachedRoutes extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCachedRoutes(Connection c) {
		super(c);
	}
	
	/**
	 * Retrieves the average age of cached routes between two airports. 
	 * @param rp the RoutePair
	 * @return the average age in days, or -1 if none found
	 * @throws DAOException if a JDBC error occurs
	 */
	public int getAverageAge(RoutePair rp) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT IFNULL(ROUND(AVG(DATEDIFF(NOW(), CREATED))), -1) FROM common.ROUTE_CACHE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?)")) {
			ps.setString(1, rp.getAirportD().getICAO());
			ps.setString(2, rp.getAirportA().getICAO());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : -1;
			}
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all the cached routes between two airports.
	 * @param rp the RoutePair
	 * @param includeInternal TRUE to include internal routes, otherwise FALSE
	 * @return a Collection of FlightRoute beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<? extends FlightRoute> getRoutes(RoutePair rp, boolean includeInternal) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM common.ROUTE_CACHE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?) ORDER BY CREATED")) {
			ps.setString(1, rp.getAirportD().getICAO());
			ps.setString(2, rp.getAirportA().getICAO());
			
			Collection<FlightRoute> results = new ArrayList<FlightRoute>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ExternalRoute rt = new ExternalRoute(rs.getString(5));
					rt.setAirportD(SystemData.getAirport(rs.getString(1)));
					rt.setAirportA(SystemData.getAirport(rs.getString(2)));
					rt.setCreatedOn(rs.getTimestamp(3).toInstant());
					rt.setCruiseAltitude(rs.getString(4));
					rt.setComments(rs.getString(6));
				
					// Get the SID/STAR out of the route
					String rawRoute = rs.getString(7);
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
				
					if (!rt.isInternal() || includeInternal)
						results.add(rt);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}