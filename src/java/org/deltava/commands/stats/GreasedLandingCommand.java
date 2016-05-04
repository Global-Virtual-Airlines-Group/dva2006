// Copyright 2005, 2006, 2008, 2009, 2010, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.flight.*;
import org.deltava.beans.acars.RunwayDistance;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display the smoothest landings.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GreasedLandingCommand extends AbstractViewCommand {

	private static final List<?> DATE_FILTER = ComboUtils.fromArray(new String[] { "30 Days", "60 Days", "90 Days", "180 Days", "1 Year" }, new String[] { "30", "60", "90", "180", "365" });

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Load the view context
		ViewContext<FlightReport> vc = initView(ctx, FlightReport.class, 25);

		// Check equipment type and how many days back to search
		String eqType = ctx.getParameter("eqType");
		int daysBack = StringUtils.parse(ctx.getParameter("days"), 90);
		ctx.setAttribute("daysBack", Integer.valueOf(daysBack), REQUEST);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO
			GetFlightReportRecognition dao = new GetFlightReportRecognition(con);
			
			// Save equipment choices
			List<Object> eqTypes = new ArrayList<Object>();
			eqTypes.add(ComboUtils.fromString("All Aircraft", ""));
			eqTypes.add(ComboUtils.fromString("Staff Members", "staff"));
			eqTypes.addAll(dao.getACARSEquipmentTypes(20));
			ctx.setAttribute("eqTypes", eqTypes, REQUEST);
			
			// Get the results
			Collection<Integer> IDs = new ArrayList<Integer>(vc.getCount() + 2);
			dao.setQueryMax(vc.getCount());
			dao.setDayFilter(daysBack);
			if (StringUtils.isEmpty(eqType))
				IDs.addAll(dao.getGreasedLandings());
			else if ("staff".equals(eqType))
				IDs.addAll(dao.getStaffReports());
			else
				IDs.addAll(dao.getGreasedLandings(eqType));
			
			// Load the PIREPs and runways
			GetFlightReports frdao = new GetFlightReports(con);
			GetACARSData acdao = new GetACARSData(con);
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			Collection<FlightReport> pireps = new ArrayList<FlightReport>();
			Map<Integer, RunwayDistance> runways = new HashMap<Integer, RunwayDistance>();
			for (Integer pirepID : IDs) {
				FlightReport fr = frdao.get(pirepID.intValue());
				pilotIDs.add(Integer.valueOf(fr.getDatabaseID(DatabaseID.PILOT)));
				pireps.add(fr);
				RunwayDistance rd = acdao.getLandingRunway(fr.getDatabaseID(DatabaseID.ACARS));
				if (rd != null) runways.put(pirepID, rd);
			}
			
			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);
			ctx.setAttribute("rwys", runways, REQUEST);
			vc.setResults(pireps);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save combobox choices
		ctx.setAttribute("dateFilter", DATE_FILTER, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/greasedLandings.jsp");
		result.setSuccess(true);
	}
}