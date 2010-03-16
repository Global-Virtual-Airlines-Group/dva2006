// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display Examination and Check Ride statistics. 
 * @author Luke
 * @version 3.0
 * @since 3.0
 */

public class ExamStatsCommand extends AbstractViewCommand {
	
	private static final List<?> SEARCH_TYPES = ComboUtils.fromArray("Check Rides", "Examinations");
	
	private static final String[] CODES = new String[] { "DATE_FORMAT(C.CREATED, '%M %Y')", "CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", "'ALL'" };
	private static final List<?> GROUP_OPTS = ComboUtils.fromArray(new String[] {"Graded On", "Scorer", "All"}, CODES);
	
	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result
		CommandResult result = ctx.getResult();
		
		// Get the view Context
		ViewContext vc = initView(ctx);
		
		// Get label / sub-label
		String label = ctx.getParameter("label");
		if (StringUtils.arrayIndexOf(CODES, label) == -1)
			label = CODES[0];
		String subLabel = ctx.getParameter("subLabel");
		if (StringUtils.arrayIndexOf(CODES, subLabel) == -1)
			subLabel = CODES[1];
		
		try {
			Connection con = ctx.getConnection();
			
			GetExamStatistics dao = new GetExamStatistics(con);
			Collection<Integer> eScorerIDs = dao.getExamScorerIDs();
			Collection<Integer> crScorerIDs = dao.getCheckRideScorerIDs();
			
			// Initialize the comparator
			PilotComparator cmp = new PilotComparator(PersonComparator.FIRSTNAME);
			
			// Load the user data
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("examScorers", CollectionUtils.sort(pdao.getByID(eScorerIDs, "PILOTS").values(), cmp), REQUEST);
			ctx.setAttribute("crScorers", CollectionUtils.sort(pdao.getByID(crScorerIDs, "PILOTS").values(), cmp), REQUEST);
			
			// Do the query
			if (!StringUtils.isEmpty(ctx.getParameter("searchType"))) {
				boolean isExam = "Examinations".equals(ctx.getParameter("searchType"));
				dao.setQueryStart(vc.getStart());
				dao.setQueryMax(vc.getCount());
				if (isExam) {
					int scorerID = StringUtils.parse(ctx.getParameter("examScorer"), 0);
					vc.setResults(dao.getExamStatistics(label, subLabel, scorerID));
				} else {
					int scorerID = StringUtils.parse(ctx.getParameter("crScorer"), 0);
					boolean academyOnly = Boolean.valueOf(ctx.getParameter("academyOnly")).booleanValue();
					vc.setResults(dao.getCheckrideStatistics(label, subLabel, academyOnly, scorerID));
				}
				
				ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save sort/group options
		ctx.setAttribute("label", label, REQUEST);
		ctx.setAttribute("subLabel", subLabel, REQUEST);
		ctx.setAttribute("searchTypes", SEARCH_TYPES, REQUEST);
		ctx.setAttribute("groupOpts", GROUP_OPTS, REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/stats/examStats.jsp");
		result.setSuccess(true);
	}
}