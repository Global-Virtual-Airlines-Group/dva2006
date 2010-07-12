// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to define Pilot accomplishments.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class Accomplishment extends DatabaseBean implements Cacheable, ViewEntry {

	/**
	 * Enumeration to track Accomplishment Units.
	 */
	public enum Unit implements ComboAlias {
		LEGS("Flight Legs", 0), MILES("Flight Miles", 1), OLEGS("Online Legs", 2), VLEGS("VATSIM Legs", 3),
		ILEGS("IVAO Legs", 4), HLEGS("Historic Legs", 5), ELEGS("Event Legs", 6), DLEGS("Dispatch Legs", 7),
		ALEGS("ACARS Legs", 8), AIRPORTS("Airports Visited", 9), AIRCRAFT("Aircraft Used", 10), 
		COUNTRIES("Countries Visited", 11), STATES("States Visited", 12), MEMBERDAYS("Days since joining", 13);
			
		private String _name;
		private int _code;
		
		Unit(String name, int code) {
			_code = code;
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public int getCode() {
			return _code;
		}
		
		public String getComboName() {
			return _name;
		}
		
		public String getComboAlias() {
			return name();
		}
	}
	
	private String _name;
	private Unit _unit;
	private int _value;
	private final Collection<String> _choices = new TreeSet<String>();
	
	private AirlineInformation _owner;
	
	private boolean _active;
	private int _color;
	
	private int _pilots;
	
	/**
	 * Creates the bean.
	 * @param name the accomplishment name
	 * @throws NullPointerException if name is null
	 * @see Accomplishment#setName(String)
	 */
	public Accomplishment(String name) {
		super();
		setName(name);
	}

	/**
	 * Returns the Accomplishment name.
	 * @return the name
	 * @see Accomplishment#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Accomplishment unit.
	 * @return the unit
	 * @see Accomplishment#setUnit(Unit)
	 */
	public Unit getUnit() {
		return _unit;
	}
	
	/**
	 * Returns the Accomplishment value.
	 * @return the value
	 * @see Accomplishment#setValue(int)
	 */
	public int getValue() {
		return _value;
	}
	
	/**
	 * Returns the color used to display this Accomplishment.
	 * @return the RGB color code
	 * @see Accomplishment#setColor(int)
	 */
	public int getColor() {
		return _color;
	}
	
	/**
	 * Returns the color used to display this Accomplishment in a CSS-friendly format.
	 * @return the RGB color code
	 */
	public String getHexColor() {
		StringBuilder buf = new StringBuilder(Integer.toHexString(_color).toLowerCase());
		while (buf.length() < 6)
			buf.insert(0, '0');
		
		return buf.toString();
	}
	
	/**
	 * Returns whether this Accomplishment is active.
	 * @return TRUE if active, otherwise FALSE
	 * @see Accomplishment#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
	}
	
	/**
	 * Returns the number of Pilots who have achieved this Accomplishment.
	 * @return the number of Pilots
	 * @see Accomplishment#setPilots(int)
	 */
	public int getPilots() {
		return _pilots;
	}
	
	/**
	 * Returns the choices for this Accomplishment. This limits the possible values used to count
	 * towards this Accomplishment.
	 * @return a Collection of Strings
	 */
	public Collection<String> getChoices() {
		return new ArrayList<String>(_choices);
	}
	
	/**
	 * Returns the owning Airline of this Accomplishment.
	 * @return the AirlineInformation bean for the owner Airline
	 * @see Accomplishment#setOwner(AirlineInformation)
	 */
	public AirlineInformation getOwner() {
		return _owner;
	}
	
	/**
	 * Updates the accomplishment name.
	 * @param name the accomplishment name
	 * @throws NullPointerException if name is null
	 * @see Accomplishment#getName()
	 */
	public void setName(String name) {
		_name = name.trim();
	}
	
	/**
	 * Updates the unit of measurement.
	 * @param u the Unit
	 * @see Accomplishment#getUnit()
	 */
	public void setUnit(Unit u) {
		_unit = u;
	}
	
	/**
	 * Updates the number of units required for this Accomplishment.
	 * @param value the number of units
	 * @see Accomplishment#getValue()
	 */
	public void setValue(int value) {
		_value = Math.max(0, value);
	}
	
	/**
	 * Updates the color used to display this Accomplishment.
	 * @param c the RGB code
	 * @see Accomplishment#getColor()
	 */
	public void setColor(int c) {
		_color = c;
	}
	
	/**
	 * Updates whether this Accomplishment is active.
	 * @param isActive TRUE if active, otherwise FALSE
	 * @see Accomplishment#getActive()
	 */
	public void setActive(boolean isActive) {
		_active = isActive;
	}
	
	/**
	 * Updates the Owner of this Accomplishment.
	 * @param info the AirlineInformation bean for the owner airline
	 * @see Accomplishment#getOwner()
	 */
	public void setOwner(AirlineInformation info) {
		_owner = info;
	}
	
	/**
	 * Updates the number of Pilots that have achieved this Accomplishment. 
	 * @param cnt the number of Pilots
	 * @see Accomplishment#getPilots()
	 */
	public void setPilots(int cnt) {
		_pilots = Math.max(0, cnt);
	}
	
	/**
	 * Updates the choices for this Accomplishment.
	 * @param choices a Collection of strings
	 */
	public void setChoices(Collection<String> choices) {
		_choices.clear();
		if (choices != null)
			_choices.addAll(choices);
	}

	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public Object cacheKey() {
		return _owner.getDB() + "!!" + String.valueOf(getID());
	}
	
	public boolean equals(Object o) {
		return ((o instanceof Accomplishment) && (compareTo(o) == 0));
	}
	
	public String getRowClassName() {
		return _active ? null : "warn";
	}
	
	public int compareTo(Object o) {
		Accomplishment a2 = (Accomplishment) o;
		int tmpResult = _unit.compareTo(a2._unit);
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_value).compareTo(Integer.valueOf(a2._value));
			
		return (tmpResult == 0) ? _name.compareTo(a2._name) : tmpResult;
	}
}