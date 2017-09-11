// Copyright 2009, 2010, 2011, 2012, 2014, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.GetFARoutes;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled task to update cached FlightAware routes.
 * @author Luke
 * @version 8.0
 * @since 2.6
 */

public class CachedRouteUpdateTask extends Task {

	/**
	 * Initializes the scheduled Task.
	 */
	public CachedRouteUpdateTask() {
		super("Cached Route Update", CachedRouteUpdateTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		log.info("Executing");
		
		// Get the flightaware DAO
		GetFARoutes fwdao = new GetFARoutes();
		fwdao.setUser(SystemData.get("schedule.flightaware.flightXML.user"));
		fwdao.setPassword(SystemData.get("schedule.flightaware.flightXML.v3"));

		// Get max routes to load
		int maxAge = SystemData.getInt("schedule.flightaware.max_age", 365);
		int maxRoutes = SystemData.getInt("schedule.flightaware.max_load", 100);
		try {
			Connection con = ctx.getConnection();
			
			// Load popular route pairs
			int routesLoaded = 0;
			GetCachedRoutes rcdao = new GetCachedRoutes(con);
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			stdao.setDayFilter(60);
			Collection<ScheduleRoute> routes = stdao.getPopularRoutes(false, false);
			for (Iterator<ScheduleRoute> i = routes.iterator(); i.hasNext() && (routesLoaded < maxRoutes); ) {
				ScheduleRoute rp = i.next();
				if (rp.getFlights() < 2)
					break;
				
				// Ensure the route includes one US airport
				boolean isUS = (rp.getAirportD().getCountry() == Country.get("US")) || (rp.getAirportA().getCountry() == Country.get("US"));
				if (!isUS) {
					log.warn(rp.getAirportD() + " to " + rp.getAirportA() + " not a US route, skipping");
					continue;
				}
				
				// Get the average age - if over three-quarters of the max age load new routes
				int avgAge = rcdao.getAverageAge(rp);
				boolean isExpired = (avgAge == -1) || (avgAge >= maxAge);
				SetCachedRoutes rcwdao = new SetCachedRoutes(con);
				if (SystemData.getBoolean("schedule.flightaware.enabled") && isExpired) {
					ctx.startTX();
					
					// Purge the routes and load new ones
					routesLoaded++;
					Collection<ExternalRoute> faroutes = fwdao.getRouteData(rp);
					if (!faroutes.isEmpty()) {
						log.warn("Loaded " + faroutes.size() + " routes between " + rp.getAirportD() + " and " + rp.getAirportA());
						rcwdao.purge(rp);
						rcwdao.write(faroutes);
					} else {
						log.warn("Created dummy route between " + rp.getAirportD() + " and " + rp.getAirportA());
						ExternalRoute rt = new ExternalRoute("Internal");
						rt.setAirportD(rp.getAirportD());
						rt.setAirportA(rp.getAirportA());
						rt.setSource(ExternalRoute.INTERNAL);
						rt.setComments("Auto-generated dummy route");
						rt.setCruiseAltitude("35000");
						rt.setCreatedOn(Instant.now());
						rt.setRoute(rp.getAirportD().getICAO() + " " + rp.getAirportA().getICAO());
						rcwdao.write(Collections.singleton(rt));
					}
					
					ctx.commitTX();
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Completed");
	}
}