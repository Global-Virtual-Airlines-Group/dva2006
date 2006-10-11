// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyIssueAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Fleet Academy Issues.
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

		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the Issue
			Issue i = null;
			AcademyIssueAccessControl ac = null;
			if (!isNew) {
				GetAcademyIssues idao = new GetAcademyIssues(con);
				i = idao.getIssue(ctx.getID());
				if (i == null)
					throw notFoundException("Invalid Issue - " + ctx.getID());
				
				// Check access
				ac = new AcademyIssueAccessControl(ctx, i, false);
				ac.validate();
				if (!ac.getCanUpdateStatus())
					throw securityException("Cannot Update Issue");

				// Update subject
				i.setSubject(ctx.getParameter("subject"));
				i.setStatus(ctx.getParameter("status"));
				i.setAssignedTo(StringUtils.parse(ctx.getParameter("assignedTo"), 0));
				if ((i.getStatus() != Issue.OPEN) && (i.getResolvedOn() == null))
					i.setResolvedOn(new Date());
				else if ((i.getStatus() == Issue.OPEN) && (i.getResolvedOn() != null))
					i.setResolvedOn(null);
			} else {
				GetAcademyCourses cdao = new GetAcademyCourses(con);
				cdao.setQueryMax(1);
				Collection<Course> courses = cdao.getByPilot(ctx.getUser().getID());

				// Check access
				ac = new AcademyIssueAccessControl(ctx, null, !courses.isEmpty());
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
				Pilot p = pdao.getPilotByCode(SystemData.getInt("academy.assignto"), SystemData.get("airline.db"));
				i.setAssignedTo(p.getID());
			}
			
			// Update fields from the request
			i.setBody(ctx.getParameter("body"));
			if (ac.getCanUpdateStatus())
				i.setPublic(Boolean.valueOf(ctx.getParameter("isPublic")).booleanValue());
			
			// Save the issue
			SetAcademyIssue iwdao = new SetAcademyIssue(con);
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
			result.setURL("academyissues.do");
		else
			result.setURL("academyissue", null, ctx.getID());
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
			
			if (!isNew) {
				//	Get the Issue
				GetAcademyIssues idao = new GetAcademyIssues(con);
				Issue i = idao.getIssue(ctx.getID());
				if (i == null)
					throw notFoundException("Invalid Issue - " + ctx.getID());

				// Check access
				AcademyIssueAccessControl ac = new AcademyIssueAccessControl(ctx, i, false);
				ac.validate();
				if (!ac.getCanUpdateStatus())
					throw securityException("Cannot Update Issue");
				
				// Save issue and access control
				ctx.setAttribute("issue", i, REQUEST);
				ctx.setAttribute("access", ac, REQUEST);
				
				// Load Pilot IDs
				ctx.setAttribute("pilots", pdao.getByID(getPilotIDs(i), "PILOTS"), REQUEST);
			} else {
				GetAcademyCourses cdao = new GetAcademyCourses(con);
				cdao.setQueryMax(1);
				Collection<Course> courses = cdao.getByPilot(ctx.getUser().getID());
				
				// Check access
				AcademyIssueAccessControl ac = new AcademyIssueAccessControl(ctx, null, !courses.isEmpty());
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot Create Issue");
				
				// Save access control
				ctx.setAttribute("access", ac, REQUEST);
			}
			
			// Get Assignees
			Collection<Pilot> assignees = new TreeSet<Pilot>(new PilotComparator<Pilot>(PersonComparator.LASTNAME));
			assignees.addAll(pdao.getByRole("HR", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("Instructor", SystemData.get("airline.db")));
			assignees.addAll(pdao.getByRole("Examiner", SystemData.get("airline.db")));
			ctx.setAttribute("assignees", assignees, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save Issue statuses
		ctx.setAttribute("statuses", ComboUtils.fromArray(Issue.STATUS_NAMES), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/issueEdit.jsp");
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
			GetAcademyIssues idao = new GetAcademyIssues(con);
			Issue i = idao.getIssue(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue - " + ctx.getID());
			
			// Calculate access rights
			AcademyIssueAccessControl ac = new AcademyIssueAccessControl(ctx, i, false);
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
		result.setURL("/jsp/academy/issueView.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Helper method to return all pilot IDs associated with a particular issue.
	 */
	private Collection<Integer> getPilotIDs(Issue i) {
		Set<Integer> results = new HashSet<Integer>();

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