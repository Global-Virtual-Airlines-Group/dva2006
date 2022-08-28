// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import java.util.*;

import org.deltava.beans.flight.FlightReport;

/**
 * An interface for Pilot log book exporters.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

abstract class LogbookExport {
	
	private final Map<String, Integer> _promoCounts = new HashMap<String, Integer>();

	/**
	 * Adds and processes a Flight Report.
	 * @param fr a FlightReport bean
	 */
	public abstract void add(FlightReport fr);

	/**
	 * Returns the content type of the output.
	 * @return the MIME type
	 */
	public abstract String getContentType();
	
	/**
	 * Returns the default file extension of the output.
	 * @return the file extension
	 */
	public abstract String getExtension();

	/**
	 * Returns the leg index for a leg that counts for promotion to Captain in an equipment program.
	 * @param fr the FlightReport
	 * @return the promotion leg index, or zero
	 */
	protected int getPromotionCount(FlightReport fr) {
		
		int maxPromoCount = 0;
		for (String captEQ : fr.getCaptEQType()) {
			int promoCount = _promoCounts.getOrDefault(captEQ, Integer.valueOf(0)).intValue() + 1;
			_promoCounts.put(captEQ, Integer.valueOf(promoCount));
			maxPromoCount = Math.max(maxPromoCount, promoCount);
		}
		
		return maxPromoCount;
	}
}