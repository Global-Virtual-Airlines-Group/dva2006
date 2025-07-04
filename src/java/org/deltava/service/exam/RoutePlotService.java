// Copyright 2008, 2009, 2010, 2012, 2016, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.exam;

import java.util.*;
import java.sql.Connection;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.security.command.*;
import org.deltava.service.*;
import org.deltava.util.StringUtils;

/**
 * A Web Service to plot maps for route plotting Examination questions. 
 * @author Luke
 * @version 11.1
 * @since 2.3
 */

public class RoutePlotService extends MapPlotService {
	
	private static final Logger log = LogManager.getLogger(RoutePlotService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		int examID = 0; int questionID = 0;
		try {
			examID = StringUtils.parseHex(ctx.getParameter("id"));
			questionID = Integer.parseInt(ctx.getParameter("q"));
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
		
		Examination ex = null;
		Collection<NavigationDataBean> routePoints = new LinkedHashSet<NavigationDataBean>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the examination
			GetExam exdao = new GetExam(con);
			ex = exdao.getExam(examID);
			if (ex == null)
				throw new IllegalArgumentException("Cannot find Examination " + examID);
			
			// Get the question
			Question q = ex.getQuestion(questionID);
			if (q == null)
				throw new IllegalArgumentException("Cannot find Question " + questionID);
			else if (!(q instanceof RoutePlot))
				throw new IllegalArgumentException("Question " + questionID + " is not a route plotting question");
			
			// Check our access to the exam, and stop if we cannot access it
			ExamAccessControl access = new ExamAccessControl(ctx, ex, null);
			access.validate();
			if (!access.getCanSubmit())
				throw new IllegalArgumentException("Cannot submit Examination");

			// Check the answer
			String answer = ctx.getParameter("route");
			if (answer == null)
				throw new IllegalArgumentException("No Answer");
			
			// Get the departure/arrival airports
			RoutePlotQuestion rpq = (RoutePlotQuestion) q;
			routePoints.add(new AirportLocation(rpq.getAirportD()));
			GetNavRoute dao = new GetNavRoute(con);

			// Parse the answer
			List<String> wps = StringUtils.split(answer, " ");
			wps.remove(rpq.getAirportD().getICAO());
			wps.remove(rpq.getAirportA().getICAO());
			if ((wps.size() > 1) && (wps.getFirst().indexOf('.') != -1)) {
				TerminalRoute sid = dao.getRoute(rpq.getAirportD(), TerminalRoute.Type.SID, wps.getFirst());
				if (sid != null) {
					routePoints.addAll(sid.getWaypoints());
					wps.remove(0);
					for (NavigationDataBean nd : sid.getWaypoints())
						if (!nd.getCode().equals(sid.getTransition()))
							wps.remove(nd.getCode());
				}
			}
			
			// Check if we have a STAR
			TerminalRoute star = null;
			if ((wps.size() > 1) && (wps.get(wps.size() - 1).indexOf('.') != -1)) {
				star = dao.getRoute(rpq.getAirportA(), TerminalRoute.Type.STAR, wps.getLast());
				if (star != null) {
					wps.remove(wps.size() - 1);
					for (NavigationDataBean nd : star.getWaypoints())
						if (!nd.getCode().equals(star.getTransition()))
							wps.remove(nd.getCode());
				}
			}
			
			// Load the route
			String rt = StringUtils.listConcat(wps, " ");
			routePoints.addAll(dao.getRouteWaypoints(rt, rpq.getAirportD()));
			
			// Add the star and the destination
			if (star != null)
				routePoints.addAll(star.getWaypoints());
			routePoints.add(new AirportLocation(rpq.getAirportA()));
			
			// Save the answer
			if (!StringUtils.isEmpty(answer)) {
				q.setAnswer(answer);
				
				// Get the DAO and write the question
				SetExam wdao = new SetExam(con);
				wdao.answer(examID, q);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} catch (AccessControlException ace) {
			throw error(SC_INTERNAL_SERVER_ERROR, ace.getMessage());
		} catch (IllegalArgumentException e) {
			throw error(SC_BAD_REQUEST, e.getMessage());
		} finally {
			ctx.release();
		}
			
		// Convert points to a JSON document
		JSONObject jo = formatPoints(new ArrayList<NavigationDataBean>(routePoints));
		
		// Return the number of seconds left
		long timeRemaining = (ex.getExpiryDate().toEpochMilli() - System.currentTimeMillis()) / 1000;
		jo.put("timeLeft", timeRemaining);
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	/**
	 * Returns if the Web Service requires authentication.
	 * @return TRUE
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}