// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
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
				throw notFoundException("Invalid Transfer Request - " + ctx.getID());

			// Check our access
			TransferAccessControl access = new TransferAccessControl(ctx, txreq);
			access.validate();
			if (!access.getCanApprove())
				throw securityException("Cannot approve Transfer Request");

			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			usr = pdao.get(txreq.getID());
			if (usr == null)
				throw notFoundException("Invalid Pilot - " + txreq.getID());
			
			// Get the current equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType currentEQ = eqdao.get(usr.getEquipmentType());
			
			// Get the new ratings
			List<StatusUpdate> updates = new ArrayList<StatusUpdate>();
			Collection<String> ratings = ctx.getParameters("ratings");
			Collection<String> newRatings = new TreeSet<String>((ratings == null) ? usr.getRatings() : ratings);

			// Check if we're switching programs
			String eqType = ctx.getParameter("eqType");
			if (eqType != null) {
				EquipmentType newEQ = eqdao.get(eqType);
				if ((currentEQ == null) || (newEQ == null))
					throw notFoundException("Invalid Equipment Program - " + eqType + " / " + usr.getEquipmentType());

				// Validate the rank
				String rank = ctx.getParameter("rank");
				if (!newEQ.getRanks().contains(rank))
					throw notFoundException("Invalid Rank - " + rank);

				// Check if we're doing a promotion or a rating change
				@SuppressWarnings("unchecked")
				RankComparator rCmp = new RankComparator((List) SystemData.getObject("ranks"));
				rCmp.setRank2(usr.getRank(), currentEQ.getStage());
				rCmp.setRank1(rank, newEQ.getStage());

				// Update the equipment program and rank
				usr.setEquipmentType(newEQ.getName());
				usr.setRank(rank);

				// Write the promotion status update
				if (rCmp.compare() > 0) {
					boolean eqChange = !usr.getEquipmentType().equals(newEQ.getName());
					int promoType = eqChange ? StatusUpdate.EXTPROMOTION : StatusUpdate.INTPROMOTION;
					StatusUpdate upd = new StatusUpdate(usr.getID(), promoType);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Promoted to " + usr.getRank() + ", " + usr.getEquipmentType());
					updates.add(upd);
				} else {
					StatusUpdate upd = new StatusUpdate(usr.getID(), StatusUpdate.RANK_CHANGE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Rank changed to " + usr.getRank() + ", " + usr.getEquipmentType());
					updates.add(upd);
				}
				
				// Add the new equipment program to the message context and request
				mctxt.addData("eqType", newEQ);
				ctx.setAttribute("eqType", newEQ, REQUEST);
			} else {
				mctxt.addData("eqType", currentEQ);
				ctx.setAttribute("eqType", currentEQ, REQUEST);
			}

			// Figure out what ratings have been added
			Collection addedRatings = CollectionUtils.getDelta(newRatings, usr.getRatings());
			if (!addedRatings.isEmpty()) {
				ctx.setAttribute("addedRatings", addedRatings, REQUEST);
				StatusUpdate upd = new StatusUpdate(usr.getID(), StatusUpdate.RATING_ADD);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Ratings added: " + StringUtils.listConcat(addedRatings, ", "));
				updates.add(upd);
			}

			// Figure out what ratings have been removed
			Collection<String> removedRatings = CollectionUtils.getDelta(usr.getRatings(), newRatings);
			if (!removedRatings.isEmpty()) {
				ctx.setAttribute("removedRatings", removedRatings, REQUEST);
				usr.removeRatings(removedRatings);

				// Note the changed ratings
				StatusUpdate upd = new StatusUpdate(usr.getID(), StatusUpdate.RATING_REMOVE);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Ratings removed: " + StringUtils.listConcat(removedRatings, ", "));
				updates.add(upd);
			}

			// Update the ratings
			usr.addRatings(newRatings);

			// Save the message context
			mctxt.addData("pilot", usr);

			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("XFERAPPROVE"));

			// Use a SQL Transaction
			ctx.startTX();

			// Save the Pilot
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(usr);

			// Write the Status Updates
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			swdao.write(updates);

			// Delete the transfer request
			SetTransferRequest txwdao = new SetTransferRequest(con);
			txwdao.delete(usr.getID());

			// Commit the transaction
			ctx.commitTX();

			// Write status attributes to the request
			ctx.setAttribute("txReq", txreq, REQUEST);
			ctx.setAttribute("isApprove", Boolean.TRUE, REQUEST);
			ctx.setAttribute("pilot", usr, REQUEST);
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