// Copyright 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;

import org.jdom2.*;

import org.deltava.beans.help.ResponseTemplate;
import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.dao.*;
import org.deltava.util.XMLUtils;

/**
 * A Web Service to return back Help Desk Response Template text.
 * @author Luke
 * @version 4.2
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
		
		Document doc = new Document();
		try {
			GetHelpTemplate dao = new GetHelpTemplate(ctx.getConnection());
			ResponseTemplate rsptmp = dao.get(ctx.getParameter("id"));
			if (rsptmp == null)
				throw error(SC_NOT_FOUND, "Invalid Response Template - " + ctx.getParameter("id"), false);
			
			// Add the data
			Element re = new Element("template");
			re.setAttribute("name", rsptmp.getTitle());
			re.addContent(XMLUtils.createElement("body", rsptmp.getBody(), true));
			doc.setRootElement(re);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
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
	 * @return TRUE always
	 */
	public final boolean isSecure() {
		return true;
	}
}