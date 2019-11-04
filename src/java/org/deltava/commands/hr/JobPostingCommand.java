// Copyright 2010, 2011, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.MultiUserSecurityContext;
import org.deltava.security.command.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Job Postings.
 * @author Luke
 * @version 9.0
 * @since 3.4
 */

public class JobPostingCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Job posting.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		boolean isNew = (ctx.getID() == 0);

		// Get mailer context
		List<EMailAddress> addrs = new ArrayList<EMailAddress>();
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		try {
			Connection con = ctx.getConnection();
			JobPosting jp = null;
			if (!isNew) {
				GetJobs dao = new GetJobs(con);
				jp = dao.get(ctx.getID());
				if (jp == null)
					throw notFoundException("Unknown Job Posting - " + ctx.getID());
				
				jp.setTitle(ctx.getParameter("title"));
			} else {
				jp = new JobPosting(ctx.getParameter("title"));
				jp.setCreatedOn(Instant.now());
			}
			
			// Validate our access
			JobPostingAccessControl access = new JobPostingAccessControl(ctx, jp);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot save Job Posting " + jp.getID());
			
			// Update from request
			jp.setSummary(ctx.getParameter("summary"));
			jp.setStatus(ctx.getParameter("status"));
			jp.setDescription(ctx.getParameter("body"));
			jp.setMinLegs(StringUtils.parse(ctx.getParameter("minLegs"), 0));
			jp.setMinAge(StringUtils.parse(ctx.getParameter("minAge"), 0));
			jp.setClosesOn(parseDateTime(ctx, "close"));
			jp.setHireManagerID(StringUtils.parse(ctx.getParameter("hireMgr"), 0));
			jp.setStaffOnly(Boolean.valueOf(ctx.getParameter("staffOnly")).booleanValue());
			
			// Get users
			if (isNew) {
				GetPilotNotify pdao = new GetPilotNotify(con);
				Collection<Pilot> pilots = pdao.gePilots(Notification.JOB);
				MultiUserSecurityContext mctx = new MultiUserSecurityContext(ctx);
				for (Pilot p : pilots) {
					mctx.setUser(p);
					JobPostingAccessControl ac = new JobPostingAccessControl(mctx, jp);
					try {
						ac.validate();
						if (ac.getCanApply())
							addrs.add(p);
					} catch (AccessControlException ace) {
						// empty
					}
				}
				
				// Load the template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("NEWJOB"));
			}
			
			// Save the posting
			SetJobs jwdao = new SetJobs(con);
			jwdao.write(jp);
			
			// Save in status
			mctxt.addData("job", jp);
			ctx.setAttribute("job", jp, REQUEST);
			ctx.setAttribute("isSave", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// If new, mail it
		if (isNew) {
			// Send email
			Mailer m = new Mailer(ctx.getUser());
			m.setContext(mctxt);
			m.send(addrs);
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/hr/jobPostUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Job posting.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			JobPosting jp = null;
			if (ctx.getID() != 0) {
				GetJobs dao = new GetJobs(con);
				jp = dao.get(ctx.getID());
				if (jp == null)
					throw notFoundException("Unknown Job Posting - " + ctx.getID());
			}
			
			// Validate our access
			JobPostingAccessControl access = new JobPostingAccessControl(ctx, jp);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Job Posting");
			
			// Load applicant IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			if (jp != null) {
				IDs.add(Integer.valueOf(jp.getHireManagerID()));
				if (access.getCanViewApplicants()) {
					for (Application a : jp.getApplications())
						IDs.add(Integer.valueOf(a.getAuthorID()));
				}
			}
			
			// Get Pilots
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Load hiring manager choices
			Collection<Pilot> hMgrs = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			hMgrs.addAll(pdao.getByRole("HR", SystemData.get("airline.db")));
			hMgrs.addAll(pdao.getByRole("HireMgr", SystemData.get("airline.db")));
			hMgrs.addAll(pdao.getPilotsByRank(Rank.CP));
			ctx.setAttribute("hireMgrs", hMgrs, REQUEST);
			
			// Save in request
			ctx.setAttribute("job", jp, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("statuses", Arrays.asList(JobPosting.STATUS_NAMES), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/hr/jobPostingEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Job posting.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Job posting
			GetJobs dao = new GetJobs(con);
			JobPosting jp = dao.get(ctx.getID());
			if (jp == null)
				throw notFoundException("Unknown Job Posting - " + ctx.getID());
			
			// Get Hiring manager
			Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
			GetPilot pdao = new GetPilot(con);
			pilots.put(Integer.valueOf(jp.getHireManagerID()), pdao.get(jp.getHireManagerID()));
			
			// Validate our access - this will throw an exception if we can't read
			JobPostingAccessControl access = new JobPostingAccessControl(ctx, jp);
			access.validate();
			
			// Load applicant IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			if (access.getCanViewApplicants()) {
				Collection<Application> apps = jp.getApplications();
				for (Iterator<Application> i = apps.iterator(); i.hasNext(); ) {
					Application a = i.next();
					
					// Check our access
					JobApplicationAccessControl aac = new JobApplicationAccessControl(ctx, jp, a);
					aac.validate();
					if (aac.getCanView())
						IDs.add(Integer.valueOf(a.getAuthorID()));
					else
						i.remove();
				}
				
				ctx.setAttribute("apps", apps, REQUEST);
			}
			
			// Load commenter IDs
			if (access.getCanComment())
				jp.getComments().stream().map(Comment::getAuthorID).forEach(IDs::add);
			
			// Load pilots
			pilots.putAll(pdao.getByID(IDs, "PILOTS"));
			
			// If we can apply, search for a tempmate
			if (access.getCanApply() && ctx.isAuthenticated()) {
				GetJobProfiles jpdao = new GetJobProfiles(con);
				ctx.setAttribute("profile", jpdao.getProfile(ctx.getUser().getID()), REQUEST);
			}

			// Save in request
			ctx.setAttribute("job", jp, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("pilots", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/hr/jobPosting.jsp");
		result.setSuccess(true);
	}
}