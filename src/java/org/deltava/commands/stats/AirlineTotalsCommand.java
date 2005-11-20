// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.system.TableInfo;

import org.deltava.commands.*;

import org.deltava.dao.GetStatistics;
import org.deltava.dao.GetTableStatus;
import org.deltava.dao.DAOException;

import org.deltava.beans.stats.AirlineTotals;

import org.deltava.util.cache.ExpiringCache;
import org.deltava.util.system.SystemData;

/**
 * A web site command to display Airline Total statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirlineTotalsCommand extends AbstractCommand {

	private ExpiringCache _cache;
    private Set _tableStatus = new TreeSet();
    
    /**
     * Initializes the command.
     * @param id the command ID
     * @param cmdName the command Name
     * @throws CommandException if initialization fails
     */
    public void init(String id, String cmdName) throws CommandException {
    	super.init(id, cmdName);
    	_cache = new ExpiringCache(1, SystemData.getInt("cache.stats") * 60);
    }
    
    /**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occrurs. Login failures are not considered errors.
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        CommandResult result = ctx.getResult();
        synchronized (this) {
        	AirlineTotals totals = (AirlineTotals) _cache.get(AirlineTotals.class);
        	
            if (totals == null) {
               _tableStatus.clear();
               
                try {
                    Connection con = ctx.getConnection();
                    
                    // Get the Statistics DAO
                    GetStatistics dao = new GetStatistics(con);
                    totals = dao.getAirlineTotals();
                    _cache.add(totals);
                    
                    // Get the Table Status for our database and common
                    GetTableStatus dao2 = new GetTableStatus(con);
                    _tableStatus.addAll(dao2.execute("common"));
                    _tableStatus.addAll(dao2.execute("acars"));
                    _tableStatus.addAll(dao2.execute("postfix"));
                    _tableStatus.addAll(dao2.execute(SystemData.get("airline.db").toLowerCase()));
                } catch (DAOException de) {
                    throw new CommandException(de);
                } finally {
                    ctx.release();
                }
            }
            
            // Calculate database size
            long dbSize = 0;
            long dbRows = 0;
            for (Iterator i = _tableStatus.iterator(); i.hasNext(); ) {
            	TableInfo info = (TableInfo) i.next();
            	dbSize += info.getSize();
            	dbSize += info.getIndexSize();
            	dbRows += info.getRows();
            }
            
            // Save database size
            totals.setDBRows(dbRows);
            totals.setDBSize(dbSize);

            // Save the results in the request
            ctx.setAttribute("totals", totals, REQUEST);
            ctx.setAttribute("tableStatus", _tableStatus, REQUEST);
            ctx.setAttribute("effectiveDate", new Date(totals.getEffectiveDate()), REQUEST);
        }
        
        // Forward to the JSP
        result.setURL("/jsp/stats/airlineTotals.jsp");
        result.setSuccess(true);
    }
}