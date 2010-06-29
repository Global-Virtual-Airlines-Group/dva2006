// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.comparators.RunwayComparator;

import org.deltava.dao.*;
import org.deltava.dao.http.GetVRouteData;

import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle editing/saving Flight Reports.
 * @author Luke
 * @version 3.1
 * @since 1.0
 */

public class PIREPCommand extends AbstractFormCommand {
	
	private static final Logger log = Logger.getLogger(PIREPCommand.class);

	private final Collection<String> _flightTimes = new LinkedHashSet<String>();
	private static final Collection<ComboAlias> _fsVersions = ComboUtils.fromArray(FlightReport.FSVERSION).subList(1, FlightReport.FSVERSION.length);

	// Month combolist values
	private static final List<ComboAlias> months = ComboUtils.fromArray(new String[] { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October", "November", "December" }, new String[] {
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" });

	// Check ride approval values
	private static final List<ComboAlias> crApprove = ComboUtils.fromArray(new String[] { "PASSED", "UNSATISFACTORY" },
			new String[] { "true", "false" });

	/**
	 * Initialize the command.
	 * @param id the Command ID
	 * @param cmdName the name of the Command
	 */
	@Override
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);
		for (int x = 2; x < 189; x++)
			_flightTimes.add(String.valueOf(x / 10.0f));
	}

	/**
	 * Callback method called when saving the PIREP.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
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
			boolean isAssignment = !doCreate && (fr.getDatabaseID(FlightReport.DBID_ASSIGN) != 0);

			// Create the access controller and validate our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			boolean hasAccess = doCreate ? ac.getCanCreate() : ac.getCanEdit();
			doSubmit &= ac.getCanSubmitIfEdit(); // If we cannot submit just turn that off
			if (!hasAccess)
				throw securityException("Not Authorized");
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = doCreate ? (Pilot) ctx.getUser() : pdao.get(fr.getDatabaseID(FlightReport.DBID_PILOT));

			// Get the airline/airports - don't allow updates if an assignment
			Airline a = isAssignment ? fr.getAirline() : SystemData.getAirline(ctx.getParameter("airline"));
			Airport aa = isAssignment ? fr.getAirportA() : SystemData.getAirport(ctx.getParameter("airportA"));
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
			List<FlightReport> draftFlights = rdao.getDraftReports(ctx.getUser().getID(), ad, aa, SystemData.get("airline.db"));
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
			fr.setDatabaseID(FlightReport.DBID_PILOT, ctx.getUser().getID());
			fr.setRank(ctx.getUser().getRank());
			fr.setAirportD(ad);
			fr.setAirportA(aa);
			fr.setEquipmentType(ctx.getParameter("eq"));
			fr.setRemarks(ctx.getParameter("remarks"));
			fr.setFSVersion(ctx.getParameter("fsVersion"));
			fr.setRoute(ctx.getParameter("route"));

			// Check for historic aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft aInfo = acdao.get(fr.getEquipmentType());
			if (aInfo != null) {
				fr.setAttribute(FlightReport.ATTR_HISTORIC, aInfo.getHistoric());
				fr.setAttribute(FlightReport.ATTR_RANGEWARN, (fr.getDistance() > aInfo.getRange()));
			}

			// Figure out what network the flight was flown on and ensure we have an ID
			String net = ctx.getParameter("network");
			if (!StringUtils.isEmpty(net)) {
				try {
					OnlineNetwork network = OnlineNetwork.valueOf(net.toUpperCase());
					if (!p.hasNetworkID(network))
						net = "";
				} catch (Exception e) {
					net = "";
				}
			}
			
			// Set network attribute
			if (OnlineNetwork.VATSIM.toString().equals(net)) {
				fr.setAttribute(FlightReport.ATTR_VATSIM, true);
				fr.setAttribute(FlightReport.ATTR_IVAO, false);
			} else if (OnlineNetwork.IVAO.toString().equals(net)) {
				fr.setAttribute(FlightReport.ATTR_IVAO, true);
				fr.setAttribute(FlightReport.ATTR_VATSIM, false);
			} else {
				fr.setAttribute(FlightReport.ATTR_VATSIM, false);
				fr.setAttribute(FlightReport.ATTR_IVAO, false);
			}

			// Get the flight time
			try {
				double fTime = Double.parseDouble(ctx.getParameter("flightTime"));
				fr.setLength((int) (fTime * 10));
			} catch (NumberFormatException nfe) {
				throw new CommandException("Invalid Flight Time", false);
			}

			// Calculate the date
			Calendar cld = Calendar.getInstance();
			Calendar pd = new GregorianCalendar(StringUtils.parse(ctx.getParameter("dateY"), cld.get(Calendar.YEAR)),
					StringUtils.parse(ctx.getParameter("dateM"), cld.get(Calendar.MONTH)), StringUtils.parse(ctx.getParameter("dateD"),
					cld.get(Calendar.DAY_OF_MONTH)));
			fr.setDate(pd.getTime());

			// Validate the date
			if (!ctx.isUserInRole("PIREP")) {
				Calendar forwardLimit = Calendar.getInstance();
				Calendar backwardLimit = Calendar.getInstance();
				forwardLimit.add(Calendar.DATE, SystemData.getInt("users.pirep.maxDays"));
				backwardLimit.add(Calendar.DATE, SystemData.getInt("users.pirep.minDays") * -1);
				if ((fr.getDate().before(backwardLimit.getTime())) || (fr.getDate().after(forwardLimit.getTime()))) {
					throw new CommandException("Invalid Flight Report Date - " + fr.getDate() + " ("
							+ backwardLimit.getTime() + " - " + forwardLimit.getTime(), false);
				}
			}

			// Get the DAO and write the updateed PIREP to the database
			SetFlightReport wdao = new SetFlightReport(con);
			wdao.write(fr);

			// Update the status for the JSP
			ctx.setAttribute("pirep", fr, REQUEST);
			ctx.setAttribute("isCreated", Boolean.valueOf(doCreate), REQUEST);
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
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get command results
		CommandResult result = ctx.getResult();
		
		// Don't allow anonymous access
		if (!ctx.isAuthenticated())
			throw securityException("Cannot create/edit Flight Report");

		// Check if we're creating a new PIREP
		Pilot usr = (Pilot) ctx.getUser();
		boolean isNew = (ctx.getID() == 0);
		boolean forcePage = (ctx.getSession() != null) && Boolean.valueOf(String.valueOf(ctx.getSession().getAttribute("forcePIREP"))).booleanValue();

		// Get the current date/time in the user's local zone
		Calendar cld = Calendar.getInstance();
		TZInfo tz = ctx.isAuthenticated() ? ctx.getUser().getTZ() : TZInfo.get(SystemData.get("time.timezone"));
		cld.setTime(DateTime.convert(cld.getTime(), tz));

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
				if (!ac.getCanCreate() || (usr.getACARSRestriction() == Pilot.ACARS_ONLY))
					throw securityException("Cannot create new PIREP");

				// Save the user object
				ctx.setAttribute("pilot", ctx.getUser(), REQUEST);

				// Get the active airlines
				for (Iterator<Airline> i = allAirlines.values().iterator(); i.hasNext();) {
					Airline a = i.next();
					if (a.getActive())
						airlines.add(a);
				}
			} else {
				FlightReport fr = dao.get(ctx.getID());
				if (fr == null)
					throw notFoundException("Invalid Flight Report - " + ctx.getID());

				// Check our access
				ac = new PIREPAccessControl(ctx, fr);
				ac.validate();
				if (!ac.getCanEdit() || (usr.getACARSRestriction() == Pilot.ACARS_ONLY))
					throw securityException("Not Authorized");

				// Save the pilot info/PIREP in the request
				GetPilot dao2 = new GetPilot(con);
				ctx.setAttribute("pilot", dao2.get(fr.getDatabaseID(FlightReport.DBID_PILOT)), REQUEST);
				ctx.setAttribute("pirep", fr, REQUEST);

				// Set PIREP date and length
				cld.setTime(DateTime.convert(fr.getDate(), ctx.getUser().getTZ()));
				ctx.setAttribute("flightTime", StringUtils.format(fr.getLength() / 10.0, "#0.0"), REQUEST);

				// Get the active airlines
				if (fr.getDatabaseID(FlightReport.DBID_ASSIGN) == 0) {
					for (Iterator<Airline> i = allAirlines.values().iterator(); i.hasNext();) {
						Airline a = i.next();
						if (a.getActive() || (fr.getAirline().equals(a)))
							airlines.add(a);
					}
				} else
					airlines.add(fr.getAirline());
			}
			
			ctx.setAttribute("airlines", airlines, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save PIREP date limitations
		Calendar forwardLimit = CalendarUtils.getInstance(null, true, SystemData.getInt("users.pirep.maxDays"));
		Calendar backwardLimit = CalendarUtils.getInstance(null, true, SystemData.getInt("users.pirep.minDays") * -1);
		ctx.setAttribute("forwardDateLimit", forwardLimit.getTime(), REQUEST);
		ctx.setAttribute("backwardDateLimit", backwardLimit.getTime(), REQUEST);
		
		// Set flight years
		Collection<String> years = new LinkedHashSet<String>();
		years.add(String.valueOf(cld.get(Calendar.YEAR)));

		// If we're in January/February, add the previous year
		if (cld.get(Calendar.MONTH) < 2)
			years.add(String.valueOf(cld.get(Calendar.YEAR) - 1));

		// Save pirep date combobox values
		ctx.setAttribute("pirepYear", StringUtils.format(cld.get(Calendar.YEAR), "0000"), REQUEST);
		ctx.setAttribute("pirepMonth", StringUtils.format(cld.get(Calendar.MONTH), "#0"), REQUEST);
		ctx.setAttribute("pirepDay", StringUtils.format(cld.get(Calendar.DATE), "#0"), REQUEST);

		// Save airport/airline lists in the request
		ctx.setAttribute("airline", SystemData.get("airline.code"), REQUEST);

		// Save Flight Simulator versions
		ctx.setAttribute("fsVersions", _fsVersions, REQUEST);

		// Set basic lists for the JSP
		ctx.setAttribute("networks", ctx.getUser().getNetworks(), REQUEST);
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);
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
	protected void execRead(CommandContext ctx) throws CommandException {

		// Calculate what map type to use
		int mapType = ctx.isAuthenticated() ? ((Pilot) ctx.getUser()).getMapType() : Pilot.MAP_GOOGLE;
		try {
			Connection con = ctx.getConnection();

			// Get the DAOs and load the flight report
			GetFlightReports dao = new GetFlightReports(con);
			GetPilot pdao = new GetPilot(con);
			FlightReport fr = dao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Get the pilot
			Pilot p = pdao.get(fr.getDatabaseID(FlightReport.DBID_PILOT));
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + fr.getDatabaseID(FlightReport.DBID_PILOT));

			// Get the pilot who approved/rejected this PIREP
			int disposalID = fr.getDatabaseID(FlightReport.DBID_DISPOSAL);
			Pilot dPilot = (disposalID != 0) ? pdao.get(disposalID) : null;
			if (dPilot != null) {
				String msg = FlightReport.STATUS[fr.getStatus()] + " - by " + dPilot.getName();
				ctx.setAttribute("statusMsg", msg, REQUEST);
			} else
				ctx.setAttribute("statusMsg", FlightReport.STATUS[fr.getStatus()], REQUEST);

			// If this PIREP was flown as part of an event, get its information
			int eventID = fr.getDatabaseID(FlightReport.DBID_EVENT);
			if (eventID != 0) {
				GetEvent evdao = new GetEvent(con);
				ctx.setAttribute("event", evdao.get(eventID), REQUEST);
			}
			
			// If this PIREP is part of a flight assignment and a draft, load the assignment
			if ((fr.getStatus() == FlightReport.DRAFT) && (fr.getDatabaseID(FlightReport.DBID_ASSIGN) != 0)) {
				GetAssignment fadao = new GetAssignment(con);
				AssignmentInfo assign = fadao.get(fr.getDatabaseID(FlightReport.DBID_ASSIGN));
				
				// Check our access
				AssignmentAccessControl aac = new AssignmentAccessControl(ctx, assign);
				aac.validate();
				
				// Save access and assignment
				ctx.setAttribute("assignmentInfo", assign, REQUEST);
				ctx.setAttribute("assignAccess", aac, REQUEST);
			}
			
			// Calculate the average time between the airports
			if (ctx.isUserInRole("PIREP")) {
				GetSchedule scdao = new GetSchedule(con);
				ctx.setAttribute("avgTime", Integer.valueOf(scdao.getFlightTime(fr.getAirportD(), fr.getAirportA())), REQUEST);
			}

			// Create the access controller and stuff it in the request
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			ctx.setAttribute("access", ac, REQUEST);
			
			// Get the Navdata DAO
			GetNavRoute navdao = new GetNavRoute(con);
			navdao.setEffectiveDate(fr.getDate());

			// Check if this is an ACARS flight - search for an open checkride, and load the ACARS data
			boolean isACARS = (fr instanceof ACARSFlightReport);
			if (isACARS) {
				if (mapType == Pilot.MAP_FALLINGRAIN)
					mapType = Pilot.MAP_GOOGLE;
				
				ctx.setAttribute("isACARS", Boolean.TRUE, REQUEST);
				ACARSFlightReport afr = (ACARSFlightReport) fr;
				int flightID = afr.getDatabaseID(FlightReport.DBID_ACARS);

				// Get the route data from the DAFIF database
				GetACARSData ardao = new GetACARSData(con);
				FlightInfo info = ardao.getInfo(flightID);
				if (info != null) {
					ctx.setAttribute("flightInfo", info, REQUEST);
					
					// Get the connection info and the IP address
					ConnectionEntry conInfo = ardao.getConnection(info.getConnectionID());
					ctx.setAttribute("conInfo", conInfo, REQUEST);
					if (ctx.isUserInRole("HR") && (conInfo != null)) {
						GetIPLocation ipdao = new GetIPLocation(con);
						ctx.setAttribute("ipInfo", ipdao.get(conInfo.getRemoteAddr()), REQUEST);
					}
					
					// Load the dispatcher if there is one
					if (info.getDispatcherID() != 0) {
						GetUserData uddao = new GetUserData(con);
						UserData ud = uddao.get(info.getDispatcherID());
						if (ud != null)
							ctx.setAttribute("dispatcher", pdao.get(ud), REQUEST);
					}
					
					// Get the aircraft profile
					GetAircraft acdao = new GetAircraft(con);
					Aircraft acInfo = acdao.get(fr.getEquipmentType());
					if ((acInfo != null) && (acInfo.getMaxWeight() > 0))
						ctx.setAttribute("acInfo", acInfo, REQUEST);
					
					// Split the route
					List<String> wps = StringUtils.split(info.getRoute(), " ");
					wps.remove(info.getAirportD().getICAO());
					wps.remove(info.getAirportA().getICAO());
					
					// Check the SID
					SetACARSData awdao = new SetACARSData(con);
					if ((info.getSID() == null) && (wps.size() > 2)) {
						TerminalRoute sid = navdao.getBestRoute(info.getAirportD(), TerminalRoute.SID, wps.get(0), wps.get(1), info.getRunwayD());
						if (sid != null) {
							wps.remove(0);
							info.setSID(sid);
							awdao.writeSIDSTAR(info.getID(), sid);
						}
					}
					
					// Check the STAR
					if ((info.getSTAR() == null) && (wps.size() > 2)) {
						TerminalRoute star = navdao.getBestRoute(info.getAirportA(), TerminalRoute.STAR, wps.get(wps.size() - 1), wps.get(wps.size() - 2), info.getRunwayA());
						if (star != null) {
							wps.remove(wps.size() - 1);
							info.setSTAR(star);
							awdao.writeSIDSTAR(info.getID(), star);
						}
					}
					
					// Build the route
					Collection<MapEntry> route = new LinkedHashSet<MapEntry>();
					route.add(info.getAirportD());
					if (info.getRunwayD() != null)
						route.add(info.getRunwayD());
					
					if (info.getSID() != null) {
						if (!CollectionUtils.isEmpty(wps))
							route.addAll(info.getSID().getWaypoints(wps.get(0)));
						else
							route.addAll(info.getSID().getWaypoints()); 
					}
						
					route.addAll(navdao.getRouteWaypoints(StringUtils.listConcat(wps," "), info.getAirportD()));
					if (info.getSTAR() != null) {
						if (!CollectionUtils.isEmpty(wps))
							route.addAll(info.getSTAR().getWaypoints(wps.get(wps.size() - 1)));
						else
							route.addAll(info.getSTAR().getWaypoints());
					}

					if (info.getRunwayA() != null)
						route.add(info.getRunwayA());
					route.add(info.getAirportA());
					
					// Load departure and arrival runways
					if (ac.getCanDispose()) {
						Collection<Runway> dRwys = navdao.getRunways(info.getAirportD().getICAO());
						if (info.getRunwayD() != null) {
							RunwayComparator rcmp = new RunwayComparator(info.getRunwayD().getHeading());	
							dRwys = CollectionUtils.sort(dRwys, Collections.reverseOrder(rcmp));	
						}
					
						Collection<Runway> aRwys = navdao.getRunways(info.getAirportA().getICAO());
						if (info.getRunwayA() != null) {
							RunwayComparator rcmp = new RunwayComparator(info.getRunwayA().getHeading());	
							aRwys = CollectionUtils.sort(aRwys, Collections.reverseOrder(rcmp));	
						}
					
						// Save runway choices
						ctx.setAttribute("dRunways", dRwys, REQUEST);
						ctx.setAttribute("aRunways", aRwys, REQUEST);
					}
					
					// Save ACARS route, stripping out excessive bits
					ctx.setAttribute("filedRoute", GeoUtils.stripDetours(route, 50), REQUEST);
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
						if (crAccess.getCanScore())
							ctx.setAttribute("crPassFail", crApprove, REQUEST);

						// Allow Examiner to score the PIREP even if they otherwise couldn't
						boolean canScoreCR = crAccess.getCanScore() && (cr.getStatus() == Test.SUBMITTED);
						canScoreCR &= (ac.getCanApprove() || cr.getAcademy());
						ctx.setAttribute("scoreCR", Boolean.valueOf(canScoreCR), REQUEST);
					} catch (AccessControlException ace) {
						// nothing
					}

					// Save the checkride
					ctx.setAttribute("checkRide", cr, REQUEST);
				}
			}
			
			// Load the online track
			if (fr.hasAttribute(FlightReport.ATTR_ONLINE_MASK) && (fr.getStatus() != FlightReport.DRAFT)) {
				GetOnlineTrack tdao = new GetOnlineTrack(con);
				Collection<PositionData> pd = tdao.get(fr.getID());
				long age = (fr.getSubmittedOn() == null) ? Long.MAX_VALUE : (System.currentTimeMillis() - fr.getSubmittedOn().getTime()) / 1000;
				if (pd.isEmpty() && (age < 86000)) {
					int trackID = tdao.getTrackID(fr.getDatabaseID(FlightReport.DBID_PILOT), fr.getNetwork(), fr.getSubmittedOn(), fr.getAirportD(), fr.getAirportA());
					if ((trackID == 0) && fr.hasAttribute(FlightReport.ATTR_VATSIM)) {
						GetVRouteData vddao = new GetVRouteData();
						try {
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
						boolean hasDataLoaded = !tdao.get(fr.getID()).isEmpty();
					
						// Save the positions if we get them
						if (!pd.isEmpty() && (age > 300) & !hasDataLoaded) {
							ctx.startTX();
							
							// Move track data from the raw table
							SetOnlineTrack twdao = new SetOnlineTrack(con);
							twdao.write(fr.getID(), pd);
							twdao.purge(trackID);
							
							// Save the route
							SetFlightReport frwdao = new SetFlightReport(con);
							frwdao.write(fr);
							
							// Commit
							ctx.commitTX();
						}
					}
				}
				
				// Write the positions
				if (mapType == Pilot.MAP_GOOGLE)
					ctx.setAttribute("onlineTrack", pd, REQUEST);
			}
			
			// If the PIREP has a route in it, load it here
			if (!StringUtils.isEmpty(fr.getRoute()) && !isACARS) {
				List<String> wps = StringUtils.split(fr.getRoute(), " ");
				wps.remove(fr.getAirportD().getICAO());
				wps.remove(fr.getAirportA().getICAO());
				
				// Build the route
				Collection<MapEntry> route = new LinkedHashSet<MapEntry>();
				route.add(fr.getAirportD());

				// Load the SID
				if (wps.size() > 2) {
					String name = wps.get(0);
					if (!Character.isDigit(name.charAt(name.length() - 1)))
						name += "%";
					else
						name = name.substring(0, name.length() - 1) + "%";
					
					TerminalRoute sid = navdao.getBestRoute(fr.getAirportD(), TerminalRoute.SID, name, wps.get(1), (String) null);
					if (sid != null) {
						wps.remove(0);
						if (!CollectionUtils.isEmpty(wps))
							route.addAll(sid.getWaypoints(wps.get(0)));
						else
							route.addAll(sid.getWaypoints()); 
					}
				}
				
				route.addAll(navdao.getRouteWaypoints(StringUtils.listConcat(wps," "), fr.getAirportD()));
				
				// Load the STAR
				if (wps.size() > 2) {
					String name = wps.get(wps.size() - 1);
					if (!Character.isDigit(name.charAt(name.length() - 1)))
						name += "%";
					else
						name = name.substring(0, name.length() - 1) + "%";
					
					TerminalRoute star = navdao.getBestRoute(fr.getAirportA(), TerminalRoute.STAR, name, wps.get(wps.size() - 2), (String) null);
					if (star != null) {
						wps.remove(wps.size() - 1);
						if (!CollectionUtils.isEmpty(wps))
							route.addAll(star.getWaypoints(wps.get(wps.size() - 1)));
						else
							route.addAll(star.getWaypoints());
					}
				}
				
				route.add(fr.getAirportA());
				ctx.setAttribute("filedRoute", GeoUtils.stripDetours(route, 65), REQUEST);				
			} else if (!isACARS && (mapType != Pilot.MAP_FALLINGRAIN))
				ctx.setAttribute("mapRoute", Arrays.asList(fr.getAirportD(), fr.getAirportA()), REQUEST);

			// If we're set to use Google Maps, calculate the route
			if (mapType == Pilot.MAP_GOOGLE) {
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