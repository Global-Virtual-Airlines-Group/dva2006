// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.time.*;
import java.util.Collection;
import java.util.stream.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

/**
 * A Scheduled Task to periodically refresh inbound/outbound Taxi times.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class TaxiUpdateTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public TaxiUpdateTask() {
		super("Taxi Time Update", TaxiUpdateTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Determine time since last run - with a sanity check
		Duration d = (ctx.getLastRun() == null) ? Duration.ofDays(31) : Duration.between(ctx.getLastRun(), Instant.now());
		try {
			Connection con = ctx.getConnection();
			
			// Get airports used
			GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);
			frsdao.setDayFilter((ctx.getLastRun() == null) ? 31 : (int) Math.min(1, d.toDays()));
			Collection<RoutePair> routes = frsdao.getAirport(true);
			Collection<Airport> airports = routes.stream().flatMap(rp -> Stream.of(rp.getAirportD(), rp.getAirportA())).collect(Collectors.toSet());
			
			// Start transaction
			ctx.startTX();
			
			// Update the airports
			SetACARSTaxiTimes twdao = new SetACARSTaxiTimes(con);
			for (Airport ap : airports) {
				log.info(String.format("Calculating Taxi times for %s (%s)", ap.getName(), ap.getICAO()));
				twdao.calculate(ap);
			}
				
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
	}
}