// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom2.*;

import org.deltava.beans.schedule.Aircraft;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.XMLUtils;

/**
 * A SimFDR Web Service to display a list of active Pilot IDs.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class AvailableEquipmentService extends SimFDRService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		authenticate(ctx);
		
		Collection<String> iataCodes = new TreeSet<String>();
		try {
			GetAircraft acdao = new GetAircraft(ctx.getConnection());
			Collection<Aircraft> acTypes = acdao.getAircraftTypes();
			acTypes.forEach(a -> iataCodes.addAll(a.getIATA()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);			
		} finally {
			ctx.release();
		}
		
		// Create the XML Document
		Document doc = new Document();
		Element re = new Element("aircraft");
		doc.setRootElement(re);
		for (String iata : iataCodes) {
			Element ae = new Element("aircraft");
			ae.setAttribute("icao", iata);
			re.addContent(ae);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setExpiry(7200);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE always
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}