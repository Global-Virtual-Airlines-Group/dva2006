// Copyright 2006, 2007, 2008, 2010, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Aircraft type information and ACARS fuel profiles. Fuel is loaded in ACARS in the order of primary, secondary
 * and other tanks, and each Microsoft Flight Simulator fuel tank can be assigned to one of these three tank types.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class Aircraft implements Comparable<Aircraft>, Auditable, Cacheable, ViewEntry {

	private String _name;
	private String _fullName;
	private boolean _historic;
	
	private String _family;

	private byte _engineCount;
	private String _engineType;
	private int _cruiseSpeed;
	private int _fuelFlow;
	private int _baseFuel;
	private int _taxiFuel;
	
	private int _maxWeight;
	private int _maxZFW;
	private int _maxTakeoffWeight;
	private int _maxLandingWeight;
	
	// Fuel Tank loading codes and percentages
	private int[] _tankCodes = { 0, 0, 0 };
	private int[] _tankPct = { 0, 0, 0 };

	private String _icao;
	private final Collection<String> _iataCodes = new TreeSet<String>();
	private final Map<String, AircraftPolicyOptions> _airlineInfo = new HashMap<String, AircraftPolicyOptions>();

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
	 * Returns the aircraft family name.
	 * @return the family name
	 * @see Aircraft#setFamily(String)
	 */
	public String getFamily() {
		return _family;
	}

	/**
	 * Returns the aircraft's ICAO equipmnet code.
	 * @return the ICAO equipment code
	 * @see Aircraft#setICAO(String)
	 */
	public String getICAO() {
		return _icao;
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
	 * Returns whether this aircraft is a Historic type.
	 * @return TRUE if this is a Historic type, otherwise FALSE
	 * @see Aircraft#setHistoric(boolean)
	 */
	public boolean getHistoric() {
		return _historic;
	}
	
	/**
	 * Returns the aircraft's full name.
	 * @return the full name
	 * @see Aircraft#setFullName(String)
	 */
	public String getFullName() {
		return _fullName;
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
	 * @see Aircraft#addApp(AircraftPolicyOptions)
	 * @see AirlineInformation
	 */
	public Collection<String> getApps() {
		return _airlineInfo.keySet();
	}
	
	/**
	 * Returns policy options for this Aircraft for a specific virtual airline.
	 * @param appCode the virtual airline code
	 * @return an AircraftPolicyOptions bean, or null if airline not found
	 */
	public AircraftPolicyOptions getOptions(String appCode) {
		return _airlineInfo.get(appCode);
	}
	
	/**
	 * Returns whether a particular web application uses this aircraft type.
	 * @param code the web application airline code
	 * @return TRUE if the aircraft is used by this web application, otherwise FALSE
	 * @see Aircraft#getApps()
	 * @see Aircraft#addApp(AircraftPolicyOptions)
	 */
	public boolean isUsed(String code) {
		return _airlineInfo.containsKey(code.toUpperCase().trim());
	}

	/**
	 * Returns the fuel tank codes for a particular tank type.
	 * @param tt the TankType
	 * @return the tank codes as a bitmap
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#getTankNames()
	 * @see Aircraft#setTanks(TankType, int)
	 */
	public int getTanks(TankType tt) {
		return _tankCodes[tt.ordinal()];
	}

	/**
	 * Returns the filling percentage for a particular tank type.
	 * @param tt the TankType
	 * @return the percentage each tank should be filled
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#setPct(TankType, int)
	 */
	public int getPct(TankType tt) {
		return _tankPct[tt.ordinal()];
	}

	/**
	 * Returns the fuel tank names, for display in a JSP.
	 * @return a Map of Collections of tank names, keyed by tank type
	 * @see Aircraft#getTankPercent()
	 */
	public Map<String, Collection<String>> getTankNames() {
		Map<String, Collection<String>> results = new LinkedHashMap<String, Collection<String>>();
		for (TankType tt : TankType.values()) {
			Collection<String> names = new LinkedHashSet<String>();
			for (FuelTank t : FuelTank.values()) {
				if ((_tankCodes[tt.ordinal()] & (1 << t.code())) > 0)
					names.add(t.getName());
			}
			
			results.put(tt.getDescription(), names);
		}

		return results;
	}
	
	/**
	 * Returns the fuel tank fill percentages, for display in a JSP.
	 * @return a Map of tank percentages, keyed by tank type
	 * @see Aircraft#getTankNames() 
	 */
	public Map<String, Integer> getTankPercent() {
		Map<String, Integer> results = new LinkedHashMap<String, Integer>();
		for (TankType tt : TankType.values())
			results.put(tt.getDescription(), Integer.valueOf(_tankPct[tt.ordinal()]));
		
		return results;
	}

	/**
	 * Returns the maximum weight of the Aircraft.
	 * @return the weight in pounds
	 * @see Aircraft#setMaxWeight(int)
	 */
	public int getMaxWeight() {
		return _maxWeight;
	}
	
	/**
	 * Returns the maximum zero fuel weight of the Aircraft.
	 * @return the weight in pounds
	 * @see Aircraft#setMaxZeroFuelWeight(int)
	 */
	public int getMaxZeroFuelWeight() {
		return _maxZFW;
	}
	
	/**
	 * Returns the maximum takeoff weight of the Aircraft.
	 * @return the weight in pounds
	 * @see Aircraft#setMaxTakeoffWeight(int)
	 */
	public int getMaxTakeoffWeight() {
		return _maxTakeoffWeight;
	}
	
	/**
	 * Returns the maximum landing weight of the Aircraft.
	 * @return the weight in pounds
	 * @see Aircraft#setMaxLandingWeight(int)
	 */
	public int getMaxLandingWeight() {
		return _maxLandingWeight;
	}
	
	/**
	 * Marks this aircraft type as used by a particular web application.
	 * @param opts the AirlinePolicyOptions bean
	 * @see Aircraft#isUsed(String)
	 * @see Aircraft#getApps()
	 * @see AircraftPolicyOptions
	 */
	public void addApp(AircraftPolicyOptions opts) {
		_airlineInfo.put(opts.getCode(), opts);
	}

	/**
	 * Removes a web application from this aircraft type.
	 * @param appCode the virtual airline code
	 */
	public void removeApp(String appCode) {
		_airlineInfo.remove(appCode);
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
	 * Updates the aircraft's full name.
	 * @param name the full name
	 * @throws NullPointerException if name is null
	 * @see Aircraft#getFullName()
	 */
	public void setFullName(String name) {
		_fullName = name.trim();
	}

	/**
	 * Updates the aircraft family code, using for multi-player fallback rendering.
	 * @param family the family code
	 * @throws NullPointerException if family is null
	 * @see Aircraft#getFamily()
	 */
	public void setFamily(String family) {
		_family = family.toUpperCase();
	}

	/**
	 * Updates whether this aircraft is a Historic type.
	 * @param isHistoric TRUE if a Historic type, otherwise FALSE
	 * @see Aircraft#getHistoric()
	 */
	public void setHistoric(boolean isHistoric) {
		_historic = isHistoric;
	}
	
	/**
	 * Updates the maximum weight of the Aircraft.
	 * @param weight the weight in pounds
	 * @see Aircraft#getMaxWeight()
	 */
	public void setMaxWeight(int weight) {
		_maxWeight = Math.max(0, weight);
	}
	
	/**
	 * Updates the maximum zero fuel weight of the Aircraft.
	 * @param weight the weight in pounds
	 * @see Aircraft#getMaxZeroFuelWeight()
	 */
	public void setMaxZeroFuelWeight(int weight) {
		_maxZFW = Math.max(0, weight);
	}

	/**
	 * Updates the maximum takeoff weight of the Aircraft.
	 * @param weight the weight in pounds
	 * @see Aircraft#getMaxTakeoffWeight()
	 */
	public void setMaxTakeoffWeight(int weight) {
		_maxTakeoffWeight = Math.min(_maxWeight, Math.max(0, weight));
	}
	
	/**
	 * Updates the maximum landing weight of the Aircraft.
	 * @param weight the weight in pounds
	 * @see Aircraft#getMaxLandingWeight()
	 */
	public void setMaxLandingWeight(int weight) {
		_maxLandingWeight = Math.min(_maxWeight, Math.max(0, weight));
	}

	/**
	 * Updates the aircraft's ICAO code.
	 * @param code the ICAO code
	 */
	public void setICAO(String code) {
		_icao = code.trim().toUpperCase();
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
			codes.forEach(c -> addIATA(c));
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
	 * @see Aircraft#getBaseFuel()
	 */
	public void setBaseFuel(int fuelAmt) {
		_baseFuel = Math.max(0, fuelAmt);
	}

	/**
	 * Updates the aircraft's taxi fuel load.
	 * @param fuelAmt the amount of fuel in pounds
	 * @see Aircraft#getTaxiFuel()
	 */
	public void setTaxiFuel(int fuelAmt) {
		_taxiFuel = Math.max(0, fuelAmt);
	}

	/**
	 * Updates the aircraft's cruise fuel flow.
	 * @param flow the fuel flow in pounds per engine per hour
	 * @see Aircraft#getFuelFlow()
	 */
	public void setFuelFlow(int flow) {
		_fuelFlow = Math.max(0, flow);
	}
	
	/**
	 * Updates the tank usage percentage for a particular fuel tank type.
	 * @param tt the TankType
	 * @param pct the percentage required to be filled before filling the next tank type
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#getPct(TankType)
	 */	
	public void setPct(TankType tt, int pct) {
		_tankPct[tt.ordinal()] = Math.min(100, Math.max(0, pct));
	}

	/**
	 * Updates the fuel tanks used in filling the aircraft.
	 * @param tt the TankType
	 * @param tankCodes the codes for the fuel tanks used in this order
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#setTanks(TankType, Collection)
	 * @see Aircraft#getTanks(TankType)
	 * @see Aircraft#getTankNames()
	 */
	public void setTanks(TankType tt, int tankCodes) {
		_tankCodes[tt.ordinal()] = tankCodes;
	}

	/**
	 * Updates the fuel tanks used in filling the aircraft.
	 * @param tt the TankType
	 * @param tankNames a Collection of tank names
	 * @throws IllegalArgumentException if tankType is invalid
	 * @see Aircraft#setTanks(TankType, int)
	 * @see Aircraft#getTanks(TankType)
	 * @see Aircraft#getTankNames()
	 */
	public void setTanks(TankType tt, Collection<String> tankNames) {
		_tankCodes[tt.ordinal()] = 0;
		if (CollectionUtils.isEmpty(tankNames))
			return;
		
		// Update the tanks
		for (FuelTank t : FuelTank.values()) {
			if (tankNames.contains(t.name()))
				_tankCodes[tt.ordinal()] |= (1 << t.code());
		}
	}
	
	@Override
	public int compareTo(Aircraft a2) {
		return _name.compareTo(a2._name);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Aircraft) ? (compareTo((Aircraft) o) == 0) : false;
	}

	@Override
	public String toString() {
		return _name;
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	public Object cacheKey() {
		return _name;
	}
	
	@Override
	public String getAuditID() {
		return _name;
	}

	@Override
	public String getRowClassName() {
		if (_fuelFlow == 0)
			return "warn";
		
		return _historic ? "opt1" : null;
	}
}