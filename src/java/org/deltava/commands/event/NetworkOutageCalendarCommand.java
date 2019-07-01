// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.NetworkOutage;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Online Network data feed outages. 
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class NetworkOutageCalendarCommand extends AbstractCalendarCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command Context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		CalendarContext cctx = initCalendar(ctx);
		try {
			GetOnlineTrack otdao = new GetOnlineTrack(ctx.getConnection());
			Collection<OnlineNetwork> networks = otdao.getFetchNetworks(cctx.getStartDate(), cctx.getEndDate());
			
			Collection<NetworkOutage> outages = new TreeSet<NetworkOutage>();
			for (OnlineNetwork net : networks)
				outages.addAll(NetworkOutage.calculate(net, otdao.getFetches(net, cctx.getStartDate(), cctx.getEndDate()), 150));
			
			ctx.setAttribute("outages", outages, REQUEST);
			ctx.setAttribute("networks", networks, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL((cctx.getDays() == 7) ? "/jsp/event/outageW.jsp" : "/jsp/event/outageM.jsp");
		result.setSuccess(true);
	}
}