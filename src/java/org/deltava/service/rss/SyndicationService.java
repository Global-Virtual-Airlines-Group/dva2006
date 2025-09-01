// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.rss;

import org.jdom2.*;
import org.deltava.beans.system.VersionInfo;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;


/**
 * An abstract Web Service to support RSS feed generation.
 * @author Luke
 * @version 12.2
 * @since 12.2
 */

abstract class SyndicationService extends WebService {

	/**
	 * Helper method to return the number of entries to display.
	 * @param sctxt the Service Context
	 * @param defaultValue the default number of entries
	 * @return the value of the count parameter, or defaultVlue
	 */
	protected static int getCount(ServiceContext sctxt, int defaultValue) {
		return StringUtils.parse(sctxt.getRequest().getParameter("count"), defaultValue);
	}
	
	/**
	 * Creates the initial RSS document. 
	 * @param title the RSS feed title
	 * @param desc the RSS feed description
	 * @param url the RSS feed URL
	 * @return a Document with the RSS header
	 */
	protected static Document initRSS(String title, String desc, String url) {
		Document doc = new Document();
		Element re = new Element("rss");
		re.setAttribute("version", "2.0");
		doc.setRootElement(re);
		
		// Create the RSS channel
		Element ch = new Element("channel");
		ch.addContent(XMLUtils.createElement("title", title));
		ch.addContent(XMLUtils.createElement("description", desc));
		ch.addContent(XMLUtils.createElement("link", url));
		ch.addContent(XMLUtils.createElement("language", "en"));
		ch.addContent(XMLUtils.createElement("copyright", VersionInfo.TXT_COPYRIGHT));
		ch.addContent(XMLUtils.createElement("webMaster", SystemData.get("airline.mail.webmaster")));
		ch.addContent(XMLUtils.createElement("generator", VersionInfo.getAppName()));
		re.addContent(ch);
		return doc;
	}
}