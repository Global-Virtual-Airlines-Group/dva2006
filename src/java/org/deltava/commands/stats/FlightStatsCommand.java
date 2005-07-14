package org.deltava.commands.stats;

import java.sql.Connection;
import java.util.List;

import org.deltava.commands.*;

import org.deltava.dao.GetStatistics;
import org.deltava.dao.DAOException;

import org.deltava.util.ComboUtils;

/**
 * A web site command to display sorted Flight Report statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */
public class FlightStatsCommand extends AbstractViewCommand {

    private static final String[] SORT_NAMES = {"Flight Legs", "Miles Flown", "Flight Hours", "Avg. Hours", "Avg. Miles"};
    private static final String[] SORT_COLUMNS = {"LEGS", "MILES", "HOURS", "AVGHOURS", "AVGMILES"};
    
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
        vc.setDefaultSortType("LEGS");
        
        // Get grouping type
        String labelType = (String) ctx.getCmdParameter(Command.ID, "CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)");
        
        try {
            Connection con = ctx.getConnection();
            
            // Get the DAO
            GetStatistics dao = new GetStatistics(con);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // Save the statistics in the request
            vc.setResults(dao.getPIREPStatistics(labelType, vc.getSortType(), true));
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Save the sorter types in the request
        ctx.setAttribute("sortTypes", _sortComboAliases, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/stats/flightStats.jsp");
        result.setSuccess(true);
    }
}