// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display statistics about Pilot equipment ratings.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class RatingCountCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load all aircraft
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allAircraft", acdao.getAircraftTypes(), REQUEST);
			
			// Check if we loaded something
			String acType = ctx.getParameter("acType");
			Aircraft ac = acdao.get(acType);
			if ((acType == null) || (ac == null)) {
				CommandResult result = ctx.getResult();
				result.setURL("/jsp/pilot/ratingCount.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the eq profile for the rating
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> eqTypes = eqdao.getAll().stream().filter(eq -> eq.getRatings().contains(acType)).collect(Collectors.toSet());
			ctx.setAttribute("eqPrograms", eqTypes, REQUEST);
			
			// Load the qualifying Pilots
			Collection<Integer> qualifyIDs = new HashSet<Integer>();
			GetExamQualifications exqdao = new GetExamQualifications(con);
			for (EquipmentType eq : eqTypes)
				qualifyIDs.addAll(exqdao.getRatedPilots(eq));
			
			// Save aircraft profile
			ctx.setAttribute("aircraft", ac, REQUEST);
			ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
			
			// Load all the pilots with ratings, and who qualify
			Collection<Integer> pilotIDs = exqdao.getRatedPilots(acType);
			ctx.setAttribute("ratedIDs", pilotIDs, REQUEST);
			ctx.setAttribute("qualifyIDs", qualifyIDs, REQUEST);
			
			// If we have people qualified, but not yet rated
			Collection<Integer> nyRatedIDs = CollectionUtils.getDelta(qualifyIDs, pilotIDs);
			ctx.setAttribute("notYetRatedIDs", nyRatedIDs, REQUEST);
			
			// Load pilot profiles
			GetPilot pdao = new GetPilot(con);
			if (nyRatedIDs.size() < 50)
				ctx.setAttribute("notYetRatedPilots", pdao.getByID(nyRatedIDs, "PILOTS").values(), REQUEST);
			if (pilotIDs.size() < 50)
				ctx.setAttribute("ratedPilots", pdao.getByID(pilotIDs, "PILOTS").values(), REQUEST);
			if (qualifyIDs.size() < 50)
				ctx.setAttribute("qualifyPilots", pdao.getByID(qualifyIDs, "PILOTS").values(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/ratingCount.jsp");
		result.setSuccess(true);
	}
}