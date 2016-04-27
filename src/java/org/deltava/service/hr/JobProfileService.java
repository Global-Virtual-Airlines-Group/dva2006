// Copyright 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.hr;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;

import org.jdom2.*;

import org.deltava.beans.hr.Profile;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;

/**
 * A Web Service to return a saved Applicant profile to a user.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class JobProfileService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		Profile p = null;
		try {
			GetJobProfiles dao = new GetJobProfiles(ctx.getConnection());
			p = dao.getProfile(ctx.getUser().getID());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		if (p == null)
			return SC_NOT_FOUND;
		
		// Create the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		re.setAttribute("id", String.valueOf(p.getID()));
		re.addContent(XMLUtils.createElement("body", p.getBody(), true));
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
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