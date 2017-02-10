// Copyright 2006, 2007, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 7.2
 * @since 1.0
 */

public class ServerCommand extends AbstractFormCommand {
	
	private static final Logger log = Logger.getLogger(ServerCommand.class);
	
	private static final Collection<String> HR_ROLES = Arrays.asList("HR");
	private static final Collection<String> PILOT_ROLES = Arrays.asList("Pilot");

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
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
				
				srv.setName(ctx.getParameter("name"));
			} else {
				srv = new Server(ctx.getParameter("name"));
			}
			
			// Get client records for the server
			GetPilot pdao = new GetPilot(con);
			ClientPilotMap srvUsers = new ClientPilotMap();
			Collection<Client> usrs = dao.getUsersByServer(srv.getID());
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
			srv.setRoles(ServerAccess.ACCESS, ctx.getParameters("accessRoles", PILOT_ROLES));
			srv.setRoles(ServerAccess.VOICE, ctx.getParameters("voxRoles", PILOT_ROLES));
			srv.setRoles(ServerAccess.ADMIN, ctx.getParameters("adminRoles", HR_ROLES));
			srv.setRoles(ServerAccess.OPERATOR, ctx.getParameters("opRoles", HR_ROLES));
			
			// Build messages collection
			Collection<String> msgs = new ArrayList<String>();
			
			// Start the transaction
			ctx.startTX();
			
			// Get the write DAO and save the virtual server
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.write(srv);
			
			// Get the access roles
			Collection<String> accessRoles = srv.getRoles().get(ServerAccess.ACCESS);
			
			// Determine what users to remove from the server
			Collection<Integer> removeIDs = new HashSet<Integer>();
			for (Pilot p : srvUsers.getPilots()) {
				if ((!RoleUtils.hasAccess(p.getRoles(), accessRoles)) || (p.getStatus() != Pilot.ACTIVE) || (p.getNoVoice())) {
					msgs.add("Removed " + p.getName() + " " + p.getPilotCode() + " from TS2 Server " + srv.getName());
					log.warn("Removing " + p.getName() + " " + p.getPilotCode() + " from TS2 Server " + srv.getName());
					removeIDs.add(Integer.valueOf(p.getID()));
				}
			}
			
			// Update the users if the roles change
			wdao.removeUsers(srv, removeIDs);
			
			// Determine what users to add to the server
			Collection<Client> addUsrs = new HashSet<Client>();
			for (Pilot p : otherPilots.getPilots()) {
				if ((RoleUtils.hasAccess(p.getRoles(), accessRoles)) && (p.getStatus() == Pilot.ACTIVE) && (!p.getNoVoice())) {
					msgs.add("Added " + p.getName() + " " + p.getPilotCode() + " to TS2 Server " + srv.getName());
					log.warn("Adding " + p.getName() + " " + p.getPilotCode() + " to TS2 Server " + srv.getName());
					Client usr = otherPilots.getClient(p.getPilotCode());
					
					// Build the new client record
					Client c = new Client(p.getPilotCode());
					c.setID(p.getID());
					c.setPassword((usr == null) ? "dummy" : usr.getPassword());
					c.addChannels(srv);
					c.setServerID(srv.getID());
					c.setAutoVoice(RoleUtils.hasAccess(p.getRoles(), srv.getRoles().get(ServerAccess.VOICE)));
					c.setServerOperator(RoleUtils.hasAccess(p.getRoles(), srv.getRoles().get(ServerAccess.OPERATOR)));
					c.setServerAdmin(RoleUtils.hasAccess(p.getRoles(), srv.getRoles().get(ServerAccess.ADMIN)));
					addUsrs.add(c);
				}
			}
			
			// If we have clients to add, write them and commit
			wdao.write(addUsrs);
			ctx.commitTX();
			
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
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
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
		Collection<String> roles = new TreeSet<String>((Collection<String>) SystemData.getObject("security.roles"));
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
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}