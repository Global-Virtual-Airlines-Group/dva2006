// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to calculate load factors on draft Flight Reports. 
 * @author Luke
 * @version 10.0
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
			fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.UPDATE, "Requested pre-flight Load Factor");
			
			// Write the flight report
			SetFlightReport frwdao = new SetFlightReport(con);
			frwdao.write(fr);
			
			// Save status attributes
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("calcLoadFactor", Boolean.TRUE, REQUEST);
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