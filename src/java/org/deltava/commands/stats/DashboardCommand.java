// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.stats.PerformanceMetrics;

import org.deltava.comparators.PerformanceComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display performance metrics.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class DashboardCommand extends AbstractCommand {
	
	private static final String[] PIREP_GROUP_NAMES = {"Equipment Type", "Pilot", "Approver", "Flight Date", "Day of Week"};
	private static final String[] PIREP_GSQL = {"EQTYPE", "PILOT_ID", "DISPOSAL_ID", "DATE", "DATE_FORMAT(DATE, '%W')"};
	
	private static final String[] EXAM_GROUP_NAMES = {"Exam Name", "Pilot", "Scorer", "Exam Date", "Day of Week"};
	private static final String[] EXAM_GSQL = {"E.NAME", "E.PILOT_ID", "E.GRADED_BY", "E.CREATED_ON",
		"DATE_FORMAT(E.CREATED_ON, '%W')"};
	
	private static final String[] CRIDE_GROUP_NAMES = {"Equipment Program", "Aircraft", "Pilot", "Scorer", "Date", "Day of Week"};
	private static final String[] CRIDE_GSQL = {"CR.EQTYPE", "CR.ACTYPE", "CR.PILOT_ID", "CR.GRADED_BY", "CR.CREATED", 
		"DATE_FORMAT(CR.CREATED, '%W')"};
	
	private static final String[] COOLER_GROUP_NAMES = {"Date", "Day of Week"};
	private static final String[] COOLER_GSQL = {"CREATED", "DATE_FORMAT(CREATED, '%W')"};

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the sort type
		String sortType = ctx.getParameter("sortType");
		if (sortType == null)
			sortType = "Category";

		// Create the comparator and the result map
		PerformanceComparator cmp = new PerformanceComparator(sortType);
		Map<String, Collection<PerformanceMetrics>> results = new HashMap<String, Collection<PerformanceMetrics>>();
		
		// Get start and end dates
		int startDays = StringUtils.parse(ctx.getParameter("startDays"), 31);
		int endDays = StringUtils.parse(ctx.getParameter("endDays"), 0);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			GetPerformance dao = new GetPerformance(con);
			
			// Load PIREP approval statistics
			dao.setCategorySQL(PIREP_GSQL[StringUtils.arrayIndexOf(PIREP_GROUP_NAMES, ctx.getParameter("paGroup"), 0)]);
			List<PerformanceMetrics> metrics = dao.getFlightApproval(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("pirepApproval", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(getUserIDs(metrics));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				updatePilotNames(pilots, metrics);
				Collections.sort(metrics, cmp);
			}
				
			// Load exam grading statistics
			dao.setCategorySQL(EXAM_GSQL[StringUtils.arrayIndexOf(EXAM_GROUP_NAMES, ctx.getParameter("examGroup"), 0)]);
			metrics = dao.getExamGrading(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("examGrading", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(getUserIDs(metrics));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				updatePilotNames(pilots, metrics);
				Collections.sort(metrics, cmp);
			}
			
			// Load Checkride grading statistics
			dao.setCategorySQL(CRIDE_GSQL[StringUtils.arrayIndexOf(CRIDE_GROUP_NAMES, ctx.getParameter("rideGroup"), 0)]);
			metrics = dao.getCheckRideGrading(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("rideGrading", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(getUserIDs(metrics));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				updatePilotNames(pilots, metrics);
				Collections.sort(metrics, cmp);
			}
			
			// Load Flight Report statistics
			dao.setCategorySQL(PIREP_GSQL[StringUtils.arrayIndexOf(PIREP_GROUP_NAMES, ctx.getParameter("frGroup"), 3)]);
			metrics = dao.getFlights(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("pirepStats", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(getUserIDs(metrics));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				updatePilotNames(pilots, metrics);
				Collections.sort(metrics, cmp);
			}
			
			// Load ACARS Flight Report statistics
			dao.setCategorySQL(PIREP_GSQL[StringUtils.arrayIndexOf(PIREP_GROUP_NAMES, ctx.getParameter("afrGroup"), 3)]);
			metrics = dao.getACARSFlights(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("acarsStats", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(getUserIDs(metrics));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				updatePilotNames(pilots, metrics);
				Collections.sort(metrics, cmp);
			}

			// Load Water Cooler statistics
			dao.setCategorySQL(COOLER_GSQL[StringUtils.arrayIndexOf(COOLER_GROUP_NAMES, ctx.getParameter("coolerGroup"), 0)]);
			//metrics = dao.getCoolerPosts(startDays, endDays);
			//Collections.sort(metrics, cmp);
			//results.put("coolerStats", metrics);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save sort options
		ctx.setAttribute("sortOptions", ComboUtils.fromArray(cmp.getTypeNames()), REQUEST);
		
		// Save group options
		ctx.setAttribute("pirepGroupOptions", ComboUtils.fromArray(PIREP_GROUP_NAMES), REQUEST);
		ctx.setAttribute("examGroupOptions", ComboUtils.fromArray(EXAM_GROUP_NAMES), REQUEST);
		ctx.setAttribute("rideGroupOptions", ComboUtils.fromArray(CRIDE_GROUP_NAMES), REQUEST);
		ctx.setAttribute("coolerGroupOptions", ComboUtils.fromArray(COOLER_GROUP_NAMES), REQUEST);
		
		// Save start/end dates
		ctx.setAttribute("startDays", new Integer(startDays), REQUEST);
		ctx.setAttribute("endDays", new Integer(endDays), REQUEST);
		
		// Save results
		ctx.setAttribute("results", results, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/dashBoard.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Helper method to extract database IDs from metrics bean categories.
	 */
	private Collection<Integer> getUserIDs(Collection<PerformanceMetrics> metrics) {
		Collection<Integer> results = new HashSet<Integer>();
		for (Iterator<PerformanceMetrics> i = metrics.iterator(); i.hasNext(); ) {
			PerformanceMetrics pm = i.next();
			try {
				results.add(new Integer(pm.getName()));
			} catch (NumberFormatException nfe) {
				// not a number
			}
		}
		
		return results;
	}
	
	/**
	 * Helper method to update Pilot names.
	 */
	private void updatePilotNames(Map<Integer, Pilot> users, Collection<PerformanceMetrics> metrics) {
		for (Iterator<PerformanceMetrics> i = metrics.iterator(); i.hasNext(); ) {
			PerformanceMetrics pm = i.next();
			try {
				Pilot usr = users.get(new Integer(pm.getName()));
				if (usr != null)
					pm.setName(usr.getName());
			} catch (NumberFormatException nfe) {
				// not a number
			}
		}
	}
}