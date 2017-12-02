// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to view/take/score Pilot Examinations.
 * @author Luke
 * @version 8.1
 * @since 1.0
 */

public class ExamCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();

			// Get the examination
			GetExam dao = new GetExam(con);
			Examination ex = dao.getExam(ctx.getID());
			if (ex == null)
				throw notFoundException("Invalid Examination - " + ctx.getID());

			// Get the Pilot taking the exam
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(ex.getAuthorID());

			// Load the examination profile
			GetExamProfiles epdao = new GetExamProfiles(con);
			ExamProfile ep = epdao.getExamProfile(ex.getName());
			if (ep == null)
				throw notFoundException("Cannot load Examination Profile - " + ex.getName());

			// Check our access level
			ExamAccessControl access = new ExamAccessControl(ctx, ex, ud, ep);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot view Examination " + ctx.getID());

			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilot", pdao.get(uddao.get(ex.getAuthorID())), REQUEST);
			if (ex.getScorerID() != 0)
				ctx.setAttribute("scorer", pdao.get(uddao.get(ex.getScorerID())), REQUEST);

			// Load the route plotting question numbers and Terminal Route choices
			GetNavRoute navdao = new GetNavRoute(con);
			Collection<Integer> rpQuestions = new TreeSet<Integer>();
			for (Iterator<Question> i = ex.getQuestions().iterator(); i.hasNext();) {
				Question q = i.next();
				if (q instanceof RoutePlot)
					rpQuestions.add(Integer.valueOf(q.getNumber()));
			}

			// Display answers only if we have the necessary role
			int activeExamID = dao.getActiveExam(ctx.getUser().getID());
			if (ex.getAuthorID() == ctx.getUser().getID())
				ctx.setAttribute("showAnswers", Boolean.valueOf(access.getCanViewAnswers() && (activeExamID == 0)), REQUEST);
			else
				ctx.setAttribute("showAnswers", Boolean.valueOf(access.getCanViewAnswers()), REQUEST);

			// Load question profiles
			if (access.getCanViewAnswers()) {
				GetExamQuestions eqdao = new GetExamQuestions(con);
				Map<Integer, QuestionProfile> qInfo = new HashMap<Integer, QuestionProfile>();
				for (Question q : ex.getQuestions()) {
					QuestionProfile qp = eqdao.getQuestionProfile(q.getID());
					if (qp != null)
						qInfo.put(Integer.valueOf(q.getID()), qp);
				}

				ctx.setAttribute("qStats", qInfo, REQUEST);
			}

			// Determine what we will do with the examination
			String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
			if (access.getCanSubmit()) {
				// Return current time (+2 seconds) for time offset sync
				ctx.setAttribute("currentTime", Long.valueOf(System.currentTimeMillis() + 2000), REQUEST);

				// Save SID/STAR and forward to the testing JSP
				result.setURL("/jsp/testing/examTake.jsp");
			} else {
				Map<Integer, Collection<NavigationDataBean>> cRoutes = new HashMap<Integer, Collection<NavigationDataBean>>();
				Map<Integer, Collection<NavigationDataBean>> aRoutes = new HashMap<Integer, Collection<NavigationDataBean>>();
				for (Iterator<Question> i = ex.getQuestions().iterator(); i.hasNext();) {
					Question q = i.next();
					if (q instanceof RoutePlot) {
						RoutePlotQuestion rpq = (RoutePlotQuestion) q;
						List<NavigationDataBean> cR = navdao.getRouteWaypoints(q.getCorrectAnswer(), rpq.getAirportD());
						cR.add(0, new AirportLocation(rpq.getAirportD()));
						cR.add(new AirportLocation(rpq.getAirportA()));
						List<NavigationDataBean> aR = navdao.getRouteWaypoints(q.getAnswer(), rpq.getAirportD());
						aR.add(0, new AirportLocation(rpq.getAirportD()));
						aR.add(new AirportLocation(rpq.getAirportA()));
						cRoutes.put(Integer.valueOf(q.getNumber()), cR);
						aRoutes.put(Integer.valueOf(q.getNumber()), aR);
					}
				}

				// Save routes
				ctx.setAttribute("aRoutes", aRoutes, REQUEST);
				ctx.setAttribute("cRoutes", cRoutes, REQUEST);

				// Determine if we are scoring or viewing
				boolean isScore = ((ex.getStatus() != TestStatus.SCORED) && access.getCanScore());
				isScore |= (("edit".equals(opName)) && (access.getCanEdit()));
				result.setURL(isScore ? "/jsp/testing/examScore.jsp" : "/jsp/testing/examView.jsp");
			}

			// Save route plot questions
			ctx.setAttribute("rpQuestions", rpQuestions, REQUEST);

			// Save the exam and access in the request
			ctx.setAttribute("exam", ex, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("hasQImages", Boolean.valueOf(ex.hasImage()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setSuccess(true);
	}
}