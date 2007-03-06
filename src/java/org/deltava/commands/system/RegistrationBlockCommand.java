// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.deltava.beans.system.RegistrationBlock;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.NetworkUtils;

/**
 * A Web Site Command to update Registration Block entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RegistrationBlockCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving a Registration Block.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			RegistrationBlock rb = null;
			if (ctx.getID() != 0) {
				// Get the DAO and the block entry
				GetSystemData dao = new GetSystemData(con);
				rb = dao.getBlock(ctx.getID());
				if (rb == null)
					throw notFoundException("Invalid Registration Block entry - " + ctx.getID());

				rb.setName(ctx.getParameter("firstName"), ctx.getParameter("lastName"));
			} else
				rb = new RegistrationBlock(ctx.getParameter("firstName"), ctx.getParameter("lastName"));

			// Copy data from the request
			rb.setAddress(NetworkUtils.pack(ctx.getParameter("addr")), NetworkUtils.pack(ctx.getParameter("netMask")));
			rb.setHostName(ctx.getParameter("hostName"));
			rb.setComments(ctx.getParameter("comments"));
			rb.setHasUserFeedback(Boolean.valueOf(ctx.getParameter("hasFeedback")).booleanValue());
			rb.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());

			// Save the bean
			SetSystemData wdao = new SetSystemData(con);
			wdao.write(rb);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("regblocks.do");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing a Registration Block.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		if (ctx.getID() != 0) {
			try {
				Connection con = ctx.getConnection();

				// Get the DAO and the block entry
				GetSystemData dao = new GetSystemData(con);
				RegistrationBlock rb = dao.getBlock(ctx.getID());
				if (rb == null)
					throw notFoundException("Invalid Registration Block entry - " + ctx.getID());

				// Save in the request
				ctx.setAttribute("block", rb, REQUEST);
				ctx.setAttribute("addr", NetworkUtils.format(NetworkUtils.convertIP(rb.getAddress())), REQUEST);
				ctx.setAttribute("mask", NetworkUtils.format(NetworkUtils.convertIP(rb.getNetMask())), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/regBlockEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when saving a Registration Block. <i>This redirects to the edit callback method.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 * @see RegistrationBlockCommand#execEdit(CommandContext)
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}