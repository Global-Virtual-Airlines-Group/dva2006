// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to retroactively flag a Flight Report as a Check Ride.
 * @author Luke
 * @version 2.2
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
			
			// Look for a transfer request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest tx = txdao.get(fr.getDatabaseID(FlightReport.DBID_PILOT));
			int crID = (tx == null) ? 0 : tx.getCheckRideID();
			
			// Look for a check ride record - if not found, create a new check ride
			GetExam exdao = new GetExam(con);
			CheckRide cr = (crID != 0) ? exdao.getCheckRide(crID) : exdao.getCheckRide(fr.getDatabaseID(FlightReport.DBID_PILOT), 
					fr.getEquipmentType(), Test.NEW);
			boolean newCR = (cr == null);
			if (newCR) {
				cr = new CheckRide(fr.getEquipmentType() + " Check Ride");
				cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
				cr.setAircraftType(fr.getEquipmentType());
				cr.setDate(fr.getDate());
				cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
				cr.setStatus(Test.SUBMITTED);
				cr.setSubmittedOn(fr.getSubmittedOn());
				cr.setScorerID(ctx.getUser().getID());
				cr.setPilotID(fr.getDatabaseID(FlightReport.DBID_PILOT));
				
				// Determine the equipment type based on the primary type
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<String> eqTypes = eqdao.getPrimaryTypes(SystemData.get("airline.db"), fr.getEquipmentType());
				if (eqTypes.isEmpty())
					throw notFoundException("No Equipment Type for " + fr.getEquipmentType());
				
				// Set the equipment type
				cr.setEquipmentType(eqTypes.iterator().next());
			} else if (cr.getStatus() == Test.NEW) {
				cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
				cr.setStatus(Test.SUBMITTED);
				cr.setSubmittedOn(fr.getSubmittedOn());
			} else if (cr.getStatus() == Test.SUBMITTED) {
				if (cr.getFlightID() != 0) {
					ACARSFlightReport ofr = frdao.getACARS(SystemData.get("airline.db"), cr.getFlightID()); 
					if ((ofr != null) && (ofr.getDatabaseID(FlightReport.DBID_ACARS) != cr.getFlightID()))
						throw securityException("Check Ride ACARS ID #" + cr.getFlightID() + " already has PIREP");
				}
						
				cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
				cr.setSubmittedOn(fr.getSubmittedOn());
			} else if ((cr.getStatus() == Test.SCORED) && !cr.getPassFail()) {
				newCR = true;
				cr = new CheckRide(fr.getEquipmentType() + " Check Ride");
				cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
				cr.setAircraftType(fr.getEquipmentType());
				cr.setDate(fr.getDate());
				cr.setFlightID(fr.getDatabaseID(FlightReport.DBID_ACARS));
				cr.setStatus(Test.SUBMITTED);
				cr.setSubmittedOn(fr.getSubmittedOn());
				cr.setScorerID(ctx.getUser().getID());
				cr.setPilotID(fr.getDatabaseID(FlightReport.DBID_PILOT));
				
				// Determine the equipment type based on the primary type
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<String> eqTypes = eqdao.getPrimaryTypes(SystemData.get("airline.db"), fr.getEquipmentType());
				if (eqTypes.isEmpty())
					throw notFoundException("No Equipment Type for " + fr.getEquipmentType());
				
				// Set the equipment type
				cr.setEquipmentType(eqTypes.iterator().next());
			} else
				throw securityException("Cannot update " + cr.getStatusName() + " Check Ride");
			
			// Update the flight report
			fr.setAttribute(FlightReport.ATTR_CHECKRIDE, true);
			fr.setAttribute(FlightReport.ATTR_NOTRATED, false);
			if (fr.getStatus() == FlightReport.HOLD)
				fr.setStatus(FlightReport.SUBMITTED);
			
			// Start the transaction
			ctx.startTX();
			
			// Save the flight report
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(fr);
			
			// Save the check ride
			SetExam ewdao = new SetExam(con);
			ewdao.write(cr);
			
			// Update the transfer request
			if (newCR && (tx != null)) {
				tx.setCheckRideID(cr.getID());
				tx.setStatus(TransferRequest.ASSIGNED);
				
				// Save the transfer request
				SetTransferRequest twdao = new SetTransferRequest(con);
				twdao.update(tx);
			}
			
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
		result.setType(ResultType.REDIRECT);
		result.setURL("pirep", "read", ctx.getID());
		result.setSuccess(true);
	}
}