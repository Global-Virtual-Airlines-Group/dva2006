// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.*;

import org.deltava.beans.EnumDescription;

/**
 * An enumeration to store Raw Schedule import flight length calculation policies. By default, flight lengths are calculated
 * based on departure/arrival times, however  
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum ImportPolicy implements EnumDescription {
	TIMES("Departure/Arrival Times"), LENGTH("Departure Time/Duration");

	private final String _desc;
	
	ImportPolicy(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns the policy description.
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return _desc;
	}

	/**
	 * Applies the policy to a schedule entry.
	 * @param se a ScheduleEntry
	 */
	public void apply(ScheduleEntry se) {
		switch (this) {
			case LENGTH:
				int minutes = se.getLength() * 6;
				Instant arrivalTime = se.getTimeD().toInstant().plusSeconds(minutes * 60);
				se.setTimeA(LocalDateTime.ofInstant(arrivalTime, se.getAirportA().getTZ().getZone()));
				break;
		
			case TIMES:
			default:
				Duration d = Duration.between(se.getTimeD(), se.getTimeA());
				se.setLength((int) (d.getSeconds() / 360));
		}
	}
}