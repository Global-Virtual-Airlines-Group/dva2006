// Copyright 2011, 2012, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * The XACARS Start Flight service.
 * @author Luke
 * @version 8.6
 * @since 4.1
 */

public class StartFlightService extends XAService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Parse the data
		List<String> data = StringUtils.split(ctx.getParameter("DATA3"), "|");
		if ((data == null) || (data.size() < 18)) {
			ctx.print("0|Invalid Flight Data");
			return SC_OK;
		}
		
		// Trim
		for (int x = 0; x < data.size(); x++)
			data.set(x, data.get(x).trim());
		
		// Parse the route
		List<String> rte = StringUtils.nullTrim(StringUtils.split(data.get(5), "~"));
		if (rte.size() < 2) {
			ctx.print("0|Invalid Flight Route");
			return SC_OK;
		}
		
		// Parse the flight data
		Flight f = FlightCodeParser.parse(data.get(2));
		if (f.getAirline() == null) {
			ctx.print("0|Invalid Airline - " + data.get(2));
			return SC_OK;
		}
		
		XAFlightInfo info = new XAFlightInfo(f.getAirline(), f.getFlightNumber());
		info.setEquipmentType(data.get(3));
		info.setAirportD(SystemData.getAirport(rte.get(0)));
		info.setAirportA(SystemData.getAirport(rte.get(rte.size() - 1)));
		info.setStartTime(Instant.now());
		info.setSimulator(getSimulator(ctx));
		if (rte.size() > 2)
			info.setRoute(StringUtils.listConcat(rte.subList(1, rte.size() - 1), " "));
		else
			info.setRoute(StringUtils.listConcat(rte, " "));
		
		// Parse the position data
		XARouteEntry pos = new XARouteEntry(GeoUtils.parseXACARS(data.get(6)), info.getStartTime());
		pos.setHeading(StringUtils.parse(data.get(12), 0));
		pos.setAltitude(StringUtils.parse(data.get(7), 0));
		pos.setFuelRemaining(StringUtils.parse(data.get(11), 0));
		pos.setPhase(info.getPhase());
		
		try {
			// Authenticate the user
			Pilot usr = authenticate(ctx, data.get(0), data.get(17));
			
			// Check the equipment type
			Connection con = ctx.getConnection();
			GetAircraft adao = new GetAircraft(con);
			Aircraft a = adao.get(info.getEquipmentType());
			if (a == null)
				throw new InvalidDataException("Invalid Equipment type");
			
			// Start transaction
			ctx.startTX();
			
			// Write the flight into the table
			info.setAuthorID(usr.getID());
			SetXACARS xwdao = new SetXACARS(con);
			xwdao.create(info);

			// Save the position data
			pos.setFlightID(info.getID());
			xwdao.write(pos);
			ctx.commitTX();
			
			// Return ID
			ctx.print("1|" + info.getID());
		} catch (InvalidDataException ide) {
			ctx.print(ide.getResponse());
		} catch (SecurityException sxe) {
			ctx.print("0|" + sxe.getMessage());
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}
}