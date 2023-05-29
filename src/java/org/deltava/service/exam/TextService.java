// Copyright 2005, 2007, 2008, 2012, 2016, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.exam;

import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.ExamAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Service to dynamically save examination answers.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class TextService extends WebService {
	
	private static final Logger log = LogManager.getLogger(TextService.class);
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// If this isn't a post, just return a 200
		if (!"post".equalsIgnoreCase(ctx.getRequest().getMethod()))
			return SC_OK;
		
		int examID = 0; int questionID = 0;
		try {
			examID = StringUtils.parseHex(ctx.getParameter("id"));
			questionID = Integer.parseInt(ctx.getParameter("q"));
		} catch (Exception e) {
			log.warn(e.getMessage());
		}

		// Return if invalid exam/question
		if ((examID == 0) || (questionID == 0))
			return SC_OK;
		
		Examination ex = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the examination
			GetExam dao = new GetExam(con);
			ex = dao.getExam(examID);
			if (ex == null)
				throw error(SC_NOT_FOUND, "Unknown Exam ID - " + examID, false);
			
			// Check our access to it, and stop if we cannot access it
			ExamAccessControl access = new ExamAccessControl(ctx, ex, null);
			try {
				access.validate();
				if (!access.getCanSubmit())
					throw new Exception();
			} catch (Exception e) {
				throw error(SC_FORBIDDEN, "Cannot read Exam " + examID, false);
			}

			// Get the question
			Question q = ex.getQuestion(questionID);
			if (q == null)
				throw error(SC_NOT_FOUND, "Unknown Question - " + questionID, false);

			// Save the answer
			String answer = ctx.getParameter("answer");
			if (!StringUtils.isEmpty(answer)) {
				q.setAnswer(answer);

				// Get the DAO and write the question
				SetExam wdao = new SetExam(con);
				wdao.answer(examID, q);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Return the number of seconds left
		long timeRemaining = (ex.getExpiryDate().toEpochMilli() - System.currentTimeMillis()) / 1000;
		try {
			ctx.setContentType("text/plain", "utf-8");
			ctx.print(String.valueOf(timeRemaining));
			ctx.setExpiry(1);
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
}