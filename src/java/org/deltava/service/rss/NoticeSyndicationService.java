// Copyright 2005, 2006, 2007, 2008, 2012, 2015, 2016, 2021, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.rss;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.net.*;
import java.util.*;
import java.io.IOException;

import org.jdom2.*;

import org.deltava.beans.News;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display a Notice to Airmen (NOTAM) RSS feed.
 * @author Luke
 * @version 12.2
 * @since 1.0
 */

public class NoticeSyndicationService extends SyndicationService {

	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		List<? extends News> entries = null;
		try {
			GetNews dao = new GetNews(ctx.getConnection());
			dao.setQueryMax(getCount(ctx, 10));
			entries = dao.getActiveNOTAMs();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Build the core RSS document
		String alName = SystemData.get("airline.name"); String serverName = ctx.getRequest().getServerName();
		Document doc = initRSS(String.format("%s NOTAMs", alName), String.format("%s Notices to Aviators", alName), String.format("https://%s/notams.do", serverName));
		Element ch = doc.getRootElement().getChild("channel");
		ch.addContent(XMLUtils.createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.news", 1440))));

		// Convert the entries to RSS items
		for (News n : entries) {
			try {
				URI url = new URI("https", serverName, "/notam.do?id=" + StringUtils.formatHex(n.getID()));
				Element item = new Element("item");
				item.addContent(XMLUtils.createElement("title", n.getSubject()));
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
			ctx.setExpiry(3600);
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
}