// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.sql.Connection;
import java.util.List;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display Water Cooler statistics.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class CoolerStatsCommand extends AbstractViewCommand {
   
	// Sort by options
   private static final String[] SORT_CODE = {"LBL", "PC", "DSTNCT"};
	private static final String[] SORT_NAMES = {"Pilot/Date", "Total Posts", "Distinct Days/Pilots"};
	private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODE);
	
	// Group by options
	private static final String[] GROUP_NAMES = {"Pilot Name", "Posting Date", "Posting Month"};
	private static final String[] GROUP_CODE = {"CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", "DATE(CP.CREATED)",
	      "DATE_FORMAT(DATE(CP.CREATED), '%M %x')"};
	private static final List<?> GROUP_OPTIONS = ComboUtils.fromArray(GROUP_NAMES, GROUP_CODE);

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs.
	 */
   public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
		   vc.setSortType(SORT_CODE[0]);

		// Get grouping type
		String labelType = ctx.getParameter("groupType");
		if (StringUtils.arrayIndexOf(GROUP_CODE, labelType) == -1)
			labelType = GROUP_CODE[0];
		
		// Calculate distinct entries column and labels
		String distinctType;
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) != 0) {
		   distinctType = "CP.AUTHOR_ID";
		   ctx.setAttribute("distinctName", "Pilots", REQUEST);
		} else {
		   distinctType = "DATE(CP.CREATED)";
		   ctx.setAttribute("distinctName", "Days", REQUEST);
		}

		try {
		   Connection con = ctx.getConnection();
		   
		   // Get the DAO and set limits
		   GetStatistics dao = new GetStatistics(con);
		   dao.setQueryStart(vc.getStart());
		   dao.setQueryMax(vc.getCount());
		   
		   // Get the statistics
		   vc.setResults(dao.getCoolerStatistics(vc.getSortType(), labelType, distinctType));
		} catch (DAOException de) {
		   throw new CommandException(de);
		} finally {
		   ctx.release();
		}
		
		// Save the sorter types and labels in the request
		ctx.setAttribute("labelName", GROUP_CODE[StringUtils.arrayIndexOf(GROUP_CODE, labelType)], REQUEST);
		ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
		ctx.setAttribute("groupTypes", GROUP_OPTIONS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/coolerStats.jsp");
		result.setSuccess(true);
   }
}