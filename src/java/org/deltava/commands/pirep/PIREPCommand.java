// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.CheckRide;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.dao.*;
import org.deltava.util.*;

import org.deltava.security.command.ExamAccessControl;
import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A web site command to handle editing/saving PIREPs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PIREPCommand extends AbstractFormCommand {

	private static List _flightTimes;
	private static List _flightYears;

	private static final Comparator _cmp = new AirportComparator(AirportComparator.NAME);

	// Flight Simulator versions
	private static final List fsVersions = ComboUtils.fromArray(FlightReport.FSVERSION).subList(1,
			FlightReport.FSVERSION.length);

	// Month combolist values
	private static final List months = ComboUtils.fromArray(new String[] { "January", "February", "March", "April",
			"May", "June", "July", "August", "September", "October", "November", "December" }, new String[] { "0", "1",
			"2", "3", "4", "5", "6", "7", "8", "9", "10", "11" });

	// Check ride approval values
	private static final List crApprove = ComboUtils.fromArray(new String[] { "PASS", "FAIL" }, new String[] { "true",
			"false" });

	/**
	 * Initialize the command.
	 * @param id the Command ID
	 * @param cmdName the name of the Command
	 */
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);

		// Initialize flight times
		if (_flightTimes == null) {
			_flightTimes = new ArrayList();
			for (int x = 2; x < 168; x++)
				_flightTimes.add(String.valueOf(x / 10.0d));
		}

		// Initialize flight years
		if (_flightYears == null) {
			Calendar c = Calendar.getInstance();
			_flightYears = new ArrayList();
			_flightYears.add(String.valueOf(c.get(Calendar.YEAR)));

			// If we're in January/February, add the previous year
			if (c.get(Calendar.MONTH) < 2)
				_flightYears.add(String.valueOf(c.get(Calendar.YEAR) - 1));
		}
	}

	/**
	 * Callback method called when saving the PIREP.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we are doing a submit & save
		boolean doSubmit = "1".equals(ctx.getParameter("doSubmit"));

		FlightReport fr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the original version from the database
			GetFlightReports rdao = new GetFlightReports(con);
			fr = rdao.get(ctx.getID());

			// Check if we are creating a new flight report
			boolean doCreate = (fr == null);

			// Create the access controller
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			boolean hasAccess = doCreate ? ac.getCanCreate() : ac.getCanEdit();
			doSubmit &= ac.getCanSubmitIfEdit(); // If we cannot submit just turn that off

			// Validate our access
			if (!hasAccess)
				throw securityException("Not Authorized");

			// Get the airports
			Airport aa = SystemData.getAirport(ctx.getParameter("airportA"));
			Airport ad = SystemData.getAirport(ctx.getParameter("airportD"));

			// If we are creating a new PIREP, check if draft PIREP exists with a similar route pair
			List draftFlights = rdao.getDraftReports(ctx.getUser().getID(), ad, aa, SystemData.get("airline.db"));
			if (doCreate && (!draftFlights.isEmpty()))
				fr = (FlightReport) draftFlights.get(0);

			// Create a new PIREP bean if we're creating one, otherwise update the flight code
			Airline a = SystemData.getAirline(ctx.getParameter("airline"));
			if (fr == null) {
				try {
					fr = new FlightReport(a, Integer.parseInt(ctx.getParameter("flightNumber")), Integer.parseInt(ctx
							.getParameter("flightLeg")));
				} catch (NumberFormatException nfe) {
					throw new CommandException("Invalid Flight/Leg Number");
				}
			} else {
				fr.setAirline(a);
				try {
					fr.setFlightNumber(Integer.parseInt(ctx.getParameter("flightNumber")));
					fr.setLeg(Integer.parseInt(ctx.getParameter("flightLeg")));
				} catch (NumberFormatException nfe) {
					throw new CommandException("Invalid Flight/Leg Number");
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

			// Figure out what network the flight was flown on
			String net = ctx.getParameter("network");
			if ("VATSIM".equals(net)) {
				fr.setAttribute(FlightReport.ATTR_VATSIM, true);
			} else if ("IVAO".equals(net)) {
				fr.setAttribute(FlightReport.ATTR_IVAO, true);
			} else if ("FPI".equals(net)) {
				fr.setAttribute(FlightReport.ATTR_FPI, true);
			}

			// Get the flight time
			try {
				double fTime = Double.parseDouble(ctx.getParameter("flightTime"));
				fr.setLength((int) (fTime * 10));
			} catch (NumberFormatException nfe) {
				throw new CommandException("Invalid Flight Time");
			}

			// Calculate the date
			try {
				Calendar pd = new GregorianCalendar(Integer.parseInt(ctx.getParameter("dateY")), Integer.parseInt(ctx
						.getParameter("dateM")), Integer.parseInt(ctx.getParameter("dateD")));
				fr.setDate(pd.getTime());
			} catch (NumberFormatException nfe) {
				throw new CommandException("Invalid Flight Date");
			}

			// Validate the date
			Calendar forwardLimit = Calendar.getInstance();
			Calendar backwardLimit = Calendar.getInstance();
			forwardLimit.add(Calendar.DATE, SystemData.getInt("users.pirep.maxDays"));
			backwardLimit.add(Calendar.DATE, SystemData.getInt("users.pirep.minDays") * -1);
			if ((fr.getDate().before(backwardLimit.getTime())) || (fr.getDate().after(forwardLimit.getTime())))
				throw new CommandException("Invalid Flight Report Date - " + fr.getDate() + " ("
						+ backwardLimit.getTime() + " - " + forwardLimit.getTime());

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
		
		// Get data for comboboxes
		Calendar cld = Calendar.getInstance();
		
		// Get all airlines
		Map allAirlines = (Map) SystemData.getObject("airlines");

		PIREPAccessControl ac = null;
		try {
			Connection con = ctx.getConnection();
			Set airlines = new TreeSet();

			// Get the DAO and load the flight report
			GetFlightReports dao = new GetFlightReports(con);
			if (isNew) {
				ac = new PIREPAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot create new PIREP");

				// Save the user object
				ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
				
				// Get the active airlines
				for (Iterator i = allAirlines.values().iterator(); i.hasNext(); ) {
					Airline a = (Airline) i.next();
					if (a.getActive())
						airlines.add(a);
				}
			} else {
				FlightReport fr = dao.get(ctx.getID());
				if (fr == null)
					throw new CommandException("Invalid Flight Report - " + ctx.getID());

				// Check our access
				ac = new PIREPAccessControl(ctx, fr);
				ac.validate();
				if (!ac.getCanEdit())
					throw securityException("Not Authorized");

				// Save the pilot info/PIREP in the request
				GetPilot dao2 = new GetPilot(con);
				ctx.setAttribute("pilot", dao2.get(fr.getDatabaseID(FlightReport.DBID_PILOT)), REQUEST);
				ctx.setAttribute("pirep", fr, REQUEST);
				
				// Set PIREP date
				cld.setTime(fr.getDate());
				
				// Get the active airlines
				for (Iterator i = allAirlines.values().iterator(); i.hasNext(); ) {
					Airline a = (Airline) i.next();
					if (a.getActive() || (fr.getAirline().equals(a)))
						airlines.add(a);
				}
			}
			
			ctx.setAttribute("airlines", airlines, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save pirepdate combobox values
		ctx.setAttribute("pirepYear", StringUtils.format(cld.get(Calendar.YEAR), "0000"), REQUEST);
		ctx.setAttribute("pirepMonth", StringUtils.format(cld.get(Calendar.MONTH), "#0"), REQUEST);
		ctx.setAttribute("pirepDay", StringUtils.format(cld.get(Calendar.DATE), "#0"), REQUEST);
		
		// Save airport/airline lists in the request
		ctx.setAttribute("airline", "DVA", REQUEST);
		ctx.setAttribute("airportSorter", _cmp, REQUEST);

		// Save Flight Simulator versions
		ctx.setAttribute("fsVersions", fsVersions, REQUEST);

		// Set basic lists for the JSP
		ctx.setAttribute("emptyList", Collections.EMPTY_LIST, REQUEST);
		ctx.setAttribute("flightTimes", _flightTimes, REQUEST);
		ctx.setAttribute("months", months, REQUEST);
		ctx.setAttribute("years", _flightYears, REQUEST);

		// Set the access controller and status attribute
		ctx.setAttribute("access", ac, REQUEST);
		ctx.setAttribute("isNew", Boolean.valueOf(isNew), REQUEST);

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
				throw new CommandException("Invalid Flight Report - " + ctx.getID());

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

			// Check if this is an ACARS flight - search for an open checkride, and load the ACARS data
			if (fr instanceof ACARSFlightReport) {
				mapType = Pilot.MAP_GOOGLE;
				ACARSFlightReport afr = (ACARSFlightReport) fr;
				int flightID = afr.getDatabaseID(FlightReport.DBID_ACARS);
				
				// Figure out the hemispheres for the start/end airports
				Set hemis = new HashSet();
				hemis.add(new Integer(fr.getAirportD().getHemisphere()));
				hemis.add(new Integer(fr.getAirportA().getHemisphere()));

				// Get the route data from the DAFIF database
				GetACARSData ardao = new GetACARSData(con);
				FlightInfo info = ardao.getInfo(flightID);
				if (info != null) {
					List routeEntries = StringUtils.split(info.getRoute(), " ");
					GeoLocation lastWaypoint = fr.getAirportD();
					
					// Get navigation aids
					GetNavData navdao = new GetNavData(con);
					NavigationDataMap navaids = navdao.getByID(routeEntries);
					
					// Filter out navaids and put them in the correct order
					List routeInfo = new ArrayList();
					for (Iterator i = routeEntries.iterator(); i.hasNext();) {
						String navCode = (String) i.next();
						NavigationDataBean wPoint = navaids.get(navCode, lastWaypoint);
						if (wPoint != null) {
						   if (hemis.contains(new Integer(wPoint.getHemisphere()))) {
						      routeInfo.add(wPoint);
						      lastWaypoint = wPoint;
						   }
						}
					}

					// Save ACARS info
					ctx.setAttribute("filedRoute", routeInfo, REQUEST);
					ctx.setAttribute("flightInfo", info, REQUEST);
				}

				// Get the check ride
				CheckRide cr = null;
				GetExam crdao = new GetExam(con);
				if (flightID != 0) 
					cr = crdao.getACARSCheckRide(flightID);
				
				// If we have a check ride, then save it and calculate the access level
				if (cr != null) {
					ExamAccessControl crAccess = new ExamAccessControl(ctx, cr);
					crAccess.validate();
					
					// Save the checkride and its access controller
					ctx.setAttribute("checkRide", cr, REQUEST);
					ctx.setAttribute("crAccess", crAccess, REQUEST);
					if (crAccess.getCanScore())
						ctx.setAttribute("crPassFail", crApprove, REQUEST);
				}
			}

			// If we're set to use Google Maps, calculate the route
			if (mapType == Pilot.MAP_GOOGLE) {
				// If this isnt't an ACARS PRIEP, calculate the GC route
				if (!(fr instanceof ACARSFlightReport))
				   ctx.setAttribute("mapRoute", GeoUtils.greatCircle(fr.getAirportD().getPosition(), fr.getAirportA().getPosition(),
							100), REQUEST);   

				// Save the route and map center for the Google Map
				ctx.setAttribute("googleMap", Boolean.TRUE, REQUEST);
				ctx.setAttribute("mapCenter", fr.getAirportD().getPosition().midPoint(fr.getAirportA().getPosition()), REQUEST);
			}

			// Get the pilot/PIREP beans in the request
			ctx.setAttribute("pilot", dao2.get(fr.getDatabaseID(FlightReport.DBID_PILOT)), REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);

			// Create the access controller and stuff it in the request
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			ctx.setAttribute("access", ac, REQUEST);
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