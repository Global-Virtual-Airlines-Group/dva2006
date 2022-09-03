// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

import java.io.*;
import java.util.Collection;
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
		validateElements(re, "params", "general", "origin", "destination", "files", "fuel");
		
		// Create the bean
		Element pe = re.getChild("params");
		BriefingPackage sb = new BriefingPackage(StringUtils.parse(pe.getChildTextTrim("static_id"), 0));
		sb.setCreatedOn(Instant.ofEpochSecond(StringUtils.parse(pe.getChildTextTrim("time_generated"), 0) * 1000));
		sb.setAIRAC(StringUtils.parse(pe.getChildTextTrim("airac"), 2208));
		sb.setRoute(XMLUtils.getChildText(re, "general", "route_navigraph"));
		sb.setRunwayD(XMLUtils.getChildText(re, "origin", "plan_rwy"));
		sb.setRunwayA(XMLUtils.getChildText(re, "destination", "plan_rwy"));
		
		// Load fuel
		sb.setBaseFuel(StringUtils.parse(XMLUtils.getChildText(re, "fuel", "reserve"), 0) + StringUtils.parse(XMLUtils.getChildText(re, "fuel", "contingency"), 0));
		sb.setTaxiFuel(StringUtils.parse(XMLUtils.getChildText(re,  "fuel", "taxi"), 0));
		sb.setEnrouteFuel(StringUtils.parse(XMLUtils.getChildText(re,  "fuel", "enroute_burn"), 0));
		sb.setAlternateFuel(StringUtils.parse(XMLUtils.getChildText(re,  "fuel", "alternate_burn"), 0));
		
		// Parse flight plans
		Element fe = re.getChild("files");
		sb.setBasePlanURL(fe.getChildTextTrim("directory"));
		Collection<Element> plans = CollectionUtils.join(fe.getChildren("file"), fe.getChildren("pdf"));
		for (Element fpe : plans) {
			FlightPlan fp = new FlightPlan(fpe.getChildTextTrim("name"), fpe.getChildTextTrim("link"));
			sb.addPlan(fp);
		}
		
		return sb;
	}
}