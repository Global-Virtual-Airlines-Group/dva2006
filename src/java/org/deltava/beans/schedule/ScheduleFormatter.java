// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An interface for schedule entry formatters. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public interface ScheduleFormatter {
	
	/**
	 * Returns the supported schedule format.
	 * @return the ScheduleFormat
	 */
	public ScheduleFormat getFormat();

	/**
	 * Formats a Schedule entry.
	 * @param se the ScheduleEntry
	 * @return the formatted object
	 */
	public String format(ScheduleEntry se);
	
	/**
	 * Returns the header element for the output file.
	 * @return the header
	 */
	public String getHeader();

	/**
	 * Returns the footer element for the output file.
	 * @return the footer
	 */
	public String getFooter();
	
	/**
	 * Returns the entry separator string.
	 * @return the separator
	 */
	public String getSeparator();
}