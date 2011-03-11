// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to list individuals who have passed Flight Academy Certifications.
 * @author Luke
 * @version 3.6
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
		try {
			Connection con = ctx.getConnection();
			
			// Load Certifications
			GetAcademyCertifications crdao = new GetAcademyCertifications(con);
			ctx.setAttribute("certs", crdao.getAll(), REQUEST);
			
			// Get our certification
			Certification cert = crdao.get(ctx.getParameter("cert"));
			ctx.setAttribute("cert", cert, REQUEST);
			
			// If we have a certification, load the users
			ViewContext vc = initView(ctx);
			if (cert != null) {
				GetAcademyCourses cdao = new GetAcademyCourses(con);
				cdao.setQueryStart(vc.getStart());
				cdao.setQueryMax(vc.getCount());
				Collection<Course> courses = cdao.getByStatus(Course.COMPLETE, "C.ENDDATE", cert);
				vc.setResults(courses);
					
				// Load the Pilot IDs
				Collection<Integer> IDs = new HashSet<Integer>();
				for (Course c : courses)
					IDs.add(new Integer(c.getPilotID()));
				
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