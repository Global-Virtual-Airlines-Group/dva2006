// Copyright 2006, 2007, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mvs;

import java.sql.Connection;

import org.deltava.beans.mvs.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update MVS Channel data.
 * @author Luke
 * @version 4.0
 * @since 1.0
 */

public class ChannelCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
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
			c.setSampleRate(SampleRate.getRate(StringUtils.parse(ctx.getParameter("rate"), SampleRate.SR11K.getRate())));
			c.setMaxUsers(StringUtils.parse(ctx.getParameter("maxUsers"), 0));
			c.setIsDefault(Boolean.valueOf(ctx.getParameter("isDefault")).booleanValue());
			c.setCenter(new GeoPosition(StringUtils.parse(ctx.getParameter("lat"), 0.0), StringUtils.parse(ctx.getParameter("lng"), 0.0)));
			c.setRange(StringUtils.parse(ctx.getParameter("range"), 0));
			
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
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/mvs/mvsUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
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
	 * Callback method called when reading the channel profile.
	 * @param ctx the Command context
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}