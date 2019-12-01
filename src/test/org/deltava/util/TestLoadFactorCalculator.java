// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;
import java.text.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.deltava.beans.econ.EconomyInfo;
import org.deltava.beans.econ.LoadFactor;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestLoadFactorCalculator extends TestCase {

	private static final NumberFormat DF = new DecimalFormat("##0.0000");
	
	private static final double TARGET_LOAD = 0.8567;
	
	public void testGaussianDistribution() {
		
		Random rnd = new Random();
		double max = Double.MIN_NORMAL; double min = Double.MAX_VALUE;
		for (int x = 0; x < 100000; x++) {
			double value = rnd.nextGaussian();
			max = Math.max(max, value);
			min = Math.min(min, value);
		}
		
		System.out.println(DF.format(min) + " " + DF.format(max));
	}
	
	/*
	 * Convert from 365 to 360.
	 */
	private static double factor(int daysPerCycle) {
		return (2 * Math.PI ) / daysPerCycle;
	}
	
	public void testSineWave() throws IOException {

		ZonedDateTime zdt = ZonedDateTime.of(2001, 6, 10, 0, 0, 0, 0, ZoneOffset.UTC);
		EconomyInfo info = new EconomyInfo(TARGET_LOAD, .12d);
		info.setStartDate(zdt.toInstant());
		info.setMinimumLoad(.125);
		info.setHourlyFactor(.1);
		info.setYearlyCycleLength(272);
		info.setHourlyCycleLength(14 * 24);
		
		LoadFactor lf = new LoadFactor(info);
		try (PrintWriter pw = new PrintWriter(new FileWriter("c:\\temp\\sine.csv"))) {
			for (int x = 0; x < 365; x++) {
				double mainTarget = (Math.sin(x * factor(272)) * info.getAmplitude()) + TARGET_LOAD;
				double jitter = (Math.cos(x * factor(14)) * info.getAmplitude()) + TARGET_LOAD;
				double total =  lf.getTargetLoad(zdt.toInstant());
				pw.println(DF.format(mainTarget * 100) +"," + DF.format(TARGET_LOAD * 100) + "," + DF.format(jitter * 100) + "," + DF.format(total * 100));
				zdt = zdt.plusDays(1);
			}
		}
	}
}