// Copyright 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Simulator versions.
 * @author Luke
 * @version 7.4
 * @since 5.1
 */

public enum Simulator implements ComboAlias {
	UNKNOWN(0, "Unknown"), FS98(98, "Microsoft Flight Simulator 98"), FS2000(2000, "Microsoft Flight Simulator 2000"), 
	FS2002(2002, "Microsoft Flight Simulator 2002"),	FS9(2004, "Microsoft Flight Simulator 2004"), 
	FSX(2006, "Microsoft Flight Simulator X"), P3D(2008, "Lockheed-Martin Prepar3D"), P3Dv4(2017, "Lockheed-Martin Prepar3D/64"),
	XP9(100, "Laminar Research X-Plane 9"), XP10(101, "Laminar Research X-Plane 10"), XP11(102, "Laminar Research X-Plane 11");

	private final String _name;
	private final int _code;
	
	Simulator(int code, String name) {
		_code = code;
		_name = name;
	}

	/**
	 * Returns the numeric simulator code.
	 * @return the code
	 */
	public int getCode() {
		return _code;
	}
	
	/**
	 * Returns the simulator name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return name();
	}
	
	/**
	 * Converts a version code to a Simulator.
	 * @param code the version code
	 * @param defaultSim the default to return
	 * @return a Simulator
	 */
	public static Simulator fromVersion(int code, Simulator defaultSim) {
		Simulator[] sims = values();
		for (int x = 0; x < sims.length; x++) {
			Simulator sim = sims[x];
			if (sim._code == code)
				return sim;
		}
		
		return defaultSim;
	}
	
	/**
	 * Converts a version code to a Simulator.
	 * @param code the version code
	 * @return a Simulator
	 */
	public static Simulator fromVersion(int code) {
		return fromVersion(code, UNKNOWN);
	}
	
	/**
	 * Exception-swallowing way to parse a simulator name.
	 * @param name the simulator name
	 * @return a Simulator
	 */
	public static Simulator fromName(String name) {
		return fromName(name, UNKNOWN);
	}
	
	/**
	 * Exception-swallowing way to parse a simulator name.
	 * @param name the simulator name
	 * @param defaultSim the default to return
	 * @return a Simulator
	 */
	public static Simulator fromName(String name, Simulator defaultSim) {
		try {
			if ("FS2K4".equals(name)) return Simulator.FS9;
			return Simulator.valueOf(name.toUpperCase());
		} catch (Exception e) {
			return defaultSim;
		}
	}
}