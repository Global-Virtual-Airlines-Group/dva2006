// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;

import org.deltava.beans.system.Issue;
import org.deltava.beans.system.IssueComment;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.ComboUtils;
import org.deltava.util.system.SystemData;

/**
 * A web site command to manipulate issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving an Issue.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Check if we're creating a new issue
		boolean isNew = (ctx.getID() == 0);

		try {
			Connection con = ctx.getConnection();

			// Read the issue
			Issue i = null;
			IssueAccessControl access;
			if (isNew) {
				// Check our access
				access = new IssueAccessControl(ctx, i);
				access.validate();
				if (!access.getCanCreate())
					throw new CommandSecurityException("Cannot create Issue");

				// Instantiate a new bean
				i = new Issue(ctx.getParameter("subject"));
				i.setStatus(Issue.STATUS_OPEN);
				i.setCreatedBy(ctx.getUser().getID());
				i.setCreatedOn(new Date());

				// Assign to default user
				GetPilot dao2 = new GetPilot(con);
				Pilot p = dao2.getPilotByCode(SystemData.getInt("issue_track.assignto"), SystemData.get("airline.db"));
				i.setAssignedTo(p.getID());
			} else {
				GetIssue dao = new GetIssue(con);
				i = dao.get(ctx.getID());
				if (i == null)
					throw new CommandException("Invalid Issue " + ctx.getID());

				// Check our access
				access = new IssueAccessControl(ctx, i);
				access.validate();
				if (!access.getCanEdit())
					throw new CommandSecurityException("Cannot save Issue " + ctx.getID());
			}

			// Update the issue from the request
			i.setArea(ctx.getParameter("area"));
			i.setPriority(ctx.getParameter("priority"));
			i.setType(ctx.getParameter("issueType"));
			i.setDescription(ctx.getParameter("desc"));

			// Set the version
			StringTokenizer tkns = new StringTokenizer(ctx.getParameter("version"), ".");
			i.setMajorVersion(Integer.parseInt(tkns.nextToken()));
			i.setMinorVersion(Integer.parseInt(tkns.nextToken()));

			// If we can resolve the issue, update the status
			if ((access.getCanResolve()) && (ctx.getParameter("status") != null)) {
				i.setStatus(ctx.getParameter("status"));
				if ((i.getStatus() != Issue.STATUS_OPEN) && (i.getResolvedOn() == null))
					i.setResolvedOn(new Date());
			}

			// If we can reassign the issue, update the assignee
			if ((access.getCanReassign()) && (ctx.getParameter("assignedTo") != null))
				i.setAssignedTo(Integer.parseInt(ctx.getParameter("assignedTo")));

			// Get the save DAO and write the issue
			SetIssue dao2 = new SetIssue(con);
			dao2.write(i);

			// Send the notification if it's a new issue
			boolean sendIssue = Boolean.valueOf(ctx.getParameter("emailIssue")).booleanValue();
			if ((isNew) && (sendIssue)) {
				MessageContext mctx = new MessageContext();
				mctx.addData("issue", i);
				mctx.addData("user", ctx.getUser());
				
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctx.setTemplate(mtdao.get("ISSUECREATE"));
				
				// Get the Assignee
				GetPilot pdao = new GetPilot(con);
				Pilot usr = pdao.get(i.getAssignedTo());
				mctx.addData("assignee", usr);
				
				// Send the message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctx);
				mailer.send(usr);
				
				// Save user info
				ctx.setAttribute("assignee", usr, REQUEST);
				ctx.setAttribute("emailSent", Boolean.TRUE, REQUEST);
			}

			// Save issue in the request
			ctx.setAttribute("issue", i, REQUEST);

			// Update the status for the JSP
			ctx.setAttribute("opName", (i.getID() == 0) ? "Created" : "Updated", REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/issue/issueUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing an existing Issue.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Check if we're creating a new Issue
		boolean isNew = (ctx.getID() == 0);

		try {
			IssueAccessControl access = null;
			Connection con = ctx.getConnection();

			// Get the Issue
			Issue i = null;
			GetIssue dao = new GetIssue(con);
			if (isNew) {
				// Check our access
				access = new IssueAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreate())
					throw new CommandSecurityException("Cannot Create new Issues");
			} else {
				i = dao.get(ctx.getID());
				if (i == null)
					throw new CommandException("Invalid Issue - " + ctx.getID());

				// Check our access
				access = new IssueAccessControl(ctx, i);
				access.validate();
				if (!access.getCanEdit())
					throw new CommandSecurityException("Cannot Edit Issue " + ctx.getID());

				// Save the issue in the request
				ctx.setAttribute("issue", i, REQUEST);
			}

			// Get the Pilots posting in this issue
			GetPilotIssue dao2 = new GetPilotIssue(con);
			ctx.setAttribute("devs", dao2.getPilotsByRole("Developer"), REQUEST);
			if (!isNew)
				ctx.setAttribute("pilots", dao2.getByID(getPilotIDs(i), "PILOTS"), REQUEST);

			// Save the objects in the request
			ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save option lists in the request
		ctx.setAttribute("priorities", ComboUtils.fromArray(Issue.PRIORITY), REQUEST);
		ctx.setAttribute("types", ComboUtils.fromArray(Issue.TYPE), REQUEST);
		ctx.setAttribute("areas", ComboUtils.fromArray(Issue.AREA), REQUEST);
		ctx.setAttribute("statuses", ComboUtils.fromArray(Issue.STATUS), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/issue/issueEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading an Issue.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the Issue
			GetIssue dao = new GetIssue(con);
			Issue i = dao.get(ctx.getID());
			if (i == null)
				throw new CommandException("Invalid Issue - " + ctx.getID());

			// Get the Pilots posting in this issue
			GetPilotIssue dao2 = new GetPilotIssue(con);
			ctx.setAttribute("pilots", dao2.getByID(getPilotIDs(i), "PILOTS"), REQUEST);
			ctx.setAttribute("devs", dao2.getPilotsByRole("Developer"), REQUEST);

			// Check our access
			IssueAccessControl access = new IssueAccessControl(ctx, i);
			access.validate();

			// Save the issue and the access in the request
			ctx.setAttribute("issue", i, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/issue/issueRead.jsp");
		result.setSuccess(true);
	}

	/**
	 * Helper method to return all pilot IDs associated with a particular issue.
	 */
	private Collection getPilotIDs(Issue i) {

		Set results = new HashSet();

		// Add Creator / Assignee
		results.add(new Integer(i.getCreatedBy()));
		results.add(new Integer(i.getAssignedTo()));

		// Add comment authors
		for (Iterator ici = i.getComments().iterator(); ici.hasNext();) {
			IssueComment ic = (IssueComment) ici.next();
			results.add(new Integer(ic.getCreatedBy()));
		}

		return results;
	}
}