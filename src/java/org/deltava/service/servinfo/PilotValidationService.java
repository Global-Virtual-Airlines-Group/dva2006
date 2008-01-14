// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.net.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;

import org.jdom.*;

import org.deltava.beans.servinfo.Pilot;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.GetVATSIMData;

import org.deltava.service.*;

import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to validate VATSIM membership data.
 * @author Luke
 * @version 2.1
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
			
			// Init the HTTP client
			HttpClient hc = new HttpClient();
			hc.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
			hc.getParams().setParameter("http.useragent",  VersionInfo.USERAGENT);
			hc.getParams().setParameter("http.tcp.nodelay", Boolean.TRUE);
			hc.getParams().setParameter("http.socket.timeout", new Integer(7500));
			hc.getParams().setParameter("http.connection.timeout", new Integer(2000));
			
			// Open the connection
			GetMethod gm = new GetMethod(url.toExternalForm());
			gm.setFollowRedirects(false);
			
			// Get the DAO
			hc.executeMethod(gm);
			GetVATSIMData dao = new GetVATSIMData(gm.getResponseBodyAsStream());
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