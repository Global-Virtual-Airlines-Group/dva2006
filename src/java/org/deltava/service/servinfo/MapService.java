// Copyright 2010, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;

import org.jdom2.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display an online network map. 
 * @author Luke
 * @version 7.0
 * @since 3.2
 */

public class MapService extends WebService {

	/**
	 * Executes the Web Service, returning ServInfo route data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the network name
		String networkName = ctx.getParameter("network");
		if (networkName == null)
			networkName = SystemData.get("online.default_network");
		
		// Get the network data
		OnlineNetwork net = OnlineNetwork.valueOf(networkName.toUpperCase());
		NetworkInfo info = ServInfoHelper.getInfo(net);
		
		// Populate pilot IDs if required
		if (!info.hasPilotIDs()) {
			try {
				GetPilotOnline dao = new GetPilotOnline(ctx.getConnection());
				Map<String, Integer> idMap = dao.getIDs(net);
				info.setPilotIDs(idMap);
			} catch (DAOException de) {
				throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
			} finally {
				ctx.release();
			}
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		re.setAttribute("date", String.valueOf(info.getValidDate().toEpochMilli()));
		doc.setRootElement(re);
		
		// Display the pilots
		List<?> codes = (List<?>) SystemData.getObject("online.highlightCodes");
		for (Iterator<Pilot> i = info.getPilots().iterator(); i.hasNext();) {
			Pilot usr = i.next();
			for (Iterator<?> ci = codes.iterator(); (ci.hasNext() && !usr.isHighlighted()); ) {
				String code = (String) ci.next();
				if (usr.getCallsign().startsWith(code))
					usr.setHighlighted(true);
			}
			
			Element pe = new Element("pilot");
			pe.setAttribute("id", String.valueOf(usr.getID()));
			pe.setAttribute("callsign", usr.getCallsign());
			pe.setAttribute("lat", StringUtils.format(usr.getLatitude(), "##0.00000"));
			pe.setAttribute("lng", StringUtils.format(usr.getLongitude(), "##0.00000"));
			pe.setAttribute("color", usr.getIconColor());
			pe.addContent(new CDATA(usr.getInfoBox()));
			re.addContent(pe);
		}
		
		// Display the controllers if required
		boolean doATC = Boolean.valueOf(ctx.getParameter("atc")).booleanValue();
		if (doATC) {
			for (Iterator<Controller> i = info.getControllers().iterator(); i.hasNext(); ) {
				Controller usr = i.next();
				if ((usr.getFacility() != Facility.FSS) && (usr.getFacility() != Facility.CTR) && (usr.getFacility() != Facility.APP))
					continue;
			
				Element ae = new Element("atc");
				ae.setAttribute("id", String.valueOf(usr.getID()));
				ae.setAttribute("callsign", usr.getCallsign());
				ae.setAttribute("type", String.valueOf(usr.getFacility()));
				ae.setAttribute("lat", StringUtils.format(usr.getLatitude(), "##0.00000"));
				ae.setAttribute("lng", StringUtils.format(usr.getLongitude(), "##0.00000"));
				ae.setAttribute("color", usr.getIconColor());	
				ae.setAttribute("range", String.valueOf(usr.getFacility().getRange()));
				ae.addContent(new CDATA(usr.getInfoBox()));
				re.addContent(ae);
			}
		}
		
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