// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.time.Instant;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.beans.flight.SimBrief;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.XMLUtils;

/**
 * A Data Access Object to parse SimBrief XML data.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class GetSimBrief extends DAO {

	/**
	 * Creates the Data Access Object.
	 * @param is the InputStream
	 */
	public GetSimBrief(InputStream is) {
		super(is);
	}
	
	private static void validateElements(Element root, String... names) throws JDOMException {
		for (String e : names) {
			Element ee = root.getChild(e);
			if (ee == null)
				throw new JDOMException(String.format("No %s element in root", e));
		}
	}

	/**
	 * Parses the SimBrief payload.
	 * @return a SimBrief bean
	 * @throws DAOException if an I/O error occurs
	 */
	public SimBrief parse() throws DAOException {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(getStream());
			Element re = doc.getRootElement();
			validateElements(re, "params", "general", "origin", "destination", "files");
			
			// Create the bean
			Element pe = re.getChild("params");
			SimBrief sb = new SimBrief(StringUtils.parse(pe.getChildTextTrim("static_id"), 0));
			sb.setCreatedOn(Instant.ofEpochSecond(StringUtils.parse(pe.getChildTextTrim("time_generated"), 0) * 1000));
			sb.setAIRAC(StringUtils.parse(pe.getChildTextTrim("airac"), 2208));
			sb.setRoute(XMLUtils.findChild(re, "general").getChildTextTrim("route_navigraph"));
			sb.setRunwayD(XMLUtils.findChild(re, "origin").getChildTextTrim("plan_rwy"));
			sb.setRunwayA(XMLUtils.findChild(re, "destination").getChildTextTrim("plan_rwy"));
			return sb;
		} catch (IOException | JDOMException e) {
			throw new DAOException(e);
		}
	}
}