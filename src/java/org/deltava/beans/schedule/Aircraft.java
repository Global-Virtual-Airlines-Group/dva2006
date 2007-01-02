// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.ViewEntry;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Aircraft type information and ACARS fuel profiles. Fuel is loaded in ACARS in the order of primary,
 * secondary and other tanks, and each Microsoft Flight Simulator fuel tank can be assigned to one of these three tank
 * types.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Aircraft implements Comparable, Cacheable, ViewEntry {

	public static final int CENTER = 0;
	public static final int LEFT_MAIN = 1;
	public static final int LEFT_AUX = 2;
	public static final int LEFT_TIP = 3;
	public static final int RIGHT_MAIN = 4;
	public static final int RIGHT_AUX = 5;
	public static final int RIGHT_TIP = 6;
	public static final int CENTER2 = 7;
	public static final int CENTER3 = 8;
	public static final int EXT1 = 9;
	public static final int EXT2 = 10;

	public static final String[] TANK_TYPES = { "Primary", "Secondary", "Other" };
	public static final String[] TANK_NAMES = { "Center", "Left Main", "Left Aux", "Left Tip", "Right Main",
			"Right Aux", "Right Tip", "Center 2", "Center 3", "External", "External 2" };

	public static final int PRIMARY = 0;
	public static final int SECONDARY = 1;
	public static final int OTHER = 2;

	private String _name;
	private boolean _historic;

	private int _maxRange;
	private byte _engineCount;
	private String _engineType;
	private int _cruiseSpeed;
	private int _fuelFlow;
	private int _baseFuel;
	private int _taxiFuel;

	// Fuel Tank loading codes and percentages
	private int[] _tankCodes = { 0, 0, 0 };
	private int[] _tankPct = { 0, 0, 0 };

	private final Collection<String> _iataCodes = new TreeSet<String>();
	private final Collection<AirlineInformation> _airlines = new HashSet<AirlineInformation>();

	/**
	 * Initializes the bean.
	 * @param name the equipment name
	 * @throws NullPointerException if name is null
	 */
	public Aircraft(String name) {
		super();
		setName(name);
	}

	/**
	 * Returns the aircraft name.
	 * @return the name
	 * @see Aircraft#setName(String)
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the maximum range of the aircraft.
	 * @return the range in miles
	 * @see Aircraft#setRange(int)
	 */
	public int getRange() {
		return _maxRange;
	}

	/**
	 * Returns the aircraft's IATA equipment code(s).
	 * @return a sorted Collection of IATA codes
	 * @see Aircraft#addIATA(String)
	 * @see Aircraft#setIATA(Collection)
	 */
	public Collection<String> getIATA() {
		return new TreeSet<String>(_iataCodes);
	}

	/**
	 * Returns wether this aircraft is a Historic type.
	 * @return TRUE if this is a Historic type, otherwise FALSE
	 * @see Aircraft#setHistoric(boolean)
	 */
	public boolean getHistoric() {
		return _historic;
	}

	/**
	 * Returns the number of engines on this aircraft.
	 * @return the number of engines
	 * @see Aircraft#setEngines(byte)
	 */
	public byte getEngines() {
		return _engineCount;
	}

	/**
	 * Returns the aircraft's engine type.
	 * @return the engine type
	 * @see Aircraft#setEngineType(String)
	 */
	public String getEngineType() {
		return _engineType;
	}

	/**
	 * Returns the aircraft's cruise speed
	 * @return the cruise speed in knots
	 * @see Aircraft#setCruiseSpeed(int)
	 */
	public int getCruiseSpeed() {
		return _cruiseSpeed;
	}

	/**
	 * Returns the aircraft's base fuel load.
	 * @return the fuel load in pounds
	 * @see Aircraft#setBaseFuel(int)
	 */
	public int getBaseFuel() {
		return _baseFuel;
	}

	/**
	 * Returns the aircraft's taxi fuel load.
	 * @return the fuel load in pounds
	 * @see Aircraft#setTaxiFuel(int)
	 */
	public int getTaxiFuel() {
		return _taxiFuel;
	}

	/**
	 * Returns the aircraft's fuel flow.
	 * @return the fuel flow in pounds per engine per hour
	 * @see Aircraft#setFuelFlow(int)
	 */
	public int getFuelFlow() {
		return _fuelFlow;
	}

	/**
	 * Returns all web applications using this aircraft type.
	 * @return a Collection of AirlineInformation beans
	 * @see Aircraft#isUsed(String)
	 * @see Aircraft#addApp(AirlineInformation)
	 * @see AirlineInformation
	 */
	public Collection<AirlineInformation> getApps() {
		return new LinkedHashSet<AirlineInformation>(_airlines);
	}

	/**
	 * Returns wether a particular web application uses this aircraft type.
	 * @param code the web application airline code
	 * @return TRUE if the aircraft is used by this web application, otherwise FALSE
	 * @see Aircraft#getApps()
	 * @see Aircraft#addApp(AirlineInformation)
	 */
	public boolean isUsed(String code) {
		for (Iterator<AirlineInformation> i = _airlines.iterator(); i.hasNext();) {
			AirlineInformation ai = i.next();
			if (ai.getCode().equalsIgnoreCase(code))
				return true;
		}

		return false;
	}

	/**
	 * Returns the fuel tank codes for a particular tank type.
	 * @param tankType the fuel tank type
	 * @return the tank codes as a bitmap
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#getTankNames()
	 * @see Aircraft#setTanks(int, int)
	 */
	public int getTanks(int tankType) {
		if ((tankType < 0) || (tankType > OTHER))
			throw new IllegalArgumentException("Invalid tank type code - " + tankType);

		return _tankCodes[tankType];
	}

	/**
	 * Returns the filling percentage for a particular tank type.
	 * @param tankType the fuel tank type
	 * @return the percentage each tank should be filled
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#setPct(int, int)
	 */
	public int getPct(int tankType) {
		if ((tankType < 0) || (tankType > OTHER))
			throw new IllegalArgumentException("Invalid tank type code - " + tankType);

		return _tankPct[tankType];
	}

	/**
	 * Returns the fuel tank names, for display in a JSP.
	 * @return a Map of Collections of tank names, keyed by tank type
	 * @see Aircraft#getTankPercent()
	 * @see Aircraft#TANK_TYPES
	 * @see Aircraft#TANK_NAMES
	 */
	public Map<String, Collection<String>> getTankNames() {
		Map<String, Collection<String>> results = new LinkedHashMap<String, Collection<String>>();
		for (int tankType = PRIMARY; tankType <= OTHER; tankType++) {
			Collection<String> names = new LinkedHashSet<String>();
			for (int x = 0; x < TANK_NAMES.length; x++) {
				if ((_tankCodes[tankType] & (1 << x)) > 0)
					names.add(TANK_NAMES[x]);
			}
			
			results.put(TANK_TYPES[tankType], names);
		}

		return results;
	}
	
	/**
	 * Returns the fuel tank fill percentages, for display in a JSP.
	 * @return a Map of tank percentages, keyed by tank type
	 * @see Aircraft#getTankNames() 
	 * @see Aircraft#TANK_TYPES
	 * @see Aircraft#TANK_NAMES
	 */
	public Map<String, Integer> getTankPercent() {
		Map<String, Integer> results = new LinkedHashMap<String, Integer>();
		for (int tankType = PRIMARY; tankType < OTHER; tankType++)
			results.put(TANK_TYPES[tankType], new Integer(_tankPct[tankType]));
		
		return results;
	}

	/**
	 * Marks this aircraft type as used by a particular web application.
	 * @param ai the AirlineInformation bean
	 * @see Aircraft#isUsed(String)
	 * @see Aircraft#getApps()
	 * @see AirlineInformation
	 */
	public void addApp(AirlineInformation ai) {
		_airlines.add(ai);
	}

	/**
	 * Clears the web applications used with this aircraft type.
	 */
	public void clearApps() {
		_airlines.clear();
	}

	/**
	 * Updates the aircraft name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Aircraft#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}

	/**
	 * Updates the maximum range of the aircraft.
	 * @param range the range in miles
	 * @throws IllegalArgumentException if range is zero or negative
	 * @see Aircraft#getRange()
	 */
	public void setRange(int range) {
		if (range < 1)
			throw new IllegalArgumentException("Invalid Range - " + range);

		_maxRange = range;
	}

	/**
	 * Update wether this aircraft is a Historic type.
	 * @param isHistoric TRUE if a Historic type, otherwise FALSE
	 * @see Aircraft#getHistoric()
	 */
	public void setHistoric(boolean isHistoric) {
		_historic = isHistoric;
	}

	/**
	 * Links an IATA equipment code to this aircraft.
	 * @param code the equipment code
	 * @throws NullPointerException if code is null
	 * @see Aircraft#getIATA()
	 * @see Aircraft#setIATA(Collection)
	 */
	public void addIATA(String code) {
		_iataCodes.add(code.trim().toUpperCase());
	}

	/**
	 * Updates this aircraft's IATA codes.
	 * @param codes a Collection of codes
	 * @see Aircraft#addIATA(String)
	 * @see Aircraft#getIATA()
	 */
	public void setIATA(Collection<String> codes) {
		_iataCodes.clear();
		if (codes != null)
			_iataCodes.addAll(codes);
	}

	/**
	 * Updates the number of engines on this aircraft.
	 * @param engines the number of engnes
	 * @throws IllegalArgumentException if engines is zero, negative or &gt; 8
	 * @see Aircraft#getEngines()
	 */
	public void setEngines(byte engines) {
		if ((engines < 1) || (engines > 8))
			throw new IllegalArgumentException("Invalid Engine count - " + engines);

		_engineCount = engines;
	}

	/**
	 * Updates the aircraft's engine type.
	 * @param engName the engine type
	 * @see Aircraft#getEngineType()
	 */
	public void setEngineType(String engName) {
		_engineType = engName;
	}

	/**
	 * Updates the aircraft's cruise speed.
	 * @param speed the speed in knots
	 * @see Aircraft#getCruiseSpeed()
	 */
	public void setCruiseSpeed(int speed) {
		if ((speed < 30) || (speed > 1600))
			throw new IllegalArgumentException("Invalid Cruise Speed - " + speed);

		_cruiseSpeed = speed;
	}

	/**
	 * Updates the aircraft's base fuel load.
	 * @param fuelAmt the amount of fuel in pounds
	 * @throws IllegalArgumentException if fuelAmt is negative
	 * @see Aircraft#getBaseFuel()
	 */
	public void setBaseFuel(int fuelAmt) {
		if (fuelAmt < 0)
			throw new IllegalArgumentException("Invalid Base Fuel - " + fuelAmt);

		_baseFuel = fuelAmt;
	}

	/**
	 * Updates the aircraft's taxi fuel load.
	 * @param fuelAmt the amount of fuel in pounds
	 * @throws IllegalArgumentException if fuelAmt is negative
	 * @see Aircraft#getTaxiFuel()
	 */
	public void setTaxiFuel(int fuelAmt) {
		if (fuelAmt < 0)
			throw new IllegalArgumentException("Invalid Taxi Fuel - " + fuelAmt);

		_taxiFuel = fuelAmt;
	}

	/**
	 * Updates the aircraft's cruise fuel flow.
	 * @param flow the fuel flow in pounds per engine per hour
	 * @see Aircraft#getFuelFlow()
	 */
	public void setFuelFlow(int flow) {
		if (flow < 0)
			throw new IllegalArgumentException("Invalid Fuel Flow - " + flow);

		_fuelFlow = flow;
	}

	/**
	 * Updates the tank usage percentage for a particular fuel tank type.
	 * @param tankType the tank type
	 * @param pct the percentage required to be filled before filling the next tank type
	 * @throws IllegalArgumentException if tankType is invalid
	 * @throws IllegalArgumentException if pct is negative or &gt; 100
	 * @see Aircraft#getPct(int)
	 */
	public void setPct(int tankType, int pct) {
		if ((tankType < 0) || (tankType > OTHER))
			throw new IllegalArgumentException("Invalid tank type code - " + tankType);
		else if ((pct < 0) || (pct > 100))
			throw new IllegalArgumentException("Invalid percentage - " + pct);

		_tankPct[tankType] = pct;
	}

	/**
	 * Updates the fuel tanks used in filling the aircraft.
	 * @param tankType the tank type
	 * @param tankCodes the codes for the fuel tanks used in this order
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#setTanks(int, Collection)
	 * @see Aircraft#getTanks(int)
	 * @see Aircraft#getTankNames()
	 */
	public void setTanks(int tankType, int tankCodes) {
		if ((tankType < 0) || (tankType > OTHER))
			throw new IllegalArgumentException("Invalid tank type code - " + tankType);

		_tankCodes[tankType] = tankCodes;
	}

	/**
	 * Updates the fuel tanks used in filling the aircraft.
	 * @param tankType the tank type
	 * @param tankNames a Collection of tank names
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#setTanks(int, int)
	 * @see Aircraft#getTanks(int)
	 * @see Aircraft#getTankNames()
	 */
	public void setTanks(int tankType, Collection<String> tankNames) {
		if ((tankType < 0) || (tankType > OTHER))
			throw new IllegalArgumentException("Invalid tank type code - " + tankType);
		else if (CollectionUtils.isEmpty(tankNames))
			return;
		
		// Update the tanks
		for (Iterator<String> i = tankNames.iterator(); i.hasNext(); ) {
			int ofs = StringUtils.arrayIndexOf(TANK_NAMES, i.next());
			if (ofs != -1)
				_tankCodes[tankType] |= (1 << ofs);
		}
	}

	/**
	 * Compares two aircraft by comparing their names.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Aircraft a2 = (Aircraft) o;
		return _name.compareTo(a2._name);
	}

	public boolean equals(Object o) {
		return (o instanceof Aircraft) ? (compareTo(o) == 0) : false;
	}

	/**
	 * Returns the aircraft name.
	 */
	public String toString() {
		return _name;
	}

	/**
	 * Returns the aircraft name hash code.
	 */
	public int hashCode() {
		return _name.hashCode();
	}

	/**
	 * Returns the aircrat name.
	 */
	public Object cacheKey() {
		return _name;
	}

	/**
	 * Returns the CSS class name to use if displaying in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _historic ? "opt1" : null;
	}
}