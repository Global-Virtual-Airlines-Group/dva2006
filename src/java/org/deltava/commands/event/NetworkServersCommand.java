// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.io.*;
import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;
import org.deltava.beans.system.IPAddressInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display VATSIM/IVAO FSD server information.
 * @author Luke
 * @version 3.4
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
		
		// Get the network name
		OnlineNetwork net = OnlineNetwork.valueOf(SystemData.get("online.default_network"));
		try {
			net = OnlineNetwork.valueOf(ctx.getParameter("id").toUpperCase());
		} catch (Exception e) {
			// empty
		}

		try {
			// Get the network info
			File f = new File(SystemData.get("online." + net.toString().toLowerCase() + ".local.info"));
			GetServInfo sidao = new GetServInfo(new FileInputStream(f));
			NetworkInfo info = sidao.getInfo(net); 
			ctx.setAttribute("netInfo", info, REQUEST);
			
			// Get IP location
			Map<Server, IPAddressInfo> addrInfo = new HashMap<Server, IPAddressInfo>();
			GetIPLocation dao = new GetIPLocation(ctx.getConnection());
			for (Server srv : info.getServers())
				addrInfo.put(srv, dao.get(srv.getAddress()));
			
			ctx.setAttribute("addrInfo", addrInfo, REQUEST);
		} catch (Exception e) {
			throw new CommandException(e);
		} finally {
			ctx.release();
		}
		
		// Load the network names and save in the request
		ctx.setAttribute("networks", SystemData.getObject("online.networks"), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/event/networkServers.jsp");
		result.setSuccess(true);
	}
}