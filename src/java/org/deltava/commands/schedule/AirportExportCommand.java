// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import java.time.Instant;

import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to export the Airport List to a CSV file.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class AirportExportCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanExport())
			throw securityException("Cannot export Airport List");

		Collection<Airport> airports = new TreeSet<Airport>(SystemData.getAirports().values());

		// Set the content type and force Save As
		String aCode = SystemData.get("airline.code");
		ctx.getResponse().setContentType("text/csv");
		ctx.getResponse().setCharacterEncoding("utf-8");
		ctx.setHeader("Content-disposition", "attachment; filename=" + aCode.toLowerCase() + "_airports.csv");

		try (PrintWriter out = ctx.getResponse().getWriter()) {
			out.println("; " + aCode + " Airport List - exported on " + StringUtils.format(Instant.now(), "MM/dd/yyyy HH:mm:ss"));
			out.println("IATA,ICAO,NAME,LATITUDE,LONGITUDE,COUNTRY,ALTITUDE,MAX_RWY_LEN,REGION,REPLACES,ADSE,AIRLINES");
			
			// Loop through airports
			for (Airport a : airports) {
				StringBuilder buf = new StringBuilder(a.getIATA());
				buf.append(',');
				buf.append(a.getICAO());
				buf.append(',');
				buf.append(a.getName());
				buf.append(',');
				buf.append(a.getLatitude());
				buf.append(',');
				buf.append(a.getLongitude());
				buf.append(',');
				buf.append(a.getCountry().getCode());
				buf.append(',');
				buf.append(a.getAltitude());
				buf.append(',');
				buf.append(a.getMaximumRunwayLength());
				buf.append(',');
				buf.append(a.getRegion());
				buf.append(',');
				if (!StringUtils.isEmpty(a.getSupercededAirport()))
					buf.append(a.getSupercededAirport());
				
				buf.append(',');
				buf.append(a.getADSE());
				buf.append(',');
				for (Iterator<String> i = a.getAirlineCodes().iterator(); i.hasNext(); ) {
					buf.append(i.next());
					if (i.hasNext())
						buf.append(',');
				}
				
				out.println(buf.toString());
			}
		} catch (IOException ie) {
			throw new CommandException(ie);
		}

		// Set the result code
		CommandResult result = ctx.getResult();
		result.setType(ResultType.HTTPCODE);
		result.setHttpCode(HttpServletResponse.SC_OK);
		result.setSuccess(true);
	}
}