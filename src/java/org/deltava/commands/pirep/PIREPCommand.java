// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.servinfo.OnlineTime;
import org.deltava.beans.servinfo.PositionData;
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
 * @version 8.0
 * @since 1.0
 */

public class PIREPCommand extends AbstractFormCommand {
	
	private static final Logger log = Logger.getLogger(PIREPCommand.class);

	private final Collection<String> _flightTimes = new LinkedHashSet<String>();

	// Month combolist values
	private static final List<ComboAlias> months = ComboUtils.fromArray(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }, 
			new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" });

	// Check ride approval values
	private static final List<ComboAlias> crApprove = ComboUtils.fromArray(new String[] { "PASSED", "UNSATISFACTORY" }, new String[] { "true", "false" });
	private static final List<ComboAlias> frApprove = ComboUtils.fromArray(new String[] {"APPROVE", "REJECT"}, new String[] {"true", "false"});

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
			GetFlightReports rdao = new GetFlightReports(con);
			fr = rdao.get(ctx.getID());

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
			List<FlightReport> draftFlights = rdao.getDraftReports(ctx.getUser().getID(), rt, SystemData.get("airline.db"));
			if (doCreate && (!draftFlights.isEmpty()))
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
			fr.setSimulator(Simulator.fromName(ctx.getParameter("fsVersion")));
			fr.setRoute(ctx.getParameter("route"));

			// Check for historic aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft aInfo = acdao.get(fr.getEquipmentType());
			if (aInfo != null) {
				fr.setAttribute(FlightReport.ATTR_HISTORIC, aInfo.getHistoric());
				fr.setAttribute(FlightReport.ATTR_RANGEWARN, (fr.getDistance() > aInfo.getRange()));
				
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
					int newPax = (int) Math.round(aInfo.getSeats() * fr.getLoadFactor());
					if (newPax != fr.getPassengers()) {
						log.warn("Updated passengers for PIREP #" + fr.getID() + " from " + fr.getPassengers() + " to " + newPax);
						fr.setPassengers(newPax);
					}
				}
			}

			// Figure out what network the flight was flown on and ensure we have an ID
			OnlineNetwork net = null;
			try {
				net = OnlineNetwork.valueOf(ctx.getParameter("network").toUpperCase());
				if (!p.hasNetworkID(net))
					throw new IllegalStateException("No " + net + " ID");
			} catch (Exception e) {
				net = null;
			} finally {
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
			LocalDateTime pd = LocalDateTime.of(StringUtils.parse(ctx.getParameter("dateY"), now.getYear()), StringUtils.parse(ctx.getParameter("dateM"), now.getMonthValue()),
					StringUtils.parse(ctx.getParameter("dateD"), now.getDayOfMonth()), 12, 0, 0);
			fr.setDate(ZonedDateTime.of(pd, ctx.getUser().getTZ().getZone()).toInstant());

			// Validate the date
			if (!ctx.isUserInRole("PIREP")) {
				Instant forwardLimit = ZonedDateTime.now().plusDays(SystemData.getInt("users.pirep.maxDays")).toInstant();
				Instant backwardLimit = ZonedDateTime.now().minusDays(SystemData.getInt("users.pirep.maxDays")).toInstant();
				if ((fr.getDate().isBefore(backwardLimit)) || (fr.getDate().isAfter(forwardLimit)))
					throw new CommandException("Invalid Flight Report Date - " + fr.getDate() + " (" + backwardLimit + " - " + forwardLimit, false);
			}

			// Get the DAO and write the updateed PIREP to the database
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.write(fr);

			// Update the status for the JSP
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("isCreated", Boolean.valueOf(doCreate), REQUEST);
			ctx.setAttribute("isOurs", Boolean.valueOf(fr.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID()), REQUEST);
		} catch (DAOException de) {
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
			
			//	Get aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(), REQUEST);

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
				FlightReport fr = dao.get(ctx.getID());
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
		ctx.setAttribute("forwardDateLimit", today.plusDays(SystemData.getInt("users.pirep.maxDays")), REQUEST);
		ctx.setAttribute("backwardDateLimit", today.minusDays(SystemData.getInt("users.pirep.maxDays")), REQUEST);
		
		// Set flight years
		Collection<String> years = new LinkedHashSet<String>();
		years.add(String.valueOf(today.get(ChronoField.YEAR)));

		// If we're in January/February, add the previous year
		if (today.get(ChronoField.MONTH_OF_YEAR) < 3)
			years.add(String.valueOf(today.get(ChronoField.YEAR) - 1));

		// Save pirep date combobox values
		ctx.setAttribute("pirepYear", StringUtils.format(today.get(ChronoField.YEAR), "0000"), REQUEST);
		ctx.setAttribute("pirepMonth", StringUtils.format(today.get(ChronoField.MONTH_OF_YEAR), "#0"), REQUEST);
		ctx.setAttribute("pirepDay", StringUtils.format(today.get(ChronoField.DAY_OF_MONTH), "#0"), REQUEST);

		// Save airport/airline lists in the request
		ctx.setAttribute("airline", SystemData.get("airline.code"), REQUEST);

		// Set basic lists for the JSP
		ctx.setAttribute("flightTimes", _flightTimes, REQUEST);
		ctx.setAttribute("months", months, REQUEST);
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
			FlightReport fr = dao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Get the pilot
			Pilot p = pdao.get(fr.getDatabaseID(DatabaseID.PILOT));
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + fr.getDatabaseID(DatabaseID.PILOT));

			// Get the pilot who approved/rejected this PIREP
			int disposalID = fr.getDatabaseID(DatabaseID.DISPOSAL);
			Pilot dPilot = (disposalID != 0) ? pdao.get(disposalID) : null;
			if (dPilot != null) {
				String msg = FlightReport.STATUS[fr.getStatus()] + " - by " + dPilot.getName();
				ctx.setAttribute("statusMsg", msg, REQUEST);
			} else
				ctx.setAttribute("statusMsg", FlightReport.STATUS[fr.getStatus()], REQUEST);

			// If this PIREP was flown as part of an event, get its information
			GetEvent evdao = new GetEvent(con);
			int eventID = fr.getDatabaseID(DatabaseID.EVENT);
			if (eventID != 0)
				ctx.setAttribute("event", evdao.get(eventID), REQUEST);
			
			// If this PIREP is part of a flight assignment and a draft, load the assignment
			if ((fr.getStatus() == FlightReport.DRAFT) && (fr.getDatabaseID(DatabaseID.ASSIGN) != 0)) {
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
				org.deltava.dao.GetSchedule scdao = new org.deltava.dao.GetSchedule(con);
				FlightTime ft = scdao.getFlightTime(fr);
				ctx.setAttribute("avgTime", Integer.valueOf(ft.getFlightTime()), REQUEST);
				ctx.setAttribute("networks", p.getNetworks(), REQUEST);
			}
			
			// If we're online and not on an event, list possible event
			if (ac.getCanDispose() && fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK) && (fr.getDatabaseID(DatabaseID.EVENT) == 0)) {
				int evID = evdao.getPossibleEvent(fr);
				if (evID != 0)
					ctx.setAttribute("possibleEvents", Collections.singleton(evdao.get(evID)), REQUEST);
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
				GetACARSData ardao = new GetACARSData(con);
				FlightInfo info = ardao.getInfo(flightID);
				if (info != null) {
					ctx.setAttribute("flightInfo", info, REQUEST);
					
					// Get the aircraft profile
					GetAircraft acdao = new GetAircraft(con);
					Aircraft acInfo = acdao.get(fr.getEquipmentType());
					if ((acInfo != null) && (acInfo.getMaxWeight() > 0))
						ctx.setAttribute("acInfo", acInfo, REQUEST);
					
					// Get the flight score
					ScorePackage pkg = new ScorePackage(acInfo, afr, info.getRunwayD(), info.getRunwayA());
					if (afr.hasAttribute(FlightReport.ATTR_CHECKRIDE) && (afr.getFDR() != Recorder.XACARS)) {
						GetACARSPositions posdao = new GetACARSPositions(con);
						posdao.getRouteEntries(info.getID(), true, info.getArchived());
					}
					
					FlightScore score = FlightScorer.score(pkg);
					if (score != FlightScore.INCOMPLETE)
						ctx.setAttribute("flightScore", score, REQUEST);
					
					// Get the IP address
					if (ctx.isUserInRole("HR")) {
						GetIPLocation ipdao = new GetIPLocation(con);
						ctx.setAttribute("ipInfo", ipdao.get(info.getRemoteAddr()), REQUEST);
					}
					
					// Get system info
					if (ctx.isUserInRole("Developer") || ctx.isUserInRole("Operations")) {
						GetSystemInfo sysdao = new GetSystemInfo(con);
						ctx.setAttribute("acarsClientInfo", sysdao.get(afr.getAuthorID(), afr.getSimulator(), afr.getSubmittedOn()), REQUEST);
					}
					
					// Load the dispatcher if there is one
					if (info.getDispatcherID() != 0) {
						GetUserData uddao = new GetUserData(con);
						UserData ud = uddao.get(info.getDispatcherID());
						if (ud != null)
							ctx.setAttribute("dispatcher", pdao.get(ud), REQUEST);
					}
					
					// Load the gates
					GetGates gdao = new GetGates(con);
					Collection<Gate> gates = gdao.get(info.getID());
					for (Gate g : gates) {
						if (g.getCode().equals(info.getAirportD().getICAO()))
							info.setGateD(g);
						else
							info.setGateA(g);
					}
					
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
						try (InputStream in = new FileInputStream(ArchiveHelper.getRoute(fr.getID()))) {
							GetSerializedRoute rtdao = new GetSerializedRoute(in);
							rtePoints.addAll(rtdao.read());
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
					rtePoints.forEach(wp -> rb.add(wp));

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
						Collection<Runway> dRwys = navdao.getRunways(info.getAirportD(), fr.getSimulator());
						if (info.getRunwayD() != null)
							dRwys = CollectionUtils.sort(dRwys, new RunwayComparator(info.getRunwayD().getHeading(), 5));	
					
						Collection<Runway> aRwys = navdao.getRunways(info.getAirportA(), fr.getSimulator());
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

						// Save the access controller
						ctx.setAttribute("crAccess", crAccess, REQUEST);
						if (crAccess.getCanScore()) {
							ctx.setAttribute("crPassFail", crApprove, REQUEST);
							ctx.setAttribute("pirepApprove", frApprove, REQUEST);
						}

						// Allow Examiner to score the PIREP even if they otherwise couldn't
						boolean canScoreCR = crAccess.getCanScore() && (cr.getStatus() == TestStatus.SUBMITTED);
						canScoreCR &= (ac.getCanApprove() || cr.getAcademy());
						ctx.setAttribute("scoreCR", Boolean.valueOf(canScoreCR), REQUEST);
					} catch (AccessControlException ace) {
						ctx.setAttribute("scoreCR", Boolean.FALSE, REQUEST);
					}

					// Save the checkride
					ctx.setAttribute("checkRide", cr, REQUEST);
				}
			}
			
			// Load the online track
			GetOnlineTrack tdao = new GetOnlineTrack(con);
			boolean hasTrack = tdao.hasTrack(fr.getID());
			if (hasTrack || (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK) && (fr.getStatus() != FlightReport.DRAFT))) {
				File f = ArchiveHelper.getOnline(fr.getID());
				Collection<PositionData> pd = new ArrayList<PositionData>();
				if (f.exists()) {
					try (InputStream in = new FileInputStream(f)) {
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
							twdao.write(fr.getID(), pd);
							twdao.purgeRaw(trackID);
							
							// Save the route
							SetFlightReport frwdao = new SetFlightReport(con);
							frwdao.write(fr);
							
							// Commit
							ctx.commitTX();
						}
					}
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
			} else if (!isACARS && (mapType != MapType.FALLINGRAIN))
				ctx.setAttribute("mapRoute", Arrays.asList(fr.getAirportD(), fr.getAirportA()), REQUEST);

			// If we're set to use Google Maps, calculate the route
			if (mapType == MapType.GOOGLE) {
				ctx.setAttribute("googleMap", Boolean.TRUE, REQUEST);
				ctx.setAttribute("mapCenter", fr.getAirportD().getPosition().midPoint(fr.getAirportA().getPosition()), REQUEST);
			}

			// Get the pilot/PIREP beans in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
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