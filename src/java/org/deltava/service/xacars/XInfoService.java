// Copyright 2011, 2014, 2016, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * The XACARS Flight Information Web Service.
 * @author Luke
 * @version 10.0
 * @since 4.1
 */

public class XInfoService extends XAService {
	
	private static final Logger log = Logger.getLogger(XInfoService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Try and parse the flight code and user ID
		UserID uid = new UserID(ctx.getParameter("DATA4"));
		Flight f = FlightCodeParser.parse(ctx.getParameter("DATA2"));
		FlightReport fr = null;
		try {
			Connection con = ctx.getConnection();
			ctx.setContentType("text/plain", "UTF-8");
			
			// Get the user ID
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.getPilotByCode(uid.getUserID(), ctx.getDB());
			if (usr == null)
				throw new InvalidDataException("Unknown User ID");
			else if (usr.getStatus() != PilotStatus.ACTIVE) {
				log.warn(usr.getName() + ", status = " + usr.getStatus().getDescription());
				throw new InvalidDataException("Inactive User ID");
			}
			
			// Load draft PIREPs, checking for the flight number
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> flights = frdao.getDraftReports(usr.getID(), null, ctx.getDB());
			for (Iterator<FlightReport> i = flights.iterator(); i.hasNext(); ) {
				FlightReport dfr = i.next();
				if (dfr.getAirline().equals(f.getAirline()) && (dfr.getFlightNumber() == f.getFlightNumber())) {
					fr = dfr;
					break;
				}
			}
			
			// Load the requested flight
			if ((f != null) && (fr == null)) {
				GetRawSchedule rsdao = new GetRawSchedule(con);
				GetSchedule sdao = new GetSchedule(con);
				sdao.setSources(rsdao.getSources(true, ctx.getDB()));
				f.setLeg(0);
				if (f.getAirline() == null)
					f.setAirline(SystemData.getAirline(uid.getAirlineCode()));
				
				ScheduleEntry se = sdao.get(f, ctx.getDB());
				if (se != null)
					fr = new FlightReport(se);
			}
			
			// Get passengers
			if ((fr != null) && usr.hasRating(fr.getEquipmentType())) {
				GetAircraft adao = new GetAircraft(con);
				Aircraft a = adao.get(fr.getEquipmentType());
				if (a == null) {
					Collection<Aircraft> allEQ = adao.getAircraftTypes();
					Map<String, Aircraft> acIATA = new HashMap<String, Aircraft>();
					allEQ.forEach(ac -> ac.getIATA().forEach(iata -> acIATA.put(iata, ac)));
					
					a = acIATA.get(fr.getEquipmentType()); 
					if (a == null)
						throw new InvalidDataException("Invalid equipment type - " + fr.getEquipmentType());
					
					log.info("Resolved " + fr.getEquipmentType() + " to " + a.getName());
				}
				
				// Get load factor and passengers
				EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
				if (eInfo != null) {
					AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
					LoadFactor lf = new LoadFactor(eInfo);
					double loadFactor = lf.generate();
					fr.setPassengers((int) Math.round(opts.getSeats() * loadFactor));
					fr.setLoadFactor(loadFactor);
				}
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} catch (InvalidDataException ide) {
			ctx.print(ide.getResponse());
			return SC_OK;
		} finally {
			ctx.release();
		}
		
		// Dump flights
		try {
			if (fr != null) {
				ctx.println("1|flightplan");
				ctx.println(fr.getAirportD().getICAO());
				ctx.println(fr.getAirportA().getICAO());
				ctx.println("");				// alternate
				ctx.println("");
				ctx.println(String.valueOf(fr.getPassengers()));
				ctx.println("0");			// cargo
				ctx.println("IFR");
				ctx.println(fr.getEquipmentType());
			} else
				ctx.print("0|Unknown Flight Number");

			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}

		return SC_OK;
	}
}