// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.Iterator;
import java.sql.Connection;

import org.deltava.beans.cooler.Channel;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerChannelAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to maintain Discussion Forum channel profiles.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class ChannelCommand extends AbstractFormCommand {

	/**
     * Callback method called when saving the Channel.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we're creating a new channel
		String channel = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (channel == null);
		String forumName = SystemData.get("airline.forum");

		try {
			Connection con = ctx.getConnection();

			// Get the Channel profile
			Channel c = null;
			if (!isNew) {
				GetCoolerChannels dao = new GetCoolerChannels(con);
				c = dao.get(channel);
				if (c == null)
					throw notFoundException("Invalid " + forumName + " Channel - " + channel);
			} else
				c = new Channel(ctx.getParameter("newName"));
			
			// Load roles and airlines - make sure write access can read
			c.setAirlines(ctx.getParameters("airline"));
			c.setRoles(false, ctx.getParameters("readRoles"));
			c.setRoles(true, ctx.getParameters("writeRoles"));
			if (!c.getReadRoles().contains("*")) {
				for (Iterator<String> i = c.getReadRoles().iterator(); i.hasNext(); )
					c.addRole(false, i.next());
			}
			
			// Check our access
			CoolerChannelAccessControl access = new CoolerChannelAccessControl(ctx, c);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit " + forumName + " Channel");

			// Update the channel from the request
			c.setDescription(ctx.getParameter("desc"));
			c.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			c.setAllowNewPosts(Boolean.valueOf(ctx.getParameter("allowNew")).booleanValue());
			
			// Get the DAO and write the channel
			SetCoolerChannel wdao = new SetCoolerChannel(con);
			if (isNew) {
				wdao.create(c);
				ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(c, ctx.getParameter("newName"));
				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
			}
			
			// Save the chanel in the request
			ctx.setAttribute("channel", c, REQUEST);
			
			// Check if we're renaming the channel
			if (!ctx.getParameter("newName").equals(c.getName())) {
				ctx.setAttribute("isRename", Boolean.valueOf(c.getName() != null), REQUEST);
				ctx.setAttribute("newName", ctx.getParameter("newName"), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/cooler/channelUpdate.jsp");
		result.setSuccess(true);
	}

	/**
     * Callback method called when editing the Channel.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Check if we're creating a new channel
		String channel = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (channel == null);
		String forumName = SystemData.get("airline.forum");

		try {
			Connection con = ctx.getConnection();
			
			Channel c = null;
			if (!isNew) {
				// Get the DAO and the channel
				GetCoolerChannels dao = new GetCoolerChannels(con);
				c = dao.get(channel);
				if (c == null)
					throw notFoundException("Invalid " + forumName + " Channel - " + channel);
			}
			
			// Check our access
			CoolerChannelAccessControl access = new CoolerChannelAccessControl(ctx, c);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit " + forumName + " Channel");
			
			// Save channel and access controller
			ctx.setAttribute("channel", c, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/channelEdit.jsp");
		result.setSuccess(true);
	}

	/**
     * Callback method called when reading the Channel.
     * @param ctx the Command context
     */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}