// Copyright 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import org.deltava.beans.WeightUnit;
import org.deltava.beans.flight.ETOPS;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A parser for SimBrief XML dispatch packages.
 * @author Luke
 * @version 11.2
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
		validateElements(re, "params", "general", "origin", "destination", "alternate", "files", "fuel", "atc", "aircraft", "weights");
		
		// Create the bean
		Element pe = re.getChild("params");
		String wtu = pe.getChildTextTrim("units");
		WeightUnit wt = EnumUtils.parse(WeightUnit.class, wtu.substring(0, wtu.length() - 1), WeightUnit.LB);
		PackageFormat fmt = PackageFormat.parse(pe.getChildTextTrim("ofp_layout"), PackageFormat.LIDO); 
		BriefingPackage sb = new BriefingPackage(StringUtils.parse(pe.getChildTextTrim("static_id"), 0), fmt);
		sb.setRequestID(pe.getChildTextTrim("request_id"));
		sb.setReleaseVersion(StringUtils.parse(XMLUtils.getChildText(re, "general", "release"), 0));
		sb.setSimBriefUserID(pe.getChildTextTrim("user_id"));
		sb.setCreatedOn(Instant.ofEpochSecond(StringUtils.parse(pe.getChildTextTrim("time_generated"), 0)));
		sb.setAIRAC(StringUtils.parse(pe.getChildTextTrim("airac"), 2208));
		sb.setRunwayD(XMLUtils.getChildText(re, "origin", "plan_rwy"));
		sb.setRunwayA(XMLUtils.getChildText(re, "destination", "plan_rwy"));
		sb.setTailCode(XMLUtils.getChildText(re, "aircraft", "reg"));
		String airframeID = XMLUtils.getChildText(re, "api_params", "type");
		if ((airframeID != null) && (airframeID.length() > 8))
			sb.setAirframeID(airframeID);
	
		re.getChildren("alternate").stream().map(ae -> SystemData.getAirport(ae.getChildTextTrim("iata_code"))).filter(Objects::nonNull).forEach(sb::addAirportL);
		sb.setXML(doc);
		
		// Parse route
		List<String> wps = StringUtils.split(XMLUtils.getChildText(re, "general", "route_navigraph"), " ");
		while (wps.contains("DCT")) wps.remove("DCT");
		sb.setRoute(StringUtils.listConcat(wps, " "));
		
		// Calculate initial altitude
		int alt = StringUtils.parse(XMLUtils.getChildText(re,  "atc", "initial_alt"), 0);
		if (alt > 0)
			sb.setCruiseAltitude(String.valueOf(alt * 100));
		
		// Load fuel
		sb.setBaseFuel(parseWeight(StringUtils.parse(XMLUtils.getChildText(re, "fuel", "reserve"), 0) + StringUtils.parse(XMLUtils.getChildText(re, "fuel", "contingency"), 0), wt));
		sb.setTaxiFuel(parseWeight(StringUtils.parse(XMLUtils.getChildText(re, "fuel", "taxi"), 0), wt));
		sb.setEnrouteFuel(parseWeight(StringUtils.parse(XMLUtils.getChildText(re, "fuel", "enroute_burn"), 0), wt));
		sb.setAlternateFuel(parseWeight(StringUtils.parse(XMLUtils.getChildText(re, "fuel", "alternate_burn"), 0), wt));
		
		// Load payload
		sb.setPax(StringUtils.parse(XMLUtils.getChildText(re, "weights", "pax_count_actual"), 0));
		sb.setBagWeight(StringUtils.parse(XMLUtils.getChildText(re, "weights", "bag_weight"), 55));
		sb.setBaggageWeight(StringUtils.parse(XMLUtils.getChildText(re, "weights", "bag_count_actual"), 0) * sb.getBagWeight());
		sb.setCargoWeight(StringUtils.parse(XMLUtils.getChildText(re, "weights", "freight_added"), 0));
		
		// Check for ETOPS
		Element ee = re.getChild("etops");
		if (ee != null) {
			sb.setETOPS(EnumUtils.parse(ETOPS.class, "ETOPS" + ee.getChildTextTrim("rule"), sb.getETOPS()));
			sb.setETOPSMidpoint(new GeoPosition(StringUtils.parse(XMLUtils.getChildText(ee, "equal_time_point", "pos_lat"), 0d), StringUtils.parse(XMLUtils.getChildText(ee, "equal_time_point", "pos_long"), 0d)));
			ee.getChildren("suitable_airport").stream().map(ae -> SystemData.getAirport(ae.getChildTextTrim("icao_code"))).filter(Objects::nonNull).forEach(sb::addETOPSAlternate);
		}
		
		// Parse flight plans
		Element fe = re.getChild("files");
		sb.setBasePlanURL(fe.getChildTextTrim("directory"));
		Collection<Element> plans = CollectionUtils.join(fe.getChildren("file"), fe.getChildren("pdf"));
		plans.stream().map(fpe -> new FlightPlan(fpe.getChildTextTrim("name"), fpe.getChildTextTrim("link"))).forEach(sb::addPlan);
		return sb;
	}

	/**
	 * Parses a SimBrief error message.
	 * @param xml the XML error message
	 * @return the error message
	 */
	public static String parseError(String xml) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new StringReader(xml));
			return XMLUtils.getChildText(doc.getRootElement(), "fetch", "status");
		} catch (IOException | JDOMException ie) {
			return null;
		}
	}
	
	private static int parseWeight(int wt, WeightUnit dstUnit) {
		return (int)Math.round(wt / dstUnit.getFactor());
	}
}