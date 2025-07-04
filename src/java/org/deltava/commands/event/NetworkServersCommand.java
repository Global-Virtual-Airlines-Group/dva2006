// Copyright 2010, 2012, 2014, 2017, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;
import org.deltava.beans.system.IPBlock;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.EnumUtils;

/**
 * A Web Site Command to display Online Network FSD server information.
 * @author Luke
 * @version 10.0
 * @since 3.4
 */

public class NetworkServersCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command Context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the network info
		OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("id"), OnlineNetwork.VATSIM);
		NetworkInfo info = ServInfoHelper.getInfo(net);
		ctx.setAttribute("netInfo", info, REQUEST);
		ctx.setAttribute("totalUsers", Integer.valueOf(info.getServers().stream().mapToInt(Server::getConnections).sum()), REQUEST);
		
		// Get IP location
		try {
			Map<Server, IPBlock> addrInfo = new HashMap<Server, IPBlock>();
			GetIPLocation dao = new GetIPLocation(ctx.getConnection());
			for (Server srv : info.getServers())
				addrInfo.put(srv, dao.get(srv.getAddress()));
			
			ctx.setAttribute("addrInfo", addrInfo, REQUEST);
		} catch (Exception e) {
			throw new CommandException(e);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/networkServers.jsp");
		result.setSuccess(true);
	}
}