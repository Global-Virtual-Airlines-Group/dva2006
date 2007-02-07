// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.rss;

import java.util.*;
import java.net.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.News;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display a Notice to Airmen (NOTAM) RSS feed.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NoticeSyndicationService extends WebService {

	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		List entries = null;
		try {
			GetNews dao = new GetNews(ctx.getConnection());
			dao.setQueryMax(getCount(ctx, 10));
			entries = dao.getActiveNOTAMs();
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
		ch.addContent(XMLUtils.createElement("title", SystemData.get("airline.name") + " NOTAMs"));
		ch
				.addContent(XMLUtils.createElement("link", "http://" + ctx.getRequest().getServerName() + "/notams.do",
						true));
		ch.addContent(XMLUtils.createElement("description", SystemData.get("airline.name") + " Notices to Airmen"));
		ch.addContent(XMLUtils.createElement("language", "en"));
		ch.addContent(XMLUtils.createElement("copyright", VersionInfo.TXT_COPYRIGHT));
		ch.addContent(XMLUtils.createElement("webMaster", SystemData.get("airline.mail.webmaster")));
		ch.addContent(XMLUtils.createElement("generator", VersionInfo.APPNAME));
		ch.addContent(XMLUtils.createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.news"))));

		// Add the channel
		re.addContent(ch);

		// Convert the entries to RSS items
		for (Iterator i = entries.iterator(); i.hasNext();) {
			News n = (News) i.next();
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/notam.do?id="
						+ StringUtils.formatHex(n.getID()));

				// Create the RSS item element
				Element item = new Element("item");
				item.addContent(XMLUtils.createElement("title", n.getSubject()));
				item.addContent(XMLUtils.createElement("link", url.toString(), true));
				item.addContent(XMLUtils.createElement("guid", url.toString(), true));

				// Add the item element
				ch.addContent(item);
			} catch (MalformedURLException mue) {
			}
		}

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}

		// Return result code
		return SC_OK;
	}
}