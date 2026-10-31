// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.finance;

import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.UserData;
import org.deltava.beans.finance.CostEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update cost ledger entries 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class CostEntryCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Populate from the request
		double amt = StringUtils.parse(ctx.getParameter("amount"), 0.0);
		Instant dt = StringUtils.parseInstant(ctx.getParameter("date"), "MM/dd/yyyy");
		CostEntry ce = new CostEntry(ctx.getParameter("category"), dt, (int)(amt * 100));
		ce.setID(ctx.getID());
		ce.setDescription(ctx.getParameter("desc"));
		
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			GetFinance fdao = new GetFinance(con);
			if (!isNew) {
				CostEntry c2 = fdao.getEntry(ctx.getID());
				if (c2 == null)
					throw notFoundException("Invalid Cost Ledger Entry", ctx.getID());
			}
			
			// Write the entry
			SetFinance fwdao = new SetFinance(con);
			fwdao.write(ce);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("entry", ce, REQUEST);
		ctx.setAttribute("isCreated", Boolean.valueOf(isNew), REQUEST);
		ctx.setAttribute("isUpdated", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/costUpdate.jsp");
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
			
			// Get the categories and the entry
			GetFinance fdao = new GetFinance(con);
			ctx.setAttribute("categories", fdao.getCategories(), REQUEST);
			if (ctx.getID() != 0) {
				CostEntry ce = fdao.getEntry(ctx.getID());
				if (ce == null)
					throw notFoundException("Invalid Cost Ledger Entry", ctx.getID());
				
				ctx.setAttribute("entry", ce, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/costEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			GetFinance fdao = new GetFinance(con);
			CostEntry ce = fdao.getEntry(ctx.getID());
			if (ce == null)
				throw notFoundException("Invalid Cost Ledger Entry", ctx.getID());
			
			// Get the author
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(ce.getAuthorID());
			ctx.setAttribute("author", pdao.get(ud), REQUEST);
			
			// Save in request
			ctx.setAttribute("entry", ce, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/finance/costView.jsp");
		result.setSuccess(true);
	}
}