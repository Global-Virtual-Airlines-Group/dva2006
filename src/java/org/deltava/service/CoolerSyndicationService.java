// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import javax.servlet.http.*;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.GetCoolerChannels;
import org.deltava.dao.GetCoolerThreads;
import org.deltava.dao.DAOException;

import org.deltava.security.command.CoolerChannelAccessControl;
import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display a Water Cooler RSS feed.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CoolerSyndicationService extends WebDataService {
	
	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the channel name
		String channel = ctx.getRequest().getParameter("channel");

		List threads = null;
		try {
			GetCoolerChannels cdao = new GetCoolerChannels(_con);

			// Get the channel and check our access to it
			if (channel != null) {
				Channel c = cdao.get(channel);
				if (c == null)
					throw new ServiceException(HttpServletResponse.SC_NOT_FOUND, "Invalid Channel");

				// Validate our access to the channel
				CoolerChannelAccessControl c_access = new CoolerChannelAccessControl(ctx, c);
				if (!c_access.getCanAccess())
					throw new ServiceException(HttpServletResponse.SC_FORBIDDEN, "Cannot access channel");
			} else {
				channel = Channel.ALL;
			}

			// Get the cooler threads
			GetCoolerThreads tdao = new GetCoolerThreads(_con);
			tdao.setQueryMax(getCount(ctx, 50));
			threads = Channel.ALL.equals(channel) ? tdao.getAll(true) : tdao.getByChannel(channel, true);

			// Filter out threads based on our access
			CoolerThreadAccessControl tac = new CoolerThreadAccessControl(ctx);
			for (Iterator i = threads.iterator(); i.hasNext();) {
				MessageThread thread = (MessageThread) i.next();

				// Get this thread's channel and see if we can read it
				Channel c = cdao.get(thread.getChannel());
				tac.updateContext(thread, c);
				try {
					tac.validate();
				} catch (Exception e) {
				}

				// If we cannot read the thread, remove it from the results
				if (!tac.getCanRead())
					i.remove();
			}
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
		ch.addContent(createElement("title", SystemData.get("airline.name") + " Water Cooler"));
		ch.addContent(createElement("description", SystemData.get("airline.name") + " Water Cooler Message Threads"));
		ch.addContent(createElement("link", "http://" + ctx.getRequest().getServerName() + "/channel.do?id=ALL"));
		ch.addContent(createElement("language", "en"));
		ch.addContent(createElement("copyright", VersionInfo.TXT_COPYRIGHT));
		ch.addContent(createElement("webMaster", SystemData.get("airline.mail.webmaster")));
		ch.addContent(createElement("generator", VersionInfo.APPNAME));
		ch.addContent(createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.cooler"))));
		
		// Add the channel to the document
		re.addContent(ch);

		// Convert the threads into RSS items
		for (Iterator i = threads.iterator(); i.hasNext(); ) {
			MessageThread mt = (MessageThread) i.next();
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/thread.do?id=" + StringUtils.formatHex(mt.getID()));
			
				// Create the RSS item element
				Element item = new Element("item");
				item.addContent(createElement("title", mt.getSubject()));
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

		// Return success code
		return HttpServletResponse.SC_OK;
	}
}