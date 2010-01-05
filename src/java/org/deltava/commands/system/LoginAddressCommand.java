// Copyright 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.net.*;
import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display all the users logging in via a particular IP address or host name.
 * @author Luke
 * @version 2.8
 * @since 1.0
 */

public class LoginAddressCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command result
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/loginAddresses.jsp");

		// Get the address parameter
		String addr = (String) ctx.getCmdParameter(ID, null);
		if (addr == null) {
			result.setSuccess(true);
			return;
		}

		// Get the address
		boolean searchNet = Boolean.valueOf(ctx.getParameter("searchNet")).booleanValue();
		if (searchNet) {
			try {
				InetAddress ipAddr = InetAddress.getByName(addr);
				addr = ipAddr.getHostAddress();
			} catch (UnknownHostException uhe) {
				ctx.setMessage("Unknown Host - " + addr);
				result.setSuccess(true);
				return;
			}
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the network block
			GetIPLocation ipdao = new GetIPLocation(con);
			IPAddressInfo addrInfo = ipdao.get(addr);
			searchNet &= (addrInfo != null);
			ctx.setAttribute("addrInfo", addrInfo, REQUEST);
			
			// Get the Addresses
			GetLoginData sysdao = new GetLoginData(con);
			Collection<LoginAddress> addrs = searchNet ? sysdao.getLoginUsers(addr, addrInfo.getBlock()) : sysdao.getLoginUsers(addr);
			ctx.setAttribute("addrs", addrs, REQUEST);
			
			// Load the users
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(addrs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		result.setSuccess(true);
	}
}