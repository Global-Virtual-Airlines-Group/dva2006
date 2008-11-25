// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.exam;

import java.util.*;
import java.sql.Connection;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.apache.log4j.Logger;

import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.security.command.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to plot maps for route plotting Examination questions. 
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class RoutePlotService extends MapPlotService {
	
	private static final Logger log = Logger.getLogger(RoutePlotService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
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
			
			// Get the departure/arrival airports
			RoutePlotQuestion rpq = (RoutePlotQuestion) q;
			TerminalRoute sid = null;
			TerminalRoute star = null;

			// Check if we have a SID
			GetNavRoute dao = new GetNavRoute(con);
			routePoints.add(new AirportLocation(rpq.getAirportD()));
			sid = dao.getRoute(ctx.getParameter("sid"));
			if (sid != null)
				routePoints.addAll(sid.getWaypoints());
			
			// Check if we have a STAR
			star = dao.getRoute(ctx.getParameter("star"));
			
			// Add the route waypoints
			String rt = ctx.getParameter("route"); 
			if (!StringUtils.isEmpty(rt)) {
				Collection<String> wps = new LinkedHashSet<String>(StringUtils.split(rt.trim(), " "));
				wps.remove(rpq.getAirportD().getICAO());
				wps.remove(rpq.getAirportA().getICAO());
				
				// Remove SID/STAR waypoints
				if (sid != null) {
					for (NavigationDataBean nd : sid.getWaypoints())
						if (!nd.getCode().equals(sid.getTransition()))
							wps.remove(nd.getCode());
				}
				if (star != null) {
					for (NavigationDataBean nd : star.getWaypoints())
						if (!nd.getCode().equals(star.getTransition()))
							wps.remove(nd.getCode());
				}
				
				// Do the route
				rt = StringUtils.listConcat(wps, " ");
				List<NavigationDataBean> points = dao.getRouteWaypoints(rt, rpq.getAirportD());
				routePoints.addAll(points);
			}
			
			// Build the route
			if (star != null)
				routePoints.addAll(star.getWaypoints());
			routePoints.add(new AirportLocation(rpq.getAirportA()));
			
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
		} catch (AccessControlException ace) {
			throw error(SC_INTERNAL_SERVER_ERROR, ace.getMessage());
		} catch (IllegalArgumentException e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			ctx.release();
		}
			
		// Convert the points into a List
		List<NavigationDataBean> points = new ArrayList<NavigationDataBean>(routePoints);
		
		// Convert points to an XML document
		Document doc = formatPoints(points, true);
		Element re = doc.getRootElement();
		
		// Return the number of seconds left
		long timeRemaining = (ex.getExpiryDate().getTime() - System.currentTimeMillis()) / 1000;
		re.setAttribute("timeLeft", String.valueOf(timeRemaining));
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
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
}