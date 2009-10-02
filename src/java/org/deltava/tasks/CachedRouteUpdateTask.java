// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.FlightRoute;
import org.deltava.beans.schedule.ScheduleRoute;

import org.deltava.dao.*;
import org.deltava.dao.wsdl.GetFARoutes;

import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled task to purge cached FlightAware routes from the database.
 * @author Luke
 * @version 2.6
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
		fwdao.setUser(SystemData.get("schedule.flightaware.download.user"));
		fwdao.setPassword(SystemData.get("schedule.flightaware.download.pwd"));

		// Get the age to purge, and max routes to load
		int maxAge = SystemData.getInt("schedule.flightaware.max_age", 365);
		int maxRoutes = SystemData.getInt("schedule.flightaware.max_load", 100);
		try {
			Connection con = ctx.getConnection();
			
			// Purge the routes
			SetCachedRoutes rcwdao = new SetCachedRoutes(con);
			log.warn("Purged " + rcwdao.purge(maxAge) + " cached routes " + maxAge + " days of age or older");
			
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
				
				// Get the average age - if over 45 days load new routes
				int avgAge = rcdao.getAverageAge(rp.getAirportD(), rp.getAirportA());
				if (SystemData.getBoolean("schedule.flightaware.enabled") && ((avgAge == -1) || (avgAge > 45))) {
					ctx.startTX();
					
					// Purge the routes and load new ones
					routesLoaded++;
					Collection<? extends FlightRoute> faroutes = fwdao.getRouteData(rp.getAirportD(), rp.getAirportA());
					log.warn("Loaded " + faroutes.size() + " routes between " + rp.getAirportD() + " and " + rp.getAirportA());
					if (!faroutes.isEmpty()) {
						rcwdao.purge(rp.getAirportD(), rp.getAirportA());
						rcwdao.write(faroutes);
					}
					
					// Commit
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