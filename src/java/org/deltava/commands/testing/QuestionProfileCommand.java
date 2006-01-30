// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.Arrays;
import java.sql.Connection;

import org.deltava.beans.testing.QuestionProfile;

import org.deltava.commands.*;

import org.deltava.dao.GetExamProfiles;
import org.deltava.dao.SetExamProfile;
import org.deltava.dao.DAOException;

import org.deltava.security.command.QuestionProfileAccessControl;

/**
 * A Web Site Command to support the modification of Examination Question Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionProfileCommand extends AbstractFormCommand {

    /**
     * Callback method called when saving the profile.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check our access level
		validateEditAccess(ctx);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the existing question profile, or create a new one
			QuestionProfile qp = null;
			if (ctx.getID() != 0) {
				GetExamProfiles rdao = new GetExamProfiles(con);
				qp = rdao.getQuestionProfile(ctx.getID());
				if (qp == null)
					throw new CommandException("Invalid Question Profile - " + ctx.getID());
				
				// Update question text
				qp.setQuestion(ctx.getParameter("question"));
			} else {
				qp = new QuestionProfile(ctx.getParameter("question"));
			}

			// Load the fields from the request
			qp.setCorrectAnswer(ctx.getParameter("correct"));
			qp.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			String examNames[] = ctx.getRequest().getParameterValues("examNames");
			if (examNames != null)
			   qp.setExams(Arrays.asList(examNames));

			// Get the write DAO and save the profile
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.write(qp);
			
			// Save the question in the request
			ctx.setAttribute("question", qp, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/profileUpdate.jsp");
		result.setSuccess(true);
	}

    /**
     * Callback method called when editing the profile.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Check our access level
		validateEditAccess(ctx);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the question profile
			GetExamProfiles dao = new GetExamProfiles(con);
			QuestionProfile qp = dao.getQuestionProfile(ctx.getID());
			if ((qp == null) && (ctx.getID() != 0))
				throw new CommandException("Invalid Question Profile - " + ctx.getID());
			
			// Get exam names
			ctx.setAttribute("examNames", dao.getExamProfiles(), REQUEST);

			// Save the profile in the request
			ctx.setAttribute("question", qp, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/questionProfileEdit.jsp");
		result.setSuccess(true);
	}

    /**
     * Callback method called when reading the profile.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	protected void execRead(CommandContext ctx) throws CommandException {

		// Check our access level
		QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx);
		access.validate();

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the question profile
			GetExamProfiles dao = new GetExamProfiles(con);
			QuestionProfile qp = dao.getQuestionProfile(ctx.getID());
			if (qp == null)
				throw new CommandException("Invalid Question Profile - " + ctx.getID());

			// Save the profile in the request
			ctx.setAttribute("question", qp, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/questionProfile.jsp");
		result.setSuccess(true);
	}

	/**
	 * Helper method to check edit/create/save access.
	 */
	private void validateEditAccess(CommandContext ctx) throws CommandException {
		QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx);
		access.validate();
		if (!access.getCanEdit())
			throw securityException("Cannot modify Examination Question Profile");
	}
}