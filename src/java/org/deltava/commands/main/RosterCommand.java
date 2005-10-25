package org.deltava.commands.main;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.comparators.PilotComparator;
import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * Command to display the Pilot Roster.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RosterCommand extends AbstractViewCommand {

    // List of query columns we can order by
    private static final String[] SORT_CODE = {"P.FIRSTNAME", "P.LASTNAME", "P.LAST_LOGIN DESC", "P.CREATED",
            "P.PILOT_ID", "P.EQTYPE", "P.RANK", "LEGS DESC", "HOURS DESC", "P.STATUS"};
    private static final List SORT_OPTIONS = ComboUtils.fromArray(PilotComparator.TYPES, SORT_CODE);
    
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
        if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
           vc.setSortType(SORT_CODE[4]);
     
        try {
            Connection con = ctx.getConnection();
            
            // Get the roster and save in the request
            GetPilot dao = new GetPilot(con);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // Get the results
            vc.setResults(dao.getActivePilots(vc.getSortType()));
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Save the sorter types in the request
        ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/roster/mainRoster.jsp");
        result.setSuccess(true);
    }
}