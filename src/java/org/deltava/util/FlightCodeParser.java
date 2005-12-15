// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.util;

import java.util.StringTokenizer;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A utility class to parse flight codes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightCodeParser {

	// Singleton
	private FlightCodeParser() {
	}

	/**
	 * Parses a flight code into a schedule entry. 
	 * @param fCode the flight Code
	 * @return the Schedule entry, or null
	 */
	public static ScheduleEntry parse(String fCode) {
		if (fCode == null)
			return null;
		
		// Instantiate temporary objects
		StringBuilder aCode = new StringBuilder();
		StringBuilder fNumber = new StringBuilder();
		StringTokenizer tkns = new StringTokenizer(fCode, " Leg ");
		String code = tkns.nextToken();
		for (int x = 0; x < code.length(); x++) {
			char c = Character.toUpperCase(code.charAt(x));
			if ("0123456789".indexOf(c) != -1) {
                fNumber.append(c);
            } else if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) != -1) {
                aCode.append(c);
            }
		}
		
		// Do default airline
		if (aCode.length() == 0)
			aCode.append(SystemData.get("airline.code"));
		
		// Generate the entry
		try {
			Airline a = SystemData.getAirline(aCode.toString());
			int fNum = Integer.parseInt(fNumber.toString());
			int lNum = (tkns.countTokens() == 0) ? 1 : Integer.parseInt(tkns.nextToken());
			return new ScheduleEntry(a, fNum, lNum);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
}