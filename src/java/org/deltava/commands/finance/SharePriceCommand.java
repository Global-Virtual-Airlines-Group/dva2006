// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.finance;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.finance.SharePrice;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to edit annual share prices. 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class SharePriceCommand extends AbstractAuditFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get any existing data
			GetFinance fdao = new GetFinance(con);
			SharePrice sp = fdao.getSharePrice(ctx.getID());
			SharePrice osp = BeanUtils.clone(sp);
				
			// Build from parameters
			double price = StringUtils.parse(ctx.getParameter("price"), 0.0d);
			sp = new SharePrice(ctx.getID(), (int)(price * 100));
			sp.setCount(StringUtils.parse(ctx.getParameter("count"), 0));
			sp.setActive(Boolean.parseBoolean(ctx.getParameter("active")));
				
			// Build the audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(osp, sp);
			AuditLog ae = AuditLog.create(sp, delta, ctx.getUser().getID());

			// Start transaction
			ctx.startTX();

			// Write to the database
			SetFinance fwdao = new SetFinance(con);
			fwdao.write(sp);
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();

			// Save in request
			ctx.setAttribute("sharePrice", sp, REQUEST);
			ctx.setAttribute("isSharePrice", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/finUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the data
			GetFinance fdao = new GetFinance(con);
			SharePrice sp = fdao.getSharePrice(ctx.getID());
			readAuditLog(ctx, sp);
			ctx.setAttribute("sp", sp, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/sharePriceEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}