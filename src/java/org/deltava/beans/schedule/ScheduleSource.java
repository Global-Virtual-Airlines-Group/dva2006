// Copyright 2019, 2020, 2023, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.Comparator;

/**
 * An enumeration of flight schedule sources. This is unique among Enumerations in that it provides a Comparator that overrides sort order based
 * on the _isPimrary flag, allowing sources set as Primary to override older sources with a lower ordinal value.
 * @author Luke
 * @version 12.5
 * @since 9.0
 */

public enum ScheduleSource implements org.deltava.beans.EnumDescription {
	VASYS("VASystems/PHPVMSv7"), DELTA("Delta PDF"), SKYTEAM("SkyTeam PDF"), INNOVATA("Innovata LLC"), LEGACY("Legacy Schedule"), MANUAL("Manual Entry"), JSON("Golgotha JSON"), DRAFT("Draft Flight Report"), SIMVECTOR("SimVector"), AVSTACK("AviationStack", true);
	
	private final boolean _isPrimary;
	private final String _desc;
	
	private static final Comparator<ScheduleSource> CMP = new Comparator<ScheduleSource>() {
		@Override
		public int compare(ScheduleSource ss1, ScheduleSource ss2) {
			int tmpResult = Boolean.compare(ss2._isPrimary, ss1._isPrimary); // reverse comparison
			return (tmpResult == 0) ? ss1.compareTo(ss2) : tmpResult;
		}
	};

	/**
	 * Creates the enumeration entry.
	 * @param desc the source description
	 * @param isPrimary TRUE if this should override sort order, otherwise FALSE
	 */
	ScheduleSource(String desc, boolean isPrimary) {
		_desc = desc;
		_isPrimary = isPrimary;
	}
	
	/**
	 * Creates a non-primary enumeration entry.
	 * @param desc the source description
	 */
	ScheduleSource(String desc) {
		this(desc, false);
	}
	
	/**
	 * Returns whether a ScheduleSource is a primary source, which overrides the sort order.
	 * @return TRUE if a primary source, otherwise FALSE
	 */
	public boolean isPrimary() {
		return _isPrimary;
	}

	@Override
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns a comparator that properly implements sort order.
	 * @return a Comparator
	 */
	public static Comparator<ScheduleSource> comparator() {
		return CMP;
	}
}