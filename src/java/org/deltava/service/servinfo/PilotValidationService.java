// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom2.*;

import org.deltava.beans.servinfo.Certificate;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.GetVATSIMData;

import org.deltava.service.*;

import org.deltava.util.*;

/**
 * A Web Service to validate VATSIM membership data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class PilotValidationService extends WebService {
	
	/**
	 * Executes the Web Service, returning VATSIM CERT data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the validation URL
		String id = ctx.getParameter("id");
		if (StringUtils.isEmpty(id))
			return SC_NOT_FOUND;
		
		Certificate c = null;
		try {
			GetVATSIMData dao = new GetVATSIMData();
			c = dao.getInfo(id);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Return a 404 if not found
		if (c == null)
			return SC_NOT_FOUND;
		
		// Build the XML Document
		Document doc = new Document();
		Element re = new Element("pilot");
		doc.setRootElement(re);
		
		// Set the properties
		re.setAttribute("id", String.valueOf(c.getID()));
		re.setAttribute("network", "VATSIM");
		re.setAttribute("firstName", c.getFirstName());
		re.setAttribute("lastName", c.getLastName());
		re.setAttribute("registeredOn", StringUtils.format(c.getRegistrationDate(), "yyyy/MM/dd HH:mm"));
		re.setAttribute("active", String.valueOf(c.isActive()));
		re.setAttribute("domain", c.getEmailDomain());
		
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
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}