// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamProfileAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to display Examination sub-pools.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class ExamPoolsCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Examination Profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		String examName = (String) ctx.getCmdParameter(ID, null);
		
		// Check if we're adding a pool, and try and retrieve the sessions
		boolean addPool = Boolean.valueOf(ctx.getParameter("addPool")).booleanValue();
		ExamProfile old_ep = (ExamProfile) ctx.getSession().getAttribute(examName + "$pool");
		
		try {
			Connection con = ctx.getConnection();
			
			// Load the examination profile
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(examName);
			if (ep == null)
				throw notFoundException("Unknown Examination - " + examName);
			
			// Check our access
			ExamProfileAccessControl access = new ExamProfileAccessControl(ctx, ep);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot view Examination Profile");
			
			// If we do not have pools in the session, then load them from the database
			if (CollectionUtils.isEmpty(old_ep.getPools()))
				old_ep.setPools(epdao.getSubPools(ep.getName()));
			
			// Update each pool
			int pNum = 0;
			for (Iterator<ExamSubPool> i = old_ep.getPools().iterator(); i.hasNext(); ) {
				ExamSubPool esp = i.next();
				pNum = Math.max(pNum + 1, esp.getID());
				esp.setName(ctx.getParameter("pName" + String.valueOf(pNum)));
				esp.setSize(StringUtils.parse(ctx.getParameter("pSize" + String.valueOf(pNum)), 0));
			}
			
			// If we're not adding a subpool, then save
			if (!addPool) {
				ctx.startTX();
				
				// Write the sub-pools
				SetExamProfile wdao = new SetExamProfile(con);
				for (Iterator<ExamSubPool> i = old_ep.getPools().iterator(); i.hasNext(); )
					wdao.write(i.next());

				// Commit and remove from session
				ctx.commitTX();
				ctx.setAttribute("isPoolUpdate", Boolean.TRUE, REQUEST);
				ctx.getSession().removeAttribute(examName + "$pool");
			} else {
				ExamSubPool esp = new ExamSubPool(ep.getName(), ctx.getParameter("pNameNew"));
				esp.setSize(StringUtils.parse(ctx.getParameter("pSizeNew"), 0));
				old_ep.addPool(esp);
			}
			
			// Save the exam in the request
			ctx.setAttribute("exam", ep, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL(addPool ? "/jsp/testing/examSubPoolEdit.jsp" : "/jsp/testing/profileUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Examination pools.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		String examName = (String) ctx.getCmdParameter(ID, null);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the exam profile
			GetExamProfiles dao = new GetExamProfiles(con);
			ExamProfile ex = dao.getExamProfile(examName);
			if (ex == null)
				throw notFoundException("Unknown Examination - " + examName);

			// Check our access
			ExamProfileAccessControl access = new ExamProfileAccessControl(ctx, ex);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot view Examination Profile");
			
			// Get the pools
			ex.setPools(dao.getSubPools(ex.getName()));

			// Save in the request and session
			ctx.setAttribute("exam", ex, REQUEST);
			ctx.setAttribute(ex.getName() + "$pool", ex, SESSION);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/examSubPoolEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Examination profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}