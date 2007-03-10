// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ts2.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display active TeamSpeak 2 servers.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ActiveServersCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if TS2 enabled
		if (!SystemData.getBoolean("airline.voice.ts2.enabled"))
			throw notFoundException("TeamSpeak 2 support disabled");
		
		Collection<Server> srvs = null;
		Map<Integer, Client> clients = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get our credentials and active servers
			GetTS2Data dao = new GetTS2Data(con);
			clients = CollectionUtils.createMap(dao.getUsers(ctx.getUser().getID()), "serverID");
			srvs = dao.getServers();
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Check our access
		Collection<Server> servers = new ArrayList<Server>();
		for (Iterator i = srvs.iterator(); i.hasNext(); ) {
			Server srv = (Server) i.next();
			Client usr = clients.get(new Integer(srv.getID()));
			
			// Make sure we can access the server
			if ((usr != null) && (RoleUtils.hasAccess(ctx.getRoles(), srv.getRoles().get(Server.ACCESS))))
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