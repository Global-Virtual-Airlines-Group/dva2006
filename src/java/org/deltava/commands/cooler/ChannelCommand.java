// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.cooler.Channel;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerChannelAccessControl;

import org.deltava.util.BeanUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to maintain Discussion Forum channel profiles.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class ChannelCommand extends AbstractAuditFormCommand {

	/**
     * Callback method called when saving the Channel.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we're creating a new channel
		String channel = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (channel == null);
		String forumName = SystemData.get("airline.forum");

		try {
			Connection con = ctx.getConnection();

			// Get the Channel profile
			Channel c = null; Channel oc = null;
			if (!isNew) {
				GetCoolerChannels dao = new GetCoolerChannels(con);
				c = dao.get(channel);
				if (c == null)
					throw notFoundException("Invalid " + forumName + " Channel - " + channel);
				
				oc = BeanUtils.clone(c);
			} else
				c = new Channel(ctx.getParameter("newName"));
			
			// Load roles and airlines - make sure write access can read
			c.setAirlines(ctx.getParameters("airline"));
			c.setRoles(Channel.InfoType.READ, ctx.getParameters("readRoles"));
			c.setRoles(Channel.InfoType.WRITE, ctx.getParameters("writeRoles"));
			c.setRoles(Channel.InfoType.NOTIFY, ctx.getParameters("notifyRoles"));
			if (!c.getWriteRoles().contains("*")) {
				for (String r : c.getReadRoles())
					c.addRole(Channel.InfoType.READ, r);
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
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oc, c);
			AuditLog ae = AuditLog.create(c, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Get the DAO and write the channel
			SetCoolerChannel wdao = new SetCoolerChannel(con);
			if (isNew) {
				wdao.create(c);
				ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(c, ctx.getParameter("newName"));
				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
			}
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
			
			// Save the chanel in the request
			ctx.setAttribute("channel", c, REQUEST);
			
			// Check if we're renaming the channel
			if (!ctx.getParameter("newName").equals(c.getName())) {
				ctx.setAttribute("isRename", Boolean.valueOf(c.getName() != null), REQUEST);
				ctx.setAttribute("newName", ctx.getParameter("newName"), REQUEST);
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/cooler/channelUpdate.jsp");
		result.setSuccess(true);
	}

	/**
     * Callback method called when editing the Channel.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Check if we're creating a new channel
		String channel = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (channel == null);
		String forumName = SystemData.get("airline.forum");

		try {
			Channel c = null;
			if (!isNew) {
				GetCoolerChannels dao = new GetCoolerChannels(ctx.getConnection());
				c = dao.get(channel);
				if (c == null)
					throw notFoundException("Invalid " + forumName + " Channel - " + channel);
				
				readAuditLog(ctx, c);
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
		
		// Get security roles
		Collection<Object> allRoles = new TreeSet<Object>((Collection<?>) SystemData.getObject("security.roles"));
		allRoles.add("Pilot");
		ctx.setAttribute("roles", allRoles, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/channelEdit.jsp");
		result.setSuccess(true);
	}

	/**
     * Callback method called when reading the Channel.
     * @param ctx the Command context
     */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}