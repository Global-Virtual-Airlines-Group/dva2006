// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.schedule.*;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Site Command to modify Airport data.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class AirportCommand extends AbstractAuditFormCommand {
	
	/**
	 * Callback method called when saving the Airport.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();

		// Get the Airport code - if we're new, check if the airport exists
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);
		if (isNew) {
			Airport a = SystemData.getAirport(ctx.getParameter("iata"));
			if (a != null) {
				ctx.setMessage("Airport already exists - " + a.getName());

				// Save directions and time zones in request
				ctx.setAttribute("latDir", Arrays.asList(GeoLocation.LAT_DIRECTIONS), REQUEST);
				ctx.setAttribute("lonDir", Arrays.asList(GeoLocation.LON_DIRECTIONS), REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/schedule/airportEdit.jsp");
				result.setSuccess(true);
				return;
			}
		}

		try {
			Airport a = null; Airport oa = null; String oldIATA = null; boolean isCodeChanged = false; 
			Connection con = ctx.getConnection();

			// Get the DAO and the Airport
			if (!isNew) {
				GetAirport dao = new GetAirport(con);
				a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Invalid Airport Code - " + aCode);

				// Load airport fields
				oa = BeanUtils.clone(a);
				oldIATA = a.getIATA();
				a.setName(ctx.getParameter("name"));
				a.setICAO(ctx.getParameter("icao"));
				a.setIATA(ctx.getParameter("iata"));
				isCodeChanged = !oldIATA.equalsIgnoreCase(a.getIATA());
			} else
				a = new Airport(ctx.getParameter("iata"), ctx.getParameter("icao"), ctx.getParameter("name"));

			// Update the aiport from the request
			a.setTZ(TZInfo.get(ctx.getParameter("tz")));
			a.setAirlines(ctx.getParameters("airline"));
			a.setCountry(Country.get(ctx.getParameter("country")));
			a.setASDE(Boolean.parseBoolean(ctx.getParameter("hasADSE")));
			a.setHasPFI(Boolean.parseBoolean(ctx.getParameter("hasUSPFI")));
			a.setIsSchengen(Boolean.parseBoolean(ctx.getParameter("isSchengen")));
			a.setHasFictionalCode(Boolean.parseBoolean(ctx.getParameter("isFictional")));
			Airport oldA = SystemData.getAirport(ctx.getParameter("oldAirport"));
			a.setSupercededAirport(((oldA == null) || oldA.getIATA().equals(a.getIATA())) ? null : oldA.getIATA());

			// Build the airport latitude/longitude
			try {
				GeoPosition gp = new GeoPosition();
				int latD = Integer.parseInt(ctx.getParameter("latD"));
				int latM = Integer.parseInt(ctx.getParameter("latM"));
				int latS = Integer.parseInt(ctx.getParameter("latS"));
				gp.setLatitude(latD, latM, latS);

				// Convert to to southern hemisphere if necessary
				if (StringUtils.arrayIndexOf(GeoLocation.LAT_DIRECTIONS, ctx.getParameter("latDir")) == 1)
					gp.setLatitude(gp.getLatitude() * -1);

				// Parse longitude
				int lonD = Integer.parseInt(ctx.getParameter("lonD"));
				int lonM = Integer.parseInt(ctx.getParameter("lonM"));
				int lonS = Integer.parseInt(ctx.getParameter("lonS"));
				gp.setLongitude(lonD, lonM, lonS);
				
				// Convert to western hemisphere if necessary
				if (StringUtils.arrayIndexOf(GeoLocation.LON_DIRECTIONS, ctx.getParameter("lonDir")) == 1)
					gp.setLongitude(gp.getLongitude() * -1);

				// Update the airport
				a.setLocation(gp.getLatitude(), gp.getLongitude());
			} catch (NumberFormatException nfe) {
				throw new CommandException("Error parsing Airport latitude/longitude", false);
			}
			
			// Load maximum runway length
			GetNavData navdao = new GetNavData(con);
			for (Runway r : navdao.getRunways(a, Simulator.P3Dv4))
				a.setMaximumRunwayLength(r.getLength());
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oa, a);
			AuditLog ae = AuditLog.create(a, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();

			// Get the DAO and write the airport
			SetAirportAirline wdao = new SetAirportAirline(con);
			if (isNew) {
				wdao.create(a);
				ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(a, oldIATA);
				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
				
				// Update accomplishments
				if (isCodeChanged) {
					Collection<Accomplishment> accs = new LinkedHashSet<Accomplishment>();
					GetAccomplishment acdao = new GetAccomplishment(con);
					accs.addAll(acdao.getByUnit(AccomplishUnit.AIRPORTS));
					accs.addAll(acdao.getByUnit(AccomplishUnit.AIRPORTD));
					accs.addAll(acdao.getByUnit(AccomplishUnit.AIRPORTA));
					
					// Filter out airports
					@SuppressWarnings("null")
					final String oc = oldIATA.toUpperCase(); final String nc = a.getIATA(); 
					accs.removeIf(acc -> !acc.renameChoice(oc, nc));
					ctx.setAttribute("accsUpdated", accs, REQUEST);
					
					// Save updated accomplishments
					SetAccomplishment acwdao = new SetAccomplishment(con);
					for (Accomplishment acc : accs)
						acwdao.write(acc);
				}
			}
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
			
			// Update the Airports and set status update flag
			EventDispatcher.send(new SystemEvent(EventType.AIRPORT_RELOAD));
			if (isCodeChanged)
				EventDispatcher.send(new IDEvent(EventType.AIRPORT_RENAME, a.getIATA(), oldIATA));

			// Save the airport in the request
			ctx.setAttribute("airport", a, REQUEST);
			ctx.setAttribute("isAirport", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Airport.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the Airport code
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);

		// Validate the airport if we are not creating a new one
		if (!isNew) {
			Airport a = null;
			try {
				Connection con = ctx.getConnection();

				// Get the DAOs and the Airport
				GetAirport dao = new GetAirport(con);
				GetTimeZone tzdao = new GetTimeZone(con);
				a = dao.get(aCode);
				readAuditLog(ctx, a);
				if (a == null) {
					String icao = dao.getICAO(aCode);
					if (icao != null) {
						GetNavData nvdao = new GetNavData(con);
						GeospaceLocation al = nvdao.getAirport(icao);
						if ((al != null) && (aCode != null)) {
							a = new Airport((aCode.length() == 3) ? aCode : "", icao, "");
							a.setLocation(al.getLatitude(), al.getLongitude());
							a.setAltitude(al.getAltitude());
							
							// Look up the time zone
							a.setTZ(tzdao.locate(a));
						}
					}

					ctx.setAttribute("isNew", Boolean.TRUE, REQUEST);
				} else {
					TZInfo tz = tzdao.locate(a);
					if ((tz != null) && !tz.equals(a.getTZ())) {
						Instant now = Instant.now();
						ctx.setAttribute("airportTZ", tz, REQUEST);
						ctx.setAttribute("tzOffsetWarn", Boolean.valueOf(a.getTZ().getZone().getRules().getOffset(now).compareTo(tz.getZone().getRules().getOffset(now)) != 0), REQUEST);
					}
				}
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}

			// If we have an airport, save it in the request
			if (a != null) {
				ctx.setAttribute("airport", a, REQUEST);
				Airport sa = SystemData.getAirport(a.getSupercededAirport());
				if (sa != null)
					ctx.setAttribute("oldAirports", List.of(sa), REQUEST);

				// Convert the geoPosition into degrees, minutes, seconds
				int latS = (int) GeoPosition.getSeconds(a.getLatitude());
				int lngS = (int) GeoPosition.getSeconds(a.getLongitude());
				
				ctx.setAttribute("latD", Integer.valueOf(Math.abs(GeoPosition.getDegrees(a.getLatitude()))), REQUEST);
				ctx.setAttribute("latM", Integer.valueOf(GeoPosition.getMinutes(a.getLatitude())), REQUEST);
				ctx.setAttribute("latS", Integer.valueOf(latS), REQUEST);
				ctx.setAttribute("latNS", GeoLocation.LAT_DIRECTIONS[((a.getLatitude() < 0) ? 1 : 0)], REQUEST);
				ctx.setAttribute("lonD", Integer.valueOf(Math.abs(GeoPosition.getDegrees(a.getLongitude()))), REQUEST);
				ctx.setAttribute("lonM", Integer.valueOf(GeoPosition.getMinutes(a.getLongitude())), REQUEST);
				ctx.setAttribute("lonS", Integer.valueOf(lngS), REQUEST);
				ctx.setAttribute("lonEW", GeoLocation.LON_DIRECTIONS[((a.getLongitude() < 0) ? 1 : 0)], REQUEST);
			}
		}

		// Save directions and time zones in request
		ctx.setAttribute("countries", new TreeSet<Country>(Country.getAll()), REQUEST);
		ctx.setAttribute("latDir", Arrays.asList(GeoLocation.LAT_DIRECTIONS), REQUEST);
		ctx.setAttribute("lonDir", Arrays.asList(GeoLocation.LON_DIRECTIONS), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airportEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Airport. <i>NOT IMPLEMENTED - Edits the Airport</i>
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}