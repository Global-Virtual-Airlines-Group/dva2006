// Copyright 2013, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.apache.axis.utils.StringUtils;
import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.GetAirCharts;
import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to refresh external approach charts. 
 * @author Luke
 * @version 7.2
 * @since 5.1
 */

public class ExternalChartRefreshCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		Airport a = SystemData.getAirport(ctx.getParameter("id"));
		if (a == null)
			throw notFoundException("Invalid Airport - " + ctx.getParameter("id"));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the charts for this airport and filter out non-external
			GetChart cdao = new GetChart(con);
			Map<String, ExternalChart> oldCharts = new HashMap<String, ExternalChart>();
			for (Chart c : cdao.getCharts(a)) {
				if (c.getIsExternal()) {
					ExternalChart ec = (ExternalChart) c;
					String id = StringUtils.isEmpty(ec.getExternalID()) ? String.valueOf(ec.getID()) : ec.getExternalID();
					oldCharts.put(id, ec);
				}
			}
			
			// Load AirCharts and map internal IDs
			GetAirCharts acdao = new GetAirCharts();
			Map<String, ExternalChart> newCharts = CollectionUtils.createMap(acdao.getCharts(a), ExternalChart::getExternalID);
			for (Map.Entry<String, ExternalChart> me : newCharts.entrySet()) {
				ExternalChart oec = oldCharts.get(me.getKey());
				if (oec != null)
					me.getValue().setID(oec.getID());
			}
			
			// Delete old external charts
			ctx.startTX();
			SetChart wdao = new SetChart(con);
			for (Chart c : oldCharts.values())
				wdao.delete(c.getID());
			
			// Add new charts
			for (ExternalChart ec : newCharts.values())
				wdao.write(ec);
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the command
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("charts", null, a.getICAO());
		result.setSuccess(true);
	}
}