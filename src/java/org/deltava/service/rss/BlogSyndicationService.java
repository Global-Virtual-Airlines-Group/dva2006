// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.rss;

import java.net.*;
import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.blog.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.XMLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to create a blog RSS data feed.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class BlogSyndicationService extends WebService {

	/**
	 * Executes the Web Service, returning an RSS data stream.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		Pilot usr = null;
		Collection<Entry> entries = null;
		try {
			Connection con = ctx.getConnection();
			// Get the blog entries
			GetBlog dao = new GetBlog(con);
			dao.setQueryMax(getCount(ctx, 10));
			int authorID = StringUtils.isEmpty(ctx.getParameter("id")) ? 0 : StringUtils.parseHex(ctx.getParameter("id"));
			entries = (authorID == 0) ? dao.getAll() : dao.getLatest(authorID, false);
			
			// Get the Author
			if (authorID != 0) {
				GetPilot pdao = new GetPilot(con);
				usr = pdao.get(authorID);
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
		
		// Build the title and URL entries 
		String title = SystemData.get("airline.name") + " Journal - " + ((usr == null) ? "All Entries" : usr.getName());
		String link = "http://" + ctx.getRequest().getServerName() + "/blog.do" + ((usr == null) ? "" : "?id=" +
				StringUtils.formatHex(usr.getID()));

		// Create the RSS channel
		Element ch = new Element("channel");
		ch.addContent(XMLUtils.createElement("title", title));
		ch.addContent(XMLUtils.createElement("description", SystemData.get("airline.name") + " Journal Entries"));
		ch.addContent(XMLUtils.createElement("link", link, true));
		ch.addContent(XMLUtils.createElement("language", "en"));
		ch.addContent(XMLUtils.createElement("copyright", VersionInfo.TXT_COPYRIGHT));
		ch.addContent(XMLUtils.createElement("webMaster", SystemData.get("airline.mail.webmaster")));
		ch.addContent(XMLUtils.createElement("generator", VersionInfo.APPNAME));
		ch.addContent(XMLUtils.createElement("ttl", String.valueOf(SystemData.getInt("cache.rss.blog", 240))));
		
		// Add the channel to the document
		re.addContent(ch);

		// Convert the entries into RSS items
		for (Iterator<Entry> i = entries.iterator(); i.hasNext(); ) {
			Entry e = i.next();
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/blogentry.do?id=" + StringUtils.formatHex(e.getID()));
				
				// Create the RSS item element
				Element item = new Element("item");
				item.addContent(XMLUtils.createElement("title", e.getTitle()));
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
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}
}