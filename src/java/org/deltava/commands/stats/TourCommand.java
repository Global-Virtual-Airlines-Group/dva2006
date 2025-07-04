// Copyright 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.sql.Connection;
import java.time.temporal.*;
import java.time.format.DateTimeFormatterBuilder;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.security.command.TourAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display flight Tours.
 * @author Luke
 * @version 11.6
 * @since 10.0
 */

public class TourCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the Tour.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();

			Tour t = null, ot = null; TourAccessControl ac = null;
			if (!isNew) {
				GetTour tdao = new GetTour(con);
				t = tdao.get(ctx.getID(), ctx.getDB());
				if (t == null)
					throw notFoundException("Invalid Tour ID - " + ctx.getID());

				// Check our access
				ac = new TourAccessControl(ctx, t);
				ac.validate();
				if (!ac.getCanEdit())
					throw securityException("Cannot edit Tour profile - " + t.getID());

				// Cloone and updaate
				ot = BeanUtils.clone(t);
				t.setName(ctx.getParameter("name"));
			} else {
				ac = new TourAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot create Tour profile");

				t = new Tour(ctx.getParameter("name"));
			}

			// Time parser init
			t.setStartDate(parseDateTime(ctx, "start", SystemData.get("time.date_format"), "HH:mm"));
			t.setEndDate(parseDateTime(ctx, "end", SystemData.get("time.date_format"), "HH:mm"));
			ZonedDateTime zst = ZonedDateTime.ofInstant(t.getStartDate(), ZoneOffset.UTC);
			DateTimeFormatterBuilder tfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
			tfb.parseDefaulting(ChronoField.YEAR_OF_ERA, zst.get(ChronoField.YEAR_OF_ERA));
			tfb.parseDefaulting(ChronoField.DAY_OF_YEAR, zst.getLong(ChronoField.DAY_OF_YEAR));
			
			// Load flights
			String jsLegs = ctx.getParameter("legCodes");
			if (!StringUtils.isEmpty(jsLegs) && ac.getCanEditLegs()) {
				t.clearFlights();
				JSONArray fo = new JSONArray(new JSONTokener(jsLegs));
				for (int x = 0; x < fo.length(); x++) {
					JSONObject lo = fo.getJSONObject(x);
					ScheduleEntry se = new ScheduleEntry(SystemData.getAirline(lo.getString("airline")), lo.getInt("flight"), lo.optInt("leg", 1));
					se.setEquipmentType(lo.getString("eqType"));
					se.setAirportD(SystemData.getAirport(lo.getJSONObject("airportD").getString("iata")));
					se.setAirportA(SystemData.getAirport(lo.getJSONObject("airportA").getString("iata")));
					se.setTimeD(LocalDateTime.parse(lo.getJSONObject("timeD").getString("text"), tfb.toFormatter()));
					se.setTimeA(LocalDateTime.parse(lo.getJSONObject("timeA").getString("text"), tfb.toFormatter()));
					t.addFlight(se);
				}
			}

			// Load from the request
			t.clearNetworks();
			Collection<String> networks = ctx.getParameters("network", Collections.emptySet());
			networks.stream().map(n -> EnumUtils.parse(OnlineNetwork.class, n, null)).filter(Objects::nonNull).forEach(t::addNetwork);
			t.setStatus(EnumUtils.parse(TourStatus.class, ctx.getParameter("status"), t.getStatus()));
			t.setActive(Boolean.parseBoolean(ctx.getParameter("active")) && !t.getFlights().isEmpty());
			t.setAllowOffline(Boolean.parseBoolean(ctx.getParameter("allowOffline")));
			t.setMatchEquipment(Boolean.parseBoolean(ctx.getParameter("matchEQ")));
			t.setMatchLeg(Boolean.parseBoolean(ctx.getParameter("matchLeg")));
			t.setACARSOnly(Boolean.parseBoolean(ctx.getParameter("acarsOnly")));
			FileUpload bf = ctx.getFile("briefPDF", 8192 * 1024);
			boolean deletePDF = Boolean.parseBoolean(ctx.getParameter("deleteBrief"));
			if ((bf != null) && PDFUtils.isPDF(bf.getBuffer()))
				t.load(bf.getBuffer());
			else if (!deletePDF) {
				String btxt = ctx.getParameter("briefing");
				if (!StringUtils.isEmpty(btxt))
					t.load(btxt);
			} else
				t.load(new byte[0]);

			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(ot, t, "buffer");
			AuditLog ae = AuditLog.create(t, delta, ctx.getUser().getID());

			// Start transaction
			ctx.startTX();

			// Save to the database
			SetTour twdao = new SetTour(con);
			twdao.write(t);

			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();

			// Write status attributes
			ctx.setAttribute("tour", t, REQUEST);
			ctx.setAttribute("isNew", Boolean.valueOf(isNew), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/tourUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Tour.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			if (!isNew) {
				GetTour tdao = new GetTour(con);
				Tour t = tdao.get(ctx.getID(), ctx.getDB());
				if (t == null)
					throw notFoundException("Invalid Tour ID - " + ctx.getID());

				// Check our access
				TourAccessControl ac = new TourAccessControl(ctx, t);
				ac.validate();
				if (!ac.getCanEdit())
					throw securityException("Cannot edit Tour profile - " + t.getID());

				// Serialize the flight data
				JSONArray ja = new JSONArray();
				t.getFlights().stream().map(JSONUtils::format).forEach(ja::put);
				
				// Load pilots in progress/completion
				Collection<Integer> progressIDs = t.getProgress().stream().map(TourProgress::getID).collect(Collectors.toSet());
				if (ac.getCanEdit() && (t.getProgress().size() < 50)) {
					GetPilot pdao = new GetPilot(con);
					ctx.setAttribute("pilots", pdao.getByID(progressIDs, "PILOTS"), REQUEST);
				}
				
				// Remove dupes between progress and completion
				progressIDs.removeAll(t.getCompletionIDs());
				ctx.setAttribute("progressIDs", progressIDs, REQUEST);

				// Save status attributes
				ctx.setAttribute("tour", t, REQUEST);
				ctx.setAttribute("legData", ja.toString(), REQUEST);
				ctx.setAttribute("access", ac, REQUEST);
				
				// Convert start/end date/times to user time zone
				ZoneId tz = ctx.getUser().getTZ().getZone();
				ctx.setAttribute("startDate", ZonedDateTime.ofInstant(t.getStartDate(), tz), REQUEST);
				ctx.setAttribute("endDate", ZonedDateTime.ofInstant(t.getEndDate(), tz), REQUEST);
			} else {
				TourAccessControl ac = new TourAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot create Tour profile");

				ctx.setAttribute("access", ac, REQUEST);
			}
			
			// Load aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(SystemData.get("airline.code")), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Get airports / airlines
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		airports.addAll(SystemData.getAirports().values());
		ctx.setAttribute("airports", airports, REQUEST);
		ctx.setAttribute("airlines", SystemData.getAirlines(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/tourEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Tour.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the Tour
			GetTour tdao = new GetTour(con);
			Tour t = tdao.get(ctx.getID(), ctx.getDB());
			if (t == null)
				throw notFoundException("Invalid Tour ID - " + ctx.getID());

			// Get the access controller
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			if (!ac.getCanRead())
				throw securityException("Cannot read Tour profile - " + t.getID());

			// Load pilots in progress/completion
			Collection<Integer> progressIDs = t.getProgress().stream().map(TourProgress::getID).collect(Collectors.toSet());
			if (ac.getCanEdit() && (t.getProgress().size() < 50)) {
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("pilots", pdao.getByID(progressIDs, "PILOTS"), REQUEST);
			}
			
			// Calculate time to completion
			if (ac.getCanEdit()) {
				double avgTime = t.getProgress().stream().filter(tp -> tp.getLegs() == t.getFlightCount()).mapToLong(tp -> tp.getProgressTime().toSeconds()).average().orElse(0);
				ctx.setAttribute("avgCompletionTime", Duration.ofSeconds((long)avgTime).truncatedTo(ChronoUnit.HOURS), REQUEST);
			}
			
			// Remove dupes between progress and completion
			progressIDs.removeAll(t.getCompletionIDs());
			ctx.setAttribute("progressIDs", progressIDs, REQUEST);
			
			// Build list of airports
			SequencedCollection<Airport> tourAirports = t.getFlights().stream().flatMap(f -> f.getAirports().stream()).collect(Collectors.toCollection(LinkedHashSet::new));
			ctx.setAttribute("tourAirports", tourAirports, REQUEST);
			
			// Load PIREPs and see current progress
			if (ctx.isAuthenticated()) {
				LocalDateTime ldt = LocalDateTime.ofInstant(t.getStartDate(), ZoneOffset.UTC);
				Instant tourStart = t.getStartDate().minusSeconds(ldt.get(ChronoField.SECOND_OF_DAY));
				GetFlightReports frdao = new GetFlightReports(con);
				List<FlightReport> tourFlights = frdao.getLogbookCalendar(ctx.getUser().getID(), ctx.getDB(), tourStart, (int)(Duration.between(tourStart, t.getEndDate()).toDays()) + 1);
				tourFlights.removeIf(fr -> (fr.getDatabaseID(DatabaseID.TOUR) != t.getID()));
				tourFlights.sort(new FlightReportComparator(FlightReportComparator.SUBMISSION));
				
				// Determine progress and remaining
				int maxLeg = tourFlights.stream().mapToInt(fr -> t.getLegIndex(fr)).max().orElse(0);
				List<Airport> myAirports = new ArrayList<Airport>();
				tourFlights.forEach(rp -> filterRoute(rp, myAirports));
				List<Airport> tourRemaining = new ArrayList<Airport>();
				t.getFlights().subList(maxLeg, t.getFlights().size()).forEach(rp -> filterRoute(rp, tourRemaining));
				
				// Set status attributes
				ctx.setAttribute("tourProgress", tourFlights, REQUEST);
				ctx.setAttribute("tourRemaining", tourRemaining, REQUEST);
				ctx.setAttribute("myTourRoute", myAirports, REQUEST);
				ctx.setAttribute("maxLeg", Integer.valueOf(maxLeg), REQUEST);
				if (!t.getFlights().isEmpty()) {
					if (myAirports.isEmpty()) 
						ctx.setAttribute("ctr", tourAirports.isEmpty() ? tourFlights.getLast().getAirportA() : tourAirports.getFirst(), REQUEST);
					else
						ctx.setAttribute("ctr", myAirports.getLast(), REQUEST);
				}
			} else if (!tourAirports.isEmpty()) {
				List<Airport> tourRemaining = new ArrayList<Airport>();
				t.getFlights().forEach(rp -> filterRoute(rp, tourRemaining));
				ctx.setAttribute("tourRemaining", tourRemaining, REQUEST);
				ctx.setAttribute("ctr", tourAirports.getFirst(), REQUEST);
			}

			readAuditLog(ctx, t);
			ctx.setAttribute("tour", t, REQUEST);
			ctx.setAttribute("fbScore", FeedbackScore.generate(t), REQUEST);
			ctx.setAttribute("hasFB", Boolean.valueOf(ctx.isAuthenticated() && t.hasFeedback(ctx.getUser().getID())), REQUEST);
			ctx.setAttribute("isActiveNow", Boolean.valueOf(t.isActiveOn(Instant.now())), REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/tourView.jsp");
		result.setSuccess(true);
	}
	
	private static void filterRoute(RoutePair rp, List<Airport> results) {
		if (results.isEmpty() || !results.get(results.size() -1).equals(rp.getAirportD()))
			results.add(rp.getAirportD());
		
		results.add(rp.getAirportA());
	}
}