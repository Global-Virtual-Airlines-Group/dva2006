// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.Arrays;
import java.sql.Connection;

import org.deltava.beans.cooler.Channel;

import org.deltava.commands.*;

import org.deltava.dao.GetCoolerChannels;
import org.deltava.dao.SetCoolerChannel;
import org.deltava.dao.DAOException;

import org.deltava.security.command.CoolerChannelAccessControl;

/**
 * A Web Site Command to maintain Water Cooler channel profiles.
 * @author Luke
 * @version 1.0
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

		try {
			Connection con = ctx.getConnection();

			// Get the Channel profile
			Channel c = null;
			if (!isNew) {
				GetCoolerChannels dao = new GetCoolerChannels(con);
				c = dao.get(channel);
				if (c == null)
					throw new CommandException("Invalid Water Cooler Channel - " + channel);
			} else {
				c = new Channel(ctx.getParameter("newName"));
			}
			
			// Check our access
			CoolerChannelAccessControl access = new CoolerChannelAccessControl(ctx, c);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Water Cooler Channel");

			// Update the channel from the request
			c.setDescription(ctx.getParameter("desc"));
			c.setActive("1".equals(ctx.getParameter("active")));
			
			// Load the roles and airlines
			c.setRoles(Arrays.asList(ctx.getRequest().getParameterValues("securityRoles")));
			c.setAirlines(Arrays.asList(ctx.getRequest().getParameterValues("airline")));
			
			// Get the DAO and write the channel
			SetCoolerChannel wdao = new SetCoolerChannel(con);
			if (isNew) {
				wdao.create(c);
				ctx.setAttribute("isCreate", Boolean.valueOf(true), REQUEST);
			} else {
				wdao.update(c, ctx.getParameter("newName"));
				ctx.setAttribute("isUpdate", Boolean.valueOf(true), REQUEST);
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

		try {
			Connection con = ctx.getConnection();
			
			Channel c = null;
			if (!isNew) {
				// Get the DAO and the channel
				GetCoolerChannels dao = new GetCoolerChannels(con);
				c = dao.get(channel);
				if (c == null)
					throw new CommandException("Invalid Water Cooler Channel - " + channel);
			}
			
			// Check our access
			CoolerChannelAccessControl access = new CoolerChannelAccessControl(ctx, c);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Water Cooler Channel");
			
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
     * Callback method called when reading the Channel. <i>NOT IMPLEMENTED</i>
     * @param ctx the Command context
     * @throws UnsupportedOperationException always
     */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}