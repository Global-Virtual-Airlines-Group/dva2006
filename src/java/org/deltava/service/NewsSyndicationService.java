// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import javax.servlet.http.*;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.News;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.GetNews;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display a System News RSS feed.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NewsSyndicationService extends WebDataService {

	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		List entries = null;
		try {
			GetNews dao = new GetNews(_con);
			dao.setQueryMax(getCount(ctx, 10));
			entries = dao.getNews();
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
		ch.addContent(createElement("title", SystemData.get("airline.name") + " Airline News"));
		ch.addContent(createElement("link", "http://" + ctx.getRequest().getServerName() + "/news.do"));
		ch.addContent(createElement("description", "What's New at " + SystemData.get("airline.name")));
		ch.addContent(createElement("language", "en"));
		ch.addContent(createElement("copyright", VersionInfo.TXT_COPYRIGHT));
		ch.addContent(createElement("webMaster", SystemData.get("airline.mail.webmaster")));
		ch.addContent(createElement("generator", VersionInfo.APPNAME));
		ch.addContent(createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.news"))));
		
		// Add the channel
		re.addContent(ch);
		
		// Convert the entries to RSS items
		for (Iterator i = entries.iterator(); i.hasNext(); ) {
			News n = (News) i.next();
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/news.do?id=" + StringUtils.formatHex(n.getID()));
			
				// Create the RSS item element
				Element item = new Element("item");
				item.addContent(createElement("title", n.getSubject()));
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
		
		// Auto-generated method stub
		return HttpServletResponse.SC_OK;
	}
}