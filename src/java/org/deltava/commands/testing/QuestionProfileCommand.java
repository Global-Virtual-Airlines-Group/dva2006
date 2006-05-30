// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionProfileAccessControl;

import org.deltava.util.StringUtils;

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
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the existing question profile, or create a new one
			QuestionProfile qp = null;
			if (ctx.getID() != 0) {
				GetExamProfiles rdao = new GetExamProfiles(con);
				qp = rdao.getQuestionProfile(ctx.getID());
				if (qp == null)
					throw notFoundException("Invalid Question Profile - " + ctx.getID());
				
				// Update question text / answer
				qp.setQuestion(ctx.getParameter("question"));
				qp.setCorrectAnswer(ctx.getParameter((qp instanceof MultipleChoice) ? "correctChoice" : "correct"));
			} else {
				// Check if we're creating a multiple-choice question
				boolean isMC = Boolean.valueOf(ctx.getParameter("isMultiChoice")).booleanValue();
				if (isMC) {
					MultiChoiceQuestionProfile mqp = new MultiChoiceQuestionProfile(ctx.getParameter("question"));
					mqp.setChoices(StringUtils.split(ctx.getParameter("answerChoices"), "\n"));
					mqp.setCorrectAnswer(ctx.getParameter("correctChoice"));
					qp = mqp;
				} else {
					qp = new QuestionProfile(ctx.getParameter("question"));
					qp.setCorrectAnswer(ctx.getParameter("correct"));
				}
			}
			
			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot modify Examination Question Profile");
			
			// Load the fields from the request
			qp.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			Collection<String> examNames = ctx.getParameters("examNames");
			if (examNames != null)
			   qp.setExams(examNames);
			
			// Start a transaction
			ctx.startTX();
			
			// Save the profile
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.write(qp);
			
			// Save/delete the image
			FileUpload imgData = ctx.getFile("imgData");
			boolean clearImg = Boolean.valueOf(ctx.getParameter("clearImg")).booleanValue();
			if (clearImg)
				wdao.clearImage(qp.getID());
			else if (imgData != null) {
				qp.load(imgData.getBuffer());
				wdao.writeImage(qp);
			}

			// Commit the transaction
			ctx.commitTX();
			
			// Save the question in the request
			ctx.setAttribute("question", qp, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/profileUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}

    /**
     * Callback method called when editing the profile.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the question profile
			GetExamProfiles dao = new GetExamProfiles(con);
			QuestionProfile qp = dao.getQuestionProfile(ctx.getID());
			if ((qp == null) && (ctx.getID() != 0))
				throw notFoundException("Invalid Question Profile - " + ctx.getID());
			
			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot modify Examination Question Profile");
			
			// Get exam names
			ctx.setAttribute("examNames", dao.getExamProfiles(), REQUEST);

			// Save the profile in the request
			ctx.setAttribute("question", qp, REQUEST);
			if (qp instanceof MultipleChoice) {
				MultipleChoice mqp = (MultipleChoice) qp;
				ctx.setAttribute("qChoices", StringUtils.listConcat(mqp.getChoices(), "\n"), REQUEST);
			}
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
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the question profile
			GetExamProfiles dao = new GetExamProfiles(con);
			QuestionProfile qp = dao.getQuestionProfile(ctx.getID());
			if (qp == null)
				throw notFoundException("Invalid Question Profile - " + ctx.getID());
			
			// Check our access level
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();

			// Save the profile in the request
			ctx.setAttribute("question", qp, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
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
}