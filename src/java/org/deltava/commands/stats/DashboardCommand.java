// Copyright 2006, 2007, 2008, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.util.stream.Collectors;
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
 * @version 7.2
 * @since 1.0
 */

public class DashboardCommand extends AbstractCommand {
	
	private static final String[] PIREP_GROUP_NAMES = {"Equipment Type", "Pilot", "Approver", "Flight Date", "Day of Week"};
	private static final String[] PIREP_GSQL = {"EQTYPE", "PILOT_ID", "DISPOSAL_ID", "DATE", "DATE_FORMAT(DATE, '%W')"};
	
	private static final String[] EXAM_GROUP_NAMES = {"Exam Name", "Pilot", "Scorer", "Exam Date", "Day of Week"};
	private static final String[] EXAM_GSQL = {"E.NAME", "E.PILOT_ID", "E.GRADED_BY", "E.CREATED_ON", "DATE_FORMAT(E.CREATED_ON, '%W')"};
	
	private static final String[] CRIDE_GROUP_NAMES = {"Equipment Program", "Aircraft", "Pilot", "Scorer", "Date", "Day of Week"};
	private static final String[] CRIDE_GSQL = {"CR.EQTYPE", "CR.ACTYPE", "CR.PILOT_ID", "CR.GRADED_BY", "CR.CREATED", "DATE_FORMAT(CR.CREATED, '%W')"};
	
	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the sort type
		String sortType = ctx.getParameter("sortType");
		if (StringUtils.isEmpty(sortType))
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
				UserDataMap udmap = uddao.get(metrics.stream().map(PerformanceMetrics::getAuthorID).collect(Collectors.toSet()));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				metrics.forEach(pm -> { Pilot usr = pilots.get(Integer.valueOf(pm.getAuthorID())); if (usr != null) pm.setName(usr.getName()); });
				Collections.sort(metrics, cmp);
			}
				
			// Load exam grading statistics
			dao.setCategorySQL(EXAM_GSQL[StringUtils.arrayIndexOf(EXAM_GROUP_NAMES, ctx.getParameter("examGroup"), 0)]);
			metrics = dao.getExamGrading(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("examGrading", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(metrics.stream().map(PerformanceMetrics::getAuthorID).collect(Collectors.toSet()));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				metrics.forEach(pm -> { Pilot usr = pilots.get(Integer.valueOf(pm.getAuthorID())); if (usr != null) pm.setName(usr.getName()); });
				Collections.sort(metrics, cmp);
			}
			
			// Load Checkride grading statistics
			dao.setCategorySQL(CRIDE_GSQL[StringUtils.arrayIndexOf(CRIDE_GROUP_NAMES, ctx.getParameter("rideGroup"), 0)]);
			metrics = dao.getCheckRideGrading(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("rideGrading", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(metrics.stream().map(PerformanceMetrics::getAuthorID).collect(Collectors.toSet()));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				metrics.forEach(pm -> { Pilot usr = pilots.get(Integer.valueOf(pm.getAuthorID())); if (usr != null) pm.setName(usr.getName()); });
				Collections.sort(metrics, cmp);
			}
			
			// Load Flight Report statistics
			dao.setCategorySQL(PIREP_GSQL[StringUtils.arrayIndexOf(PIREP_GROUP_NAMES, ctx.getParameter("frGroup"), 3)]);
			metrics = dao.getFlights(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("pirepStats", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(metrics.stream().map(PerformanceMetrics::getAuthorID).collect(Collectors.toSet()));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				metrics.forEach(pm -> { Pilot usr = pilots.get(Integer.valueOf(pm.getAuthorID())); if (usr != null) pm.setName(usr.getName()); });
				Collections.sort(metrics, cmp);
			}
			
			// Load ACARS Flight Report statistics
			dao.setCategorySQL(PIREP_GSQL[StringUtils.arrayIndexOf(PIREP_GROUP_NAMES, ctx.getParameter("afrGroup"), 3)]);
			metrics = dao.getACARSFlights(startDays, endDays);
			Collections.sort(metrics, cmp);
			results.put("acarsStats", metrics);
			if (dao.isPilotID()) {
				UserDataMap udmap = uddao.get(metrics.stream().map(PerformanceMetrics::getAuthorID).collect(Collectors.toSet()));
				Map<Integer, Pilot> pilots = pdao.getByID(udmap.getByTable(SystemData.get("airline.db") + ".PILOTS"), "PILOTS");
				metrics.forEach(pm -> { Pilot usr = pilots.get(Integer.valueOf(pm.getAuthorID())); if (usr != null) pm.setName(usr.getName()); });
				Collections.sort(metrics, cmp);
			}
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
		
		// Save start/end dates
		ctx.setAttribute("startDays", Integer.valueOf(startDays), REQUEST);
		ctx.setAttribute("endDays", Integer.valueOf(endDays), REQUEST);
		
		// Save results
		ctx.setAttribute("results", results, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/dashBoard.jsp");
		result.setSuccess(true);
	}
}