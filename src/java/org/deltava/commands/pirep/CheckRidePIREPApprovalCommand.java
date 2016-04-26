// Copyright 2005, 2006, 2007, 2009, 2010, 2012, 2013, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.acars.ACARSRouteEntry;
import org.deltava.beans.fb.NewsEntry;
import org.deltava.beans.flight.*;
import org.deltava.beans.hr.TransferRequest;
import org.deltava.beans.stats.*;
import org.deltava.beans.stats.AccomplishmentHistoryHelper.Result;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.SetFacebookData;
import org.deltava.mail.*;

import org.deltava.security.command.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to approve Flight Reports and Check Rides.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CheckRidePIREPApprovalCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		// Get the checkride approval
		boolean crApproved = Boolean.valueOf(ctx.getParameter("crApprove")).booleanValue();
		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Flight Report to modify
			GetFlightReports rdao = new GetFlightReports(con);
			FlightReport fr = rdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Flight Report Not Found");

			// Get the DAO and the CheckRide
			GetExam crdao = new GetExam(con);
			CheckRide cr = crdao.getACARSCheckRide(fr.getDatabaseID(DatabaseID.ACARS));
			if (cr == null)
				cr = crdao.getCheckRide(fr.getDatabaseID(DatabaseID.PILOT), fr.getEquipmentType(), TestStatus.SUBMITTED);
			
			// Get the Pilot object
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserData ud = uddao.get(fr.getDatabaseID(DatabaseID.PILOT));
			p = pdao.get(ud);
			if (p == null)
				throw notFoundException("Unknown Pilot - " + fr.getDatabaseID(DatabaseID.PILOT));

			// Check our access levels
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			ExamAccessControl crAccess = new ExamAccessControl(ctx, cr, ud);
			crAccess.validate();
			if (!crAccess.getCanScore())
				throw securityException("Cannot score Check Ride");

			// Validate that we can approve the flight Report, OR its Academy and we can approve the checkride
			boolean canApprove = access.getCanApprove() || (cr.getAcademy() && crAccess.getCanScore());
			if (!canApprove)
				throw securityException("Cannot approve Flight Report");

			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.getByCheckRide(cr.getID());

			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get(crApproved ? "CRPASS" : "CRFAIL"));

			// Set message context objects
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("flightLength", new Double(fr.getLength() / 10.0));
			mctx.addData("flightDate", StringUtils.format(fr.getDate(), "MM/dd/yyyy"));
			mctx.addData("pilot", p);

			// Update the checkride
			cr.setScore(crApproved);
			cr.setScoredOn(Instant.now());
			cr.setScorerID(ctx.getUser().getID());
			cr.setSubmittedOn(fr.getSubmittedOn());
			cr.setFlightID(fr.getDatabaseID(DatabaseID.ACARS));
			cr.setStatus(TestStatus.SCORED);
			
			// Update the flight report
			int pirepStatus = Boolean.valueOf(ctx.getParameter("frApprove")).booleanValue() ? FlightReport.OK : FlightReport.REJECTED;
			fr.setStatus(pirepStatus);
			if (ctx.getParameter("dComments") != null)
				fr.setComments(ctx.getParameter("dComments"));
			
			// Figure out what network the flight was flown on and ensure we have an ID
			OnlineNetwork net = null;
			try {
				net = OnlineNetwork.valueOf(ctx.getParameter("network").toUpperCase());
				if (!p.hasNetworkID(net))
					throw new IllegalStateException("No " + net + " ID");
			} catch (Exception e) {
				net = fr.getNetwork();
			} finally {
				fr.setNetwork(net);
			}
			
			// Load the flights for accomplishment purposes
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			if (fr.getStatus() == FlightReport.OK) {
				Collection<FlightReport> flights = rdao.getByPilot(p.getID(), null);
				rdao.getCaptEQType(flights);
				AccomplishmentHistoryHelper acchelper = new AccomplishmentHistoryHelper(p);
				flights.forEach(pirep -> acchelper.add(pirep));
			
				// Load accomplishments
				GetAccomplishment accdao = new GetAccomplishment(con);
				Collection<Accomplishment> accs = accdao.getAll();
			
				// Loop through each accomplishment and only save the ones we don't meet yet
				for (Iterator<Accomplishment> i = accs.iterator(); i.hasNext(); ) {
					Accomplishment a = i.next();
					if (acchelper.has(a) != Result.NOTYET)
						i.remove();
				}
				
				// Add the approved PIREP
				acchelper.add(fr);

				// See if we meet any accomplishments now
				for (Iterator<Accomplishment> i = accs.iterator(); i.hasNext(); ) {
					Accomplishment a = i.next();
					if (acchelper.has(a) == Result.MEET) {
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
						
						// Write the entry
						SetFacebookData fbwdao = new SetFacebookData();
						fbwdao.setWarnMode(true);
						for (Iterator<Accomplishment> i = accs.iterator(); i.hasNext(); ) {
							Accomplishment a = i.next();
							fbctxt.addData("accomplish", a);
							NewsEntry nws = new NewsEntry(fbctxt.getBody());
							fbwdao.reset();
							
							// Write to user feed or app page
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
			}
			
			// Start a JDBC transaction
			ctx.startTX();

			// Get the PIREP write DAO and perform the operation
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.dispose(SystemData.get("airline.db"), ctx.getUser(), fr, fr.getStatus());

			// Archive the Position data
			if (fr instanceof ACARSFlightReport) {
				GetACARSPositions posdao = new GetACARSPositions(con);
				SetACARSArchive acdao = new SetACARSArchive(con);
				int acarsID = fr.getDatabaseID(DatabaseID.ACARS);
				Collection<ACARSRouteEntry> entries = posdao.getRouteEntries(acarsID, false);
				acdao.archive(acarsID, entries);
				ctx.setAttribute("acarsArchive", Boolean.TRUE, REQUEST);
			}

			// Get the CheckRide write DAO and update the checkride
			SetExam ewdao = new SetExam(con);
			ewdao.write(cr);

			// If we are approving the checkride, then approve the transfer request
			if (txreq != null) {
				mctx.addData("txReq", txreq);
				txreq.setStatus(cr.getPassFail() ? TransferRequest.OK : TransferRequest.PENDING);

				// Write the transfer request
				SetTransferRequest txwdao = new SetTransferRequest(con);
				txwdao.update(txreq);
			}

			// Write the Status Updates
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			swdao.write(upds);

			// Commit the transaction
			ctx.commitTX();

			// Save the flight report/checkride in the request and the Message Context
			ctx.setAttribute("isApprove", Boolean.valueOf(crApproved), REQUEST);
			ctx.setAttribute("isReject", Boolean.valueOf(!crApproved), REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("checkRide", cr, REQUEST);
			mctx.addData("pirep", fr);
			mctx.addData("checkRide", cr);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(p);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}