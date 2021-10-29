// Copyright 2010, 2011, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mvs;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.mvs.*;
import org.deltava.beans.mvs.Channel.Access;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Site Command to update MVS Channel data.
 * @author Luke
 * @version 10.2
 * @since 4.0
 */

public class ChannelCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we're creating a new channel
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the channel profile
			Channel c = null;
			GetMVSChannel dao = new GetMVSChannel(con);
			if (!isNew) {
				c = dao.get(ctx.getID());
				if (c == null)
					throw notFoundException("Invalid Channel - " + ctx.getID());
				
				c.setName(ctx.getParameter("name"));
			} else
				c = new Channel(ctx.getParameter("name"));
			
			// Update the bean from the request
			c.setDescription(ctx.getParameter("desc"));
			c.setSampleRate(SampleRate.getRate(StringUtils.parse(ctx.getParameter("rate"), SampleRate.SR8K.getRate())));
			c.setMaxUsers(StringUtils.parse(ctx.getParameter("maxUsers"), 0));
			c.setRange(StringUtils.parse(ctx.getParameter("range"), 0));
			
			// Update the roles
			c.clearRoles();
			c.addRoles(Access.VIEW, ctx.getParameters("joinRoles"));
			c.addRoles(Access.TALK, ctx.getParameters("talkRoles"));
			c.addRoles(Access.TALK_IF_PRESENT, ctx.getParameters("dynTalkRoles"));
			c.addRoles(Access.ADMIN, ctx.getParameters("adminRoles"));
			
			// Set airlines
			Collection<String> alCodes = ctx.getParameters("airline");
			if (alCodes != null) {
				c.getAirlines().clear();
				for (String alCode : alCodes)
					c.addAirline(SystemData.getApp(alCode));
			}
			
			// Get the write DAO and save the channel
			SetMVSChannel wdao = new SetMVSChannel(con);
			wdao.write(c);
			
			// Save the channel in the request
			ctx.setAttribute("channel", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
		EventDispatcher.send(new SystemEvent(EventType.MVS_RELOAD));
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/mvs/channelUpdate.jsp");
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
			GetMVSChannel dao = new GetMVSChannel(ctx.getConnection());
			Channel c = dao.get(ctx.getID());
			if ((c == null) && (ctx.getID() != 0))
				throw notFoundException("Invalid Channel - " + ctx.getID());
				
			ctx.setAttribute("channel", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/mvs/channelEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the channel profile - redirects to edit.
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}