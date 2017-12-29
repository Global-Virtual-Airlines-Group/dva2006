// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.system.AirlineInformation;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.comparators.AirportComparator;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionProfileAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to support the modification of Examination Question Profiles.
 * @author Luke
 * @version 8.1
 * @since 1.0
 */

public class QuestionProfileCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the existing question profile, or create a new one
			QuestionProfile qp = null; QuestionProfile oqp = null;
			if (ctx.getID() != 0) {
				GetExamQuestions rdao = new GetExamQuestions(con);
				qp = rdao.getQuestionProfile(ctx.getID());
				if (qp == null)
					throw notFoundException("Invalid Question Profile - " + ctx.getID());

				// Update question text / answer
				oqp = BeanUtils.clone(qp);
				qp.setQuestion(ctx.getParameter("question"));
				if (qp instanceof MultipleChoice) {
					MultiChoiceQuestionProfile mqp = (MultiChoiceQuestionProfile) qp;
					mqp.setCorrectAnswer(ctx.getParameter("correctChoice"));
					if (qp instanceof RoutePlot) {
						RoutePlotQuestionProfile rqp = (RoutePlotQuestionProfile) qp;
						rqp.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
						rqp.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
						rqp.setChoices(new HashSet<String>());
						rqp.addChoice(ctx.getParameter("route1"));
						rqp.addChoice(ctx.getParameter("route2"));
						rqp.addChoice(ctx.getParameter("route3"));
						rqp.addChoice(ctx.getParameter("route4"));
						rqp.addChoice(ctx.getParameter("route5"));
					} else
						mqp.setChoices(StringUtils.split(ctx.getParameter("answerChoices"), "\n"));
				} else
					qp.setCorrectAnswer(ctx.getParameter("correct"));
			} else {
				// Check if we're creating a multiple-choice or route plot question
				boolean isMC = Boolean.valueOf(ctx.getParameter("isMultiChoice")).booleanValue();
				boolean isRP = Boolean.valueOf(ctx.getParameter("isRoutePlot")).booleanValue();
				if (isRP) {
					RoutePlotQuestionProfile rqp = new RoutePlotQuestionProfile(ctx.getParameter("question"));
					rqp.setCorrectAnswer(ctx.getParameter("correctChoice"));
					rqp.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
					rqp.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
					rqp.addChoice(ctx.getParameter("route1"));
					rqp.addChoice(ctx.getParameter("route2"));
					rqp.addChoice(ctx.getParameter("route3"));
					rqp.addChoice(ctx.getParameter("route4"));
					rqp.addChoice(ctx.getParameter("route5"));
					qp = rqp;
				} else if (isMC) {
					MultiChoiceQuestionProfile mqp = new MultiChoiceQuestionProfile(ctx.getParameter("question"));
					mqp.setChoices(StringUtils.split(ctx.getParameter("answerChoices"), "\n"));
					mqp.setCorrectAnswer(ctx.getParameter("correctChoice"));
					qp = mqp;
				} else {
					qp = new QuestionProfile(ctx.getParameter("question"));
					qp.setCorrectAnswer(ctx.getParameter("correct"));
				}

				qp.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			}

			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot modify Examination Question Profile");

			// Load the fields from the request
			Collection<String> myExamNames = new HashSet<String>(ctx.getParameters("examNames", Collections.emptySet())); 
			qp.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			qp.setAirlines(ctx.getParameters("airlines", Collections.emptySet()).stream().map(ac -> SystemData.getApp(ac)).filter(Objects::nonNull).collect(Collectors.toSet()));
			
			// Check if we are in any exams for airlines not included
			GetExamProfiles epdao = new GetExamProfiles(con); final QuestionProfile fqp = qp; 
			Collection<ExamProfile> qExams = epdao.getAllExamProfiles().stream().filter(exp -> fqp.getExams().contains(exp.getName())).collect(Collectors.toSet());
			myExamNames.addAll(qExams.stream().filter(ep -> fqp.getAirlines().contains(ep.getOwner())).map(ExamProfile::getName).collect(Collectors.toSet())); // load exams from other airlines
			qp.setExams(myExamNames);
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oqp, qp, "inputStream");
			AuditLog ae = AuditLog.create(qp, delta, ctx.getUser().getID());

			// Start a transaction
			ctx.startTX();

			// Save the profile
			SetExamQuestion wdao = new SetExamQuestion(con);
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
			
			// Write audit log
			ctx.setAttribute("question", qp, REQUEST);
			writeAuditLog(ctx, ae);
			ctx.commitTX();
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
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		boolean doEdit = false; boolean isRP = false;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the question profile
			GetExamQuestions eqdao = new GetExamQuestions(con);
			QuestionProfile qp = eqdao.getQuestionProfile(ctx.getID());
			if ((qp == null) && (ctx.getID() != 0))
				throw notFoundException("Invalid Question Profile - " + ctx.getID());

			// Validate our access
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			if (!access.getCanEdit() && !access.getCanInclude())
				throw securityException("Cannot modify Examination Question Profile");

			// If we cannot edit, we're just including
			doEdit = access.getCanEdit();
			ctx.setAttribute("access", access, REQUEST);
			readAuditLog(ctx, qp);

			// Get exam names - if we're editing,
			AirlineInformation ourAirline = SystemData.getApp(null);
			GetExamProfiles epdao = new GetExamProfiles(con);
			Collection<ExamProfile> allExams = epdao.getAllExamProfiles();
			Collection<ExamProfile> myExams = allExams.stream().filter(ex -> ex.getOwner().equals(ourAirline)).collect(Collectors.toList());
			if (doEdit) {
				ctx.setAttribute("examNames", myExams, REQUEST);
				if (qp != null)
					ctx.setAttribute("otherExamNames", CollectionUtils.getDelta(allExams, myExams).stream().filter(ex -> qp.getExams().contains(ex.getName())).collect(Collectors.toSet()), REQUEST);
			} else
				ctx.setAttribute("examNames", myExams, REQUEST);

			// Set the center of the map
			if (qp == null) {
				ctx.setAttribute("mapCenter", SystemData.getAirport(ctx.getUser().getHomeAirport()), REQUEST);
				isRP = Boolean.valueOf(ctx.getParameter("isRP")).booleanValue();
			} else if (qp instanceof RoutePlot)
				isRP = true;
			
			// Load SID/STAR list if a route plot question
			if (isRP && (qp != null)) {
				RoutePlot rp = (RoutePlot) qp;
				ctx.setAttribute("mapCenter", rp.getMidPoint(), REQUEST);
				
				GetNavRoute trdao = new GetNavRoute(con);
				ctx.setAttribute("sids", trdao.getRoutes(rp.getAirportD(), TerminalRoute.Type.SID), REQUEST);
				ctx.setAttribute("stars", trdao.getRoutes(rp.getAirportA(), TerminalRoute.Type.STAR), REQUEST);
			}

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

		// Save airports if route plotting
		if (isRP) {
			Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
			airports.addAll(SystemData.getAirports().values());
			ctx.setAttribute("airports", airports, REQUEST);
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setSuccess(true);
		if (!doEdit)
			result.setURL("/jsp/testing/questionProfileInclude.jsp");
		else
			result.setURL("/jsp/testing/" + (isRP ? "questionProfileEditRP.jsp" : "questionProfileEdit.jsp"));
	}

	/**
	 * Callback method called when reading the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and load the question profile
			GetExamQuestions dao = new GetExamQuestions(con);
			QuestionProfile qp = dao.getQuestionProfile(ctx.getID());
			if (qp == null)
				throw notFoundException("Invalid Question Profile - " + ctx.getID());

			// Check our access level
			QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, qp);
			access.validate();
			readAuditLog(ctx, qp);

			// Display route
			if (qp instanceof RoutePlot) {
				RoutePlotQuestionProfile rp = (RoutePlotQuestionProfile) qp;
				List<String> wps = StringUtils.split(rp.getCorrectAnswer(), " ");
				
				GetNavRoute rtdao = new GetNavRoute(con);
				Collection<NavigationDataBean> rt = new LinkedHashSet<NavigationDataBean>();
				rt.add(new AirportLocation(rp.getAirportD()));
				if ((wps.size() > 1) && (wps.get(0).indexOf('.') != -1)) {
					TerminalRoute sid = rtdao.getRoute(rp.getAirportD(), TerminalRoute.Type.SID, wps.get(0));
					if (sid != null) {
						rt.addAll(sid.getWaypoints());
						wps.remove(0);
					}
				}
				
				rt.addAll(rtdao.getRouteWaypoints(StringUtils.listConcat(wps, " "), rp.getAirportD()));
				if ((wps.size() > 1) && (wps.get(wps.size() - 1).indexOf('.') != -1)) {
					TerminalRoute star = rtdao.getRoute(rp.getAirportA(), TerminalRoute.Type.STAR, wps.get(wps.size() - 1));
					if (star != null)
						rt.addAll(star.getWaypoints());
				}
				
				rt.add(new AirportLocation(rp.getAirportA()));
				ctx.setAttribute("route", rt, REQUEST);
			}

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