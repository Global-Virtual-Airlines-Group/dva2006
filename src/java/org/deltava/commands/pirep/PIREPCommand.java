// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.temporal.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.academy.Status;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.econ.FlightEliteScore;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.servinfo.NetworkOutage;
import org.deltava.beans.servinfo.OnlineTime;
import org.deltava.beans.servinfo.PositionData;
import org.deltava.beans.stats.*;
import org.deltava.beans.system.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.dao.http.GetVRouteData;

import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle editing/saving Flight Reports.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class PIREPCommand extends AbstractFormCommand {
	
	private static final Logger log = Logger.getLogger(PIREPCommand.class);

	private final Collection<String> _flightTimes = new LinkedHashSet<String>();

	/**
	 * Initialize the command.
	 * @param id the Command ID
	 * @param cmdName the name of the Command
	 */
	@Override
	public void init(String id, String cmdName) {
		super.init(id, cmdName);
		for (int x = 2; x < 189; x++)
			_flightTimes.add(String.valueOf(x / 10.0f));
	}

	/**
	 * Callback method called when saving the PIREP.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we are doing a submit & save
		boolean doSubmit = Boolean.valueOf(ctx.getParameter("doSubmit")).booleanValue();
		FlightReport fr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the original version from the database
			final int id = ctx.getID();
			GetFlightReports rdao = new GetFlightReports(con);
			fr = rdao.get(id, ctx.getDB());

			// Check if we are creating a new flight report or editing one with an assignment
			boolean doCreate = (fr == null);
			boolean isAssignment = (fr != null) && (fr.getDatabaseID(DatabaseID.ASSIGN) != 0);

			// Create the access controller and validate our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			boolean hasAccess = doCreate ? ac.getCanCreate() : ac.getCanEdit();
			doSubmit &= ac.getCanSubmitIfEdit(); // If we cannot submit just turn that off
			if (!hasAccess)
				throw securityException("Not Authorized");
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			@SuppressWarnings("null")
			Pilot p = doCreate ? (Pilot) ctx.getUser() : pdao.get(fr.getDatabaseID(DatabaseID.PILOT));

			// Get the airline/airports - don't allow updates if an assignment
			@SuppressWarnings("null")
			Airline a = isAssignment ? fr.getAirline() : SystemData.getAirline(ctx.getParameter("airline"));
			@SuppressWarnings("null")
			Airport aa = isAssignment ? fr.getAirportA() : SystemData.getAirport(ctx.getParameter("airportA"));
			@SuppressWarnings("null")
			Airport ad = isAssignment ? fr.getAirportD() : SystemData.getAirport(ctx.getParameter("airportD"));
			if (a == null)
				a = SystemData.getAirline(SystemData.get("airline.code"));
			
			// Load airports from the code if they're empty
			if (!isAssignment) {
				if (ad == null)
					ad = SystemData.getAirport(ctx.getParameter("airportDCode"));
				if (aa == null)
					aa = SystemData.getAirport(ctx.getParameter("airportACode"));
			}
			
			// Validate airports
			if ((aa == null) || (ad == null))
				throw notFoundException("Invalid Airport(s) - " + ctx.getParameter("airportDCode") + " / " 	+ ctx.getParameter("airportACode"));

			// If we are creating a new PIREP, check if draft PIREP exists with a similar route pair
			ScheduleRoute rt = new ScheduleRoute(ad, aa);
			List<FlightReport> draftFlights = rdao.getDraftReports(ctx.getUser().getID(), rt, ctx.getDB());
			Optional<FlightReport> ofr = draftFlights.stream().filter(dfr -> (dfr.getID() == id)).findAny();
			if (ofr.isPresent())
				fr = ofr.get();
			else if (doCreate && !draftFlights.isEmpty())
				fr = draftFlights.get(0);

			// Create a new PIREP bean if we're creating one, otherwise update the flight code
			if (fr != null) {
				fr.setAirline(a);
				fr.setFlightNumber(StringUtils.parse(ctx.getParameter("flightNumber"), 1));
				fr.setLeg(StringUtils.parse(ctx.getParameter("flightLeg"), 1));
			} else
				fr = new FlightReport(a, StringUtils.parse(ctx.getParameter("flightNumber"), 1), StringUtils.parse(ctx.getParameter("flightLeg"), 1));

			// Update the original PIREP with fields from the request
			fr.setDatabaseID(DatabaseID.PILOT, ctx.getUser().getID());
			fr.setRank(ctx.getUser().getRank());
			fr.setAirportD(ad);
			fr.setAirportA(aa);
			fr.setEquipmentType(ctx.getParameter("eq"));
			fr.setRemarks(ctx.getParameter("remarks"));
			fr.setSimulator(Simulator.fromName(ctx.getParameter("fsVersion"), Simulator.UNKNOWN));
			fr.setRoute(ctx.getParameter("route"));
			if (fr.getID() == 0)
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Draft Report created"); 

			// Check for historic aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft aInfo = acdao.get(fr.getEquipmentType());
			if (aInfo != null) {
				AircraftPolicyOptions opts = aInfo.getOptions(SystemData.get("airline.code"));
				fr.setAttribute(FlightReport.ATTR_HISTORIC, aInfo.getHistoric());
				fr.setAttribute(FlightReport.ATTR_ACADEMY, aInfo.getAcademyOnly());
				fr.setAttribute(FlightReport.ATTR_RANGEWARN, (fr.getDistance() > opts.getRange()));
				
				// Check for excessive weight
				if (fr instanceof ACARSFlightReport) {
					ACARSFlightReport afr = (ACARSFlightReport) fr;
					if ((aInfo.getMaxTakeoffWeight() != 0) && (afr.getTakeoffWeight() > aInfo.getMaxTakeoffWeight()))
						afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
					else if ((aInfo.getMaxLandingWeight() != 0) && (afr.getLandingWeight() > aInfo.getMaxLandingWeight()))
						afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
					else if (afr.hasAttribute(FlightReport.ATTR_WEIGHTWARN))
						afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, false);
				}
				
				// If the passengers are non-zero, update the count
				if (fr.getPassengers() != 0) {
					int newPax = (int) Math.round(opts.getSeats() * fr.getLoadFactor());
					if (newPax != fr.getPassengers()) {
						fr.addStatusUpdate(0, HistoryType.SYSTEM, "Updated passengers from " + fr.getPassengers() + " to " + newPax);
						fr.setPassengers(newPax);
					}
				}
			}

			// Figure out what network the flight was flown on and ensure we have an ID
			OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), null);
			if ((net != null) && !p.hasNetworkID(net))
				throw new IllegalStateException("No " + net + " ID");

			if (fr.getNetwork() != net) {
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.SYSTEM, "Updated online network from " + fr.getNetwork() + " to " + ((net == null) ? "Offline" : net));
				fr.setNetwork(net);
			}
			
			// Get the flight time
			try {
				double fTime = Double.parseDouble(ctx.getParameter("flightTime"));
				fr.setLength((int) (fTime * 10));
			} catch (NumberFormatException nfe) {
				throw new CommandException("Invalid Flight Time", false);
			}

			// Calculate the date
			ZonedDateTime now = ZonedDateTime.now(ctx.getUser().getTZ().getZone());
			Month mn = EnumUtils.parse(Month.class, ctx.getParameter("dateM"), now.getMonth());
			LocalDateTime pd = LocalDateTime.of(StringUtils.parse(ctx.getParameter("dateY"), now.getYear()), mn.getValue(), StringUtils.parse(ctx.getParameter("dateD"), now.getDayOfMonth()), 12, 0, 0);
			fr.setDate(ZonedDateTime.of(pd, ctx.getUser().getTZ().getZone()).toInstant());

			// Validate the date
			if (!ctx.isUserInRole("PIREP")) {
				Instant forwardLimit = ZonedDateTime.now().plusDays(SystemData.getInt("users.pirep.maxDays")).toInstant();
				Instant backwardLimit = ZonedDateTime.now().minusDays(SystemData.getInt("users.pirep.maxDays")).toInstant();
				if ((fr.getDate().isBefore(backwardLimit)) || (fr.getDate().isAfter(forwardLimit)))
					throw new CommandException("Invalid Flight Report Date - " + fr.getDate() + " (" + backwardLimit + " - " + forwardLimit + ")", false);
			}
			
			// Start transaction
			ctx.startTX();

			// Get the DAO and write the updateed PIREP to the database
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.write(fr);
			
			// If the flight is already approved, recalc statistics
			if (fr.getStatus() == FlightStatus.OK) {
				SetAggregateStatistics swdao = new SetAggregateStatistics(con);
				swdao.update(fr);
			}

			// Update the status for the JSP
			ctx.commitTX();
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("isCreated", Boolean.valueOf(doCreate), REQUEST);
			ctx.setAttribute("isOurs", Boolean.valueOf(fr.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID()), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set the redirection URL
		CommandResult result = ctx.getResult();
		result.setSuccess(true);
		if (doSubmit)
			result.setURL("submit", null, fr.getID());
		else {
			ctx.setAttribute("isSaved", Boolean.TRUE, REQUEST);
			result.setType(ResultType.REQREDIRECT);
			result.setURL("/jsp/pilot/pirepUpdate.jsp");
		}
	}

	/**
	 * Callback method called when editing the PIREP.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get command results
		CommandResult result = ctx.getResult();
		
		// Don't allow anonymous access
		if (!ctx.isAuthenticated())
			throw securityException("Cannot create/edit Flight Report");

		// Check if we're creating a new PIREP
		Pilot usr = ctx.getUser();
		boolean isNew = (ctx.getID() == 0);
		boolean forcePage = (ctx.getSession() != null) && Boolean.valueOf(String.valueOf(ctx.getSession().getAttribute("forcePIREP"))).booleanValue();

		// Get the current date/time in the user's local zone
		TZInfo tz = ctx.isAuthenticated() ? ctx.getUser().getTZ() : TZInfo.get(SystemData.get("time.timezone"));
		ZonedDateTime today = ZonedDateTime.now(tz.getZone());

		// Get all airlines
		Map<String, Airline> allAirlines = SystemData.getAirlines();
		Collection<Airline> airlines = new TreeSet<Airline>();
		PIREPAccessControl ac = null;
		try {
			Connection con = ctx.getConnection();

			// Send to the ACARS nag page
			if (isNew && (usr.getACARSLegs() == 0) && (!forcePage) && SystemData.getBoolean("acars.enabled")) {
				GetEquipmentType eqdao = new GetEquipmentType(con);
				ctx.setAttribute("eqType", eqdao.get(usr.getEquipmentType()), REQUEST);
				ctx.release();
				
				// Set force flag and display JSP
				ctx.setAttribute("forcePIREP", Boolean.TRUE, SESSION);
				result.setURL("/jsp/pilot/pirepNagACARS.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check for academy course
			GetAcademyCourses crsdao = new GetAcademyCourses(con);
			boolean hasCourse = crsdao.getByPilot(usr.getID()).stream().anyMatch(crs -> crs.getStatus() == Status.STARTED);
			
			//	Get aircraft types
			GetAircraft acdao = new GetAircraft(con);
			Collection<Aircraft> eqTypes = acdao.getAircraftTypes();
			if (!hasCourse)
				eqTypes.removeIf(Aircraft::getAcademyOnly);
			
			ctx.setAttribute("eqTypes", eqTypes, REQUEST);

			// Get the DAO and load the flight report
			GetFlightReports dao = new GetFlightReports(con);
			if (isNew) {
				ac = new PIREPAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate() || (usr.getACARSRestriction() == Restriction.NOMANUAL))
					throw securityException("Cannot create new PIREP");

				// Save the user object
				ctx.setAttribute("pilot", usr, REQUEST);
				ctx.setAttribute("networks", usr.getNetworks(), REQUEST);

				// Get the active airlines
				allAirlines.values().stream().filter(Airline::getActive).forEach(a-> airlines.add(a));
			} else {
				FlightReport fr = dao.get(ctx.getID(), ctx.getDB());
				if (fr == null)
					throw notFoundException("Invalid Flight Report - " + ctx.getID());

				// Check our access
				ac = new PIREPAccessControl(ctx, fr);
				ac.validate();
				if (!ac.getCanEdit() || (usr.getACARSRestriction() == Restriction.NOMANUAL))
					throw securityException("Not Authorized");

				// Save the pilot info/PIREP in the request
				GetPilot pdao = new GetPilot(con);
				Pilot p = pdao.get(fr.getDatabaseID(DatabaseID.PILOT));
				ctx.setAttribute("pilot", p, REQUEST);
				ctx.setAttribute("pirep", fr, REQUEST);
				ctx.setAttribute("networks", p.getNetworks(), REQUEST);

				// Set PIREP date and length
				today = ZonedDateTime.ofInstant(fr.getDate(), ctx.getUser().getTZ().getZone());
				ctx.setAttribute("flightTime", StringUtils.format(fr.getLength() / 10.0, "#0.0"), REQUEST);

				// Get the active airlines
				if (fr.getDatabaseID(DatabaseID.ASSIGN) == 0)
					allAirlines.values().stream().filter(a -> (a.getActive() || fr.getAirline().equals(a))).forEach(airlines::add);
				else
					airlines.add(fr.getAirline());
			}
			
			ctx.setAttribute("airlines", airlines, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save PIREP date limitations
		int maxRange = SystemData.getInt("users.pirep.maxDays", 1);
		int minRange = SystemData.getInt("users.pirep.minDays", 7);
		ctx.setAttribute("forwardDateLimit", today.plusDays(maxRange), REQUEST);
		ctx.setAttribute("backwardDateLimit", today.minusDays(minRange), REQUEST);
		
		// Set flight years
		Collection<Integer> years = new TreeSet<Integer>();
		years.add(Integer.valueOf(today.get(ChronoField.YEAR)));

		// If we're in the range, add the previous year
		if (today.get(ChronoField.DAY_OF_YEAR) <= maxRange)
			years.add(Integer.valueOf(today.get(ChronoField.YEAR) - 1));
		
		// If we're new years eve, add next year
		if ((today.get(ChronoField.MONTH_OF_YEAR) == 12) && (today.get(ChronoField.DAY_OF_MONTH) > 30))
			years.add(Integer.valueOf(today.get(ChronoField.YEAR) + 1));

		// Save pirep date combobox values
		ctx.setAttribute("pirepYear", StringUtils.format(today.get(ChronoField.YEAR), "0000"), REQUEST);
		ctx.setAttribute("pirepMonth", StringUtils.format(today.get(ChronoField.MONTH_OF_YEAR), "#0"), REQUEST);
		ctx.setAttribute("pirepDay", StringUtils.format(today.get(ChronoField.DAY_OF_MONTH), "#0"), REQUEST);

		// Save airport/airline lists in the request
		ctx.setAttribute("airline", SystemData.get("airline.code"), REQUEST);

		// Set basic lists for the JSP
		ctx.setAttribute("flightTimes", _flightTimes, REQUEST);
		ctx.setAttribute("months", ComboUtils.properCase(Month.values()), REQUEST);
		ctx.setAttribute("years", years, REQUEST);

		// Set the access controller
		ctx.setAttribute("access", ac, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/pilot/pirepEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the PIREP.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {

		// Calculate what map type to use
		MapType mapType = ctx.isAuthenticated() ? ctx.getUser().getMapType() : MapType.GOOGLE;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs and load the flight report
			GetFlightReports dao = new GetFlightReports(con);
			GetPilot pdao = new GetPilot(con);
			FlightReport fr = dao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Get the pilot
			Pilot p = pdao.get(fr.getDatabaseID(DatabaseID.PILOT));
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + fr.getDatabaseID(DatabaseID.PILOT));
			
			// If the flight report is a draft, then load it
			if (fr.getStatus() == FlightStatus.DRAFT) {
				final int id = fr.getID();
				Collection<FlightReport> draftReports = dao.getDraftReports(p.getID(), fr, ctx.getDB());
				fr = draftReports.stream().filter(dfr -> (dfr.getID() == id)).findFirst().orElse(fr);
			}
			
			// Get the pilot who approved/rejected this PIREP
			int disposalID = fr.getDatabaseID(DatabaseID.DISPOSAL);
			if (disposalID != 0)
				ctx.setAttribute("disposedBy", pdao.get(disposalID), REQUEST);

			// If this PIREP was flown as part of an event, get its information
			GetEvent evdao = new GetEvent(con);
			int eventID = fr.getDatabaseID(DatabaseID.EVENT);
			if (eventID != 0)
				ctx.setAttribute("event", evdao.get(eventID), REQUEST);
			
			// If this PIREP is part of a flight assignment and a draft, load the assignment
			if ((fr.getStatus() == FlightStatus.DRAFT) && (fr.getDatabaseID(DatabaseID.ASSIGN) != 0)) {
				GetAssignment fadao = new GetAssignment(con);
				AssignmentInfo assign = fadao.get(fr.getDatabaseID(DatabaseID.ASSIGN));
				
				// Check our access
				AssignmentAccessControl aac = new AssignmentAccessControl(ctx, assign);
				aac.validate();
				
				// Save access and assignment
				ctx.setAttribute("assignmentInfo", assign, REQUEST);
				ctx.setAttribute("assignAccess", aac, REQUEST);
			}
			
			// Create the access controller and stuff it in the request
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			ctx.setAttribute("access", ac, REQUEST);
			
			// Calculate the average time between the airports and user's networks
			if (ac.getCanDispose()) {
				GetRawSchedule rsdao = new GetRawSchedule(con);
				org.deltava.dao.GetSchedule scdao = new org.deltava.dao.GetSchedule(con);
				scdao.setSources(rsdao.getSources(true, ctx.getDB()));
				FlightTime ft = scdao.getFlightTime(fr, ctx.getDB());
				ctx.setAttribute("avgTime", Integer.valueOf(ft.getFlightTime()), REQUEST);
				ctx.setAttribute("networks", p.getNetworks(), REQUEST);
			}
			
			// Load status history
			if (ac.getCanViewComments()) {
				GetFlightReportHistory stdao = new GetFlightReportHistory(con);
				Collection<FlightHistoryEntry> history = stdao.getEntries(fr.getID());
				Collection<Integer> IDs = history.stream().filter(upd -> (upd.getAuthorID() != 0)).map(AuthoredBean::getAuthorID).collect(Collectors.toSet());
				ctx.setAttribute("statusHistory", history, REQUEST);
				ctx.setAttribute("statusHistoryUsers", pdao.getByID(IDs, "PILOTS"), REQUEST);
			}
			
			// If we're online and not on an event, list possible event
			if (ac.getCanDispose() && fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK) && (fr.getDatabaseID(DatabaseID.EVENT) == 0))
				ctx.setAttribute("possibleEvents", evdao.getPossibleEvents(fr, SystemData.get("airline.code")), REQUEST);
			
			// Get the elite status if applicable
			if (SystemData.getBoolean("econ.elite.enabled")) {
				FlightEliteScore es = dao.getElite(fr.getID());
				if (es != null) {
					es.setAuthorID(fr.getAuthorID());
					GetElite edao = new GetElite(con);	
					ctx.setAttribute("eliteLevel", edao.get(es.getEliteLevel(), es.getYear(), ctx.getDB()), REQUEST);
					ctx.setAttribute("eliteScore", es, REQUEST);
				}
			}
			
			// Get tour eligibility
			if (fr.getDatabaseID(DatabaseID.TOUR) != 0) {
				GetTour tdao = new GetTour(con);
				Tour t = tdao.get(fr.getDatabaseID(DatabaseID.TOUR), ctx.getDB());
				if (t != null) {
					ctx.setAttribute("tour", t, REQUEST);
					ctx.setAttribute("tourIdx", Integer.valueOf(t.getLegIndex(fr)), REQUEST);
				}
			}

			// Get the Navdata DAO
			GetNavRoute navdao = new GetNavRoute(con);
			navdao.setEffectiveDate(fr.getDate());

			// Check if this is an ACARS flight - search for an open checkride, and load the ACARS data
			boolean isACARS = (fr instanceof FDRFlightReport);
			if (isACARS) {
				FDRFlightReport afr = (FDRFlightReport) fr;
				mapType = MapType.GOOGLE;
				ctx.setAttribute("isACARS", Boolean.TRUE, REQUEST);
				ctx.setAttribute("isSimFDR", Boolean.valueOf(afr.getFDR() == Recorder.SIMFDR), REQUEST);
				ctx.setAttribute("isXACARS", Boolean.valueOf(afr.getFDR() == Recorder.XACARS), REQUEST);
				int flightID = afr.getDatabaseID(DatabaseID.ACARS);

				// Get the route data from the DAFIF database
				GetACARSLog ardao = new GetACARSLog(con);
				FlightInfo info = ardao.getInfo(flightID);
				if (info != null) {
					ctx.setAttribute("flightInfo", info, REQUEST);
					
					// Get the aircraft profile
					GetAircraft acdao = new GetAircraft(con);
					Aircraft acInfo = acdao.get(fr.getEquipmentType());
					if ((acInfo != null) && (acInfo.getMaxWeight() > 0))
						ctx.setAttribute("acInfo", acInfo, REQUEST);
					
					// Get the flight score
					AircraftPolicyOptions opts = (acInfo == null) ? null : acInfo.getOptions(SystemData.get("airline.code"));
					ScorePackage pkg = new ScorePackage(acInfo, afr, info.getRunwayD(), info.getRunwayA(), opts);
					if (afr.hasAttribute(FlightReport.ATTR_CHECKRIDE) && (afr.getFDR() != Recorder.XACARS)) {
						GetACARSPositions posdao = new GetACARSPositions(con);
						Collection<GeospaceLocation> positions = posdao.getRouteEntries(info.getID(), true, info.getArchived());
						positions.stream().filter(ACARSRouteEntry.class::isInstance).map(ACARSRouteEntry.class::cast).forEach(pkg::add);
					}
					
					FlightScore score = FlightScorer.score(pkg);
					if (score != FlightScore.INCOMPLETE)
						ctx.setAttribute("flightScore", pkg, REQUEST);
					
					// Get the IP address
					if (ctx.isUserInRole("HR")) {
						GetIPLocation ipdao = new GetIPLocation(con);
						ctx.setAttribute("ipInfo", ipdao.get(info.getRemoteAddr()), REQUEST);
					}
					
					// Get on-time data
					if (afr.getFDR() != Recorder.XACARS) {
						ACARSFlightReport acfr = (ACARSFlightReport) afr;
						if (acfr.getOnTime() != OnTime.UNKNOWN) {
							GetACARSOnTime aotdao = new GetACARSOnTime(con);
							ctx.setAttribute("onTimeEntry", aotdao.getOnTime(acfr), REQUEST);
						}
					}
					
					// Get system info
					if (ctx.isUserInRole("Developer") || ctx.isUserInRole("Operations")) {
						GetSystemInfo sysdao = new GetSystemInfo(con);
						GetACARSPerformance apdao = new GetACARSPerformance(con);
						ctx.setAttribute("acarsClientInfo", sysdao.get(afr.getAuthorID(), afr.getSimulator(), afr.getSubmittedOn()), REQUEST);
						ctx.setAttribute("acarsTimerInfo", apdao.getTimers(info.getID()), REQUEST);
						if (info.getArchived())
							ctx.setAttribute("archiveMetadata", ardao.getArchiveInfo(info.getID()), REQUEST);
					}
					
					// Load the dispatcher if there is one
					if (info.getDispatcherID() != 0) {
						GetUserData uddao = new GetUserData(con);
						UserData ud = uddao.get(info.getDispatcherID());
						if (ud != null)
							ctx.setAttribute("dispatcher", pdao.get(ud), REQUEST);
					}
					
					// Load the dispatch log entry
					if (info.getDispatchLogID() != 0)
						ctx.setAttribute("dispatchLog", ardao.getDispatchLog(info.getDispatchLogID()), REQUEST);
					
					// Load the gates
					GetGates gdao = new GetGates(con);
					gdao.populate(info);
					
					// Load taxi times
					GetACARSTaxiTimes ttdao = new GetACARSTaxiTimes(con); int year = LocalDate.ofInstant(fr.getDate(), ZoneOffset.UTC).getYear();
					ctx.setAttribute("avgTaxiInTime", ttdao.getTaxiTime(afr.getAirportA(), year), REQUEST);
					ctx.setAttribute("avgTaxiOutTime", ttdao.getTaxiTime(afr.getAirportD(), year), REQUEST);
					
					// Build the route
					RouteBuilder rb = new RouteBuilder(fr, info.getRoute());
					Collection<MapEntry> route = new LinkedHashSet<MapEntry>();
					route.add((info.getGateD() != null) ? info.getGateD() : info.getAirportD());
					if (info.getRunwayD() != null)
						route.add(info.getRunwayD());
					rb.add(info.getSID());
					
					// Load the serialized route
					Collection<NavigationDataBean> rtePoints = new ArrayList<NavigationDataBean>();
					if (ArchiveHelper.getRoute(fr.getID()).exists()) {
						GetNavCycle ncdao = new GetNavCycle(con);
						try (InputStream in = ArchiveHelper.getStream(ArchiveHelper.getRoute(fr.getID()))) {
							GetSerializedRoute rtdao = new GetSerializedRoute(in);
							ArchivedRoute arcRt = rtdao.read();
							rtePoints.addAll(arcRt.getWaypoints());
							if (arcRt.getAIRACVersion() > 0)
								ctx.setAttribute("routeCycleInfo", ncdao.getCycle(String.valueOf(arcRt.getAIRACVersion())), REQUEST);
						} catch (IOException ie) {
							log.error("Error loading serialized route - " + ie.getMessage(), ie);
						}
					}
					
					// Check the SID
					if ((info.getSID() == null) && (rb.getSID() != null)) {
						TerminalRoute sid = navdao.getBestRoute(info.getAirportD(), TerminalRoute.Type.SID, TerminalRoute.makeGeneric(rb.getSID()), rb.getSIDTransition(), info.getRunwayD());
						rb.add(sid);
					} else if (info.getSID() != null)
						rb.add(info.getSID());
						
					if (rtePoints.isEmpty())
						rtePoints.addAll(navdao.getRouteWaypoints(rb.getRoute(), info.getAirportD()));
					rtePoints.forEach(rb::add);

					// Check the STAR
					if ((info.getSTAR() == null) && (rb.getSTAR() != null)) {
						TerminalRoute star = navdao.getBestRoute(info.getAirportA(), TerminalRoute.Type.STAR, TerminalRoute.makeGeneric(rb.getSTAR()), rb.getSTARTransition(), info.getRunwayA());
						rb.add(star);
					} else if (info.getSTAR() != null)
						rb.add(info.getSTAR());

					route.addAll(rb.getPoints());
					if (info.getRunwayA() != null)
						route.add(info.getRunwayA());
					route.add((info.getGateA() != null) ? info.getGateA() : info.getAirportA());
					
					// Load departure and arrival runways
					if (ac.getCanDispose()) {
						Collection<Runway> dRwys = navdao.getRunways(fr.getAirportD(), fr.getSimulator());
						if (info.getRunwayD() != null)
							dRwys = CollectionUtils.sort(dRwys, new RunwayComparator(info.getRunwayD().getHeading(), 5));	
					
						Collection<Runway> aRwys = navdao.getRunways(fr.getAirportA(), fr.getSimulator());
						if (info.getRunwayA() != null)
							aRwys = CollectionUtils.sort(aRwys, new RunwayComparator(info.getRunwayA().getHeading(), 5));	
					
						// Save runway choices
						ctx.setAttribute("dRunways", dRwys, REQUEST);
						ctx.setAttribute("aRunways", aRwys, REQUEST);
					}
					
					// Save ACARS route, stripping out excessive bits
					ctx.setAttribute("filedRoute", GeoUtils.stripDetours(route, 250), REQUEST);
				}

				// Get the check ride
				CheckRide cr = null;
				GetExam crdao = new GetExam(con);
				if (flightID != 0)
					cr = crdao.getACARSCheckRide(flightID);

				// If we have a check ride, then save it and calculate the access level
				if (cr != null) {
					ExamAccessControl crAccess = null;
					try {
						crAccess = new ExamAccessControl(ctx, cr, null);
						crAccess.validate();
						ctx.setAttribute("crAccess", crAccess, REQUEST);

						// Allow Examiner to score the PIREP even if they otherwise couldn't
						boolean canScoreCR = crAccess.getCanScore() && (cr.getStatus() == TestStatus.SUBMITTED);
						canScoreCR &= (ac.getCanApprove() || cr.getAcademy());
						ctx.setAttribute("scoreCR", Boolean.valueOf(canScoreCR), REQUEST);
					} catch (AccessControlException ace) {
						ctx.setAttribute("scoreCR", Boolean.FALSE, REQUEST);
					}

					ctx.setAttribute("checkRide", cr, REQUEST);
				}
			}
			
			// Load the online track
			GetOnlineTrack tdao = new GetOnlineTrack(con);
			boolean hasTrack = tdao.hasTrack(fr.getID());
			if (hasTrack || (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK) && (fr.getStatus() != FlightStatus.DRAFT))) {
				File f = ArchiveHelper.getOnline(fr.getID());
				Collection<PositionData> pd = new ArrayList<PositionData>();
				if (f.exists()) {
					try (InputStream in = ArchiveHelper.getStream(f)) {
						GetSerializedOnline stdao = new GetSerializedOnline(in);
						pd.addAll(stdao.read());
					} catch (IOException ie) {
						log.error("Error loading serialized Online Track data - " + ie.getMessage(), ie);
						f.delete();
					}
				}
			
				if (pd.isEmpty())
					pd.addAll(tdao.get(fr.getID()));
				
				long age = (fr.getSubmittedOn() == null) ? Long.MAX_VALUE : (System.currentTimeMillis() - fr.getSubmittedOn().toEpochMilli()) / 1000;
				if (pd.isEmpty() && (age < 86000)) {
					int trackID = tdao.getTrackID(fr.getDatabaseID(DatabaseID.PILOT), fr.getNetwork(), fr.getSubmittedOn(), fr.getAirportD(), fr.getAirportA());
					if ((trackID == 0) && (fr.getNetwork() == OnlineNetwork.VATSIM)) {
						try {
							GetVRouteData vddao = new GetVRouteData();
							pd = vddao.getPositions(p, fr.getAirportD(), fr.getAirportA());
							ctx.setAttribute("vRouteData", Boolean.valueOf(pd.size() > 0), REQUEST);
						} catch (DAOException de) {
							log.warn("Cannot download VRoute position data - " + de.getMessage());
						}
					} else if (trackID != 0) {
						pd = tdao.getRaw(trackID);
						if (StringUtils.isEmpty(fr.getRoute()))
							fr.setRoute(tdao.getRoute(trackID));
					}
					
					// Save the position and the route
					synchronized (this) {
						boolean hasDataLoaded = tdao.hasTrack(fr.getID());
					
						// Save the positions if we get them
						if (!pd.isEmpty() && (age > 300) & !hasDataLoaded) {
							ctx.startTX();
							
							// Move track data from the raw table
							SetOnlineTrack twdao = new SetOnlineTrack(con);
							twdao.write(fr.getID(), pd, ctx.getDB());
							twdao.purgeRaw(trackID);
							
							// Save the route
							SetFlightReport frwdao = new SetFlightReport(con);
							frwdao.write(fr);
							
							// Commit
							ctx.commitTX();
						}
					}
				}
				
				// Check for data outages
				if (fr.hasAttribute(FlightReport.ATTR_FDR_MASK) && (fr.getNetwork( ) != null) && (ac.getCanDispose() || ctx.isUserInRole("PIREP") || ctx.isUserInRole("Operations"))) {
					FDRFlightReport ffr = (FDRFlightReport) fr;
					Collection<NetworkOutage> networkOutages = NetworkOutage.calculate(fr.getNetwork(), tdao.getFetches(fr.getNetwork(), ffr.getStartTime(), ffr.getEndTime()), 120);
					ctx.setAttribute("networkOutages", networkOutages, REQUEST);
				}
				
				// Calculate the online time
				ctx.setAttribute("onlineTime", Integer.valueOf(OnlineTime.calculate(pd, SystemData.getInt("online.track_gap", 20))), REQUEST);
				
				// Write the positions
				if (mapType == MapType.GOOGLE)
					ctx.setAttribute("onlineTrack", pd, REQUEST);
			}
			
			// If the PIREP has a route in it, load it here
			if (!StringUtils.isEmpty(fr.getRoute()) && !isACARS) {
				RouteBuilder rb = new RouteBuilder(fr, fr.getRoute());

				// Load the SID
				if (rb.getSID() != null) {
					TerminalRoute sid = navdao.getBestRoute(fr.getAirportD(), TerminalRoute.Type.SID, TerminalRoute.makeGeneric(rb.getSID()), rb.getSIDTransition(), (String) null);
					rb.add(sid);
				}
				
				navdao.getRouteWaypoints(rb.getRoute(), fr.getAirportD()).forEach(wp -> rb.add(wp));
				
				// Load the STAR
				if (rb.getSTAR() != null) {
					TerminalRoute star = navdao.getBestRoute(fr.getAirportA(), TerminalRoute.Type.STAR, TerminalRoute.makeGeneric(rb.getSTAR()), rb.getSTARTransition(), (String) null);
					rb.add(star);
				}

				// Build the route
				Collection<MapEntry> route = new LinkedHashSet<MapEntry>();
				route.add(fr.getAirportD());
				route.addAll(rb.getPoints());
				route.add(fr.getAirportA());
				ctx.setAttribute("filedRoute", GeoUtils.stripDetours(route, 65), REQUEST);			
				mapType = MapType.GOOGLEStatic;
			} else if (!isACARS && (mapType != MapType.FALLINGRAIN)) {
				Collection<GeoLocation> rt = List.of(fr.getAirportD(), fr.getAirportA());
				ctx.setAttribute("mapRoute", rt, REQUEST);
				ctx.setAttribute("filedRoute", rt, REQUEST);
				if (!hasTrack && (mapType == MapType.GOOGLE))
					mapType = MapType.GOOGLEStatic;
			}

			// If we're set to use Google Maps, check API usage
			if (mapType == MapType.GOOGLE) {
				HTTPContextData hctxt = (HTTPContextData) ctx.getRequest().getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME);
				boolean isSpider = (hctxt == null) || (hctxt.getBrowserType() == BrowserType.SPIDER);
				
				int max = SystemData.getInt("api.max.googleMaps", -1);
				int dailyMax = max / 30;
				if (isSpider)
					dailyMax *= 0.2;
				else if (!ctx.isAuthenticated())
					dailyMax *= 0.75;

				// Get today's predicted use
				GetSystemLog sldao = new GetSystemLog(con);
				APIUsage todayUse = sldao.getCurrentAPIUsage(API.GoogleMaps, "DYNAMIC");
				APIUsage predictedUse = APIUsageHelper.predictToday(todayUse);
				if (ctx.isUserInRole("Developer"))
					ctx.setHeader("X-API-DailyUsage", "Max " + dailyMax + " / a=" + todayUse.getTotal() + ",p=" + predictedUse.getTotal());
				
				// Override usage
				boolean isOurs = ctx.isAuthenticated() && (fr.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID());
				boolean forceMap = isOurs || ctx.isUserInRole("Developer") || ctx.isUserInRole("PIREP") || ctx.isUserInRole("Instructor") || ctx.isUserInRole("Operations");
				boolean isDraft = (fr.getStatus() == FlightStatus.DRAFT);
				
				// If we're below the daily max, all good
				if ((predictedUse.getTotal() > dailyMax) && !isSpider && !isDraft) {
					String method = API.GoogleMaps.createName("DYNAMIC");
					Collection<APIUsage> usage = sldao.getAPIRequests(API.GoogleMaps, 31);
					predictedUse = APIUsageHelper.predictUsage(usage, method);
					
					// Calculate actual usage
					APIUsage totalUse = new APIUsage(Instant.now(), method);
					usage.stream().forEach(u -> { totalUse.setTotal(totalUse.getTotal() + u.getTotal()); totalUse.setAnonymous(totalUse.getAnonymous() + u.getAnonymous()); });
					if (ctx.isUserInRole("Developer"))
						ctx.setHeader("X-API-MonthUsage", "Max " + max + " / a=" + totalUse.getTotal() + ",p=" + predictedUse.getTotal());

					// If predicted usage is less than 90% of max or less than 110% of max and we're auth, OK
					if (!forceMap && ((predictedUse.getTotal() > (max * 1.10)) || (!ctx.isAuthenticated() && (predictedUse.getTotal() > (max *0.9))))) {
						log.warn("GoogleMap disabled - usage [max=" + max + ", predicted=" + predictedUse.getTotal() + ", actual=" + totalUse.getTotal() + "] : " + ctx.getRequest().getRemoteHost() + " spider=" + isSpider);
						mapType = MapType.GOOGLEStatic;
					}
				} else if (isSpider || isDraft || (predictedUse.getTotal() > dailyMax))
					mapType = MapType.GOOGLEStatic;
			}				

			// Get the pilot/PIREP beans in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("frMap", Boolean.valueOf(mapType == MapType.GOOGLEStatic), REQUEST);
			ctx.setAttribute("googleMap", Boolean.valueOf(mapType == MapType.GOOGLE), REQUEST);
			ctx.setAttribute("googleStaticMap", Boolean.valueOf(mapType == MapType.GOOGLEStatic), REQUEST);
			ctx.setAttribute("mapCenter", fr.getAirportD().getPosition().midPoint(fr.getAirportA().getPosition()), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepRead.jsp");
		result.setSuccess(true);
	}
}