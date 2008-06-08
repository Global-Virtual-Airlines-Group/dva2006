// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.rss;

import java.util.*;
import java.net.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.*;
import org.deltava.security.command.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display a Discussion Forum RSS feed.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class CoolerSyndicationService extends WebService {
	
	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the channel name
		String channel = ctx.getParameter("channel");
		String forumName = SystemData.get("airline.forum");

		List<MessageThread> threads = null;
		try {
			Connection con = ctx.getConnection();
			GetCoolerChannels cdao = new GetCoolerChannels(con);

			// Get the channel and check our access to it
			if (channel != null) {
				Channel c = cdao.get(channel);
				if (c == null)
					throw error(SC_NOT_FOUND, "Invalid Channel");

				// Validate our access to the channel
				CoolerChannelAccessControl c_access = new CoolerChannelAccessControl(ctx, c);
				if (!c_access.getCanAccess())
					throw error(SC_FORBIDDEN, "Cannot access channel");
			} else {
				channel = Channel.ALL.getName();
			}

			// Get the cooler threads
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			tdao.setQueryMax(getCount(ctx, 50));
			threads = Channel.ALL.equals(channel) ? tdao.getByChannel(null, true) : tdao.getByChannel(channel, true);

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
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Generate the data element
		Document doc = new Document();
		Element re = new Element("rss");
		re.setAttribute("version", "2.0");
		doc.setRootElement(re);

		// Create the RSS channel
		Element ch = new Element("channel");
		ch.addContent(XMLUtils.createElement("title", SystemData.get("airline.name") + " " + forumName));
		ch.addContent(XMLUtils.createElement("description", SystemData.get("airline.name") + " " + forumName + " Message Threads"));
		ch.addContent(XMLUtils.createElement("link", "http://" + ctx.getRequest().getServerName() + "/channel.do?id=ALL", true));
		ch.addContent(XMLUtils.createElement("language", "en"));
		ch.addContent(XMLUtils.createElement("copyright", VersionInfo.TXT_COPYRIGHT));
		ch.addContent(XMLUtils.createElement("webMaster", SystemData.get("airline.mail.webmaster")));
		ch.addContent(XMLUtils.createElement("generator", VersionInfo.APPNAME));
		ch.addContent(XMLUtils.createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.cooler"))));
		
		// Add the channel to the document
		re.addContent(ch);

		// Convert the threads into RSS items
		for (Iterator i = threads.iterator(); i.hasNext(); ) {
			MessageThread mt = (MessageThread) i.next();
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/thread.do?id=" + StringUtils.formatHex(mt.getID()));
			
				// Create the RSS item element
				Element item = new Element("item");
				item.addContent(XMLUtils.createElement("title", mt.getSubject()));
				item.addContent(XMLUtils.createElement("link", url.toString(), true));
				item.addContent(XMLUtils.createElement("guid", url.toString(), true));
			
				// Add the item element
				ch.addContent(item);
			} catch (MalformedURLException mue) { }
		}

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return SC_OK;
	}
}