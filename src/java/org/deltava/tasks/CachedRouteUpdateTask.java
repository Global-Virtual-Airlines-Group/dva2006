// Copyright 2009, 2010, 2011, 2012, 2014, 2016, 2017, 2019, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.GetFARoutes;
import org.deltava.taskman.*;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled task to update cached FlightAware routes.
 * @author Luke
 * @version 11.2
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
		fwdao.setKey(SystemData.get("schedule.flightaware.flightXML.v4"));

		// Get max routes to load
		int routesLoaded = 0;
		int maxAge = SystemData.getInt("schedule.flightaware.max_age", 365);
		int maxRoutes = SystemData.getInt("schedule.flightaware.max_load", 100);
		try {
			Connection con = ctx.getConnection();
			
			// Load popular route pairs
			Random r = new Random();
			GetCachedRoutes rcdao = new GetCachedRoutes(con);
			GetFlightReportStatistics stdao = new GetFlightReportStatistics(con);
			stdao.setDayFilter(60);
			Collection<ScheduleRoute> routes = stdao.getPopularRoutes(false, false);
			for (Iterator<ScheduleRoute> i = routes.iterator(); i.hasNext() && (routesLoaded < maxRoutes); ) {
				ScheduleRoute rp = i.next();
				if (rp.getFlights() < 2) {
					log.warn("{} Flights between {} and {}, terminating", rp.getAirportD(), rp.getAirportA());
					break;
				}
				
				// Ensure the route includes one US airport
				boolean isUS = (rp.getAirportD().getCountry() == Country.get("US")) || (rp.getAirportA().getCountry() == Country.get("US"));
				if (!isUS) {
					log.info("{} to {} not a US route, skipping", rp.getAirportD(), rp.getAirportA());
					continue;
				}
				
				// Get the average age - if over three-quarters of the max age load new routes
				int avgAge = rcdao.getAverageAge(rp);
				boolean isExpired = (avgAge == -1) || (avgAge >= maxAge);
				if (SystemData.getBoolean("schedule.flightaware.enabled") && isExpired) {
					ThreadUtils.sleep(r.nextLong(350) + 6000); // Limited to 10/min
					ctx.startTX();
					
					// Purge the routes and load new ones
					routesLoaded++;
					Collection<ExternalRoute> faroutes = fwdao.getRouteData(rp);
					SetCachedRoutes rcwdao = new SetCachedRoutes(con);
					if (!faroutes.isEmpty()) {
						log.warn("Loaded {} routes between {} and {}", Integer.valueOf(faroutes.size()), rp.getAirportD(), rp.getAirportA());
						rcwdao.purge(rp);
						rcwdao.write(faroutes);
					} else {
						log.warn("Created dummy route between {} and {}", rp.getAirportD(), rp.getAirportA());
						ExternalRoute rt = new ExternalRoute("Internal");
						rt.setAirportD(rp.getAirportD());
						rt.setAirportA(rp.getAirportA());
						rt.setSource(ExternalRoute.INTERNAL);
						rt.setComments("Auto-generated dummy route");
						rt.setCruiseAltitude("35000");
						rt.setCreatedOn(Instant.now());
						rt.setRoute(String.format("% - %s", rp.getAirportD().getICAO(), rp.getAirportA().getICAO()));
						rcwdao.write(Collections.singleton(rt));
					}
					
					ctx.commitTX();
					fwdao.reset();
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}
		
		log.info("Completed - {} routes loaded", Integer.valueOf(routesLoaded));
	}
}