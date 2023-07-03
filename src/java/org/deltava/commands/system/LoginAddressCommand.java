// Copyright 2007, 2008, 2009, 2010, 2012, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display all the users logging in via a particular IP address or host name.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class LoginAddressCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the context and command result
		ViewContext<LoginAddress> vctx = initView(ctx, LoginAddress.class);
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/loginAddresses.jsp");

		// Get the address parameter
		String addr = (String) ctx.getCmdParameter(ID, null);
		if (addr == null) {
			result.setSuccess(true);
			return;
		}

		// Get the address and search type
		boolean searchNet = Boolean.parseBoolean(ctx.getParameter("searchNet"));
		String resolvedAddr = NetworkUtils.getByName(addr);
		try {
			Connection con = ctx.getConnection();
			
			// Find the address via previous logins
			if (StringUtils.isEmpty(resolvedAddr)) {
				GetLoginData lddao = new GetLoginData(con);
				LoginAddress laddr = lddao.getAddresses(addr).stream().findFirst().orElse(null);
				if (laddr == null) {
					ctx.setMessage("Unknown Host - " + addr);
					result.setSuccess(true);
					return;
				}
				
				addr = laddr.getRemoteAddr();
			}
			
			// Get the network block
			GetIPLocation ipdao = new GetIPLocation(con);
			IPBlock addrInfo = ipdao.get(addr);
			searchNet &= (addrInfo != null);
			ctx.setAttribute("addrInfo", addrInfo, REQUEST);
			
			// Get the Addresses
			GetLoginData sysdao = new GetLoginData(con);
			sysdao.setQueryStart(vctx.getStart());
			sysdao.setQueryMax(vctx.getCount());
			Collection<LoginAddress> addrs = searchNet ? sysdao.getLoginUsers(addr, addrInfo) : sysdao.getLoginUsers(addr);
			vctx.setResults(addrs);
			
			// Load the users
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(addrs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set search attribute and forward to the JSP
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		result.setSuccess(true);
	}
}