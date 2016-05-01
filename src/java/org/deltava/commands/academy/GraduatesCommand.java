// Copyright 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to list individuals who have passed Flight Academy Certifications.
 * @author Luke
 * @version 7.0
 * @since 3.6
 */

public class GraduatesCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		boolean showAll = ctx.isUserInRole("HR") || ctx.isUserInRole("Instructor") || ctx.isUserInRole("AcademyAdmin") || ctx.isUserInRole("AcademyAudit");
		try {
			Connection con = ctx.getConnection();
			
			// Load Certifications
			GetAcademyCertifications crdao = new GetAcademyCertifications(con);
			ctx.setAttribute("certs", crdao.getWithGraduates(!showAll), REQUEST);
			
			// Get our certification
			Certification cert = crdao.get(ctx.getParameter("cert"));
			ctx.setAttribute("cert", cert, REQUEST);
			
			// If we have a certification, load the users
			ViewContext<Course> vc = initView(ctx,Course.class);
			if (cert != null) {
				GetAcademyCourses cdao = new GetAcademyCourses(con);
				cdao.setQueryStart(vc.getStart());
				cdao.setQueryMax(vc.getCount());
				vc.setResults(cdao.getByStatus(Status.COMPLETE, "C.ENDDATE", cert));
					
				// Load the Pilot IDs
				Collection<Integer> IDs = vc.getResults().stream().map(Course::getPilotID).collect(Collectors.toSet());
				
				// Load the Pilots
				GetUserData uddao = new GetUserData(con);
				GetPilot pdao = new GetPilot(con);
				UserDataMap udm = uddao.get(IDs);
				ctx.setAttribute("userData", udm, REQUEST);
				ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/graduates.jsp");
		result.setSuccess(true);
	}
}