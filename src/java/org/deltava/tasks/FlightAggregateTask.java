// Copyright 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.flight.*;
import org.deltava.beans.servinfo.PositionData;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to aggregate Flight statistics. 
 * @author Luke
 * @version 12.2
 * @since 11.1
 */

public class FlightAggregateTask extends Task {
	
	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");

	/**
	 * Creates the Task.
	 */
	public FlightAggregateTask() {
		super("Flight Statistics Aggregation", FlightAggregateTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetFlightReportQueue qdao = new GetFlightReportQueue(con);
			SetFlightReportQueue qwdao = new SetFlightReportQueue(con);
			
			SetFlightReport fwdao = new SetFlightReport(con);
			SetAssignment fawdao = new SetAssignment(con);
			SetAccomplishment acwdao = new SetAccomplishment(con);
			SetAggregateStatistics stwdao = new SetAggregateStatistics(con);
			
			// Get the queue
			Collection<ApprovalStatus> flights = qdao.getPostApprovalQueue();
			flights.removeIf(ap -> !ap.isPending(ApprovalOperation.STATS) && !ap.isPending(ApprovalOperation.COMPLETION));
			log.log(flights.isEmpty() ? Level.INFO : Level.WARN, "{} processing {} Flight Reports", SystemData.get("airline.code"), Integer.valueOf(flights.size()));
			
			// Process each flight
			for (ApprovalStatus ap : flights) {
				FlightReport fr = frdao.get(ap.getID(), ctx.getDB());
				if (fr == null) {
					log.warn("Missing Flight Report - {}", Integer.valueOf(ap.getID()));
					continue;
				}
				
				// Do post-approval activities
				ctx.startTX();
				Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
				if (ap.isPending(ApprovalOperation.COMPLETION) && (fr.getStatus() == FlightStatus.OK)) {
					IntervalTaskTimer tt = new IntervalTaskTimer();
					Pilot p = pdao.get(fr.getAuthorID());
							
					// Load Pilot logbook
					CacheableCollection<FlightReport> pireps = _cache.get(p.cacheKey());
					if (pireps == null) {
						Collection<FlightReport> data = frdao.getByPilot(p.getID(), null);
						frdao.loadCaptEQTypes(p.getID(), data, ctx.getDB());
						
						// Add to cache
						pireps = new CacheableList<FlightReport>(p.cacheKey(), data);
						_cache.add(pireps);
					}
					
					// Populate helper
					AccomplishmentHistoryHelper acchelper = new AccomplishmentHistoryHelper(p);
					pireps.forEach(acchelper::add);
					tt.mark("flights");

					// Load accomplishments - only save the ones we haven't obtained yet
					GetAccomplishment accdao = new GetAccomplishment(con);
					Collection<Accomplishment> allAccs = accdao.getAll();
					Collection<Accomplishment> pAccs = accdao.getByPilot(p, ctx.getDB()).stream().map(Accomplishment::new).collect(Collectors.toList());
					Collection<Accomplishment> accs = allAccs.stream().filter(a -> !pAccs.contains(a)).collect(Collectors.toList());
					
					// Add the approved PIREP
					acchelper.add(fr);
					
					// See if we meet any accomplishments now
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
				
				// Do stuff if it's rejected or approved
				if (ap.isPending(ApprovalOperation.COMPLETION)) {
					// If this is part of a flight assignment, load it
					GetAssignment fadao = new GetAssignment(con);
					AssignmentInfo assign = (fr.getDatabaseID(DatabaseID.ASSIGN) == 0) ? null : fadao.get(fr.getDatabaseID(DatabaseID.ASSIGN));
					if (assign != null) {
						List<FlightReport> assignflights = frdao.getByAssignment(assign.getID(), ctx.getDB());
						assignflights.forEach(assign::addFlight);
						if (assign.isComplete()) {
							fawdao.complete(assign, false);
							fr.addStatusUpdate(0, HistoryType.LIFECYCLE, String.format("Flight Assignment Completed (%d legs)", Integer.valueOf(assign.size())));
						}
					}
					
					int acarsID = fr.getDatabaseID(DatabaseID.ACARS);
					GetACARSPositions posdao = new GetACARSPositions(con);
					SetACARSArchive acdao = new SetACARSArchive(con);
					if (fr instanceof ACARSFlightReport) {
						SequencedCollection<ACARSRouteEntry> entries = posdao.getRouteEntries(acarsID, false);
						acdao.archive(acarsID, entries);
						fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Archived %d ACARS position updates", Integer.valueOf(entries.size())));
					} else if (fr instanceof XACARSFlightReport) {
						SequencedCollection<? extends RouteEntry> entries = posdao.getXACARSEntries(acarsID);
						acdao.archive(acarsID, entries);
						fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Archived %d XACARS position updates", Integer.valueOf(entries.size())));
					}
					
					// Write the online track data
					GetOnlineTrack tdao = new GetOnlineTrack(con);
					if (tdao.hasTrack(fr.getID())) {
						SequencedCollection<PositionData> onlineEntries = tdao.get(fr.getID());
						try (OutputStream os = new BufferedOutputStream(new FileOutputStream(ArchiveHelper.getOnline(fr.getID())))) {
							SetSerializedOnline owdao = new SetSerializedOnline(os);
							owdao.archive(fr.getID(), onlineEntries);
							fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Archived %d %s position updates", Integer.valueOf(onlineEntries.size()), fr.getNetwork()));
						} catch (IOException ie) {
							throw new DAOException(ie);
						}

						SetOnlineTrack twdao = new SetOnlineTrack(con);
						twdao.purge(fr.getID());
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
								fr.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Archived %d route points", Integer.valueOf(arcRt.getSize())));
							} catch (IOException ie) {
								log.atWarn().withThrowable(ie).log("Error writing serialized route data");
							}
						}
					}
					
					qwdao.complete(ap.getID(), ApprovalOperation.COMPLETION);
				}
				
				// Update statistics
				if (ap.isPending(ApprovalOperation.STATS)) {
					TaskTimer tt = new TaskTimer();
					stwdao.update(fr);
					qwdao.complete(ap.getID(), ApprovalOperation.STATS);
					fr.addStatusUpdate(0, HistoryType.LIFECYCLE, "Updated Flight Statistics Totals");
				
					long ms = tt.stop();
					log.log((ms > 4500) ? Level.WARN : Level.INFO, "Aggregates for Flight Report {} completed in {}ms", Integer.valueOf(ap.getID()), Long.valueOf(ms));
				}
				
				// Write status updates (if any)
				SetStatusUpdate swdao = new SetStatusUpdate(con);
				swdao.write(upds);
				fwdao.writeHistory(fr.getStatusUpdates(), ctx.getDB());
				
				// Commit
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			logError("Error aggregating flights", de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}