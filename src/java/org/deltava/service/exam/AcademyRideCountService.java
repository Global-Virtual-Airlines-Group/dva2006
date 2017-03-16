// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.exam;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;
import org.deltava.beans.academy.*;
import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to list the number of check rides available for a Flight Academy Certification. 
 * @author Luke
 * @version 5.3
 * @since 5.3
 */

public class AcademyRideCountService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		JSONArray ja = new JSONArray();
		try {
			GetAcademyCertifications dao = new GetAcademyCertifications(ctx.getConnection());
			Certification cert = dao.get(ctx.getParameter("id"));
			if (cert == null)
				throw new NullPointerException("Unknown Certification - " + ctx.getParameter("id"));
			
			// Check for the existing rides
			for (int x = 1; x <= cert.getRideCount(); x++) {
				AcademyRideScript sc = dao.getScript(new AcademyRideID(cert.getName(), x));
				if (sc == null)
					ja.put(x);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), true);
		} catch (Exception e) {
			throw error(SC_NOT_FOUND, e.getMessage(), false);
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(ja.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}