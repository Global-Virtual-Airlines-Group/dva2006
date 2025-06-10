// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.temporal.*;
import java.sql.Connection;

import org.apache.logging.log4j.*;

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
import org.deltava.beans.simbrief.BriefingPackage;
import org.deltava.beans.stats.*;
import org.deltava.beans.system.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle editing/saving Flight Reports.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class PIREPCommand extends AbstractFormCommand {
	
	private static final Logger log = LogManager.getLogger(PIREPCommand.class);

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
		boolean doSubmit = Boolean.parseBoolean(ctx.getParameter("doSubmit"));
		FlightReport fr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the original version from the database
			final int id = ctx.getID();
			GetFlightReports rdao = new GetFlightReports(con);
			fr = rdao.get(id, ctx.getDB());

			// Check if we are creating a new flight report or editing one with an assignment
			boolean doCreate = (fr == null);
			boolean isAssignment = (fr != null) && ((fr.getDatabaseID(DatabaseID.ASSIGN) != 0) || (fr.getDatabaseID(DatabaseID.ACARS) != 0));

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
			List<FlightReport> draftFlights = rdao.getDraftReports(ctx.getUser().getID(), RoutePair.of(ad, aa), ctx.getDB());
			Optional<FlightReport> ofr = draftFlights.stream().filter(dfr -> (dfr.getID() == id)).findAny();
			if (ofr.isPresent())
				fr = ofr.get();
			else if (doCreate && !draftFlights.isEmpty()) {
				fr = draftFlights.getFirst();
				fr.addStatusUpdate(0, HistoryType.SYSTEM, "Merged new manual Flight Report");
			}

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
			fr.setSimulator(Simulator.fromName(ctx.getParameter("fsVersion"), fr.getSimulator()));
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
				if (fr instanceof ACARSFlightReport afr) {
					if ((aInfo.getMaxTakeoffWeight() != 0) && (afr.getTakeoffWeight() > aInfo.getMaxTakeoffWeight()))
						afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
					else if ((aInfo.getMaxLandingWeight() != 0) && (afr.getLandingWeight() > aInfo.getMaxLandingWeight()))
						afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
					else if (afr.hasAttribute(FlightReport.ATTR_WEIGHTWARN))
						afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, false);
				}
				
				// If the passengers are non-zero, update the count
				if ((fr.getPassengers() != 0) && ac.getCanEditCore()) {
					int newPax = (int) Math.round(opts.getSeats() * fr.getLoadFactor());
					if (newPax != fr.getPassengers()) {
						fr.addStatusUpdate(0, HistoryType.SYSTEM, "Updated passengers from " + fr.getPassengers() + " to " + newPax);
						fr.setPassengers(newPax);
					}
				}
			}
			
			// Get the flight time/date
			if (ac.getCanEditCore()) {
				try {
					double fTime = Double.parseDouble(ctx.getParameter("flightTime"));
					fr.setLength((int) (fTime * 10));
				} catch (NumberFormatException | NullPointerException nfe) {
					if (fr.getStatus() != FlightStatus.DRAFT)
						throw new CommandException(String.format("Invalid Flight Time - %s", nfe.getClass().getSimpleName()), false);
				}
				
				// Figure out what network the flight was flown on and ensure we have an ID
				OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), fr.getNetwork());
				if ((net != null) && !p.hasNetworkID(net))
					throw new IllegalStateException(String.format("No %s ID", net));

				if (fr.getNetwork() != net) {
					fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.SYSTEM, "Updated online network from " + fr.getNetwork() + " to " + ((net == null) ? "Offline" : net));
					fr.setNetwork(net);
				}

				// Calculate the date
				ZonedDateTime now = ZonedDateTime.now(ctx.getUser().getTZ().getZone());
				Month mn = EnumUtils.parse(Month.class, ctx.getParameter("dateM"), now.getMonth());
				LocalDateTime pd = LocalDateTime.of(StringUtils.parse(ctx.getParameter("dateY"), now.getYear()), mn.getValue(), StringUtils.parse(ctx.getParameter("dateD"), now.getDayOfMonth()), 12, 0, 0);
				fr.setDate(ZonedDateTime.of(pd, ctx.getUser().getTZ().getZone()).toInstant());

				// Validate the date
				if (!ac.getCanOverrideDateRange()) {
					final String FMT = "MM/dd/yyyy HH:mm";
					Instant fdl = ZonedDateTime.now().plusDays(SystemData.getInt("users.pirep.maxDays", 1) + 1).minusSeconds(60).truncatedTo(ChronoUnit.DAYS).toInstant();
					Instant bdl = ZonedDateTime.now().minusDays(SystemData.getInt("users.pirep.maxDays", 7)).truncatedTo(ChronoUnit.DAYS).toInstant();
					if (fr.getDate().isBefore(bdl) || fr.getDate().isAfter(fdl))
						throw new CommandException(String.format("Invalid Flight Report Date - %s (%s - %s)", StringUtils.format(fr.getDate(), FMT), StringUtils.format(bdl, FMT), StringUtils.format(fdl, FMT)), false);
				}
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
		boolean forcePage = (ctx.getSession() != null) && Boolean.parseBoolean(String.valueOf(ctx.getSession().getAttribute("forcePIREP")));

		// Get the current date/time in the user's local zone
		TZInfo tz = ctx.isAuthenticated() ? ctx.getUser().getTZ() : TZInfo.get(SystemData.get("time.timezone"));
		ZonedDateTime today = ZonedDateTime.now(tz.getZone());
		
		// Get all airlines
		Collection<Airline> airlines = new TreeSet<Airline>();
		PIREPAccessControl ac = null; FlightReport fr = null;
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
				ctx.setAttribute("access", ac, REQUEST);

				// Get the active airlines
				SystemData.getAirlines().stream().filter(Airline::getActive).forEach(airlines::add);
			} else {
				fr = dao.get(ctx.getID(), ctx.getDB());
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
				ctx.setAttribute("access", ac, REQUEST);
				ctx.setAttribute("networks", p.getNetworks(), REQUEST);
				ctx.setAttribute("flightTime", StringUtils.format(fr.getLength() / 10.0, "#0.0"), REQUEST);
				ctx.setAttribute("isACARS", Boolean.valueOf(fr.getDatabaseID(DatabaseID.ACARS) != 0), REQUEST);

				// Get the active airlines
				if (ac.getCanEditCore()) {
					FlightReport fr2 = fr;
					SystemData.getAirlines().stream().filter(a -> (a.getActive() || fr2.getAirline().equals(a))).forEach(airlines::add);
				} else
					airlines.add(fr.getAirline());
				
				// Add ACARS data if available
				if (fr.getDatabaseID(DatabaseID.ACARS) != 0) {
					GetACARSData fddao = new GetACARSData(con);
					ctx.setAttribute("flightInfo", fddao.getInfo(fr.getDatabaseID(DatabaseID.ACARS)), REQUEST);
				}
			}
			
			// Get aircraft types
			GetAircraft acdao = new GetAircraft(con);
			if (ac.getCanEditCore() || (fr == null)) {
				Collection<Aircraft> eqTypes = acdao.getAircraftTypes();
				eqTypes.removeIf(eq -> !hasCourse && eq.getAcademyOnly());
				ctx.setAttribute("eqTypes", eqTypes, REQUEST);
			} else
				ctx.setAttribute("eqTypes", Set.of(acdao.get(fr.getEquipmentType())), REQUEST);
			
			// Get airlines and networks
			ctx.setAttribute("airlines", airlines, REQUEST);
			if (fr == null || ac.getCanEditCore())
				ctx.setAttribute("networks", usr.getNetworks(), REQUEST);
			else if (fr.getNetwork() != null)
				ctx.setAttribute("networks", List.of(fr.getNetwork()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate PIREP date limitations
		int maxRange = SystemData.getInt("users.pirep.maxDays", 1); ZonedDateTime fdl = today.plusDays(maxRange + 1).truncatedTo(ChronoUnit.DAYS).minusSeconds(60);
		int minRange = SystemData.getInt("users.pirep.minDays", 7); ZonedDateTime bdl = today.minusDays(minRange).truncatedTo(ChronoUnit.DAYS);
		ctx.setAttribute("forwardDateLimit", fdl, REQUEST);
		ctx.setAttribute("backwardDateLimit", bdl, REQUEST);
		
		// Set combo choices
		if (ac.getCanEditCore() || (fr == null)) {
			Collection<Integer> years = new TreeSet<Integer>();
			years.add(Integer.valueOf(today.get(ChronoField.YEAR)));
			years.add(Integer.valueOf(fdl.getYear())); years.add(Integer.valueOf(bdl.getYear()));
			ctx.setAttribute("months", ComboUtils.properCase(Month.values()), REQUEST);
			ctx.setAttribute("years", years, REQUEST);
			ctx.setAttribute("flightTimes", _flightTimes, REQUEST);
		}

		// Save request attributes
		ctx.setAttribute("pirepYear", StringUtils.format(today.get(ChronoField.YEAR), "0000"), REQUEST);
		ctx.setAttribute("pirepMonth", StringUtils.format(today.get(ChronoField.MONTH_OF_YEAR), "#0"), REQUEST);
		ctx.setAttribute("pirepDay", StringUtils.format(today.get(ChronoField.DAY_OF_MONTH), "#0"), REQUEST);
		ctx.setAttribute("airline", SystemData.get("airline.code"), REQUEST);

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
		MapType mapType = ctx.isAuthenticated() ? MapType.MAPBOX : MapType.GOOGLEStatic;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs and load the flight report
			GetFlightReportACARS dao = new GetFlightReportACARS(con);
			GetPilot pdao = new GetPilot(con);
			FlightReport fr = dao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Get the pilot
			Pilot p = pdao.get(fr.getDatabaseID(DatabaseID.PILOT));
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + fr.getDatabaseID(DatabaseID.PILOT));
			
			// If the flight report is a draft, then load it
			boolean isACARS = (fr instanceof FDRFlightReport);
			if (fr.getStatus() == FlightStatus.DRAFT)
				fr = dao.getDraft(fr.getID(), ctx.getDB());
			
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
			
			// Load the aircraft profile
			GetAircraft acdao = new GetAircraft(con);
			Aircraft acInfo = acdao.get(fr.getEquipmentType());
			AircraftPolicyOptions acOpts = (acInfo == null) ? null : acInfo.getOptions(SystemData.get("airline.code"));
			ctx.setAttribute("acInfo", acInfo, REQUEST);
			ctx.setAttribute("acPolicy", acOpts, REQUEST);
			
			// Load taxi times
			if (ac.getCanUseSimBrief() || isACARS) {
				GetACARSTaxiTimes ttdao = new GetACARSTaxiTimes(con);
				int year = LocalDate.ofInstant(fr.getDate(), ZoneOffset.UTC).getYear();
				TaxiTime ttA = ttdao.getTaxiTime(fr.getAirportA(), year);
				TaxiTime ttD = ttdao.getTaxiTime(fr.getAirportD(), year);
				if (ttA.isEmpty())
					ttA = ttdao.getTaxiTime(fr.getAirportA());
				if (ttD.isEmpty())
					ttD = ttdao.getTaxiTime(fr.getAirportD());
				
				// Display airport taxi times
				ctx.setAttribute("avgTaxiInTime", ttA, REQUEST);
				ctx.setAttribute("avgTaxiOutTime", ttD, REQUEST);
				
				// Load PIREP taxi time
				if (isACARS)
					ctx.setAttribute("taxiTime", ttdao.getTaxiTime(fr.getDatabaseID(DatabaseID.ACARS)), REQUEST);
			}
			
			// Check for SimBrief package
			BriefingPackage sbPkg = null;
			if (fr.hasAttribute(FlightReport.ATTR_SIMBRIEF)) {
				GetSimBriefPackages sbpdao = new GetSimBriefPackages(con);
				sbPkg = sbpdao.getSimBrief(fr.getID(), ctx.getDB());
				if (sbPkg != null) {
					List<NavigationDataBean> mrks = sbPkg.getETOPSAlternates().stream().map(ETOPSHelper::generateAlternateMarker).collect(Collectors.toList());
					GeoLocation mp = sbPkg.getETOPSMidpoint();
					if ((mp.getLatitude() != 0) && (mp.getLongitude() != 0))
						mrks.add(ETOPSHelper.generateMidpointMarker(mp, sbPkg.getETOPSAlternates()));
					
					ctx.setAttribute("sbPackage", sbPkg, REQUEST);
					ctx.setAttribute("sbMarkers", mrks, REQUEST);
				}
			}
			
			// Load SimBrief-specific data
			if (ac.getCanUseSimBrief()) {
				ctx.setAttribute("versionInfo", VersionInfo.getFullBuild(), REQUEST);
				
				// Calculate Alternates
				AlternateAirportHelper aah = new AlternateAirportHelper(SystemData.get("airline.code"));
				List<Airport> alts = aah.calculateAlternates(acInfo, fr.getAirportA());
				if (alts.size() > 4)
					alts.removeAll(alts.subList(4, alts.size()));
				
				ctx.setAttribute("alternates", alts, REQUEST);
				
				// List possible tail codes, custom airframes and ETOPS options
				if ((sbPkg == null) && (acInfo != null) && (acOpts != null)) {
					int maxETOPS = Math.min(acOpts.getETOPS().ordinal(), ETOPS.ETOPS330.ordinal());
					List<ETOPS> etopsRange = List.of(ETOPS.values()).stream().filter(e -> e.ordinal() <= maxETOPS).collect(Collectors.toList());
					Collections.reverse(etopsRange);
					ctx.setAttribute("etopsOV", etopsRange.stream().map(e -> ComboUtils.fromString(e.name(), String.valueOf(e.getTime()))).collect(Collectors.toList()), REQUEST);
				}
				
				// Determine if deprture time has already passed
				DraftFlightReport dfr = (DraftFlightReport) fr;
				ZonedDateTime now = ZonedDateTime.now(ctx.getUser().getTZ().getZone());
				ZonedDateTime zdt = ZonedDateTime.of(LocalDate.ofInstant(fr.getDate(), now.getZone()), now.toLocalTime(), now.getZone());
				if (dfr.getTimeD() != null) {
					zdt = ZonedDateTime.of(LocalDate.ofInstant(fr.getDate(), now.getZone()), dfr.getTimeD().toLocalTime(), dfr.getTimeD().getZone()); // use airport's zone, not user
					if (zdt.isBefore(now))
						zdt = zdt.plusDays(1);
				}
				
				ctx.setAttribute("departureTime", zdt, REQUEST);
				ctx.setAttribute("departureTimeUTC", ZonedDateTime.ofInstant(zdt.toInstant(), ZoneOffset.UTC), REQUEST);
			}
			
			// Calculate the average time between the airports and user's networks
			if (ac.getCanDispose()) {
				GetRawSchedule rsdao = new GetRawSchedule(con);
				org.deltava.dao.GetSchedule scdao = new org.deltava.dao.GetSchedule(con);
				scdao.setSources(rsdao.getSources(true, ctx.getDB()));
				FlightTime ft = scdao.getFlightTime(fr, ctx.getDB());
				ctx.setAttribute("avgTime", ft.getFlightTime(), REQUEST);
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
			if (ac.getCanAdjustEvents() && fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK) && (fr.getDatabaseID(DatabaseID.EVENT) == 0))
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
			
			// List on-time statistics
			GetACARSOnTime otdao = new GetACARSOnTime(con);
			ctx.setAttribute("onTimeRoute", otdao.getOnTimeStatistics(fr, ctx.getDB()), REQUEST);
			
			// Get tour eligibility
			GetTour trdao = new GetTour(con);
			if (fr.getDatabaseID(DatabaseID.TOUR) != 0) {
				Tour t = trdao.get(fr.getDatabaseID(DatabaseID.TOUR), ctx.getDB());
				if (t != null) {
					ctx.setAttribute("tour", t, REQUEST);
					ctx.setAttribute("tourIdx", Integer.valueOf(t.getLegIndex(fr)), REQUEST);
				}
			} else if (ctx.isUserInRole("Operations") || ctx.isUserInRole("Event")) {
				Instant dt = (fr instanceof ACARSFlightReport afr) ? afr.getTakeoffTime() : fr.getDate(); // Non-ACARS should be 12:00 already
				
				// Make sure we haven't flown this leg already
				List<Tour> possibleTours = trdao.findLeg(fr, dt, ctx.getDB()); final RoutePair rp = fr;
				for (Iterator<Tour> i = possibleTours.iterator(); i.hasNext(); ) {
					Tour t = i.next();
					Collection<FlightReport> tourFlights = dao.getByTour(fr.getAuthorID(), t.getID(), ctx.getDB());
					if (tourFlights.stream().anyMatch(rp::matches))
						i.remove();
				}
				
				ctx.setAttribute("possibleTours", possibleTours, REQUEST);
			}

			// Get the Navdata DAO
			GetNavRoute navdao = new GetNavRoute(con);
			navdao.setEffectiveDate(fr.getDate());

			// Online time variables; for scoping issues
			int otMaxGap = SystemData.getInt("online.track_gap", 20); int onlineTime = 0;
			
			// Check if this is an ACARS flight - search for an open checkride, and load the ACARS data
			if (isACARS && (fr instanceof FDRFlightReport afr)) {
				mapType = MapType.MAPBOX;
				ctx.setAttribute("isACARS", Boolean.TRUE, REQUEST);
				ctx.setAttribute("isSimFDR", Boolean.valueOf(afr.getFDR() == Recorder.SIMFDR), REQUEST);
				ctx.setAttribute("isXACARS", Boolean.valueOf(afr.getFDR() == Recorder.XACARS), REQUEST);
				int flightID = afr.getDatabaseID(DatabaseID.ACARS);
				
				// Check if we can track online time from client
				final long ocMask = Capabilities.IVAP.getMask() | Capabilities.VPILOT.getMask() | Capabilities.XIVAP.getMask();
				boolean hasClientOnlineTime = ((ocMask & afr.getCapabilities()) != 0);

				// Get the route data from the DAFIF database
				GetACARSLog ardao = new GetACARSLog(con);
				FlightInfo info = ardao.getInfo(flightID);
				if (info != null) {
					ctx.setAttribute("flightInfo", info, REQUEST);
					
					// Load position data and scoring package - we use package for online time calculations as well
					AircraftPolicyOptions opts = (acInfo == null) ? null : acInfo.getOptions(SystemData.get("airline.code"));
					ScorePackage pkg = new ScorePackage(acInfo, afr, info.getRunwayD(), info.getRunwayA(), opts);
					if ((ac.getCanViewScore() || hasClientOnlineTime) && (afr.getFDR() != Recorder.XACARS)) {
						GetACARSPositions posdao = new GetACARSPositions(con);
						ArchiveMetadata md = posdao.getArchiveInfo(info.getID());
						if (!info.getArchived() || (md != null)) {
							Collection<GeospaceLocation> positions = posdao.getRouteEntries(info.getID(), true, info.getArchived());
							positions.stream().filter(ACARSRouteEntry.class::isInstance).map(ACARSRouteEntry.class::cast).forEach(pkg::add);
						
							// Get online data if we can
							if (afr.hasAttribute(FlightReport.ATTR_ONLINE_MASK)) {
								Collection<PositionData> entries = pkg.getData().stream().filter(ACARSRouteEntry::getNetworkConnected).map(re -> new PositionData(re.getDate(), new GeoPosition(re))).collect(Collectors.toList());
								onlineTime = OnlineTime.calculate(entries, otMaxGap);
							}
						}

						// Calculate the score
						if (ac.getCanViewScore()) {
							FlightScore score = FlightScorer.score(pkg);
							if (score != FlightScore.INCOMPLETE)
								ctx.setAttribute("flightScore", pkg, REQUEST);	
						}
					}
					
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
					if (ac.getCanViewDiagnosticData()) {
						GetSystemInfo sysdao = new GetSystemInfo(con);
						GetACARSPerformance apdao = new GetACARSPerformance(con);
						ctx.setAttribute("acarsClientInfo", sysdao.get(afr.getAuthorID(), afr.getSimulator(), afr.getSubmittedOn()), REQUEST);
						ctx.setAttribute("acarsTimerInfo", apdao.getTimers(info.getID()), REQUEST);
						ctx.setAttribute("acarsFrames", apdao.getFrames(info.getID()), REQUEST);
						ctx.setAttribute("acarsPerfCtrs", apdao.getCounters(info.getID()), REQUEST);
						if (info.getArchived())
							ctx.setAttribute("archiveMetadata", ardao.getArchiveInfo(info.getID()), REQUEST);
					}
					
					// Load the dispatcher if there is one
					if (info.getDispatcherID() != 0) {
						GetUserData uddao = new GetUserData(con);
						UserData ud = uddao.get(info.getDispatcherID());
						ctx.setAttribute("dispatcher", pdao.get(ud), REQUEST);
					}
					
					// Load the dispatch log entry
					if (info.getDispatchLogID() != 0)
						ctx.setAttribute("dispatchLog", ardao.getDispatchLog(info.getDispatchLogID()), REQUEST);
					
					// Load the gates
					GetGates gdao = new GetGates(con);
					gdao.populate(info);
					
					// Build the route
					RouteBuilder rb = new RouteBuilder(fr, info.getRoute());
					Collection<MapEntry> route = new LinkedHashSet<MapEntry>();
					if (info.getGateD() != null) {
						route.add(info.getGateD());
						ctx.setAttribute("gDView", GeoUtils.bearingPointS(info.getGateD(), 0.95, info.getGateD().getHeading() - 90), REQUEST);
					}
					
					if (info.getRunwayD() != null) {
						route.add(info.getRunwayD());
						ctx.setAttribute("rDView", GeoUtils.bearingPointS(info.getRunwayD(), 1.25, info.getRunwayD().getHeading() - 90), REQUEST);	
					}
					rb.add(info.getSID());
					
					// Load the serialized route
					Collection<NavigationDataBean> rtePoints = new ArrayList<NavigationDataBean>();
					if (ArchiveHelper.getRoute(fr.getID()).exists()) {
						GetNavCycle ncdao = new GetNavCycle(con);
						try (InputStream in = new BufferedInputStream(new FileInputStream(ArchiveHelper.getRoute(fr.getID())))) {
							GetSerializedRoute rtdao = new GetSerializedRoute(in);
							ArchivedRoute arcRt = rtdao.read();
							rtePoints.addAll(arcRt.getWaypoints());
							if (arcRt.getAIRACVersion() > 0)
								ctx.setAttribute("routeCycleInfo", ncdao.getCycle(String.valueOf(arcRt.getAIRACVersion())), REQUEST);
						} catch (IOException ie) {
							log.atError().withThrowable(ie).log("Error loading serialized route - {}", ie.getMessage());
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
					if (info.getRunwayA() != null) {
						Runway rA = info.getRunwayA();
						route.add(rA);
						if (rA.getThresholdLength() > 0) route.add(rA.getThreshold());
						ctx.setAttribute("rAView", GeoUtils.bearingPointS(rA, 1.25, rA.getHeading() - 90), REQUEST);
					}
					
					route.add((info.getGateA() != null) ? info.getGateA() : info.getAirportA());
					if (info.getGateA() != null)
						ctx.setAttribute("gAView", GeoUtils.bearingPointS(info.getGateA(), 0.95, info.getGateA().getHeading() - 90), REQUEST);
					
					// Load departure and arrival runways
					if (ac.getCanDispose()) {
						List<Runway> dRwys = navdao.getRunways(fr.getAirportD(), fr.getSimulator());
						if (info.getRunwayD() != null)
							dRwys.sort(new RunwayComparator(info.getRunwayD().getHeading(), 5, false));	
					
						List<Runway> aRwys = navdao.getRunways(fr.getAirportA(), fr.getSimulator());
						if (info.getRunwayA() != null)
							aRwys.sort(new RunwayComparator(info.getRunwayA().getHeading(), 5, false));	
					
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
					try (InputStream in = new BufferedInputStream(new FileInputStream(f))) {
						GetSerializedOnline stdao = new GetSerializedOnline(in);
						pd.addAll(stdao.read());
					} catch (IOException ie) {
						log.atError().withThrowable(ie).log("Error loading serialized online track data - {}", ie.getMessage());
						f.delete();
					}
				}
			
				if (pd.isEmpty())
					pd.addAll(tdao.get(fr.getID()));
				
				hasTrack |= !pd.isEmpty();
				long age = (fr.getSubmittedOn() == null) ? Long.MAX_VALUE : (System.currentTimeMillis() - fr.getSubmittedOn().toEpochMilli()) / 1000;
				if (pd.isEmpty() && (age < 86000)) {
					int trackID = tdao.getTrackID(fr.getDatabaseID(DatabaseID.PILOT), fr.getNetwork(), fr.getSubmittedOn(), fr.getAirportD(), fr.getAirportA());
					if (trackID != 0) {
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
					Collection<NetworkOutage> networkOutages = NetworkOutage.calculate(fr.getNetwork(), ffr, tdao.getFetches(fr.getNetwork(), ffr), 120);
					ctx.setAttribute("networkOutages", networkOutages, REQUEST);
					ctx.setAttribute("networkOutageTotal", Duration.ofSeconds(networkOutages.stream().mapToLong(o -> o.getDuration().toSeconds()).sum()), REQUEST);
				}
				
				// Calculate the online time
				int onlineTrackTime = OnlineTime.calculate(pd, otMaxGap);
				ctx.setAttribute("onlineTime", Duration.ofSeconds(Math.max(onlineTime, onlineTrackTime)), REQUEST);
				ctx.setAttribute("onlineTrackTime", Duration.ofSeconds(onlineTrackTime), REQUEST);
				
				// Write the positions
				if (mapType == MapType.MAPBOX)
					ctx.setAttribute("onlineTrack", pd, REQUEST);
			}
			
			// If the PIREP has a route in it, load it here
			if (!StringUtils.isEmpty(fr.getRoute()) && !isACARS) {
				RouteBuilder rb = new RouteBuilder(fr, fr.getRoute());

				// Load the SID
				if (rb.getSID() != null) {
					String rwyD = (sbPkg == null) ? null : ("RW" + sbPkg.getRunwayD());
					TerminalRoute sid = navdao.getBestRoute(fr.getAirportD(), TerminalRoute.Type.SID, TerminalRoute.makeGeneric(rb.getSID()), rb.getSIDTransition(), rwyD);
					rb.add(sid);
				}
				
				navdao.getRouteWaypoints(rb.getRoute(), fr.getAirportD()).forEach(rb::add);
				
				// Load the STAR
				if (rb.getSTAR() != null) {
					String rwyA = (sbPkg == null) ? null : ("RW" + sbPkg.getRunwayA());
					TerminalRoute star = navdao.getBestRoute(fr.getAirportA(), TerminalRoute.Type.STAR, TerminalRoute.makeGeneric(rb.getSTAR()), rb.getSTARTransition(), rwyA);
					rb.add(star);
				}

				// Build the route
				Collection<MapEntry> route = new LinkedHashSet<MapEntry>();
				route.add(fr.getAirportD());
				route.addAll(rb.getPoints());
				route.add(fr.getAirportA());
				ctx.setAttribute("filedRoute", GeoUtils.stripDetours(route, 65), REQUEST);
				
				// Calculate ETOPS for route
				ETOPSResult etopsInfo = ETOPSHelper.classify(route);
				ctx.setAttribute("filedETOPS", etopsInfo, REQUEST);
				if ((fr.getStatus() == FlightStatus.DRAFT) && (acOpts != null) && (acOpts.getETOPS() != ETOPS.INVALID) && (etopsInfo.getResult().getTime() > acOpts.getETOPS().getTime())) {
					fr.setAttribute(FlightReport.ATTR_ETOPSWARN, true);
					mapType = MapType.GOOGLEStatic;
				}
			} else if (!isACARS && (mapType != MapType.FALLINGRAIN)) {
				Collection<? extends GeoLocation> rt = fr.getAirports();
				ctx.setAttribute("mapRoute", rt, REQUEST);
				ctx.setAttribute("filedRoute", rt, REQUEST);
				if (!hasTrack && (mapType == MapType.MAPBOX) && (sbPkg == null))
					mapType = MapType.GOOGLEStatic;
			}
			
			// Check for Spiders
			HTTPContextData hctxt = (HTTPContextData) ctx.getRequest().getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME);
			boolean captchaOK = ctx.passedCAPTCHA(); boolean isSpider = (hctxt == null) || (hctxt.getBrowserType() == BrowserType.SPIDER);
			if (isSpider || ((mapType == MapType.MAPBOX) && !captchaOK && !ctx.isAuthenticated()))
				mapType = MapType.NONE;

			// Get the pilot/PIREP beans in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("googleStaticMap", Boolean.valueOf(mapType == MapType.GOOGLEStatic), REQUEST);
			ctx.setAttribute("googleMap", Boolean.valueOf(mapType == MapType.MAPBOX), REQUEST);
			ctx.setAttribute("mapCenter", fr.midPoint(), REQUEST);
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