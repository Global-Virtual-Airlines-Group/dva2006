// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.jdom.*;

import org.deltava.beans.acars.Bandwidth;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display ACARS bandwidth statistics to an Amline Flash chart.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class BandwidthInfoService extends WebService {
	
	private static final String[] TITLES = {"Connections", "Messages In (1000)", "Messages Out (1000)", 
		"Bytes In (MB)", "Bytes Out (MB)", "Max Connections", "Max Messages (1000)", "Max Bytes (MB)"};

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get hourly or daily
		boolean isDaily = Boolean.valueOf(ctx.getParameter("daily")).booleanValue();
		boolean isRaw = Boolean.valueOf(ctx.getParameter("raw")).booleanValue();
		int maxCols = isRaw ? 5 : 8;
		
		// Get stats
		Collection<Bandwidth> stats = new ArrayList<Bandwidth>();
		try {
			GetACARSBandwidth bwdao = new GetACARSBandwidth(ctx.getConnection());
			if (!isRaw) {
				bwdao.setQueryMax(isDaily ? 30 : 24);
				stats = isDaily ? bwdao.getDaily() : bwdao.getHourly();
			} else
				stats = bwdao.getRaw();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("chart");
		doc.setRootElement(re);

		// Generate X-axis
		Element xae = new Element("xaxis");
		re.addContent(xae);
		
		// Generate Y- axes
		Element[] axes = new Element[8];
		Element ae = new Element("graphs");
		re.addContent(ae);
		for (int x = 1; x <= maxCols; x++) {
			Element e = new Element("graph");
			e.setAttribute("gid", String.valueOf(x));
			e.setAttribute("title", TITLES[x - 1]);
			e.setAttribute("unit", " flights");
			axes[x - 1] = e;
			ae.addContent(e);
		}
		
		// Create the entries
		int xid = 1;
		for (Bandwidth bw : stats) {
			Element xe = new Element("value");
			xe.setAttribute("xid", String.valueOf(xid));
			xae.addContent(xe);
			if (isDaily)
				xe.setText(StringUtils.format(bw.getDate(), "MMMM dd yyyy"));
			else if (!isRaw)
				xe.setText(StringUtils.format(bw.getDate(), "MMMM dd yyyy HH") + ":00");
			else
				xe.setText(StringUtils.format(bw.getDate(), "MMMM dd yyyy HH:MM"));
			
			// Add the Y-axis
			Element ve = new Element("value");
			ve.setAttribute("xid", String.valueOf(xid));
			
			// Add Connections
			Element ve2 = (Element) ve.clone();
			ve2.setText(String.valueOf(bw.getConnections()));
			axes[0].addContent(ve2);
			
			// Add Messages In
			Element ve3 = (Element) ve.clone();
			ve3.setText(String.valueOf(bw.getMsgsIn() / 1000));
			axes[1].addContent(ve3);
			
			// Add Messages Out
			Element ve4 = (Element) ve.clone();
			ve4.setText(String.valueOf(bw.getMsgsOut() / 1000));
			axes[2].addContent(ve4);
			
			// Add Bytes In
			Element ve5 = (Element) ve.clone();
			ve5.setText(String.valueOf(bw.getBytesIn() / 1000000));
			axes[3].addContent(ve5);

			// Add Bytes Out
			Element ve6 = (Element) ve.clone();
			ve6.setText(String.valueOf(bw.getBytesOut() / 1000000));
			axes[4].addContent(ve6);
			
			if (!isRaw) {
				// Add Max Connections
				Element ve7 = (Element) ve.clone();
				ve7.setText(String.valueOf(bw.getMaxConnections()));
				axes[5].addContent(ve7);

				// Add Max Messages
				Element ve8 = (Element) ve.clone();
				ve8.setText(String.valueOf(bw.getMaxMsgs() / 1000));
				axes[6].addContent(ve8);

				// Add Max Bytes
				Element ve9 = (Element) ve.clone();
				ve9.setText(String.valueOf(bw.getMaxBytes() / 1000000));
				axes[7].addContent(ve9);
			}
			
			xid++;
		}
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(XMLUtils.format(doc, "ISO-8859-1"));
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error");
		}
		
		// Return success code
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}
	
	/**
	 * Tells the Web Service Servlet to secure this Service.
	 * @return TRUE
	 */
	public final boolean isSecure() {
		return true;
	}
}