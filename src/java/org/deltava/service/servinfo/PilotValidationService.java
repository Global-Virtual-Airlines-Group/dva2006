// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.net.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.servinfo.Pilot;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.GetVATSIMData;

import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to validate VATSIM membership data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotValidationService extends WebService {
	
	/**
	 * Executes the Web Service, returning VATSIM CERT data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the validation URL
		String uri = SystemData.get("online.vatsim.validation_url");
		if (uri == null)
			return SC_NOT_FOUND;
		
		Pilot p = null;
		try {
			URL url = new URL(uri + "?id=" + ctx.getParameter("id")); 
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(2500);
			con.setReadTimeout(7500);
			
			// Get the DAO
			GetVATSIMData dao = new GetVATSIMData(con);
			p = dao.getInfo();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, ie.getMessage());
		}
		
		// Build the XML Document
		Document doc = new Document();
		Element re = new Element("pilot");
		doc.setRootElement(re);
		
		// Set the properties
		re.setAttribute("id", ctx.getParameter("id"));
		re.setAttribute("network", "VATSIM");
		re.setAttribute("name", p.getName());
		re.setAttribute("status", p.getComments());
		re.setAttribute("domain", p.getEquipmentCode());
		re.setAttribute("nameOK", String.valueOf(p.getName().equals(ctx.getParameter("name"))));
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Return success code
		return SC_OK;
	}
	
	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
}