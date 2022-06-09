// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.Runway;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display and update Gate metadata. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class GateInformationCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the airport
		Airport a = SystemData.getAirport((String) ctx.getCmdParameter(ID, null));
		if (a == null)
			throw notFoundException("Unknown Airport - " + ctx.getCmdParameter(ID, null));

		// Get runway data and build bounding box
		try {
			GetNavData nddao = new GetNavData(ctx.getConnection());
			Collection<Runway> allRwys = nddao.getRunways(a, Simulator.P3Dv4);
			ctx.setAttribute("rwys", allRwys.isEmpty() ? Collections.singleton(a) : allRwys.stream().map(GeoPosition::new).collect(Collectors.toSet()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Load active airports
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME)); 
		airports.addAll(SystemData.getAirports().values().stream().filter(ap -> !ap.getAirlineCodes().isEmpty()).collect(Collectors.toList()));
		ctx.setAttribute("airports", airports, REQUEST);

		// Save metadata
		ctx.setAttribute("airport", a, REQUEST);
		ctx.setAttribute("airlines", a.getAirlineCodes().stream().map(c -> SystemData.getAirline(c)).filter(al -> !al.getHistoric()).collect(Collectors.toCollection(TreeSet::new)), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/gateInfo.jsp");
		result.setSuccess(true);
	}
}