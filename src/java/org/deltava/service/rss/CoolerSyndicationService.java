// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2015, 2017, 2021, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.rss;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.net.*;
import java.io.IOException;
import java.sql.Connection;

import org.jdom2.*;

import org.deltava.beans.cooler.*;

import org.deltava.dao.*;
import org.deltava.security.command.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display a Discussion Forum RSS feed.
 * @author Luke
 * @version 12.2
 * @since 1.0
 */

public class CoolerSyndicationService extends SyndicationService {
	
	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
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
					throw error(SC_NOT_FOUND, "Invalid Channel", false);

				// Validate our access to the channel
				CoolerChannelAccessControl c_access = new CoolerChannelAccessControl(ctx, c);
				if (!c_access.getCanAccess())
					throw error(SC_FORBIDDEN, "Cannot access channel", false);
			} else {
				channel = Channel.ALL.getName();
			}

			// Get the cooler threads
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			tdao.setQueryMax(getCount(ctx, 50));
			threads = Channel.ALL.getName().equals(channel) ? tdao.getByChannel(null, true) : tdao.getByChannel(channel, true);

			// Filter out threads based on our access
			CoolerThreadAccessControl tac = new CoolerThreadAccessControl(ctx);
			for (Iterator<MessageThread> i = threads.iterator(); i.hasNext();) {
				MessageThread thread = i.next();

				// Get this thread's channel and see if we can read it
				Channel c = cdao.get(thread.getChannel());
				tac.updateContext(thread, c);
				try {
					tac.validate();
					if (!tac.getCanRead())
						i.remove();
				} catch (Exception e) {
					i.remove();
				}
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Build the core RSS document
		String alName = SystemData.get("airline.name"); String serverName = ctx.getRequest().getServerName();
		Document doc = initRSS(String.format("%s %s", alName, forumName), String.format("%s %s Message Threads", alName, forumName), String.format("https://%s/channel.do?id=ALL", serverName));
		Element ch = doc.getRootElement().getChild("channel");
		ch.addContent(XMLUtils.createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.cooler", 10))));

		// Convert the threads into RSS items
		for (MessageThread mt : threads) {
			try {
				URI url = new URI("https", serverName, "/thread.do?id=" + StringUtils.formatHex(mt.getID()));
				Element item = new Element("item");
				item.addContent(XMLUtils.createElement("title", mt.getSubject()));
				item.addContent(XMLUtils.createElement("link", url.toString(), true));
				item.addContent(XMLUtils.createElement("guid", url.toString(), true));
				ch.addContent(item);
			} catch (URISyntaxException se) {
				// empty
			}
		}

		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "utf-8");
			ctx.setExpiry(300);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
}