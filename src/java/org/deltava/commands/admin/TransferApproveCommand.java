// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.TransferRequest;

import org.deltava.comparators.RankComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.TransferAccessControl;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to Approve equipment program Transfers.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransferApproveCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Create the Message context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Transfer Request
			GetTransferRequest txdao = new GetTransferRequest(con);
			TransferRequest txreq = txdao.get(ctx.getID());
			if (txreq == null)
				throw new CommandException("Invalid Transfer Request - " + ctx.getID());

			// Check our access
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();
			if (!access.getCanApprove())
				throw new CommandSecurityException("Cannot approve Transfer Request");

			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			usr = pdao.get(txreq.getID());
			if (usr == null)
				throw new CommandException("Invalid Pilot - " + txreq.getID());

			// Get the old/new Equipment Programs
			String eqType = ctx.getParameter("eqType");
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType currentEQ = eqdao.get(usr.getEquipmentType());
			EquipmentType newEQ = eqdao.get(eqType);
			if ((currentEQ == null) || (newEQ == null))
				throw new CommandException("Invalid Equipment Program - " + eqType + " / " + usr.getEquipmentType());

			// Validate the rank
			String rank = ctx.getParameter("rank");
			if (!newEQ.getRanks().contains(rank))
				throw new CommandException("Invalid Rank - " + rank);
			
			// Add new ratings, and figure out what ratings we added
			List oldRatings = usr.getRatings();
			usr.addRatings(newEQ.getPrimaryRatings());
			usr.addRatings(newEQ.getSecondaryRatings());
			Collection newRatings = CollectionUtils.getDelta(usr.getRatings(), oldRatings);

			// Check if we're doing a promotion or a rating change
			RankComparator rCmp = new RankComparator((List) SystemData.getObject("ranks"));
			rCmp.setRank1(usr.getRank(), currentEQ.getStage());
			rCmp.setRank2(rank, newEQ.getStage());
			
			// Set the status update code
			int statusCode = (rCmp.compare() < 0) ? StatusUpdate.EXTPROMOTION : StatusUpdate.RANK_CHANGE;

			// Update the equipment program and rank
			usr.setEquipmentType(newEQ.getName());
			usr.setRank(rank);

			// Save the Pilot
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(usr);

			// Write the promotion Status Update
			List updates = new ArrayList();
			StatusUpdate upd1 = new StatusUpdate(usr.getID(), statusCode);
			upd1.setAuthorID(ctx.getUser().getID());
			upd1.setDescription("Equipment Program / Rank changed to " + usr.getRank() + ", " + newEQ.getName());
			updates.add(upd1);

			// Write the rating change Status Update
			if (!newRatings.isEmpty()) {
				StatusUpdate upd2 = new StatusUpdate(usr.getID(), StatusUpdate.RATING_ADD);
				upd2.setAuthorID(ctx.getUser().getID());
				upd2.setDescription("Ratings added: " + StringUtils.listConcat(newRatings, ","));
				updates.add(upd2);
			}

			// Save the message context
			mctxt.addData("pilot", usr);
			mctxt.addData("eqType", newEQ);
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("XFERAPPROVE"));

			// Use a SQL Transaction
			ctx.startTX();

			// Write the Status Updates
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			swdao.write(updates);

			// Delete the transfer request
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.delete(usr.getID());

			// Commit the transaction
			ctx.commitTX();

			// Write status attributes to the request
			ctx.setAttribute("isApprove", Boolean.valueOf(true), REQUEST);
			ctx.setAttribute("pilot", usr, REQUEST);
			ctx.setAttribute("eqType", newEQ, REQUEST);
			ctx.setAttribute("newRatings", newRatings, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(usr);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/txRequestUpdate.jsp");
		result.setSuccess(true);
	}
}