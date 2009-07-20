// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.StringTokenizer;

import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A utility class to parse flight codes.
 * @author Luke
 * @version 2.6
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
			if ("0123456789".indexOf(c) != -1)
                fNumber.append(c);
            else if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) != -1)
                aCode.append(c);
		}
		
		// Do default airline
		if (aCode.length() == 0)
			aCode.append(SystemData.get("airline.code"));
		
		// Generate the entry
		Airline a = SystemData.getAirline(aCode.toString());
		int fNum = StringUtils.parse(fNumber.toString(), 1);
		int lNum = (tkns.countTokens() == 0) ? 1 : StringUtils.parse(tkns.nextToken(), 1);
		return new ScheduleEntry(a, fNum, lNum);
	}
}