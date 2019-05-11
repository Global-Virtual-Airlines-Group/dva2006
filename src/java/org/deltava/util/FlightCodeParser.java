// Copyright 2005, 2009, 2010, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.deltava.beans.Flight;
import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A utility class to parse flight codes.
 * @author Luke
 * @version 8.4
 * @since 1.0
 */

public class FlightCodeParser {

	// Singleton
	private FlightCodeParser() {
		super();
	}

	/**
	 * Parses a flight code into a schedule entry. 
	 * @param fCode the flight Code
	 * @return the Schedule entry, or null
	 */
	public static Flight parse(String fCode) {
		return parse(fCode, SystemData.get("airline.code"));
	}
	
	/**
	 * Parses a flight code into a schedule entry. 
	 * @param fCode the flight Code
	 * @param defaultAirlineCode the default airline code to use
	 * @return the Schedule entry, or null
	 */
	public static Flight parse(String fCode, String defaultAirlineCode) {
		if (StringUtils.isEmpty(fCode))
			return null;
		
		// Instantiate temporary objects
		StringBuilder aCode = new StringBuilder();
		StringBuilder fNumber = new StringBuilder();
		
		// Split based on leg
		int lPos = fCode.indexOf(" Leg ");
		String code = (lPos < 1) ? fCode : fCode.substring(0, lPos); 
		for (int x = 0; x < code.length(); x++) {
			char c = Character.toUpperCase(code.charAt(x));
			if (Character.isDigit(c))
                fNumber.append(c);
            else if (Character.isLetter(c))
                aCode.append(c);
		}
		
		// Do default airline
		if (aCode.length() == 0)
			aCode.append(defaultAirlineCode);
		
		// Generate the entry
		Airline a = SystemData.getAirline(aCode.toString());
		int fNum = Math.min(9999, StringUtils.parse(fNumber.toString(), 1));
		int lNum = (lPos < 1) ? 1 : StringUtils.parse(fCode.substring(lPos + 5), 1);
		return new ScheduleEntry(a, fNum, lNum);
	}
}