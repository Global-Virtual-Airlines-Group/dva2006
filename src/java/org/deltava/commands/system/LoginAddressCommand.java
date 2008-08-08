// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.system.LoginAddress;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to display all the users logging in via a particular IP address or host name.
 * @author Luke
 * @version 2.2
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

		// Get the address parameter
		String addr = (String) ctx.getCmdParameter(ID, null);
		if (addr == null) {
			result.setURL("/jsp/admin/loginAddresses.jsp");
			result.setSuccess(true);
			return;
		}

		// Determine if we are searching by host name or address
		int netMask = -1;
		if (!StringUtils.isEmpty(ctx.getParameter("mask1")) && (!addr.contains("%"))) {
			int mask1 = StringUtils.parse(ctx.getParameter("mask1"), 0) & 0xFF;
			int mask2 = StringUtils.parse(ctx.getParameter("mask2"), 0) & 0xFF;
			int mask3 = StringUtils.parse(ctx.getParameter("mask3"), 0) & 0xFF;
			int mask4 = StringUtils.parse(ctx.getParameter("mask4"), 0) & 0xFF;
			netMask = (mask1 << 24) + (mask2 << 16) + (mask3 << 8) + mask4;
		}
		
		// Special case for IP lookups in links
		if ("net".equals(ctx.getCmdParameter(OPERATION, null)))
			netMask = 0xFFFFFF00;
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Addresses
			GetLoginData sysdao = new GetLoginData(con);
			Collection<LoginAddress> addrs = (netMask == -1) ? sysdao.getLoginUsers(addr) : sysdao.getLoginUsers(addr, netMask);
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
		result.setURL("/jsp/admin/loginAddresses.jsp");
		result.setSuccess(true);
	}
}