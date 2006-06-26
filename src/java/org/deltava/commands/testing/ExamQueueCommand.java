// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.Test;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

/**
 * A Web Site Command to display Pilot Examinations awaiting scoring.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get/set start/count parameters
		ViewContext vc = initView(ctx);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the submitted examinations
			GetExam dao = new GetExam(con);
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());
			List results = dao.getSubmitted();

			// Check our access level, remove those exams we cannot score and build a list of Pilot IDs
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			for (Iterator i = results.iterator(); i.hasNext();) {
				Test t = (Test) i.next();
				ExamAccessControl access = new ExamAccessControl(ctx, t);
				try {
					access.validate();
					pilotIDs.add(new Integer(t.getPilotID()));
				} catch (AccessControlException ace) {
					i.remove();
				}
			}
			
			// Get the Pilot Profiles
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);

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