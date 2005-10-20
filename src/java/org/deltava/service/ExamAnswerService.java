// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import javax.servlet.http.HttpServletResponse;

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

public class ExamAnswerService extends WebDataService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
   public int execute(ServiceContext ctx) throws ServiceException {
      
      // If this isn't a post, just return a 200
      if (!ctx.getRequest().getMethod().equalsIgnoreCase("post"))
         return HttpServletResponse.SC_OK;

      // Get the exam ID and question number
      int examID = StringUtils.parseHex(ctx.getParameter("id"));
      int questionNo = Integer.parseInt(ctx.getParameter("q"));
      String answer = ctx.getParameter("answer");
      
      try {
         // Get the examination
         GetExam dao = new GetExam(_con);
         Examination ex = dao.getExam(examID);
         if (ex == null)
            throw new ServiceException(HttpServletResponse.SC_NOT_FOUND, "Unknown Exam ID - " + examID);
         
         // Check our access to it, and stop if we cannot access it
         ExamAccessControl access = new ExamAccessControl(ctx, ex);
         try {
            access.validate();
            if (!access.getCanSubmit())
               throw new Exception();
         } catch (Exception e) {
            throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Cannot read Exam");
         }
         
         // Get the question
         Question q = ex.getQuestion(questionNo);
         if (q == null)
            throw new ServiceException(HttpServletResponse.SC_NOT_FOUND, "Unknown Question - " + questionNo);
         
         // Save the answer
         if (!StringUtils.isEmpty(answer)) {
            q.setAnswer(answer);
            
            // Get the DAO and write the question
            SetExam wdao = new SetExam(_con);
            wdao.answer(examID, q);
         }
      } catch (DAOException de) {
         throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
      }
      
      // Return success code
      return HttpServletResponse.SC_OK;
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