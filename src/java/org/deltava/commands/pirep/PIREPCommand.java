// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.util.*;

import org.deltava.security.command.*;

import org.deltava.util.system.SystemData;

/**
 * A web site command to handle editing/saving PIREPs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PIREPCommand extends AbstractFormCommand {

	private static final DateFormat _df = new SimpleDateFormat("yyyy, M, d");

	private static final Collection<String> _flightTimes = new LinkedHashSet<String>();
	private static final Collection<String> _flightYears = new LinkedHashSet<String>();
	private static final Collection _fsVersions = ComboUtils.fromArray(FlightReport.FSVERSION).subList(1,
			FlightReport.FSVERSION.length);

	// Month combolist values
	private static final List<ComboAlias> months = ComboUtils.fromArray(new String[] { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October", "November", "December" }, new String[] {
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" });

	// Check ride approval values
	private static final List<ComboAlias> crApprove = ComboUtils.fromArray(new String[] { "PASS", "UNSATISFACTORY" },
			new String[] { "true", "false" });

	/**
	 * Initialize the command.
	 * @param id the Command ID
	 * @param cmdName the name of the Command
	 */
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);
		if (!_flightTimes.isEmpty())
			return;

		// Initialize flight times
		for (int x = 2; x < 185; x++)
			_flightTimes.add(String.valueOf(x / 10.0d));

		// Initialize flight years
		Calendar c = Calendar.getInstance();
		_flightYears.add(String.valueOf(c.get(Calendar.YEAR)));

		// If we're in January/February, add the previous year
		if (c.get(Calendar.MONTH) < 2)
			_flightYears.add(String.valueOf(c.get(Calendar.YEAR) - 1));
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

			// Create the access controller
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			boolean hasAccess = doCreate ? ac.getCanCreate() : ac.getCanEdit();
			doSubmit &= ac.getCanSubmitIfEdit(); // If we cannot submit just turn that off

			// Validate our access
			if (!hasAccess)
				throw securityException("Not Authorized");

			// Get the airline/airports - don't allow updates if an assignment
			Airline a = isAssignment ? fr.getAirline() : SystemData.getAirline(ctx.getParameter("airline"));
			Airport aa = isAssignment ? fr.getAirportA() : SystemData.getAirport(ctx.getParameter("airportA"));
			Airport ad = isAssignment ? fr.getAirportD() : SystemData.getAirport(ctx.getParameter("airportD"));
			if (a == null)
				a = SystemData.getAirline(SystemData.get("airline.code"));
			
			// Validate airports
			if ((aa == null) || (ad == null))
				throw notFoundException("Invalid Airport(s) - " + ctx.getParameter("airportD") + " / " 
						+ ctx.getParameter("airportA"));

			// If we are creating a new PIREP, check if draft PIREP exists with a similar route pair
			List draftFlights = rdao.getDraftReports(ctx.getUser().getID(), ad, aa, SystemData.get("airline.db"));
			if (doCreate && (!draftFlights.isEmpty()))
				fr = (FlightReport) draftFlights.get(0);

			// Create a new PIREP bean if we're creating one, otherwise update the flight code
			if (fr == null) {
				try {
					fr = new FlightReport(a, Integer.parseInt(ctx.getParameter("flightNumber")), Integer.parseInt(ctx
							.getParameter("flightLeg")));
				} catch (NumberFormatException nfe) {
					CommandException ce = new CommandException("Invalid Flight/Leg Number");
					ce.setLogStackDump(false);
					throw ce;
				}
			} else {
				fr.setAirline(a);
				try {
					fr.setFlightNumber(Integer.parseInt(ctx.getParameter("flightNumber")));
					fr.setLeg(Integer.parseInt(ctx.getParameter("flightLeg")));
				} catch (NumberFormatException nfe) {
					CommandException ce = new CommandException("Invalid Flight/Leg Number");
					ce.setLogStackDump(false);
					throw ce;
				}
			}

			// Update the original PIREP with fields from the request
			fr.setDatabaseID(FlightReport.DBID_PILOT, ctx.getUser().getID());
			fr.setRank(ctx.getUser().getRank());
			fr.setAirportD(ad);
			fr.setAirportA(aa);
			fr.setEquipmentType(ctx.getParameter("eq"));
			fr.setRemarks(ctx.getParameter("remarks"));
			fr.setFSVersion(ctx.getParameter("fsVersion"));

			// Check for historic aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft aInfo = acdao.get(fr.getEquipmentType());
			fr.setAttribute(FlightReport.ATTR_HISTORIC, (aInfo != null) && (aInfo.getHistoric()));
			fr.setAttribute(FlightReport.ATTR_RANGEWARN, (fr.getDistance() > aInfo.getRange()));

			// Figure out what network the flight was flown on
			String net = ctx.getParameter("network");
			if (OnlineNetwork.VATSIM.equals(net))
				fr.setAttribute(FlightReport.ATTR_VATSIM, true);
			else if (OnlineNetwork.IVAO.equals(net))
				fr.setAttribute(FlightReport.ATTR_IVAO, true);
			else if (OnlineNetwork.FPI.equals(net))
				fr.setAttribute(FlightReport.ATTR_FPI, true);
			else if (OnlineNetwork.INTVAS.equals(net))
				fr.setAttribute(FlightReport.ATTR_INTVAS, true);

			// Get the flight time
			try {
				double fTime = Double.parseDouble(ctx.getParameter("flightTime"));
				fr.setLength((int) (fTime * 10));
			} catch (NumberFormatException nfe) {
				CommandException ce = new CommandException("Invalid Flight Time");
				ce.setLogStackDump(false);
				throw ce;
			}

			// Calculate the date
			try {
				Calendar pd = new GregorianCalendar(Integer.parseInt(ctx.getParameter("dateY")), Integer.parseInt(ctx
						.getParameter("dateM")), Integer.parseInt(ctx.getParameter("dateD")));
				fr.setDate(pd.getTime());
			} catch (NumberFormatException nfe) {
				CommandException ce = new CommandException("Invalid Flight Date");
				ce.setLogStackDump(false);
				throw ce;
			}

			// Validate the date
			if (!ctx.isUserInRole("PIREP")) {
				Calendar forwardLimit = Calendar.getInstance();
				Calendar backwardLimit = Calendar.getInstance();
				forwardLimit.add(Calendar.DATE, SystemData.getInt("users.pirep.maxDays"));
				backwardLimit.add(Calendar.DATE, SystemData.getInt("users.pirep.minDays") * -1);
				if ((fr.getDate().before(backwardLimit.getTime())) || (fr.getDate().after(forwardLimit.getTime()))) {
					CommandException ce = new CommandException("Invalid Flight Report Date - " + fr.getDate() + " ("
							+ backwardLimit.getTime() + " - " + forwardLimit.getTime());
					ce.setLogStackDump(false);
					throw ce;
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
		if (doSubmit) {
			result.setURL("submit", null, fr.getID());
		} else {
			ctx.setAttribute("isSaved", Boolean.TRUE, REQUEST);
			result.setType(CommandResult.REQREDIRECT);
			result.setURL("/jsp/pilot/pirepUpdate.jsp");
		}
	}

	/**
	 * Callback method called when editing the PIREP.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Check if we're creating a new PIREP
		boolean isNew = (ctx.getID() == 0);

		// Get the current date/time in the user's local zone
		Calendar cld = Calendar.getInstance();
		TZInfo tz = ctx.isAuthenticated() ? ctx.getUser().getTZ() : TZInfo.get(SystemData.get("time.timezone"));
		cld.setTime(DateTime.convert(cld.getTime(), tz));

		// Get all airlines
		Map<String, Airline> allAirlines = SystemData.getAirlines();

		PIREPAccessControl ac = null;
		try {
			Connection con = ctx.getConnection();
			Collection<Airline> airlines = new TreeSet<Airline>();
			Pilot usr = (Pilot) ctx.getUser();
			
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
				for (Iterator i = allAirlines.values().iterator(); i.hasNext();) {
					Airline a = (Airline) i.next();
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
				if (!ac.getCanEdit())
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
		synchronized (_df) {
			ctx.setAttribute("forwardDateLimit", _df.format(forwardLimit.getTime()), REQUEST);
			ctx.setAttribute("backwardDateLimit", _df.format(backwardLimit.getTime()), REQUEST);
		}

		// Save pirep date combobox values
		ctx.setAttribute("pirepYear", StringUtils.format(cld.get(Calendar.YEAR), "0000"), REQUEST);
		ctx.setAttribute("pirepMonth", StringUtils.format(cld.get(Calendar.MONTH), "#0"), REQUEST);
		ctx.setAttribute("pirepDay", StringUtils.format(cld.get(Calendar.DATE), "#0"), REQUEST);

		// Save airport/airline lists in the request
		ctx.setAttribute("airline", SystemData.get("airline.code"), REQUEST);

		// Save Flight Simulator versions
		ctx.setAttribute("fsVersions", _fsVersions, REQUEST);

		// Set basic lists for the JSP
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);
		ctx.setAttribute("flightTimes", _flightTimes, REQUEST);
		ctx.setAttribute("months", months, REQUEST);
		ctx.setAttribute("years", _flightYears, REQUEST);

		// Set the access controller
		ctx.setAttribute("access", ac, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
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
			GetPilot dao2 = new GetPilot(con);
			FlightReport fr = dao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());

			// Get the pilot who approved/rejected this PIREP
			int disposalID = fr.getDatabaseID(FlightReport.DBID_DISPOSAL);
			Pilot dPilot = (disposalID != 0) ? dao2.get(disposalID) : null;
			if (dPilot != null) {
				String msg = FlightReport.STATUS[fr.getStatus()] + " - by " + dPilot.getFirstName() + " "
						+ dPilot.getLastName();
				ctx.setAttribute("statusMsg", msg, REQUEST);
			} else {
				ctx.setAttribute("statusMsg", FlightReport.STATUS[fr.getStatus()], REQUEST);
			}

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

			// Create the access controller and stuff it in the request
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			ctx.setAttribute("access", ac, REQUEST);

			// Check if this is an ACARS flight - search for an open checkride, and load the ACARS data
			if (fr instanceof ACARSFlightReport) {
				mapType = Pilot.MAP_GOOGLE;
				ACARSFlightReport afr = (ACARSFlightReport) fr;
				int flightID = afr.getDatabaseID(FlightReport.DBID_ACARS);

				// Get the route data from the DAFIF database
				GetACARSData ardao = new GetACARSData(con);
				FlightInfo info = ardao.getInfo(flightID);
				if (info != null) {
					List<String> routeEntries = StringUtils.split(info.getRoute(), " ");
					GeoPosition lastWaypoint = new GeoPosition(fr.getAirportD());

					// Get navigation aids
					GetNavData navdao = new GetNavData(con);
					NavigationDataMap navaids = navdao.getByID(routeEntries);

					// Filter out navaids and put them in the correct order
					List<NavigationDataBean> routeInfo = new ArrayList<NavigationDataBean>();
					for (Iterator<String> i = routeEntries.iterator(); i.hasNext();) {
						String navCode = i.next();
						NavigationDataBean wPoint = navaids.get(navCode, lastWaypoint);
						if (wPoint != null) {
							if (lastWaypoint.distanceTo(wPoint) < fr.getDistance()) {
								routeInfo.add(wPoint);
								lastWaypoint.setLatitude(wPoint.getLatitude());
								lastWaypoint.setLongitude(wPoint.getLongitude());
							}
						}
					}

					// Get the connectoin data
					ConnectionEntry conInfo = ardao.getConnection(info.getConnectionID());

					// Save ACARS info
					ctx.setAttribute("filedRoute", routeInfo, REQUEST);
					ctx.setAttribute("flightInfo", info, REQUEST);
					ctx.setAttribute("conInfo", conInfo, REQUEST);
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
						crAccess = new ExamAccessControl(ctx, cr);
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

			// If we're set to use Google Maps, calculate the route
			if (mapType == Pilot.MAP_GOOGLE) {
				// If this isnt't an ACARS PRIEP, calculate the GC route
				if (!(fr instanceof ACARSFlightReport))
					ctx
							.setAttribute("mapRoute", GeoUtils.greatCircle(fr.getAirportD(), fr.getAirportA(), 100),
									REQUEST);

				// Determine if we are crossing the International Date Line
				double longD = fr.getAirportD().getLongitude();
				double longA = fr.getAirportA().getLongitude();
				boolean crossIDL = ((longD > 80) && (longA < -40)) || ((longD < -40) && (longA > 80));
				ctx.setAttribute("crossIDL", Boolean.valueOf(crossIDL), REQUEST);

				// Save the route and map center for the Google Map
				ctx.setAttribute("googleMap", Boolean.TRUE, REQUEST);
				ctx.setAttribute("mapCenter", fr.getAirportD().getPosition().midPoint(fr.getAirportA().getPosition()),
						REQUEST);
			}

			// Get the pilot/PIREP beans in the request
			ctx.setAttribute("pilot", dao2.get(fr.getDatabaseID(FlightReport.DBID_PILOT)), REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
		} catch (DAOException de) {
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