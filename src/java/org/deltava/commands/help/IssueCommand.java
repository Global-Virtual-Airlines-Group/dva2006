// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.help.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Help Desk Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		boolean sendIssue = false;
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the Issue
			Issue i = null;
			HelpDeskAccessControl ac = null;
			if (!isNew) {
				GetHelp idao = new GetHelp(con);
				i = idao.getIssue(ctx.getID());
				if (i == null)
					throw notFoundException("Invalid Issue - " + ctx.getID());
				
				// Check access
				ac = new HelpDeskAccessControl(ctx, i);
				ac.validate();
				if (!ac.getCanUpdateStatus())
					throw securityException("Cannot Update Issue");
				
				// Determine if we're reassigning
				int newAssignee = StringUtils.parse(ctx.getParameter("assignedTo"), i.getAssignedTo());
				if (newAssignee != i.getAssignedTo()) {
					i.setAssignedTo(newAssignee);
					sendIssue = Boolean.valueOf(ctx.getParameter("sendIssue")).booleanValue();
				}

				// Update subject
				i.setSubject(ctx.getParameter("subject"));
				i.setStatus(ctx.getParameter("status"));
				if ((i.getStatus() != Issue.OPEN) && (i.getResolvedOn() == null))
					i.setResolvedOn(new Date());
				else if ((i.getStatus() == Issue.OPEN) && (i.getResolvedOn() != null))
					i.setResolvedOn(null);
			} else {
				// Check access
				ac = new HelpDeskAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot Create Issue");
				
				// Build the issue
				i = new Issue(ctx.getParameter("subject"));
				i.setAuthorID(ctx.getUser().getID());
				i.setCreatedOn(new Date());
				i.setStatus(Issue.OPEN);
				
				// Set default assignee
				GetPilot pdao = new GetPilot(con);
				Pilot p = pdao.getPilotByCode(SystemData.getInt("helpdesk.assignto"), SystemData.get("airline.db"));
				i.setAssignedTo(p.getID());
			}
			
			// Update fields from the request
			i.setBody(ctx.getParameter("body"));
			if (ac.getCanUpdateStatus())
				i.setPublic(Boolean.valueOf(ctx.getParameter("isPublic")).booleanValue());
			
			// Send an issue
			if (isNew || sendIssue) {
				MessageContext mctx = new MessageContext();
				mctx.addData("issue", i);
				mctx.addData("user", ctx.getUser());
				
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctx.setTemplate(mtdao.get(isNew ? "HDISSUECREATE" : "HDISSUEASSIGN"));
				
				// Get the Assignee and copyto
				GetPilot pdao = new GetPilot(con);
				Pilot usr = pdao.get(i.getAssignedTo());
				mctx.addData("assignee", usr);

				// Create the message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctx);
				Collection<Pilot> ids = pdao.getByID(getPilotIDs(i), "PILOTS").values();
				for (Iterator<Pilot> cci = ids.iterator(); cci.hasNext(); )
					mailer.setCC(cci.next());
				
				// Send the message
				mailer.send(usr);
				
				// Save user info
				ctx.setAttribute("assignee", usr, REQUEST);
				ctx.setAttribute("emailSent", Boolean.TRUE, REQUEST);
			}
			
			// Save the issue
			SetHelp iwdao = new SetHelp(con);
			iwdao.write(i);
			 
			// Save issue in the request
			ctx.setAttribute("issue", i, REQUEST);
			ctx.setAttribute("isNew", Boolean.valueOf(isNew), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
		if (isNew)
			result.setURL("hdissues.do");
		else
			result.setURL("hdissue", null, ctx.getID());
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			
			HelpDeskAccessControl ac = null;
			if (!isNew) {
				//	Get the Issue
				GetHelp idao = new GetHelp(con);
				Issue i = idao.getIssue(ctx.getID());
				if (i == null)
					throw notFoundException("Invalid Issue - " + ctx.getID());

				// Check access
				ac = new HelpDeskAccessControl(ctx, i);
				ac.validate();
				if (!ac.getCanUpdateStatus())
					throw securityException("Cannot Update Issue");
				
				// Save issue and access control
				ctx.setAttribute("issue", i, REQUEST);
				ctx.setAttribute("access", ac, REQUEST);
				
				// Load Pilot IDs
				ctx.setAttribute("pilots", pdao.getByID(getPilotIDs(i), "PILOTS"), REQUEST);
			} else {
				// Check access
				ac = new HelpDeskAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot Create Issue");
				
				// Save access control
				ctx.setAttribute("access", ac, REQUEST);
			}
			
			// Get Assignees
			Collection<Pilot> assignees = new TreeSet<Pilot>(new PilotComparator(PersonComparator.LASTNAME));
			assignees.addAll(pdao.getByRole("HR", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("Instructor", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("Examiner", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("PIREP", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("Examination", SystemData.get("airline.db")));
			ctx.setAttribute("assignees", assignees, REQUEST);
			
			// Get options for issue conversion
			if (ac.getCanUpdateStatus() && !isNew) {
				ctx.setAttribute("devs", pdao.getByRole("Developer", SystemData.get("airline.db")), REQUEST);
				ctx.setAttribute("areaNames", ComboUtils.fromArray(org.deltava.beans.system.Issue.AREA), REQUEST);
				ctx.setAttribute("typeNames", ComboUtils.fromArray(org.deltava.beans.system.Issue.TYPE), REQUEST);
				ctx.setAttribute("priorityNames", ComboUtils.fromArray(org.deltava.beans.system.Issue.PRIORITY), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save Issue statuses
		ctx.setAttribute("statuses", ComboUtils.fromArray(Issue.STATUS_NAMES), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/issueEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the Issue
			GetHelp idao = new GetHelp(con);
			Issue i = idao.getIssue(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue - " + ctx.getID());
			
			// Calculate access rights
			HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, i);
			ac.validate();
			
			// Save in request
			ctx.setAttribute("issue", i, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
			
			// Get Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(getPilotIDs(i), "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/issueView.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Helper method to return all pilot IDs associated with a particular issue.
	 */
	private Collection<Integer> getPilotIDs(Issue i) {
		Collection<Integer> results = new HashSet<Integer>();

		// Add Creator
		results.add(new Integer(i.getAuthorID()));
		results.add(new Integer(i.getAssignedTo()));

		// Add comment authors
		for (Iterator<IssueComment> ici = i.getComments().iterator(); ici.hasNext();) {
			IssueComment ic = ici.next();
			results.add(new Integer(ic.getAuthorID()));
		}

		return results;
	}
}