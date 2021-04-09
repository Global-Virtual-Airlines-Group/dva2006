// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeFormatterBuilder;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.Tour;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.comparators.*;

import org.deltava.security.command.TourAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display flight Tours.
 * @author Luke
 * @version 10.0
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
			t.setStartDate(parseDateTime(ctx, "start"));
			t.setEndDate(parseDateTime(ctx, "end"));
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
			t.getNetworks().clear();
			Collection<String> networks = ctx.getParameters("network", Collections.emptySet());
			networks.stream().map(n -> EnumUtils.parse(OnlineNetwork.class, n, null)).filter(Objects::nonNull).forEach(t::addNetwork);
			t.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue() && !t.getFlights().isEmpty());
			t.setAllowOffline(Boolean.valueOf(ctx.getParameter("allowOffline")).booleanValue());
			t.setMatchEquipment(Boolean.valueOf(ctx.getParameter("matchEQ")).booleanValue());
			t.setMatchLeg(Boolean.valueOf(ctx.getParameter("matchLeg")).booleanValue());
			t.setACARSOnly(Boolean.valueOf(ctx.getParameter("acarsOnly")).booleanValue());
			FileUpload bf = ctx.getFile("briefPDF");
			boolean deletePDF = Boolean.valueOf(ctx.getParameter("deleteBrief")).booleanValue();
			if ((bf != null) && PDFUtils.isPDF(bf.getBuffer()))
				t.load(bf.getBuffer());
			else if (!deletePDF) {
				String btxt = ctx.getParameter("briefing");
				if (!StringUtils.isEmpty(btxt))
					t.load(btxt);
			} else
				t.load(new byte[0]);

			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(ot, t, "inputStream");
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
				if (ac.getCanEdit() && (t.getProgressIDs().size() < 50)) {
					GetPilot pdao = new GetPilot(con);
					ctx.setAttribute("pilots", pdao.getByID(t.getProgressIDs(), "PILOTS"), REQUEST);
				}

				// Save status attributes
				ctx.setAttribute("tour", t, REQUEST);
				ctx.setAttribute("legData", ja.toString(), REQUEST);
				ctx.setAttribute("access", ac, REQUEST);
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

		// Get airlines
		Collection<Airline> airlines = new TreeSet<Airline>(new AirlineComparator(AirlineComparator.NAME));
		airlines.addAll(SystemData.getAirlines().values());
		ctx.setAttribute("airlines", airlines, REQUEST);

		// Get airports
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		airports.addAll(SystemData.getAirports().values());
		ctx.setAttribute("airports", airports, REQUEST);

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
			if (ac.getCanEdit() && (t.getProgressIDs().size() < 50)) {
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("pilots", pdao.getByID(t.getProgressIDs(), "PILOTS"), REQUEST);
			}

			readAuditLog(ctx, t);
			ctx.setAttribute("tour", t, REQUEST);
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
}