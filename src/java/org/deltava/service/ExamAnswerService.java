// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.deltava.beans.testing.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Service to dynamically save examination answers.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamAnswerService extends WebService {
	
	private static final Logger log = Logger.getLogger(ExamAnswerService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// If this isn't a post, just return a 200
		if (!ctx.getRequest().getMethod().equalsIgnoreCase("post"))
			return SC_OK;

		// Get the exam ID and question number
		int examID = 0;
		int questionNo = 0;
		try {
			examID = StringUtils.parseHex(ctx.getParameter("id"));
			questionNo = StringUtils.parse(ctx.getParameter("q"), 0);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		
		// Return if invalid exam/question
		if ((examID == 0) || (questionNo == 0))
			return SC_OK;
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the examination
			GetExam dao = new GetExam(con);
			Examination ex = dao.getExam(examID);
			if (ex == null)
				throw new ServiceException(SC_NOT_FOUND, "Unknown Exam ID - " + examID, false);
			
			// Check our access to it, and stop if we cannot access it
			ExamAccessControl access = new ExamAccessControl(ctx, ex, null);
			try {
				access.validate();
				if (!access.getCanSubmit())
					throw new Exception();
			} catch (Exception e) {
				throw new ServiceException(SC_FORBIDDEN, "Cannot read Exam " + examID, false);
			}

			// Get the question
			Question q = ex.getQuestion(questionNo);
			if (q == null)
				throw new ServiceException(SC_NOT_FOUND, "Unknown Question - " + questionNo, false);

			// Save the answer
			String answer = ctx.getParameter("answer");
			if (!StringUtils.isEmpty(answer)) {
				q.setAnswer(answer);

				// Get the DAO and write the question
				SetExam wdao = new SetExam(con);
				wdao.answer(examID, q);
			}
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Return success code
		return SC_OK;
	}

	/**
	 * Returns if the Web Service requires authentication.
	 * @return TRUE
	 */
	public final boolean isSecure() {
		return true;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}