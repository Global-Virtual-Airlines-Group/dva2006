// Copyright 2005, 2006, 2009, 2011, 2012, 2016, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to manipulate issues.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class IssueCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving an Issue.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();

			// Read the issue
			Issue i = null, oi = null;
			IssueAccessControl access;
			if (isNew) {
				// Check our access
				access = new IssueAccessControl(ctx, i);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Issue");

				// Instantiate a new bean
				i = new Issue(ctx.getParameter("subject"));
				i.setStatus(IssueStatus.OPEN);
				i.setAuthorID(ctx.getUser().getID());
				i.setCreatedOn(Instant.now());
				i.setAirlines(List.of(SystemData.get("airline.code")));
				i.addAirline(SystemData.getApp(null));
				i.setSecurity(EnumUtils.parse(IssueSecurity.class, ctx.getParameter("security"), IssueSecurity.USERS));
				i.setArea(EnumUtils.parse(IssueArea.class, ctx.getParameter("area"), IssueArea.WEBSITE));

				// Assign to default user for that issue type
				GetPilot dao2 = new GetPilot(con);
				String attr = "issue_track.assignto." + i.getArea().name().toLowerCase();
				Pilot p = dao2.getPilotByCode(SystemData.getInt(attr), ctx.getDB());
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

				oi = BeanUtils.clone(i);
				i.setSubject(ctx.getParameter("subject"));
				i.setArea(EnumUtils.parse(IssueArea.class, ctx.getParameter("area"), IssueArea.WEBSITE));
			}
			
			// Update security
			if (ctx.getParameter("security") != null)
				i.setSecurity(EnumUtils.parse(IssueSecurity.class, ctx.getParameter("security"), IssueSecurity.USERS));

			// Update the issue from the request
			i.setPriority(EnumUtils.parse(IssuePriority.class, ctx.getParameter("priority"), IssuePriority.LOW));
			i.setType(EnumUtils.parse(Issue.IssueType.class, ctx.getParameter("issueType"), Issue.IssueType.BUG));
			i.setDescription(ctx.getParameter("desc"));
			
			// Update airlines
			Collection<String> appCodes = ctx.getParameters("apps", Collections.emptyList());
			if (ctx.isUserInRole("Developer") && !appCodes.isEmpty())
				i.setAirlines(appCodes);

			// Set the version
			StringTokenizer tkns = new StringTokenizer(ctx.getParameter("version"), ".");
			i.setMajorVersion(StringUtils.parse(tkns.nextToken(), VersionInfo.MAJOR));
			i.setMinorVersion(StringUtils.parse(tkns.nextToken(), VersionInfo.MINOR));

			// If we can resolve the issue, update the status
			if (access.getCanResolve() && (ctx.getParameter("status") != null)) {
				i.setStatus(EnumUtils.parse(IssueStatus.class, ctx.getParameter("status"), i.getStatus()));
				if ((i.getStatus() != IssueStatus.OPEN) && (i.getResolvedOn() == null))
					i.setResolvedOn(Instant.now());
			}

			// If we can reassign the issue, update the assignee
			if (access.getCanReassign() && (ctx.getParameter("assignedTo") != null))
				i.setAssignedTo(StringUtils.parse(ctx.getParameter("assignedTo"), 0));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oi, i);
			AuditLog ae = AuditLog.create(i, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();

			// Write the issue
			SetIssue dao2 = new SetIssue(con);
			dao2.write(i);
			
			// Write the audit log and commit
			writeAuditLog(ctx, ae);
			ctx.commitTX();

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
			ctx.rollbackTX();
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
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		boolean isNew = (ctx.getID() == 0);
		try {
			IssueAccessControl access = null;
			@SuppressWarnings("unchecked")
			Collection<String> versions = (Collection<String>) SystemData.getObject("issue_track.versions");
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
				
				// Get current version string
				String currentVer = String.valueOf(VersionInfo.MAJOR) + "." + String.valueOf(VersionInfo.MINOR);
				versions.add(currentVer);
				ctx.setAttribute("currentVersion", currentVer, REQUEST);  
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
				String currentVer = String.valueOf(i.getMajorVersion()) + "." + String.valueOf(i.getMinorVersion());
				ctx.setAttribute("issue", i, REQUEST);
				versions.add(currentVer);
				ctx.setAttribute("currentVersion", currentVer, REQUEST);
			}

			// Get developers
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Collection<Pilot> devs = new HashSet<Pilot>();
			Collection<?> apps = ((Map<?, ?>) SystemData.getObject("apps")).values();
			for (Iterator<?> it = apps.iterator(); it.hasNext();) {
				AirlineInformation aInfo = (AirlineInformation) it.next();
				devs.addAll(pdao.getByRole("Developer", aInfo.getDB()));
			}

			// Save developers in request
			ctx.setAttribute("allApps", apps, REQUEST);
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
	@Override
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
			ctx.setAttribute("multiComment", Boolean.valueOf(udm.size() > 2), REQUEST);
			
			// Save the issue and the access in the request
			ctx.setAttribute("issue", i, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			readAuditLog(ctx, i);
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

	/*
	 * Helper method to return all pilot IDs associated with a particular issue.
	 */
	private static Collection<Integer> getPilotIDs(Issue i) {
		Collection<Integer> results = new HashSet<Integer>();
		results.add(Integer.valueOf(i.getAuthorID()));
		results.add(Integer.valueOf(i.getAssignedTo()));
		i.getComments().stream().map(IssueComment::getAuthorID).forEach(results::add);
		return results;
	}
}