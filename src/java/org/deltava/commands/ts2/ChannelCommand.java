// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.sql.Connection;

import org.deltava.beans.ts2.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update TeamSpeak 2 channel data.
 * @author Luke
 * @version 1.0
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
			GetTS2Data dao = new GetTS2Data(con);
			if (!isNew) {
				c = dao.getChannel(ctx.getID());
				if (c == null)
					throw notFoundException("Invalid Channel - " + ctx.getID());
				
				c.setName(ctx.getParameter("name"));
			} else
				c = new Channel(ctx.getParameter("name"));
			
			// Update the bean from the request
			c.setDescription(ctx.getParameter("desc"));
			c.setTopic(ctx.getParameter("topic"));
			c.setMaxUsers(StringUtils.parse(ctx.getParameter("maxUsers"), 1));
			c.setPassword(ctx.getParameter("pwd"));
			c.setServerID(StringUtils.parseHex(ctx.getParameter("server")));
			c.setModerated(Boolean.valueOf(ctx.getParameter("isModerated")).booleanValue());
			c.setDefault(Boolean.valueOf(ctx.getParameter("isDefault")).booleanValue());
			c.setCodec(StringUtils.parse(ctx.getParameter("codec"), 0));
			
			// Start a transaction
			ctx.startTX();

			// Get the write DAO and save the channel
			SetTS2Data wdao = new SetTS2Data(con);
			if (isNew)
				wdao.write(c);
			else
				wdao.update(c);
			
			// Check for a default channel
			wdao.setDefault(c.getServerID());
			
			// Commit the transaction
			ctx.commitTX();

			// Save the channel in the request
			ctx.setAttribute("channel", c, REQUEST);
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
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			// Get the channel
			if (ctx.getID() != 0) {
				Connection con = ctx.getConnection();
				
				// Get the DAO and the Channel
				GetTS2Data dao = new GetTS2Data(con);
				Channel c = dao.getChannel(ctx.getID());
				if (c == null)
					throw notFoundException("Invalid Channel - " + ctx.getID());
				
				// Get the server
				Server srv = dao.getServer(c.getServerID());
				
				// Save in the request
				ctx.setAttribute("channel", c, REQUEST);
				ctx.setAttribute("server", srv, REQUEST);
				ctx.setAttribute("servers", dao.getServers(), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the codec list
		ctx.setAttribute("codecs", Channel.CODECS, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/channelEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the channel profile. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}