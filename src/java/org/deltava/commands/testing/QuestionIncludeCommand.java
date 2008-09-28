// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionProfileAccessControl;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update the Examinations a Question appears in, if the current web
 * application is not the owner of the Question.
 * @author Luke
 * @version 2.1
 * @since 2.0
 */

public class QuestionIncludeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the question profile
			GetExamQuestions rdao = new GetExamQuestions(con);
			QuestionProfile qp = rdao.getQuestionProfile(ctx.getID());
			if (qp == null)
				throw notFoundException("Invalid Question Profile - " + ctx.getID());
			
			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanInclude())
				throw securityException("Cannot modify Question Profile examination list");
			
			// Load the exam pools from the request
			Collection<String> examPools = ctx.getParameters("examNames");
			Collection<ExamSubPool> pools = new LinkedHashSet<ExamSubPool>();
			if (examPools != null) {
				for (Iterator<String> i = examPools.iterator(); i.hasNext(); ) {
					String poolName = i.next();
					int pos = poolName.lastIndexOf('-');
					if (pos == -1)
						pools.add(new ExamSubPool(poolName, ""));
					else {
						ExamSubPool esp = new ExamSubPool(poolName.substring(0, pos), "");
						esp.setID(StringUtils.parse(poolName.substring(pos + 1), 0));
						pools.add(esp);
					}
				}
			}
			
			// Get our airline's exam subpool names
			GetExamProfiles epdao = new GetExamProfiles(con);
			Collection<ExamSubPool> ourExams = epdao.getSubPools(); 
			
			// Ensure that we have only selected exams in our airline
			pools.removeAll(CollectionUtils.getDelta(pools, ourExams));
			
			// Update the exams - remove all from our airline, then re-add what we have left
			Collection<ExamSubPool> exams = qp.getPools();
			exams.removeAll(ourExams);
			exams.addAll(pools);
			qp.setPools(exams);
			
			// Save the question profile
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.writeExams(qp);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/profileUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}