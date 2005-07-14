// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.List;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetSystemData;
import org.deltava.dao.DAOException;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display HTTP Server totals.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServerStatsCommand extends AbstractViewCommand {
	
	private static final String[] SORT_NAMES = {"Date", "Total Hits", "Server Time", "Home Page Hits", "Bandwidth"};
    private static final String[] SORT_COLUMNS = {"DATE DESC", "REQUESTS DESC", "EXECTIME DESC", "HOMEHITS DESC", "BANDWIDTH DESC"};
    
    private static List _sortComboAliases;

    /**
     * Initializes the command. This will create the ComboAlias lists for sorting the statistics.
     * @param id the Command ID
     * @param cmdName the Command Name
     * @throws CommandException if an unhandled error occurs
     * @see AbstractCommand#init(String, String)
     */
    public void init(String id, String cmdName) throws CommandException {
        super.init(id, cmdName);
        _sortComboAliases = ComboUtils.fromArray(SORT_NAMES, SORT_COLUMNS);
    }
    
    /**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs.
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);
		vc.setDefaultSortType("DATE DESC");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetSystemData dao = new GetSystemData(con);
			dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // Get the statistics
			vc.setResults(dao.getHTTPStats(vc.getSortType()));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
        // Save the sorter types in the request
        ctx.setAttribute("sortTypes", _sortComboAliases, REQUEST);

        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/stats/httpStats.jsp");
        result.setSuccess(true);
	}
}