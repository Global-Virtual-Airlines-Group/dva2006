// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.hr;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.jdom.*;

import org.deltava.beans.Pilot;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.service.ServiceContext;
import org.deltava.service.ServiceException;
import org.deltava.service.WebService;
import org.deltava.util.XMLUtils;

/**
 * A Web Service to display Pilots eligible for promotion to Senior Captain.
 * @author Luke
 * @version 3.3
 * @since 3.3
 */

public class PilotNominationService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get eligible pilots
		Collection<Pilot> pilots = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
		try {
			GetPilotRecognition pdao = new GetPilotRecognition(ctx.getConnection());
			pilots.addAll(pdao.getByID(pdao.getNominationEligible(), "PILOTS").values());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Create the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);
		
		// Add each pilot
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
			Pilot p = i.next();
			Element pe = new Element("pilot");
			pe.setAttribute("id", p.getHexID());
			pe.setAttribute("name", p.getName());
			pe.setAttribute("code", p.getPilotCode());
			re.addContent(pe);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setExpiry(1800);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}