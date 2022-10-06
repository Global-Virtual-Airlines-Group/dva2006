// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

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
	 * @param xml the XML
	 * @return a SimBrief bean
	 */
	public static BriefingPackage parse(String xml) {
		
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(new StringReader(xml));
		} catch (IOException | JDOMException ie) {
			throw new IllegalStateException(ie);
		}
		
		// Validate XML elements
		Element re = doc.getRootElement();
		validateElements(re, "params", "general", "origin", "destination", "alternate", "files", "fuel", "atc");
		
		// Create the bean
		Element pe = re.getChild("params");
		PackageFormat fmt = EnumUtils.parse(PackageFormat.class, pe.getChildTextTrim("ofp_layout"), PackageFormat.LIDO); 
		BriefingPackage sb = new BriefingPackage(StringUtils.parse(pe.getChildTextTrim("static_id"), 0), fmt);
		sb.setSimBriefUserID(pe.getChildTextTrim("user_id"));
		sb.setCreatedOn(Instant.ofEpochSecond(StringUtils.parse(pe.getChildTextTrim("time_generated"), 0)));
		sb.setAIRAC(StringUtils.parse(pe.getChildTextTrim("airac"), 2208));
		sb.setRunwayD(XMLUtils.getChildText(re, "origin", "plan_rwy"));
		sb.setRunwayA(XMLUtils.getChildText(re, "destination", "plan_rwy"));
		re.getChildren("alternate").stream().map(ae -> SystemData.getAirport(ae.getChildTextTrim("iata_code"))).filter(Objects::nonNull).forEach(sb::addAirportL);
		sb.setXML(doc);
		
		// Parse route
		List<String> wps = StringUtils.split(XMLUtils.getChildText(re, "general", "route_navigraph"),  " ");
		while (wps.contains("DCT")) wps.remove("DCT");
		sb.setRoute(StringUtils.listConcat(wps, " "));
		
		// Calculate initial altitude
		int alt = StringUtils.parse(XMLUtils.getChildText(re,  "atc", "initial_alt"), 0);
		if (alt > 0)
			sb.setCruiseAltitude(String.valueOf(alt * 100));
		
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