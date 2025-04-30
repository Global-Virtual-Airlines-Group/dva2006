// Copyright 2010, 2012, 2015, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;

import org.json.*;

import org.deltava.beans.help.ResponseTemplate;
import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.dao.*;

/**
 * A Web Service to return back Help Desk Response Template text.
 * @author Luke
 * @version 11.6
 * @since 3.2
 */

public class IssueTemplateService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check our access
		try {
			HelpDeskAccessControl access = new HelpDeskAccessControl(ctx, null);
			access.validate();
			if (!access.getCanUseTemplate())
				throw new SecurityException();
		} catch (Exception e) {
			throw error(SC_UNAUTHORIZED, "Cannot use Response Template", false);
		}
		
		JSONObject jo = new JSONObject();
		try {
			GetHelpTemplate dao = new GetHelpTemplate(ctx.getConnection());
			ResponseTemplate rsptmp = dao.get(ctx.getParameter("id"));
			if (rsptmp == null)
				throw error(SC_NOT_FOUND, "Invalid Response Template - " + ctx.getParameter("id"), false);
			
			// Add the data
			jo.put("name", rsptmp.getTitle());
			jo.put("body", rsptmp.getBody());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}
}