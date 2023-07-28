// Copyright 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;
import java.util.*;

import org.jdom2.*;

import org.deltava.beans.Pilot;

import org.deltava.crypt.MessageDigester;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.XMLUtils;

/**
 * A Web Service to display active User IDs. 
 * @author Luke
 * @version 11.1
 * @since 7.0
 */

public class UserListService extends SimFDRService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		authenticate(ctx);
		
		Collection<Pilot> users = null;
		try {
			GetPilot pdao = new GetPilot(ctx.getConnection());
			Collection<Integer> IDs = pdao.getActivePilots("ID");
			users = pdao.getByID(IDs, "PILOTS").values();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);	
		} finally {
			ctx.release();
		}
		
		// Create the XML Document
		Document doc = new Document();
		Element re = new Element("users");
		doc.setRootElement(re);
		
		MessageDigester md = new MessageDigester("MD5");
		for (Iterator<Pilot> i = users.iterator(); i.hasNext(); ) {
			Pilot p = i.next(); i.remove();
			if (p.getPilotNumber() == 0) continue;
			Element pe = new Element("user");
			pe.setAttribute("id", p.getPilotCode());
			if (!p.isInvalid()) {
				pe.setAttribute("email", MessageDigester.convert(md.digest(p.getEmail().getBytes())));
				pe.setAttribute("hash", "MD5");
				md.reset();
			}
				
			re.addContent(pe);
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.setExpiry(3600);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}