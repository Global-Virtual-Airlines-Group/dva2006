// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetStatistics;
import org.deltava.dao.GetTableStatus;
import org.deltava.dao.DAOException;

import org.deltava.beans.stats.AirlineTotals;
import org.deltava.util.system.SystemData;

/**
 * A web site command to display Airline Total statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirlineTotalsCommand extends AbstractCommand {

    private AirlineTotals _totals = new AirlineTotals(0);
    private Set _tableStatus = new TreeSet();
    
    /**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occrurs. Login failures are not considered errors.
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        int cacheExpiry = SystemData.getInt("cache.stats") * 60000;

        CommandResult result = ctx.getResult();
        synchronized (this) {
            if ((_totals.getEffectiveDate() + cacheExpiry) < System.currentTimeMillis()) {
               _tableStatus.clear();
               
                try {
                    Connection con = ctx.getConnection();
                    
                    // Get the Statistics DAO
                    GetStatistics dao = new GetStatistics(con);
                    _totals = dao.getAirlineTotals();
                    
                    // Get the Table Status for our database and common
                    GetTableStatus dao2 = new GetTableStatus(con);
                    _tableStatus.addAll(dao2.execute("common"));
                    _tableStatus.addAll(dao2.execute("acars"));
                    _tableStatus.addAll(dao2.execute(SystemData.get("airline.db").toLowerCase()));
                } catch (DAOException de) {
                    throw new CommandException(de);
                } finally {
                    ctx.release();
                }
            }
            
            // Save the results in the request
            ctx.setAttribute("totals", _totals, REQUEST);
            ctx.setAttribute("tableStatus", _tableStatus, REQUEST);
            ctx.setAttribute("effectiveDate", new Date(_totals.getEffectiveDate()), REQUEST);
        }
        
        // Forward to the JSP
        result.setURL("/jsp/stats/airlineTotals.jsp");
        result.setSuccess(true);
    }
}