// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.IOException;
import java.net.*;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.jdom.*;
import org.jdom.output.*;	

import org.deltava.beans.event.Event;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.GetEvent;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display an Online Event RSS feed.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventSyndicationService extends WebDataService {

	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		List entries = null;
		try {
			GetEvent dao = new GetEvent(_con);
			dao.setQueryMax(getCount(ctx, 5));
			entries = dao.getEvents();
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Generate the data element
		Document doc = new Document();
		Element re = new Element("rss");
		re.setAttribute("version", "2.0");
		doc.setRootElement(re);
		
		// Create the RSS channel
		Element ch = new Element("channel");
		ch.addContent(createElement("title", SystemData.get("airline.name") + " Online Events"));
		ch.addContent(createElement("link", "http://" + ctx.getRequest().getServerName() + "/event.do"));
		ch.addContent(createElement("description", "Online Events at " + SystemData.get("airline.name")));
		ch.addContent(createElement("language", "en"));
		ch.addContent(createElement("copyright", VersionInfo.TXT_COPYRIGHT));
		ch.addContent(createElement("webMaster", SystemData.get("airline.mail.webmaster")));
		ch.addContent(createElement("generator", VersionInfo.APPNAME));
		ch.addContent(createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.events"))));

		// Add the channel
		re.addContent(ch);
		
		// Convert the entries to RSS items
		for (Iterator i = entries.iterator(); i.hasNext(); ) {
			Event e = (Event) i.next();
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/event.do?id=" + StringUtils.formatHex(e.getID()));
				
				// Create the RSS item element
				Element item = new Element("item");
				item.addContent(createElement("title", e.getName()));
				item.addContent(createElement("link", url.toString()));
				item.addContent(createElement("guid", url.toString()));

				// Add the item element
				ch.addContent(item);
			} catch (MalformedURLException mue) { }
		}
		
		// Dump the XML to the output stream
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		String xml = xmlOut.outputString(doc);
		ctx.getResponse().setContentType("text/xml");
		ctx.getResponse().setContentLength(xml.length());

		try {
			ctx.getResponse().getWriter().println(xml);
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}
		
		// Return result code
		return HttpServletResponse.SC_OK;
	}
}