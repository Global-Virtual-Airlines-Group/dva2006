// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Instant;

import org.apache.logging.log4j.*;

import com.newrelic.api.agent.NewRelic;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.servinfo.PositionData;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.mail.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Flight Report status changes.
 * @author Luke
 * @version 11.2
 * @since 1.0
 */

public class PIREPDisposalCommand extends AbstractCommand {

	private static final Logger log = LogManager.getLogger(PIREPDisposalCommand.class);
	
	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");

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

		Pilot p = null; IntervalTaskTimer tt = new IntervalTaskTimer();
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
			tt.mark("load");
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
			if (fr.hasAttribute(FlightReport.ATTR_NOTRATED) != !isRated) {
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.SYSTEM, String.format("Updating NotRated flag for %s, eq = %s, ratings = %s", p.getName(), fr.getEquipmentType(), p.getRatings()));
				fr.setAttribute(FlightReport.ATTR_NOTRATED, !isRated);
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
			
			// Load the flights for accomplishment purposes
			if (op == FlightStatus.OK) {
				CacheableCollection<FlightReport> pireps = _cache.get(p.cacheKey());
				if (pireps == null) {
					Collection<FlightReport> data = rdao.getByPilot(p.getID(), null);
					rdao.loadCaptEQTypes(p.getID(), data, ctx.getDB());
					
					// Add to cache
					pireps = new CacheableList<FlightReport>(p.cacheKey(), data);
					_cache.add(pireps);
				}
				
				AccomplishmentHistoryHelper acchelper = new AccomplishmentHistoryHelper(p);
				pireps.forEach(acchelper::add);
				NewRelic.addCustomParameter("pilot.flights", Integer.valueOf(pireps.size()));
				tt.mark("flights");

				// Load accomplishments - only save the ones we haven't obtained yet
				GetAccomplishment accdao = new GetAccomplishment(con);
				Collection<Accomplishment> allAccs = accdao.getAll();
				Collection<Accomplishment> pAccs = accdao.getByPilot(p, ctx.getDB()).stream().map(Accomplishment::new).collect(Collectors.toList());
				Collection<Accomplishment> accs = allAccs.stream().filter(a -> !pAccs.contains(a)).collect(Collectors.toList());
				
				// Add the approved PIREP
				acchelper.add(fr);

				// See if we meet any accomplishments now
				SetAccomplishment acwdao = new SetAccomplishment(con);
				for (Iterator<Accomplishment> i = accs.iterator(); i.hasNext();) {
					Accomplishment a = i.next();
					if (acchelper.has(a) != AccomplishmentHistoryHelper.Result.NOTYET) {
						StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.RECOGNITION);
						upd.setAuthorID(ctx.getUser().getID());
						upd.setDescription("Joined " + a.getName());
						if (a.getUnit() == AccomplishUnit.MEMBERDAYS)
							upd.setDate(acchelper.achieved(a));
						
						acwdao.achieve(p.getID(), a, upd.getDate());
						upds.add(upd);
						fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.SYSTEM, upd.getDescription());
					} else
						i.remove();
				}

				// Log Accomplishments
				tt.mark("accomplishments");
				if (!accs.isEmpty())
					ctx.setAttribute("accomplishments", accs, REQUEST);
				
				// Check for Tour completion
				if (fr.getDatabaseID(DatabaseID.TOUR) != 0) {
					GetTour trdao = new GetTour(con);
					Tour t = trdao.get(fr.getDatabaseID(DatabaseID.TOUR), ctx.getDB());
					TourFlightHelper tfh = new TourFlightHelper(fr, false);
					tfh.addFlights(pireps);
					tt.mark("tours");
					
					int idx = tfh.isLeg(t);
					if (idx == 0) {
						fr.setDatabaseID(DatabaseID.TOUR, 0);
						tfh.getMessages().forEach(msg -> fr.addStatusUpdate(0, HistoryType.SYSTEM, msg));
					} else {
						tfh.addFlights(List.of(fr));
						if (tfh.isComplete(t)) {
							fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, String.format("Tour %s completed", t.getName()));
							StatusUpdate upd = new StatusUpdate(fr.getAuthorID(), UpdateType.TOUR);
							upd.setAuthorID(ctx.getUser().getID());
							upd.setDescription(String.format("Tour %s completed (%d legs)", t.getName(), Integer.valueOf(idx)));
							upds.add(upd);
						}
					}
				}
			}
			
			// Delete Elite data if not approving
			SetFlightReport wdao = new SetFlightReport(con);
			if (SystemData.getBoolean("econ.elite.enabled") && (op != FlightStatus.OK)) {
				boolean isDeleted = wdao.deleteElite(fr);
				ctx.setAttribute("eliteDataCleared", Boolean.valueOf(isDeleted), REQUEST);
			}

			// Get the write DAO and update/dispose of the PIREP
			wdao.dispose(ctx.getDB(), ctx.getUser(), fr, op);
			tt.mark("disposal");

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
				dfr.setAttribute(FlightReport.ATTR_HISTORIC, fr.hasAttribute(FlightReport.ATTR_HISTORIC));
				dfr.setAttribute(FlightReport.ATTR_DIVERT, true);
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
				tt.mark("divert");
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
			}

			// If we're approving the PIREP and it's part of a Flight Assignment, check completion
			if (((op == FlightStatus.OK) || (op == FlightStatus.REJECTED)) && (assign != null)) {
				List<FlightReport> flights = rdao.getByAssignment(assign.getID(), ctx.getDB());
				flights.forEach(assign::addFlight);

				// If the assignment is complete, then mark it as such
				if (assign.isComplete()) {
					SetAssignment fawdao = new SetAssignment(con);
					fawdao.complete(assign, false);
					ctx.setAttribute("assignComplete", Boolean.TRUE, REQUEST);
					fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, String.format("Flight Assignment Completed (%d legs)", Integer.valueOf(assign.size())));
				}
			}

			// Update PIREP statistics
			if ((op == FlightStatus.OK) || (op == FlightStatus.REJECTED)) {
				SetAggregateStatistics fstdao = new SetAggregateStatistics(con);
				fstdao.addQueueEntry(fr.getID());
				tt.mark("stats");
			}
			
			// Write status updates (if any)
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			swdao.write(upds);

			// If we're approving an ACARS PIREP, archive the position data
			if ((op == FlightStatus.OK) || (op == FlightStatus.REJECTED)) {
				int acarsID = fr.getDatabaseID(DatabaseID.ACARS);
				GetACARSPositions posdao = new GetACARSPositions(con);
				SetACARSArchive acdao = new SetACARSArchive(con);
				if (fr instanceof ACARSFlightReport) {
					SequencedCollection<ACARSRouteEntry> entries = posdao.getRouteEntries(acarsID, false);
					acdao.archive(acarsID, entries);
				} else if (fr instanceof XACARSFlightReport) {
					SequencedCollection<? extends RouteEntry> entries = posdao.getXACARSEntries(acarsID);
					acdao.archive(acarsID, entries);
				}

				// Write the online track data
				GetOnlineTrack tdao = new GetOnlineTrack(con);
				if (tdao.hasTrack(fr.getID())) {
					SequencedCollection<PositionData> onlineEntries = tdao.get(fr.getID());
					try (OutputStream os = new BufferedOutputStream(new FileOutputStream(ArchiveHelper.getOnline(fr.getID())))) {
						SetSerializedOnline owdao = new SetSerializedOnline(os);
						owdao.archive(fr.getID(), onlineEntries);
					} catch (IOException ie) {
						throw new DAOException(ie);
					}

					SetOnlineTrack twdao = new SetOnlineTrack(con);
					twdao.purge(fr.getID());
					ctx.setAttribute("onlineArchive", Boolean.TRUE, REQUEST);
				}

				// Write the route data
				boolean hasRoute = ArchiveHelper.getRoute(fr.getID()).exists();
				if (!hasRoute) {
					GetACARSData fidao = new GetACARSData(con);
					GetNavRoute navdao = new GetNavRoute(con);
					GetMetadata mddao = new GetMetadata(con);
					FlightInfo fi = (fr instanceof FDRFlightReport) ? fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS)) : null;
					RouteBuilder rb = new RouteBuilder(fr, (fi == null) ? fr.getRoute() : fi.getRoute());
					navdao.getRouteWaypoints(rb.getRoute(), fr.getAirportD()).forEach(rb::add);
					if (rb.hasData()) {
						String currentCycle = mddao.get("navdata.cycle");
						ArchivedRoute arcRt = new ArchivedRoute(fr.getID(), StringUtils.parse(currentCycle, -1));
						rb.getPoints().forEach(arcRt::addWaypoint);
						try (OutputStream os = new BufferedOutputStream(new FileOutputStream(ArchiveHelper.getRoute(fr.getID())))) {
							SetSerializedRoute rtw = new SetSerializedRoute(os);
							rtw.archive(arcRt);
						} catch (IOException ie) {
							log.atWarn().withThrowable(ie).log("Error writing serialized route data");
						}
					}
				}

				tt.mark("archive");
				ctx.setAttribute("acarsArchive", Boolean.valueOf(fr instanceof FDRFlightReport), REQUEST);
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
		
		// Log timings
		if (tt.stop()> 2750)
			log.warn("Disposed Flight Report #{} - {}", Integer.valueOf(ctx.getID()), tt);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}