// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
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
	
	private static final Logger log = Logger.getLogger(ServerCommand.class);
	
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
			
			// Get client records for the server
			GetPilot pdao = new GetPilot(con);
			Collection<User> usrs = dao.getUsers(srv.getID());
			Collection<String> pilotCodes = new HashSet<String>();
			Map<User, Pilot> pilots = new HashMap<User, Pilot>();
			for (Iterator<User> i = usrs.iterator(); i.hasNext(); ) {
				User usr = i.next();
				if (usr.getUserID().startsWith(SystemData.get("airline.code"))) {
					Pilot p = pdao.getPilotByCode(usr.getPilotCode(), SystemData.get("airline.db"));
					if (p != null) {
						pilotCodes.add(usr.getUserID());
						pilots.put(usr, p);
					}
				}
			}
			
			// Get client records for other servers
			Map<User, Pilot> otherPilots = new HashMap<User, Pilot>();
			for (Iterator<User> i = dao.getUsers().iterator(); i.hasNext(); ) {
				User usr = i.next();
				if (!pilotCodes.contains(usr.getUserID())) {
					Pilot p = pdao.getPilotByCode(usr.getPilotCode(), SystemData.get("airline.db"));
					if ((p != null) && (!otherPilots.values().contains(p)))
						otherPilots.put(usr, p);
				}
			}
			
			// Update the bean from the request
			srv.setDescription(ctx.getParameter("desc"));
			srv.setWelcomeMessage(ctx.getParameter("msg"));
			srv.setMaxUsers(Integer.parseInt(ctx.getParameter("maxUsers")));
			srv.setPassword(ctx.getParameter("pwd"));
			srv.setPort(Integer.parseInt(ctx.getParameter("port")));
			srv.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			srv.setRoles(CollectionUtils.loadList(ctx.getRequest().getParameterValues("securityRole"), DEFAULT_ROLES));
			
			// Build messages collection
			Collection<String> msgs = new ArrayList<String>();
			
			// Start the transaction
			ctx.startTX();
			
			// Get the write DAO and save the virtual server
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.write(srv);
			
			// Determine what users to remove from the server
			Collection<String> removeIDs = new HashSet<String>();
			for (Iterator<User> i = pilots.keySet().iterator(); i.hasNext(); ) {
				User usr = i.next();
				Pilot p = pilots.get(usr);
				if (CollectionUtils.hasMatches(srv.getRoles(), p.getRoles()) == 0) {
					msgs.add("Removed " + p.getName() + " " + p.getPilotCode() + " from TS2 Server " + srv.getName());
					log.warn("Removing " + p.getName() + " " + p.getPilotCode() + " from TS2 Server " + srv.getName());
					removeIDs.add(usr.getUserID());
				}
			}
			
			// Update the users if the roles change
			wdao.removeUsers(srv, removeIDs);
			
			// Determine what users to add to the server
			for (Iterator<User> i = otherPilots.keySet().iterator(); i.hasNext(); ) {
				User usr = i.next();
				Pilot p = pilots.get(usr);
				if (CollectionUtils.hasMatches(srv.getRoles(), p.getRoles()) > 0) {
					msgs.add("Added " + p.getName() + " " + p.getPilotCode() + " to TS2 Server " + srv.getName());
					log.warn("Adding " + p.getName() + " " + p.getPilotCode() + " to TS2 Server " + srv.getName());
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