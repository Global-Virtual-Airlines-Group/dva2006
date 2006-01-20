// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ts2.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update TeamSpeak 2 virtual server data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServerCommand extends AbstractFormCommand {
	
	private static final Collection<String> DEFAULT_ROLES = Arrays.asList(new String[] {"Pillot"});

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Check if we're creating a new server
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the server profile
			Server srv = null;
			GetTS2Data dao = new GetTS2Data(con);
			if (!isNew) {
				srv = dao.getServer(ctx.getID());
				if (srv == null)
					throw new CommandException("Invalid Server ID - " + ctx.getID());
				
				srv.setName(ctx.getParameter("name"));
			} else {
				srv = new Server(ctx.getParameter("name"));
			}
			
			// Update the bean from the request
			srv.setDescription(ctx.getParameter("desc"));
			srv.setWelcomeMessage(ctx.getParameter("msg"));
			srv.setMaxUsers(Integer.parseInt(ctx.getParameter("maxUsers")));
			srv.setPassword(ctx.getParameter("pwd"));
			srv.setPort(Integer.parseInt(ctx.getParameter("port")));
			srv.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			
			// Update the server roles
			srv.setRoles(CollectionUtils.loadList(ctx.getRequest().getParameterValues("securityRole"), DEFAULT_ROLES));
			
			// TODO Update the users if the roles change
			
			// Get the write DAO and save the virtual server
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.write(srv);
			
			// Reload the system model
			SystemData.add("ts2Servers", dao.getServers());
			
			// Save the server in the request
			ctx.setAttribute("server", srv, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/ts2Update.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			// Get the server
			if (ctx.getID() != 0) {
				Connection con = ctx.getConnection();
				
				GetTS2Data dao = new GetTS2Data(con);
				Server srv = dao.getServer(ctx.getID());
				if (srv == null)
					throw new CommandException("Invalid Server ID - " + ctx.getID());
				
				ctx.setAttribute("server", srv, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/serverEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the server profile. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}