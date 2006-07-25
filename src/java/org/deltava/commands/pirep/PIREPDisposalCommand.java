// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Flight Report status changes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PIREPDisposalCommand extends AbstractCommand {
	
	// Operation constants
	private static final String[] OPNAMES = { "", "", "hold", "approve", "reject" };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the operation
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
		int opCode = StringUtils.arrayIndexOf(OPNAMES, opName);
		if (opCode < 2) {
			CommandException ce = new CommandException("Invalid Operation - " + opName);
			ce.setLogStackDump(false);
			throw ce;
		}
		
		ctx.setAttribute("opName", opName, REQUEST);

		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Flight Report to modify
			GetFlightReports rdao = new GetFlightReports(con);
			FlightReport fr = rdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Flight Report Not Found");

			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();

			// Get the Message Template DAO
			GetMessageTemplate mtdao = new GetMessageTemplate(con);

			// Determine if we can perform the operation in question and set a request attribute
			boolean isOK = false;
			switch (opCode) {
				case FlightReport.HOLD:
					ctx.setAttribute("isHold", Boolean.TRUE, REQUEST);
					mctx.setTemplate(mtdao.get("PIREPHOLD"));
					isOK = access.getCanHold();
					break;

				case FlightReport.OK:
					ctx.setAttribute("isApprove", Boolean.TRUE, REQUEST);
					mctx.setTemplate(mtdao.get("PIREPAPPROVE"));
					isOK = access.getCanApprove();
					break;

				case FlightReport.REJECTED:
					ctx.setAttribute("isReject", Boolean.TRUE, REQUEST);
					mctx.setTemplate(mtdao.get("PIREPREJECT"));
					isOK = access.getCanReject();
			}

			// If we cannot perform the operation, then stop
			if (!isOK)
				throw securityException("Cannot dispose of Flight Report #" + fr.getID());
			
			// Load the comments
			if (ctx.getParameter("dComments") != null)
				fr.setComments(ctx.getParameter("dComments"));

			// Get the Pilot object
			GetPilot pdao = new GetPilot(con);
			p = pdao.get(fr.getDatabaseID(FlightReport.DBID_PILOT));
			if (p == null)
			   throw notFoundException("Unknown Pilot - " + fr.getDatabaseID(FlightReport.DBID_PILOT));
			
			// Get the number of approved flights (we load it here since the disposed PIREP will be uncommitted
			int pirepCount = rdao.getCount(p.getID()) + 1;
			
			// Set message context objects
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("flightLength", new Double(fr.getLength() / 10.0));
			mctx.addData("flightDate", StringUtils.format(fr.getDate(), "MM/dd/yyyy"));
			mctx.addData("pilot", p);
			
			// Start a JDBC transaction
			ctx.startTX();
			
			// Get the write DAO
			SetFlightReport wdao = new SetFlightReport(con);
			
			// Dispose of the PIREP
			wdao.dispose(ctx.getUser(), fr, opCode);
			fr.setStatus(opCode);
			
			// If we're approving and we have hit a century club milestone, log it
			Map ccLevels = (Map) SystemData.getObject("centuryClubLevels");
			if ((opCode == FlightReport.OK) && (ccLevels.containsKey("CC" + pirepCount))) {
			   StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RECOGNITION);
			   upd.setAuthorID(ctx.getUser().getID());
			   upd.setDescription("Joined " + ccLevels.get("CC" + pirepCount));
			   
			   // Log Century Club name
			   ctx.setAttribute("centuryClub", ccLevels.get("CC" + pirepCount), REQUEST);
			   
			   // Write the Status Update
			   SetStatusUpdate swdao = new SetStatusUpdate(con);
			   swdao.write(upd);
			}
			
			// If we're approving and have not assigned a Pilot Number yet, assign it
			if ((opCode == FlightReport.OK) && (p.getPilotNumber() == 0)) {
			   SetPilot pwdao = new SetPilot(con);
			   pwdao.assignID(p);
			   ctx.setAttribute("assignID", Boolean.TRUE, REQUEST);
			}
			
			// If we're approving the PIREP and it's part of a Flight Assignment, check completion
			int assignID = fr.getDatabaseID(FlightReport.DBID_ASSIGN);
			if (((opCode == FlightReport.OK) || (opCode == FlightReport.REJECTED)) && (assignID != 0)) {
			   GetAssignment fadao = new GetAssignment(con);
			   AssignmentInfo assign = fadao.get(assignID);
			   List flights = rdao.getByAssignment(assignID, SystemData.get("airline.db"));
			   for (Iterator i = flights.iterator(); i.hasNext(); )
			      assign.addFlight((FlightReport) i.next());
			   
			   // If the assignment is complete, then mark it as such
			   if (assign.isComplete()) {
			      SetAssignment fawdao = new SetAssignment(con);
			      fawdao.complete(assign);
			      ctx.setAttribute("assignComplete", Boolean.TRUE, REQUEST);
			   }
			}
			
			// If we're approving an ACARS PIREP, archive the position data
			if (((opCode == FlightReport.OK) || (opCode == FlightReport.REJECTED)) && (fr instanceof ACARSFlightReport)) {
			   SetACARSLog acdao = new SetACARSLog(con);
			   acdao.archivePositions(fr.getDatabaseID(FlightReport.DBID_ACARS));
			   ctx.setAttribute("acarsArchive", Boolean.TRUE, REQUEST);
			}
			
			// Commit the transaction
			ctx.commitTX();
			
			// Save the flight report in the request and the Message Context
			ctx.setAttribute("pirep", fr, REQUEST);
			mctx.addData("pirep", fr);
		} catch (DAOException de) {
		   ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send a notification message
		if ((opCode != FlightReport.OK) || (p.getNotifyOption(Person.PIREP))) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctx);
			mailer.send(p);
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}