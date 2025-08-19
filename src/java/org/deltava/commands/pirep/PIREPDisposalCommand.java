// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import com.newrelic.api.agent.NewRelic;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.discord.Bot;
import org.deltava.mail.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Flight Report status changes.
 * @author Luke
 * @version 12.2
 * @since 1.0
 */

public class PIREPDisposalCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the operation
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
		ctx.setAttribute("opName", opName, REQUEST);
		FlightStatus op = FlightStatus.fromVerb(opName);
		if (op == null)
			throw new CommandException(String.format("Invalid Operation - %s", opName), false);

		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Flight Report to modify
			GetFlightReports rdao = new GetFlightReports(con);
			FlightReport fr = rdao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException(String.format("Invalid Flight Report - %d", Integer.valueOf(ctx.getID())));

			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			
			// Get the Message Template DAO
			GetMessageTemplate mtdao = new GetMessageTemplate(con);

			// Determine if we can perform the operation in question and set a request attribute
			boolean isOK = false;
			switch (op) {
			case HOLD:
				ctx.setAttribute("isHold", Boolean.TRUE, REQUEST);
				mctx.setTemplate(mtdao.get("PIREPHOLD"));
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Held"); 
				isOK = access.getCanHold();
				break;

			case OK:
				int reviewTime = StringUtils.parse(ctx.getParameter("reviewTime"), 0) / 1000;
				ctx.setAttribute("isApprove", Boolean.TRUE, REQUEST);
				mctx.setTemplate(mtdao.get("PIREPAPPROVE"));
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, ((reviewTime > 0) && (reviewTime < 5)) ? String.format("Approved after %ds", Integer.valueOf(reviewTime)) : "Approved"); 
				isOK = access.getCanApprove();
				break;

			case REJECTED:
				ctx.setAttribute("isReject", Boolean.TRUE, REQUEST);
				mctx.setTemplate(mtdao.get("PIREPREJECT"));
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Rejected"); 
				if (fr.getDatabaseID(DatabaseID.TOUR) != 0) {
					fr.setDatabaseID(DatabaseID.TOUR, 0);
					fr.addStatusUpdate(0, HistoryType.SYSTEM, "Removed Flight Tour leg");
				}
				
				isOK = access.getCanReject();
				break;

			default:
				throw new IllegalArgumentException(String.format("Invalid Status - %s", op));
			}

			// If we cannot perform the operation, then stop
			if (!isOK)
				throw securityException(String.format("Cannot dispose of Flight Report #%d", Integer.valueOf(fr.getID())));
			
			// Load the comments
			Collection<String> comments = new LinkedHashSet<String>();
			if (ctx.getParameter("dComments") != null)
				comments.add(ctx.getParameter("dComments"));
			
			// Update Online Network
			OnlineNetwork newNetwork = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), null);
			if ((newNetwork != fr.getNetwork()) ) {
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.SYSTEM, String.format("Updated online network from %s to %s by %s", fr.getNetwork(), ((newNetwork == null) ? "Offline" : newNetwork), ctx.getUser().getName()));
				fr.setNetwork(newNetwork);
			}

			// Get the Pilot object
			GetPilot pdao = new GetPilot(con);
			CacheManager.invalidate("Pilots", Integer.valueOf(fr.getDatabaseID(DatabaseID.PILOT)));
			p = pdao.get(fr.getDatabaseID(DatabaseID.PILOT));
			if (p == null)
				throw notFoundException(String.format("Unknown Pilot - %d", Integer.valueOf(fr.getDatabaseID(DatabaseID.PILOT))));
			
			// Send Pilot to NewRelic
			NewRelic.addCustomParameter("pilot.name", p.getName());

			// Load the pilot's equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(p.getEquipmentType());

			// Check if the pilot is rated in the equipment type
			Collection<String> allRatings = new HashSet<String>(p.getRatings());
			allRatings.addAll(eq.getRatings());
			boolean isRated = allRatings.contains(fr.getEquipmentType());
			ctx.setAttribute("notRated", Boolean.valueOf(!isRated), REQUEST);
			if (fr.hasAttribute(Attribute.NOTRATED) != !isRated) {
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.SYSTEM, String.format("Updating NotRated flag for %s, eq = %s, ratings = %s", p.getName(), fr.getEquipmentType(), p.getRatings()));
				fr.setAttribute(Attribute.NOTRATED, !isRated);
			}

			// Update comments
			if (!comments.isEmpty())
				fr.setComments(StringUtils.listConcat(comments, "\r\n"));

			// Set message context objects
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("flightLength", Double.valueOf(fr.getLength() / 10.0));
			mctx.addData("flightDate", StringUtils.format(fr.getDate(), p.getDateFormat()));
			mctx.addData("pilot", p);
			fr.setStatus(op);

			// Start a JDBC transaction
			ctx.startTX();
			
			// Delete Elite data if not approving
			SetFlightReport wdao = new SetFlightReport(con);
			if (SystemData.getBoolean("econ.elite.enabled") && (op != FlightStatus.OK)) {
				boolean isDeleted = wdao.deleteElite(fr);
				ctx.setAttribute("eliteDataCleared", Boolean.valueOf(isDeleted), REQUEST);
			}

			// Get the write DAO and update/dispose of the PIREP
			wdao.dispose(ctx.getDB(), ctx.getUser(), fr, op);

			// If this is part of a flight assignment, load it
			GetAssignment fadao = new GetAssignment(con);
			AssignmentInfo assign = (fr.getDatabaseID(DatabaseID.ASSIGN) == 0) ? null : fadao.get(fr.getDatabaseID(DatabaseID.ASSIGN));
			if (assign != null) {
				List<FlightReport> flights = rdao.getByAssignment(assign.getID(), ctx.getDB());
				flights.forEach(assign::addFlight);
			}

			// Diversion handling
			boolean doDivert = Boolean.parseBoolean(ctx.getParameter("holdDivert"));
			if (doDivert && (op == FlightStatus.HOLD) && (fr instanceof ACARSFlightReport)) {
				GetACARSData fidao = new GetACARSData(con);
				FlightInfo fInfo = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));

				// Remove this leg from the assignment
				if (assign != null)
					assign.remove(fInfo);

				// Create the draft PIREP
				DraftFlightReport dfr = new DraftFlightReport(fr.getAirline(), fr.getFlightNumber(), fr.getLeg() + 1);
				dfr.setAirportD(fr.getAirportA());
				dfr.setAirportA(fInfo.getAirportA());
				dfr.setRank(p.getRank());
				dfr.setAuthorID(fr.getAuthorID());
				dfr.setDate(Instant.now());
				dfr.setEquipmentType(fr.getEquipmentType());
				dfr.setAttribute(Attribute.HISTORIC, fr.hasAttribute(Attribute.HISTORIC));
				dfr.setAttribute(Attribute.DIVERT, true);
				dfr.setLoadFactor(fr.getLoadFactor());
				dfr.setPassengers(fr.getPassengers());
				dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, String.format("Diversion completion flight to %s", fInfo.getAirportA().getIATA()));

				// Create a new flight assignment
				AssignmentInfo newAssign = new AssignmentInfo(fr.getEquipmentType());
				newAssign.setPilotID(fr.getAuthorID());
				newAssign.setStatus(AssignmentStatus.RESERVED);
				newAssign.setRandom(true);
				newAssign.setPurgeable(true);
				newAssign.setAssignDate(Instant.now());
				newAssign.addAssignment(new AssignmentLeg(fr));
				newAssign.addAssignment(new AssignmentLeg(dfr));

				// Save the assignment
				SetAssignment fawdao = new SetAssignment(con);
				fawdao.write(newAssign, ctx.getDB());
				wdao.write(dfr);
			}

			// If we're approving and have not assigned a Pilot Number yet, assign it
			if ((op == FlightStatus.OK) && (p.getPilotNumber() == 0)) {
				SetPilot pwdao = new SetPilot(con);
				pwdao.assignID(p, ctx.getDB());
				ctx.setAttribute("assignID", Boolean.TRUE, REQUEST);
				fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Assigned Pilot ID %s", p.getPilotCode()));

				// Create status update
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.STATUS_CHANGE);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription(String.format("Assigned Pilot ID %s", p.getPilotCode()));
				upds.add(upd);
				
				// Update Discord nickname if already registered 
				String discordID = p.getExternalID(ExternalID.DISCORD);
				if (SystemData.getBoolean("discord.enabled") && (discordID != null)) {
					Bot.assignNickname(p, Collections.emptyList());
				
					StatusUpdate upd2 = new StatusUpdate(p.getID(), UpdateType.EXT_AUTH);
					upd2.setAuthorID(ctx.getUser().getID());
					upd2.setDescription(String.format("Assigned Discord Nickname %s", p.getPilotCode()));
					upds.add(upd2);
				}
			}

			// Add to post-approval queue
			if ((op == FlightStatus.OK) || (op == FlightStatus.REJECTED)) {
				SetFlightReportQueue fqdao = new SetFlightReportQueue(con);
				fqdao.add(fr.getID(), SystemData.getBoolean("econ.elite.enabled"), ctx.getDB());
			}

			// Commit and Invalidate the pilot again to reflect the new totals
			ctx.commitTX();
			if (op == FlightStatus.OK)
				CacheManager.invalidate("Pilots", Integer.valueOf(fr.getAuthorID()));

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
		if ((op != FlightStatus.OK) || p.hasNotifyOption(Notification.PIREP)) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctx);
			mailer.send(p);
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}