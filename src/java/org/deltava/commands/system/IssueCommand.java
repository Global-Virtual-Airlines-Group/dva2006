// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A web site command to manipulate issues.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class IssueCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving an Issue.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
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
					throw securityException("Cannot create Issue");

				// Instantiate a new bean
				i = new Issue(ctx.getParameter("subject"));
				i.setStatus(Issue.STATUS_OPEN);
				i.setAuthorID(ctx.getUser().getID());
				i.setCreatedOn(new Date());
				i.setSecurity(StringUtils.arrayIndexOf(Issue.SECURITY, ctx.getParameter("security"), Issue.SECURITY_USERS));

				// Assign to default user
				GetPilot dao2 = new GetPilot(con);
				Pilot p = dao2.getPilotByCode(SystemData.getInt("issue_track.assignto"), SystemData.get("airline.db"));
				i.setAssignedTo(p.getID());
			} else {
				GetIssue dao = new GetIssue(con);
				i = dao.get(ctx.getID());
				if (i == null)
					throw notFoundException("Invalid Issue " + ctx.getID());

				// Check our access
				access = new IssueAccessControl(ctx, i);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot save Issue " + ctx.getID());
				
				// Update the subject
				i.setSubject(ctx.getParameter("subject"));
				i.setSecurity(StringUtils.arrayIndexOf(Issue.SECURITY, ctx.getParameter("security")));
			}

			// Update the issue from the request
			i.setArea(StringUtils.arrayIndexOf(Issue.AREA, ctx.getParameter("area")));
			i.setPriority(StringUtils.arrayIndexOf(Issue.PRIORITY, ctx.getParameter("priority")));
			i.setType(StringUtils.arrayIndexOf(Issue.TYPE,ctx.getParameter("issueType")));
			i.setDescription(ctx.getParameter("desc"));

			// Set the version
			StringTokenizer tkns = new StringTokenizer(ctx.getParameter("version"), ".");
			i.setMajorVersion(Integer.parseInt(tkns.nextToken()));
			i.setMinorVersion(Integer.parseInt(tkns.nextToken()));

			// If we can resolve the issue, update the status
			if ((access.getCanResolve()) && (ctx.getParameter("status") != null)) {
				i.setStatus(StringUtils.arrayIndexOf(Issue.STATUS, ctx.getParameter("status")));
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/issue/issueUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing an existing Issue.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

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
					throw securityException("Cannot Create new Issues");
			} else {
				i = dao.get(ctx.getID());
				if (i == null)
					throw notFoundException("Invalid Issue - " + ctx.getID());

				// Check our access
				access = new IssueAccessControl(ctx, i);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot Edit Issue " + ctx.getID());

				// Save the issue in the request
				ctx.setAttribute("issue", i, REQUEST);
			}
			
			// Get the Pilot DAO
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			
			// Get developers
			Collection<Pilot> devs = new HashSet<Pilot>();
			Collection<?> apps = ((Map<?, ?>) SystemData.getObject("apps")).values();
			for (Iterator<?> it = apps.iterator(); it.hasNext(); ) {
			   AirlineInformation aInfo = (AirlineInformation) it.next();
			   devs.addAll(pdao.getByRole("Developer", aInfo.getDB()));
			}
			
			// Save developers in request
			ctx.setAttribute("devs", devs, REQUEST);
			
			// Get the Pilots posting in this issue
			if (!isNew) {
				GetUserData uddao = new GetUserData(con);
				UserDataMap udm = uddao.get(getPilotIDs(i));
				ctx.setAttribute("userData", udm, REQUEST);
				ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
			}

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
		ctx.setAttribute("securityLevels", ComboUtils.fromArray(Issue.SECURITY), REQUEST);

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
				throw notFoundException("Invalid Issue - " + ctx.getID());
			
			// Check our access
			IssueAccessControl access = new IssueAccessControl(ctx, i);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot view Issue - " + ctx.getID());
			
			// Get the userData DAO
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(getPilotIDs(i));
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);

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
	private Collection<Integer> getPilotIDs(Issue i) {
		Collection<Integer> results = new HashSet<Integer>();

		// Add Creator / Assignee
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