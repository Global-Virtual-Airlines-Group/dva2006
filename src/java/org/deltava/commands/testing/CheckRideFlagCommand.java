// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to retroactively flag a Flight Report as a Check Ride.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRideFlagCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			else if (!(fr instanceof ACARSFlightReport))
				throw notFoundException("Flight Report does not use ACARS");
			
			// Look for a check ride record - if not found, create a new check ride
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(SystemData.get("airline.db"), fr.getDatabaseID(FlightReport.DBID_PILOT),
					fr.getEquipmentType(), Test.NEW);
			if (cr == null) {
				cr = new CheckRide(fr.getEquipmentType() + " Check Ride");
				cr.setAircraftType(fr.getEquipmentType());
				cr.setDate(fr.getDate());
				cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
				cr.setStatus(Test.SUBMITTED);
				cr.setSubmittedOn(new java.util.Date());
				cr.setScorerID(ctx.getUser().getID());
				cr.setPilotID(fr.getDatabaseID(FlightReport.DBID_PILOT));
				
				// Determine the equipment type based on the primary type
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<String> eqTypes = eqdao.getPrimaryTypes(SystemData.get("airline.db"), fr.getEquipmentType());
				if (eqTypes.isEmpty())
					throw notFoundException("No Equipment Type for " + fr.getEquipmentType());
				
				// Set the equipment type
				cr.setEquipmentType(eqTypes.iterator().next());
			} else {
				cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
				cr.setStatus(Test.SUBMITTED);
			}
			
			// Update the flight report
			fr.setAttribute(FlightReport.ATTR_CHECKRIDE, true);
			fr.setAttribute(FlightReport.ATTR_NOTRATED, false);
			
			// Start the transaction
			ctx.startTX();
			
			// Save the flight report
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(fr);
			
			// Save the check ride
			SetExam ewdao = new SetExam(con);
			ewdao.write(cr);
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("pirep", "read", ctx.getID());
		result.setSuccess(true);
	}
}