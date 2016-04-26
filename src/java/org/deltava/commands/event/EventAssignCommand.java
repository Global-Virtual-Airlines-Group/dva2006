// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to assign Flights for an Online Event.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class EventAssignCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
				UserData usrData = usrmap.get(s.getPilotID());
				Pilot usr = pdao.get(usrData);
				mctxt.addData("pilot", usr);
				
				// Create a Flight Assignment
				AssignmentInfo ai = new AssignmentInfo(s.getEquipmentType());
				ai.setAssignDate(Instant.now());
				ai.setPilotID(s.getPilotID());
				ai.setEventID(e.getID());
				ai.setStatus(AssignmentInfo.RESERVED);
				
				// Calculate the flight number
				int flightID = usr.getPilotNumber();
				if (flightID == 0)
					flightID = LocalDateTime.now().get(ChronoField.DAY_OF_YEAR);
				else if (flightID > 10000)
					flightID %= 10000;
				
				// Create an Assignment Leg
				AssignmentLeg leg = new AssignmentLeg(SystemData.getAirline(usrData.getAirlineCode()), flightID, 1);
				leg.setEquipmentType(s.getEquipmentType());
				leg.setAirportD(s.getAirportD());
				leg.setAirportA(s.getAirportA());
				ai.addAssignment(leg);
				
				// Write the Flight Assignment
				awdao.write(ai, usrData.getDB());
				
				// Create a Flight Report
				FlightReport fr = new FlightReport(leg);
				fr.setRank(usr.getRank());
				fr.setDatabaseID(DatabaseID.PILOT, s.getPilotID());
				fr.setDatabaseID(DatabaseID.ASSIGN, ai.getID());
				fr.setDatabaseID(DatabaseID.EVENT, e.getID());
				fr.setDate(e.getStartTime());
				fr.setNetwork(e.getNetwork());
				
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