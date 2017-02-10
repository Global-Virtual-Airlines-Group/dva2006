// Copyright 2006, 2007, 2009, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.util.*;

import org.deltava.beans.ts2.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display active TeamSpeak 2 servers.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class ActiveServersCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if TS2 enabled
		if (!SystemData.getBoolean("airline.voice.ts2.enabled"))
			throw notFoundException("TeamSpeak 2 support disabled");
		
		// Get our credentials and active servers
		Collection<Server> srvs = null;
		Map<Integer, Client> clients = null;
		try {
			GetTS2Data dao = new GetTS2Data(ctx.getConnection());
			clients = CollectionUtils.createMap(dao.getUsers(ctx.getUser().getID()), Client::getServerID);
			srvs = dao.getServers();
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Check our access
		Collection<Server> servers = new ArrayList<Server>();
		for (Server srv : srvs) {
			Client usr = clients.get(Integer.valueOf(srv.getID()));
			
			// Make sure we can access the server
			if ((usr != null) && (RoleUtils.hasAccess(ctx.getRoles(), srv.getRoles().get(ServerAccess.ACCESS))))
				servers.add(srv);
		}
		
		// Save the server/credential list
		ctx.setAttribute("ts2servers", servers, REQUEST);
		ctx.setAttribute("clientInfo", clients, REQUEST);
		
		// Forwrd to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/activeServers.jsp");
		result.setSuccess(true);
	}
}