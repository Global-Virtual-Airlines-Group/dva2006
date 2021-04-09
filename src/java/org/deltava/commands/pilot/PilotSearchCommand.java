// Copyright 2005, 2007, 2008, 2012, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserData;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search for Pilots.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class PilotSearchCommand extends AbstractCommand {

	private static final int DEFAULT_RESULTS = 25;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();
		
		// Load all equipment types
		try {
			GetAircraft acdao = new GetAircraft(ctx.getConnection());
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Check if we're doing a GET
		if (ctx.getParameter("firstName") == null) {
			ctx.setAttribute("noResults", Boolean.TRUE, REQUEST);
			ctx.setAttribute("maxResults", Integer.valueOf(DEFAULT_RESULTS), REQUEST);
			result.setURL("/jsp/roster/pilotSearch.jsp");
			result.setSuccess(true);
			return;
		}

		// Check if we're doing an exact match
		boolean exactMatch = Boolean.valueOf(ctx.getParameter("exactMatch")).booleanValue();

		// Build the parameters
		String fName = buildParameter(ctx.getParameter("firstName"), exactMatch);
		String lName = buildParameter(ctx.getParameter("lastName"), exactMatch);
		String eMail = buildParameter(ctx.getParameter("eMail"), exactMatch);
		Collection<String> ratings = ctx.getParameters("ratings");
		UserID id = new UserID(ctx.getParameter("pilotCode"));

		// Set the result size
		int maxResults = StringUtils.parse(ctx.getParameter("maxResults"), 1);
		if ((maxResults < 1) || (maxResults > 99))
			maxResults = DEFAULT_RESULTS;

		// Check for inter-airline search
		boolean crossAirlineSearch = Boolean.valueOf(ctx.getParameter("allAirlines")).booleanValue();
		
		UserDataMap udmap = null;
		Collection<Pilot> results = new ArrayList<Pilot>();
		try {
			Connection con = ctx.getConnection();

			// Load Airline information
			GetUserData uddao = new GetUserData(con);
			Map<String, AirlineInformation> apps = uddao.getAirlines(true);
			Map<String, Integer> sizes = new HashMap<String, Integer>();

			// Get the DAOs
			GetStatistics stdao = new GetStatistics(con);
			GetPilot dao = new GetPilot(con);
			dao.setQueryMax(maxResults);

			// Get the search results
			if (id.getUserID() > 0) {
				AirlineInformation app = apps.get(id.getAirlineCode());
				boolean isCrossSearch = !SystemData.get("airline.code").equals(id.getAirlineCode());
				
				// Load the profile
				if ((app != null) && isCrossSearch && ctx.isUserInRole("HR")) {
					sizes.put(app.getCode(), Integer.valueOf(stdao.getActivePilots(app.getDB())));
					Pilot p = dao.getPilotByCode(id.getUserID(), app.getDB());
					if (p != null)
						results.add(p);
				} else {
					sizes.put(SystemData.get("airline.code"), Integer.valueOf(stdao.getActivePilots(ctx.getDB())));
					Pilot p = dao.getPilotByCode(id.getUserID(), ctx.getDB());
					if (p != null)
						results.add(p);
				}
			} else {
				boolean isCrossSearch = crossAirlineSearch && ctx.isUserInRole("HR");
				if (isCrossSearch) {
					for (AirlineInformation app : apps.values()) {
						results.addAll(dao.search(app.getDB(), fName, lName, eMail, ratings));
						sizes.put(app.getCode(), Integer.valueOf(stdao.getActivePilots(app.getDB())));
					}
				} else {
					results.addAll(dao.search(ctx.getDB(), fName, lName, eMail, ratings));
					sizes.put(SystemData.get("airline.code"), Integer.valueOf(stdao.getActivePilots(ctx.getDB())));
				}
			}
			
			// Save the airline sizes
			ctx.setAttribute("airlineSizes", sizes, REQUEST);
			
			// Load the pilot IDs and user locations
			Collection<Integer> IDs = results.stream().map(Pilot::getID).collect(Collectors.toSet());
			udmap = uddao.get(IDs);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate access to each search result
		Map<Integer, PilotAccessControl> accessMap = new HashMap<Integer, PilotAccessControl>();
		for (Pilot p : results) {
			UserData usrInfo = udmap.get(p.getID());

			// Calculate the access level
			PilotAccessControl access = ctx.getDB().equals(usrInfo.getDB()) ? new PilotAccessControl(ctx, p) : new CrossAppPilotAccessControl(ctx, p);
			access.validate();

			// Save the access level in the map, indexed by pilot ID
			accessMap.put(Integer.valueOf(p.getID()), access);
		}

		// Save the results and access level in the request
		ctx.setAttribute("ratings", ratings, REQUEST);
		ctx.setAttribute("results", results, REQUEST);
		ctx.setAttribute("accessMap", accessMap, REQUEST);
		ctx.setAttribute("maxResults", Integer.valueOf(maxResults), REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/roster/pilotSearch.jsp");
		result.setSuccess(true);
	}

	/*
	 * Helper method to take a parameter and add LIKE wildcards.
	 */
	private static String buildParameter(String pValue, boolean exMatch) {
		if (StringUtils.isEmpty(pValue)) return null;
		return exMatch ? pValue : "%" + pValue + "%";
	}
}