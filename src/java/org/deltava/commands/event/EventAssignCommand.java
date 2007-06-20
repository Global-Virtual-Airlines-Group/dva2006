// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.FlightReport;
import org.deltava.beans.assign.*;
import org.deltava.beans.event.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to assign Flights for an Online Event.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventAssignCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Event
			GetEvent dao = new GetEvent(con);
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Unknown Online Event - " + ctx.getID());
			
			// Check our access level
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanAssignFlights())
				throw securityException("Cannot assign flights for Online Event " + e.getName());
			
			// Get the Pilots signed up for the event
			GetUserData usrdao = new GetUserData(con);
			UserDataMap usrmap = usrdao.getByEvent(e.getID());
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EVENTASSIGN"));
			mctxt.addData("event", e);
			
			// Get the write DAOs
			SetFlightReport fwdao = new SetFlightReport(con);
			SetAssignment awdao = new SetAssignment(con);
			
			// Start the transaction
			ctx.startTX();
			
			// Get the signups for this event
			GetPilot pdao = new GetPilot(con);
			for (Iterator<Signup> i = e.getSignups().iterator(); i.hasNext(); ) {
				Signup s = i.next();
				
				// Get the Pilot
				UserData usrData = (UserData) usrmap.get(s.getPilotID());
				Pilot usr = pdao.get(usrData);
				mctxt.addData("pilot", usr);
				
				// Create a Flight Assignment
				AssignmentInfo ai = new AssignmentInfo(s.getEquipmentType());
				ai.setAssignDate(new Date());
				ai.setPilotID(s.getPilotID());
				ai.setEventID(e.getID());
				ai.setStatus(AssignmentInfo.RESERVED);
				
				// Create an Assignment Leg
				AssignmentLeg leg = new AssignmentLeg(SystemData.getAirline(usrData.getAirlineCode()), usr.getPilotNumber(),1);
				leg.setEquipmentType(s.getEquipmentType());
				leg.setAirportD(s.getAirportD());
				leg.setAirportA(s.getAirportA());
				ai.addAssignment(leg);
				
				// Write the Flight Assignment
				awdao.write(ai, usrData.getDB());
				
				// Create a Flight Report
				FlightReport fr = new FlightReport(leg);
				fr.setRank(usr.getRank());
				fr.setDatabaseID(FlightReport.DBID_PILOT, s.getPilotID());
				fr.setDatabaseID(FlightReport.DBID_ASSIGN, ai.getID());
				fr.setDatabaseID(FlightReport.DBID_EVENT, e.getID());
				fr.setDate(e.getStartTime());
				switch (e.getNetwork()) {
					case Event.NET_VATSIM :
						fr.setAttribute(FlightReport.ATTR_VATSIM, true);
						break;
						
					case Event.NET_IVAO :
						fr.setAttribute(FlightReport.ATTR_IVAO, true);
						break;
				}
				
				// Write the Flight Report to the proper database
				fwdao.write(fr, usrData.getDB());
				mctxt.addData("pirep", fr);
				
				// Send the message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctxt);
				mailer.send(usr);
			}
			
			// Commit the transaction
			ctx.commitTX();
			
			// Save the event in the request
			ctx.setAttribute("event", e, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/eventFlightAssign.jsp");
		result.setSuccess(true);
	}
}