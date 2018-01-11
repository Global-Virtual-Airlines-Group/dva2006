// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.flight.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.hr.TransferRequest;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to retroactively flag a Flight Report as a Check Ride.
 * @author Luke
 * @version 8.1
 * @since 1.0
 */

public class CheckRideFlagCommand extends AbstractTestHistoryCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			else if (!(fr instanceof FDRFlightReport))
				throw notFoundException("Flight Report does not use ACARS/XACARS");
			
			// Look for a transfer request or an academy course
			int crID = 0; int pilotID = fr.getDatabaseID(DatabaseID.PILOT);
			TransferRequest tx = null; Course crs = null;
			if (fr.hasAttribute(FlightReport.ATTR_ACADEMY)) {
				GetAcademyCourses crsdao = new GetAcademyCourses(con);
				Collection<Course> courses = crsdao.getByPilot(pilotID);
				for (Iterator<Course> i = courses.iterator(); i.hasNext() && (crs == null); ) {
					Course c = i.next();
					if (c.getStatus() == Status.STARTED)
						crs = c;
				}
			} else {
				GetTransferRequest txdao = new GetTransferRequest(con);	
				tx = txdao.get(fr.getDatabaseID(DatabaseID.PILOT));
				if (tx != null)
					crID = tx.getLatestCheckRideID();
			}
			
			// Look for a check ride record - if not found, create a new check ride
			GetExam exdao = new GetExam(con);
			CheckRide cr = (crID != 0) ? exdao.getCheckRide(crID) : exdao.getCheckRide(pilotID, fr.getEquipmentType(), TestStatus.NEW);
			boolean newCR = (cr == null);
			if (cr == null)
				cr = new CheckRide(fr.getEquipmentType() + " Check Ride");
			else if (cr.getStatus() == TestStatus.NEW) {
				// empty
			} else if (cr.getStatus() == TestStatus.SUBMITTED) {
				if (cr.getFlightID() != 0) {
					FDRFlightReport ofr = frdao.getACARS(SystemData.get("airline.db"), cr.getFlightID()); 
					if (ofr != null)
						throw securityException("Check Ride ACARS ID #" + cr.getFlightID() + " already has PIREP");
				}
			} else if ((cr.getStatus() == TestStatus.SCORED) && !cr.getPassFail()) {
				newCR = true;
				cr = new CheckRide(fr.getEquipmentType() + " Check Ride");
			} else
				throw securityException("Cannot update " + cr.getStatus().getName() + " Check Ride");
			
			// Set common checkride fields
			cr.setFlightID(fr.getDatabaseID(DatabaseID.ACARS));
			cr.setStatus(TestStatus.SUBMITTED);
			cr.setSubmittedOn(fr.getSubmittedOn());
			if (newCR) {
				cr.setOwner(SystemData.getApp(SystemData.get("airline.code")));
				cr.setAircraftType(fr.getEquipmentType());
				cr.setDate(fr.getDate());
				cr.setScorerID(ctx.getUser().getID());
				cr.setAuthorID(fr.getAuthorID());
				
				// Determine the equipment type based on the primary type or academy type
				GetEquipmentType eqdao = new GetEquipmentType(con);
				if (crs == null) {
					Collection<String> eqTypes = eqdao.getPrimaryTypes(SystemData.get("airline.db"), fr.getEquipmentType());
					if (eqTypes.isEmpty())
						throw notFoundException("No Equipment Type for " + fr.getEquipmentType());
				
					// Set the equipment type
					cr.setEquipmentType(eqTypes.iterator().next());
					
					// If we are doing recurring check rides, pull up the history and set the ride type
					if (ctx.getUser().getProficiencyCheckRides()) {
						GetPilot pdao = new GetPilot(con);
						TestingHistoryHelper helper = initTestHistory(pdao.get(pilotID), con);
						try {
							cr.setType(helper.canRequestCheckRide(eqdao.get(cr.getEquipmentType())));
						} catch (IneligibilityException ie) {
							cr.setComments("Ineligibility issue - " + ie.getMessage());
						}
					}
				} else
					cr.setEquipmentType(eqdao.getDefault(SystemData.get("airline.db")));
			}
			
			// Set academy fields
			if (crs != null)
			{
				cr.setAcademy(true);
				cr.setCourseID(crs.getID());
			}
			
			// Update the flight report
			fr.setAttribute(FlightReport.ATTR_CHECKRIDE, true);
			fr.setAttribute(FlightReport.ATTR_NOTRATED, false);
			if (fr.getStatus() == FlightStatus.HOLD)
				fr.setStatus(FlightStatus.SUBMITTED);
			
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
				tx.addCheckRideID(cr.getID());
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