// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

import java.io.*;
import java.time.Instant;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.util.*;

/**
 * A parser for SimBrief XML dispatch packages.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class SimBriefParser {

	// static class
	private SimBriefParser() {
		super();
	}
	
	private static void validateElements(Element root, String... names) {
		for (String e : names) {
			Element ee = root.getChild(e);
			if (ee == null)
				throw new IllegalStateException(String.format("No %s element in root", e));
		}
	}
	
	/**
	 * Parses a SimBrief XML briefing package.
	 * @param r the XML Reader
	 * @return a SimBrief bean
	 */
	public static BriefingPackage parse(Reader r) {
		
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(r);
		} catch (IOException | JDOMException ie) {
			throw new IllegalStateException(ie);
		}
		
		Element re = doc.getRootElement();
		validateElements(re, "params", "general", "origin", "destination", "files");
		
		// Create the bean
		Element pe = re.getChild("params");
		BriefingPackage sb = new BriefingPackage(StringUtils.parse(pe.getChildTextTrim("static_id"), 0));
		sb.setCreatedOn(Instant.ofEpochSecond(StringUtils.parse(pe.getChildTextTrim("time_generated"), 0) * 1000));
		sb.setAIRAC(StringUtils.parse(pe.getChildTextTrim("airac"), 2208));
		sb.setRoute(XMLUtils.findChild(re, "general").getChildTextTrim("route_navigraph"));
		sb.setRunwayD(XMLUtils.findChild(re, "origin").getChildTextTrim("plan_rwy"));
		sb.setRunwayA(XMLUtils.findChild(re, "destination").getChildTextTrim("plan_rwy"));
		
		// Parse flight plans
		Element fe = re.getChild("files");
		sb.setBasePlanURL(fe.getChildTextTrim("directory"));
		for (Element fpe : fe.getChildren("file")) {
			FlightPlan fp = new FlightPlan(fpe.getChildTextTrim("name"), fpe.getChildTextTrim("link"));
			sb.addPlan(fp);
		}
		
		return sb;
	}
}