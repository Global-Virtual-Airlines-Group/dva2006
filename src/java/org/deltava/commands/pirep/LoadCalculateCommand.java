// Copyright 2021, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.time.*;
import java.sql.Connection;

import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to calculate load factors on draft Flight Reports. 
 * @author Luke
 * @version 11.2
 * @since 10.0
 */

public class LoadCalculateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the flight report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Ensure we can set the load factor 
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanCalculateLoad())
				throw securityException("Cannot calculate load factor on Flight Report " + fr.getID());
			
			// If it's draft, get the draft version with Gate data
			if (fr.getStatus() == FlightStatus.DRAFT) {
				DraftFlightReport dfr = frdao.getDraft(fr.getID(), ctx.getDB());
				if (dfr != null)
					fr = dfr;
			}
			
			// Check for prior calculated load factor
			if (fr.getLoadFactor() > 0) {
				ctx.setAttribute("oldLoadFactor", Double.valueOf(fr.getLoadFactor()), REQUEST);
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Updated pre-flight Load Factor");
			} else
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Requested pre-flight Load Factor");
			
			// Get the aircraft data
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.get(fr.getEquipmentType());
			AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
			
			// Get the calculator
			EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
			if (eInfo == null)
				throw new CommandException("No Economy data for Airline");
			
			// Calculate the load factor
			LoadFactor lf = new LoadFactor(eInfo);
			double loadFactor = lf.generate(fr.getDate());
			fr.setPassengers((int) Math.round(opts.getSeats() * loadFactor));
			fr.setLoadFactor(loadFactor);
			
			// If the date is in the past, move it forward to today
			if ((fr instanceof DraftFlightReport dfr) && dfr.getTimeD().isBefore(ZonedDateTime.now())) {
				fr.setDate(LocalDate.now().atTime(12, 0).toInstant(ZoneOffset.UTC));
				fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Adjusted draft flight date to %s", StringUtils.format(fr.getDate(), ctx.getUser().getDateFormat())));
			}
			
			// Write the flight report
			SetFlightReport frwdao = new SetFlightReport(con);
			frwdao.write(fr);
			
			// Save status attributes
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("calcLoadFactor", Boolean.TRUE, REQUEST);
			ctx.setAttribute("targetLoad", Double.valueOf(lf.getTargetLoad(fr.getDate())), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}