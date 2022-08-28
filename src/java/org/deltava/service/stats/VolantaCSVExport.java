// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import java.time.*;
import java.time.format.DateTimeFormatter;

import org.deltava.beans.flight.*;

import org.deltava.util.StringUtils;

/**
 * A log book export class to generate Volanta-formatted CSV logbooks.  
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

class VolantaCSVExport extends CSVExport {
	
	private final DateTimeFormatter _df = DateTimeFormatter.ofPattern("HH:mm");

	/**
	 * Creates the exporter.
	 */
	VolantaCSVExport() {
		super("Origin,Destination,DepartureTime,Duration,Airline,FlightNumber,AircraftType,Distance");
	}
	
	@Override
	public void add(FlightReport fr) {
		
		// Only format FDRFlightReports
		if (fr.getFDR() == null) return;
		FDRFlightReport fdr = (FDRFlightReport) fr;
		
		// Write data
		StringBuilder buf = new StringBuilder(); 
		buf.append(fdr.getAirportD().getICAO());
		buf.append(',');
		buf.append(fdr.getAirportA().getICAO());
		buf.append(',');
		buf.append((fdr.getTakeoffTime() == null) ? "-" : StringUtils.format(fdr.getTakeoffTime(), "HH:mm"));
		buf.append(',');
		ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(fdr.getDuration().getSeconds()), ZoneId.of("Z"));
		buf.append(_df.format(zdt));
		buf.append(',');
		buf.append(fr.getAirline().getCode());
		buf.append(',');
		buf.append(StringUtils.format(fr.getFlightNumber(), "#000"));
		buf.append(',');
		buf.append(fr.getEquipmentType());
		buf.append(',');
		buf.append(String.valueOf(fr.getDistance()));
		buf.append(',');
		writeln(buf);
	}
}