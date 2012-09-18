// Copyright 2005, 2006, 2007, 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.testing.Test;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

/**
 * A Web Site Command to display Pilot Examinations awaiting scoring.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class ExamQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the submitted examinations
			GetExam dao = new GetExam(con);
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());
			List<? extends Test> results = dao.getSubmitted();

			// Check our access level, remove those exams we cannot score and build a list of Pilot IDs
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			for (Iterator<? extends Test> i = results.iterator(); i.hasNext();) {
				Test t = i.next();
				ExamAccessControl access = new ExamAccessControl(ctx, t, null);
				try {
					access.validate();
					pilotIDs.add(Integer.valueOf(t.getAuthorID()));
				} catch (AccessControlException ace) {
					i.remove();
				}
			}
			
			// Get the Pilot Profiles
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(pilotIDs);
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);

			// Save the examination queue
			vc.setResults(results);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/examQueue.jsp");
		result.setSuccess(true);
	}
}