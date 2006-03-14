// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.ts2.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update TeamSpeak 2 virtual server data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServerCommand extends AbstractFormCommand {
	
	private static final Logger log = Logger.getLogger(ServerCommand.class);
	
	private static final Collection<String> DEFAULT_ROLES = Arrays.asList(new String[] {"Pilot"});

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
					throw notFoundException("Invalid Server ID - " + ctx.getID());
				
				dao.setQueryMax(0);
				srv.setName(ctx.getParameter("name"));
			} else {
				srv = new Server(ctx.getParameter("name"));
			}
			
			// Get client records for the server
			GetPilot pdao = new GetPilot(con);
			ClientPilotMap srvUsers = new ClientPilotMap();
			Collection<Client> usrs = dao.getUsers(srv.getID());
			for (Iterator<Client> i = usrs.iterator(); i.hasNext(); ) {
				Client usr = i.next();
				if (usr.getUserID().startsWith(SystemData.get("airline.code"))) {
					Pilot p = pdao.getPilotByCode(usr.getPilotCode(), SystemData.get("airline.db"));
					srvUsers.add(p, usr);
				}
			}
			
			// Get client records for other servers
			Collection<Client> allUsers = dao.getUsers();
			ClientPilotMap otherPilots = new ClientPilotMap();
			for (Iterator<Client> i = allUsers.iterator(); i.hasNext(); ) {
				Client usr = i.next();
				if (usr.getUserID().startsWith(SystemData.get("airline.code"))) {
					if (!srvUsers.contains(usr.getUserID())) {
						Pilot p = pdao.getPilotByCode(usr.getPilotCode(), SystemData.get("airline.db"));
						otherPilots.add(p, usr);
					}
				}
			}
			
			// Update the bean from the request
			srv.setDescription(ctx.getParameter("desc"));
			srv.setWelcomeMessage(ctx.getParameter("msg"));
			srv.setMaxUsers(Integer.parseInt(ctx.getParameter("maxUsers")));
			srv.setPassword(ctx.getParameter("pwd"));
			srv.setPort(Integer.parseInt(ctx.getParameter("port")));
			srv.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			srv.setACARSOnly(Boolean.valueOf(ctx.getParameter("isACARS")).booleanValue());
			srv.setRoles(CollectionUtils.loadList(ctx.getRequest().getParameterValues("securityRoles"), DEFAULT_ROLES));
			
			// Build messages collection
			Collection<String> msgs = new ArrayList<String>();
			
			// Start the transaction
			ctx.startTX();
			
			// Get the write DAO and save the virtual server
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.write(srv);
			
			// Determine what users to remove from the server
			Collection<String> removeIDs = new HashSet<String>();
			for (Iterator<Pilot> i = srvUsers.getPilots().iterator(); i.hasNext(); ) {
				Pilot p = i.next();
				if (!RoleUtils.hasAccess(p.getRoles(), srv.getRoles())) {
					msgs.add("Removed " + p.getName() + " " + p.getPilotCode() + " from TS2 Server " + srv.getName());
					log.warn("Removing " + p.getName() + " " + p.getPilotCode() + " from TS2 Server " + srv.getName());
					removeIDs.add(p.getPilotCode());
				}
			}
			
			// Update the users if the roles change
			wdao.removeUsers(srv, removeIDs);
			
			// Determine what users to add to the server
			for (Iterator<Pilot> i = otherPilots.getPilots().iterator(); i.hasNext(); ) {
				Pilot p = i.next();
				if (RoleUtils.hasAccess(p.getRoles(), srv.getRoles())) {
					msgs.add("Added " + p.getName() + " " + p.getPilotCode() + " to TS2 Server " + srv.getName());
					log.warn("Adding " + p.getName() + " " + p.getPilotCode() + " to TS2 Server " + srv.getName());
					Client usr = otherPilots.getClient(p.getPilotCode());
					wdao.addToServer(usr, srv.getID());
				}
			}
			
			// Commit transaction
			ctx.commitTX();
			
			// Reload the system model
			SystemData.add("ts2Servers", dao.getServers());
			
			// Save the server and msgs in the request
			ctx.setAttribute("server", srv, REQUEST);
			ctx.setAttribute("msgs", msgs, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/ts2Update.jsp");
		result.setType(CommandResult.REQREDIRECT);
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
					throw notFoundException("Invalid Server ID - " + ctx.getID());
				
				ctx.setAttribute("server", srv, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get security roles
		@SuppressWarnings("unchecked")
		Collection<String> roles = (Collection<String>) SystemData.getObject("security.roles");
		roles.add("Pilot");
		ctx.setAttribute("roles", roles, REQUEST);

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