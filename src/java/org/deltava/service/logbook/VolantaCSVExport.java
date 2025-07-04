// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import org.apache.logging.log4j.*;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.util.StringUtils;

/**
 * A log book export class to generate Volanta-formatted CSV logbooks.  
 * @author Luke
 * @version 11.1
 * @since 10.3
 */

class VolantaCSVExport extends CSVExport {
	
	private static final Logger log = LogManager.getLogger(VolantaCSVExport.class);
	
	/**
	 * Creates the exporter.
	 */
	public VolantaCSVExport() {
		super("Origin,Destination,DepartureTime,Duration,Airline,FlightNumber,AircraftType,Distance");
	}
	
	@Override
	public void add(FlightReport fr) {
		
		// Only format FDRFlightReports
		if (fr.getFDR() == null) return;
		FDRFlightReport fdr = (FDRFlightReport) fr;
		Aircraft ac = getAircraft(fdr.getEquipmentType());
		if (ac == null)
			log.warn("Unknown Equipment for Flight {} - {}", Integer.valueOf(fr.getID()), fr.getEquipmentType());
		
		// Write data
		StringBuilder buf = new StringBuilder(); 
		buf.append(fdr.getAirportD().getICAO());
		buf.append(',');
		buf.append(fdr.getAirportA().getICAO());
		buf.append(',');
		buf.append((fdr.getTakeoffTime() == null) ? "-" : StringUtils.format(fdr.getTakeoffTime(), "MM/dd/yyyy HH:mm"));
		buf.append(',');
		buf.append(fr.getDuration().toMinutes());
		buf.append(',');
		//buf.append(fr.getAirline().getCode()); //optional - blank
		buf.append(',');
		buf.append(fr.getShortCode());
		buf.append(',');
		buf.append((ac == null) ? "ZZZZ" : ac.getICAO());
		buf.append(',');
		buf.append(String.valueOf(fr.getDistance()));
		writeln(buf);
	}
}