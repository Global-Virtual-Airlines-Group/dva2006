// Copyright 2006, 2007, 2010, 2011, 2012, 2014, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
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
 * @version 9.0
 * @since 1.0
 */

public class IssueCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
					i.setResolvedOn(Instant.now());
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
				i.setCreatedOn(Instant.now());
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
			
			// Save the issue
			SetHelp iwdao = new SetHelp(con);
			iwdao.write(i);
			 
			// Send an issue
			if (isNew || sendIssue) {
				MessageContext mctx = new MessageContext();
				mctx.addData("issue", i);
				mctx.addData("user", ctx.getUser());
				
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctx.setTemplate(mtdao.get(isNew ? "HDISSUECREATE" : "HDISSUEASSIGN"));
				
				// Get the Assignee and copyto
				GetPilotDirectory pdao = new GetPilotDirectory(con);
				Pilot usr = pdao.get(i.getAssignedTo());
				mctx.addData("assignee", usr);
				
				// Get users to notify
				Collection<Pilot> notifyPilots = new HashSet<Pilot>(pdao.getByID(getPilotIDs(i), "PILOTS").values());
				
				// If new, notify additional users
				Collection<?> roles = (Collection<?>) SystemData.getObject("helpdesk.notify_roles");
				if (isNew && !CollectionUtils.isEmpty(roles)) {
					for (Iterator<?> nri = roles.iterator(); nri.hasNext(); ) {
						String roleName = String.valueOf(nri.next());
						notifyPilots.addAll(pdao.getByRole(roleName, SystemData.get("airline.db")));
					}
				}

				// Create the message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctx);
				for (Pilot p : notifyPilots) {
					if (p.getStatus() == PilotStatus.ACTIVE)
						mailer.setCC(p);
				}
				
				// Send the message
				mailer.send(usr);
				
				// Save user info
				ctx.setAttribute("assignee", usr, REQUEST);
				ctx.setAttribute("emailSent", Boolean.TRUE, REQUEST);
			}
			
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
		result.setType(ResultType.REDIRECT);
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
	@Override
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
			assignees.addAll(pdao.getByRole("AcademyAdmin", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("PIREP", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("Examination", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("Signature", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("HelpDesk", SystemData.get("airline.db")));
			List<Pilot> activeAssignees = assignees.stream().filter(p -> (p.getStatus() == PilotStatus.ACTIVE)).collect(Collectors.toList());
			ctx.setAttribute("assignees", activeAssignees, REQUEST);
			
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
	@Override
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
			
			// Load templates if necessary
			if (ac.getCanUseTemplate()) {
				GetHelpTemplate tmpdao = new GetHelpTemplate(con);
				ctx.setAttribute("rspTemplates", tmpdao.getAll(), REQUEST);
			}
			
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
	
	/*
	 * Helper method to return all pilot IDs associated with a particular issue.
	 */
	private static Collection<Integer> getPilotIDs(Issue i) {
		Collection<Integer> results = new HashSet<Integer>(16);

		// Add creator/assignee and comment authors
		results.add(Integer.valueOf(i.getAuthorID()));
		results.add(Integer.valueOf(i.getAssignedTo()));
		for (IssueComment ic : i.getComments())
			results.add(Integer.valueOf(ic.getAuthorID()));

		return results;
	}
}