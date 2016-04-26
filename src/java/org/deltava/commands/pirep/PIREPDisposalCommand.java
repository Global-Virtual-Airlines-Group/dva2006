// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.fb.NewsEntry;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.SetFacebookData;

import org.deltava.mail.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Flight Report status changes.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class PIREPDisposalCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(PIREPDisposalCommand.class);
	
	// Operation constants
	private static final String[] OPNAMES = { "", "", "hold", "approve", "reject" };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the operation
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
		int opCode = StringUtils.arrayIndexOf(OPNAMES, opName);
		if (opCode < 2)
			throw new CommandException("Invalid Operation - " + opName,false);
		
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
					break;
					
				default:
					throw new IllegalArgumentException("Invalid Op Code - " + opCode);
			}

			// If we cannot perform the operation, then stop
			if (!isOK)
				throw securityException("Cannot dispose of Flight Report #" + fr.getID());
			
			// Load the comments
			Collection<String> comments = new LinkedHashSet<String>();
			if (ctx.getParameter("dComments") != null)
				comments.add(ctx.getParameter("dComments"));
			
			// Get the Pilot object
			GetPilot pdao = new GetPilot(con);
			CacheManager.invalidate("Pilots", Integer.valueOf(fr.getDatabaseID(DatabaseID.PILOT)));
			p = pdao.get(fr.getDatabaseID(DatabaseID.PILOT));
			if (p == null)
			   throw notFoundException("Unknown Pilot - " + fr.getDatabaseID(DatabaseID.PILOT));
			
			// Load the pilot's equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(p.getEquipmentType());
			
			// Check if the pilot is rated in the equipment type
			Collection<String> allRatings = new HashSet<String>(p.getRatings());
			allRatings.addAll(eq.getRatings());
			boolean isRated = allRatings.contains(fr.getEquipmentType());
			ctx.setAttribute("notRated", Boolean.valueOf(!isRated), REQUEST);
			if (fr.hasAttribute(FlightReport.ATTR_NOTRATED) != !isRated) {
				log.warn("Updating NotRated flag for " + p.getName() + ", eq="  + fr.getEquipmentType() + " ratings = " + p.getRatings());
				log.warn("NotRated was " + fr.hasAttribute(FlightReport.ATTR_NOTRATED) + ", now " + !isRated);
				fr.setAttribute(FlightReport.ATTR_NOTRATED, !isRated);
			}
			
			// Update comments
			if (!comments.isEmpty())
				fr.setComments(StringUtils.listConcat(comments, "\r\n"));
			
			// Set message context objects
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("flightLength", new Double(fr.getLength() / 10.0));
			mctx.addData("flightDate", StringUtils.format(fr.getDate(), p.getDateFormat()));
			mctx.addData("pilot", p);
			fr.setStatus(opCode);
			
			// Start a JDBC transaction
			ctx.startTX();
			
			// Load the flights for accomplishment purposes
			if (opCode == FlightReport.OK) {
				Collection<FlightReport> pireps = rdao.getByPilot(p.getID(), null);
				rdao.getCaptEQType(pireps);
				AccomplishmentHistoryHelper acchelper = new AccomplishmentHistoryHelper(p);
				pireps.forEach(pr -> acchelper.add(pr));
				
				// Load accomplishments - only save the ones we don't meet yet
				GetAccomplishment accdao = new GetAccomplishment(con);
				Collection<Accomplishment> accs = accdao.getAll().stream().filter(a -> (acchelper.has(a) == AccomplishmentHistoryHelper.Result.NOTYET)).collect(Collectors.toList());
				
				// Add the approved PIREP
				acchelper.add(fr);
				
				// See if we meet any accomplishments now
				SetAccomplishment acwdao = new SetAccomplishment(con);
				for (Iterator<Accomplishment> i = accs.iterator(); i.hasNext(); ) {
					Accomplishment a = i.next();
					if (acchelper.has(a) != AccomplishmentHistoryHelper.Result.NOTYET) {
						acwdao.achieve(p.getID(), a, Instant.now());
						StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RECOGNITION);
						upd.setAuthorID(ctx.getUser().getID());
						upd.setDescription("Joined " + a.getName());
						upds.add(upd);
					} else
						i.remove();
				}
				
				// Log Accomplishments
				if (!accs.isEmpty()) {
					ctx.setAttribute("accomplishments", accs, REQUEST);
				
					// Write Facebook update
					if (!StringUtils.isEmpty(SystemData.get("users.facebook.id"))) {
						MessageContext fbctxt = new MessageContext();
						fbctxt.addData("user", p);
						fbctxt.setTemplate(mtdao.get("FBACCOMPLISH"));
						
						// Write the post
						SetFacebookData fbwdao = new SetFacebookData();
						fbwdao.setWarnMode(true);
						for (Iterator<Accomplishment> i = accs.iterator(); i.hasNext(); ) {
							Accomplishment a = i.next();
							fbctxt.addData("accomplish", a);
							NewsEntry nws = new NewsEntry(fbctxt.getBody());
							
							// Write to user feed or app page
							fbwdao.reset();
							if (p.hasIM(IMAddress.FBTOKEN)) {
								fbwdao.setToken(p.getIMHandle(IMAddress.FBTOKEN));
								fbwdao.write(nws);
							} else {
								fbwdao.setAppID(SystemData.get("users.facebook.pageID"));		
								fbwdao.setToken(SystemData.get("users.facebook.pageToken"));
								fbwdao.writeApp(nws);
							}
						}
					}
				}
				
				// Figure out what network the flight was flown on and ensure we have an ID
				OnlineNetwork net = null;
				try {
					net = OnlineNetwork.valueOf(ctx.getParameter("network").toUpperCase());
					if (!p.hasNetworkID(net))
						throw new IllegalStateException("No " + net + " ID for " + p.getName());
				} catch (Exception e) {
					net = null;
					if (!StringUtils.isEmpty(e.getMessage()))
						log.warn(e.getMessage());
				} finally {
					fr.setNetwork(net);
				}
			}
			
			// Get the write DAO and update/dispose of the PIREP
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.dispose(SystemData.get("airline.db"), ctx.getUser(), fr, opCode);
			
			// If we're approving and have not assigned a Pilot Number yet, assign it
			if ((opCode == FlightReport.OK) && (p.getPilotNumber() == 0)) {
			   SetPilot pwdao = new SetPilot(con);
			   pwdao.assignID(p, SystemData.get("airline.db"));
			   ctx.setAttribute("assignID", Boolean.TRUE, REQUEST);
			   
			   // Create status update
			   StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
			   upd.setAuthorID(ctx.getUser().getID());
			   upd.setDescription("Assigned Pilot ID " + p.getPilotCode());
			   upds.add(upd);
			   
			   // Write Facebook update
			   if (p.hasIM(IMAddress.FBTOKEN)) {
				   MessageContext fbctxt = new MessageContext();
				   fbctxt.addData("user", p);
				   fbctxt.setTemplate(mtdao.get("FBIDASSIGNED"));
				   NewsEntry nws = new NewsEntry(fbctxt.getBody());
				   
					// Write to user feed
					SetFacebookData fbwdao = new SetFacebookData();
					fbwdao.setWarnMode(true);
					fbwdao.setToken(p.getIMHandle(IMAddress.FBTOKEN));
					fbwdao.write(nws);
			   }
			}
			
			// If we're approving the PIREP and it's part of a Flight Assignment, check completion
			int assignID = fr.getDatabaseID(DatabaseID.ASSIGN);
			if (((opCode == FlightReport.OK) || (opCode == FlightReport.REJECTED)) && (assignID != 0)) {
			   GetAssignment fadao = new GetAssignment(con);
			   AssignmentInfo assign = fadao.get(assignID);
			   List<FlightReport> flights = rdao.getByAssignment(assignID, SystemData.get("airline.db"));
			   for (Iterator<FlightReport> i = flights.iterator(); i.hasNext(); )
			      assign.addFlight(i.next());
			   
			   // If the assignment is complete, then mark it as such
			   if (assign.isComplete()) {
			      SetAssignment fawdao = new SetAssignment(con);
			      fawdao.complete(assign);
			      ctx.setAttribute("assignComplete", Boolean.TRUE, REQUEST);
			   }
			}
			
			// Update PIREP statistics
			if ((opCode == FlightReport.OK) || (opCode == FlightReport.REJECTED)) {
				SetAggregateStatistics fstdao = new SetAggregateStatistics(con);
				fstdao.update(fr);
			}
			
			// Write status updates (if any)
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			swdao.write(upds);
			
			// If we're approving an ACARS PIREP, archive the position data
			if ((opCode == FlightReport.OK) || (opCode == FlightReport.REJECTED)) {
				int acarsID = fr.getDatabaseID(DatabaseID.ACARS);
				GetACARSPositions posdao = new GetACARSPositions(con);
				SetACARSArchive acdao = new SetACARSArchive(con);
				if (fr instanceof ACARSFlightReport) {
					Collection<ACARSRouteEntry> entries = posdao.getRouteEntries(acarsID, false);
					acdao.archive(acarsID, entries);
				} else if (fr instanceof XACARSFlightReport) {
					Collection<? extends RouteEntry> entries = posdao.getXACARSEntries(acarsID);
					acdao.archive(acarsID, entries);
				}
				
				ctx.setAttribute("acarsArchive", Boolean.TRUE, REQUEST);
			}
			
			// Commit the transaction
			ctx.commitTX();
			
			// Invalidate the pilot again to reflect the new totals
			if (opCode == FlightReport.OK)
				CacheManager.invalidate("Pilots", Integer.valueOf(fr.getDatabaseID(DatabaseID.PILOT)));				
			
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
		if ((opCode != FlightReport.OK) || (p.hasNotifyOption(Notification.PIREP))) {
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