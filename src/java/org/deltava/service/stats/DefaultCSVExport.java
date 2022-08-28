// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import org.deltava.beans.flight.FlightReport;

import org.deltava.util.StringUtils;

/**
 * A log book export class to generate default CSV logbooks.  
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

class DefaultCSVExport extends CSVExport {

	/**
	 * Creates the exporter.
	 */
	DefaultCSVExport() {
		super("Date,Submitted,Flight,Network,Departed,DCode,Arrived,ACode,Equipment,Distance,Time,ACARS,Promotion");
	}
	
	@Override
	public void add(FlightReport fr) {
		
		StringBuilder buf = new StringBuilder(); 
		buf.append(StringUtils.format(fr.getDate(), "MM/dd/yyyy"));
		buf.append(',');
		buf.append((fr.getSubmittedOn() == null) ? "-" : StringUtils.format(fr.getSubmittedOn(), "MM/dd/yyyy"));
		buf.append(',');
		buf.append(fr.getFlightCode());
		buf.append(',');
		buf.append((fr.getNetwork() == null) ? "-" : fr.getNetwork().toString());
		buf.append(',');
		buf.append(fr.getAirportD().getName());
		buf.append(',');
		buf.append(fr.getAirportD().getIATA());
		buf.append(',');
		buf.append(fr.getAirportA().getName());
		buf.append(',');
		buf.append(fr.getAirportA().getIATA());
		buf.append(',');
		buf.append(fr.getEquipmentType());
		buf.append(',');
		buf.append(String.valueOf(fr.getDistance()));
		buf.append(',');
		buf.append(StringUtils.format(fr.getLength() / 10.0f, "#0.0"));
		buf.append(',');
		buf.append(fr.hasAttribute(FlightReport.ATTR_ACARS) ? "Y" : "-");
		
		int promoCount = getPromotionCount(fr);
		if (promoCount > 0) {
			buf.append(',');
			buf.append(promoCount);
			buf.append(',');
			buf.append(StringUtils.listConcat(fr.getCaptEQType(), " "));
		}

		writeln(buf);
	}
}