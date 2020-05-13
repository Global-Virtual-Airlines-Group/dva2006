// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.Helper;

/**
 * A utility class to predict API usage.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */
 
@Helper(APIUsage.class)
public class APIUsageHelper {

	// static class
	private APIUsageHelper() {
		super();
	}
	
	/**
	 * Calculates predicted API usage for the rest of the month, based on prior usage.
	 * @param history a Collection of APIUsage beans
	 * @param name the API/method name
	 * @return an APIUsage with the expected total/anonymous usage for the month
	 */
	public static APIUsage predictUsage(Collection<APIUsage> history, String name) {
		
		// Determine start of month
		LocalDate today = LocalDate.ofInstant(Instant.now(), ZoneOffset.UTC);
		LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.of(today.getYear(), today.getMonthValue(), 1), LocalTime.MIDNIGHT);
		Instant t = Instant.ofEpochSecond(startOfMonth.toEpochSecond(ZoneOffset.UTC) - 1);
		Instant ts = Instant.ofEpochSecond(today.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC));
		
		// Filter out other APIs and calculate our daily average
		APIUsage result = new APIUsage(ts, name);
		List<APIUsage> filteredUsage = history.stream().filter(u -> u.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
		OptionalDouble avgTotal = filteredUsage.stream().mapToInt(APIUsage::getTotal).average();
		OptionalDouble avgAnon = filteredUsage.stream().mapToInt(APIUsage::getAnonymous).average();
		if (avgTotal.isEmpty())
			return result;
		
		// Determine actuals for the month
		int monthlyTotal = filteredUsage.stream().filter(u -> u.getDate().isAfter(t) && u.getDate().isBefore(ts)).mapToInt(APIUsage::getTotal).sum();
		int monthlyAnon = filteredUsage.stream().filter(u -> u.getDate().isAfter(t) && u.getDate().isBefore(ts)).mapToInt(APIUsage::getAnonymous).sum();
		
		// Determine days left in month and multiply by average
		int daysLeft = today.getMonth().length(today.isLeapYear()) - (today.getDayOfMonth() - 1);
		int expectedTotal = Math.round((float)avgTotal.getAsDouble() * daysLeft);
		int expectedAnon = Math.round((float)avgAnon.getAsDouble() * daysLeft);
		result.setTotal(monthlyTotal + expectedTotal);
		result.setAnonymous(monthlyAnon + expectedAnon);
		return result;
	}
	
	/**
	 * Calculates today's predicted API usage.
	 * @param u an APIUsage bean with today's use
	 * @return an APIUsage with today's predicted use
	 */
	public static APIUsage predictToday(APIUsage u) {
		
		// Determine percentage of the day
		int seconds = LocalTime.ofInstant(Instant.now(), ZoneOffset.UTC).toSecondOfDay();
		double pct = 1 - (seconds / 86400d);
		
		// Apply percentage
		APIUsage result = new APIUsage(u.getDate(), u.getName());
		result.setTotal((int)(pct * u.getTotal()));
		result.setAnonymous((int)(pct * u.getAnonymous()));
		return result;
	}
	
	/**
	 * Returns the number of days remaining in the month, including today.
	 * @return the number of days
	 */
	public static int getDaysLeftInMonth() {
		LocalDate today = LocalDate.ofInstant(Instant.now(), ZoneOffset.UTC);
		return today.getMonth().length(today.isLeapYear()) - (today.getDayOfMonth() - 1);
	}
}