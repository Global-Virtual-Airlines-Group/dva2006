// Copyright 2005, 2009, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Airports.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class AirportListCommand extends AbstractViewCommand {
	
	private static final String NO_GATE = "$NOGATE";
	private static final String[] SORT_TYPES = {"IATA", "ICAO", "NAME"};
	private static final List<ComboAlias> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"IATA Code", "ICAO Code", "Airport Name"}, SORT_TYPES);

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the sort type
		ViewContext<Airport> vc = initView(ctx, Airport.class);
		int sortOfs = StringUtils.arrayIndexOf(SORT_TYPES, vc.getSortType(), 0);
		vc.setSortType(SORT_TYPES[sortOfs]);

		// Get the airline
		String aCode = ctx.getParameter("airline");
		Airline a = SystemData.getAirline(aCode);
		if (NO_GATE.equals(aCode)) {
			List<Airport> airports = new HashSet<Airport>(SystemData.getAirports().values()).stream().filter(ap -> !ap.getGateData() && !ap.getAirlineCodes().isEmpty()).collect(Collectors.toList());
			AirportComparator cmp = new AirportComparator(sortOfs);
			airports.sort(cmp);
			vc.setResults(airports.subList(vc.getStart(), vc.getStart() + vc.getCount()));
			ctx.setAttribute("airline", NO_GATE, REQUEST);
		} else
			ctx.setAttribute("airline", a, REQUEST);
		
		// Get all airlines
		Collection<ComboAlias> airlines = new ArrayList<ComboAlias>();
		airlines.add(ComboUtils.fromString("No Airline", ""));
		airlines.add(ComboUtils.fromString("No Gate Data", NO_GATE));
		airlines.addAll(SystemData.getAirlines().values());
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("sortOptions", SORT_OPTIONS, REQUEST);
		
		if (!NO_GATE.equals(aCode)) {
			try {
				GetAirport dao = new GetAirport(ctx.getConnection());
				dao.setQueryMax(vc.getCount());
				dao.setQueryStart(vc.getStart());
				vc.setResults(dao.getByAirline(a, vc.getSortType()));
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airportList.jsp");
		result.setSuccess(true);
	}
}