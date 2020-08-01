// Copyright 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

/**
 * A bean to define Pilot Accomplishments.
 * @author Luke
 * @version 9.0
 * @since 3.2
 */

public class Accomplishment extends DatabaseBean implements ComboAlias, Auditable, ViewEntry {

	private String _name;
	private AccomplishUnit _unit;
	private int _value;
	private final Collection<String> _choices = new TreeSet<String>();
	
	private AirlineInformation _owner;
	
	private boolean _active;
	private boolean _alwaysDisplay;
	private int _color;
	
	private int _pilots;
	
	/**
	 * Creates the bean.
	 * @param name the accomplishment name
	 * @see Accomplishment#setName(String)
	 */
	public Accomplishment(String name) {
		super();
		setName(name);
	}
	
	/**
	 * Creates an Accomplishment from a DatedAccomplishment
	 * @param da the DatedAccomplishment
	 */
	public Accomplishment(DatedAccomplishment da) {
		this(da.getName());
		setID(da.getID());
		setUnit(da.getUnit());
		setActive(da.getActive());
		setValue(da.getValue());
		setPilots(da.getPilots());
		setColor(da.getColor());
		setOwner(da.getOwner());
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
	 * @see Accomplishment#setUnit(AccomplishUnit)
	 */
	public AccomplishUnit getUnit() {
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
	 * Returns whether the Accomplishment should always display, even if the Pilot has achieved an
	 * accomplishment with the same Unit and a higher count.
	 * @return TRUE if the accomplishment should always be displayed if achieved, otherwise FALSE
	 */
	public boolean getAlwaysDisplay() {
		return _alwaysDisplay;
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
	
	@Override
	public String getComboName() {
		return _name;
	}
	
	@Override
	public String getComboAlias() {
		return getHexID();
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
	 * @param u the AccomplishUnit
	 * @see Accomplishment#getUnit()
	 */
	public void setUnit(AccomplishUnit u) {
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
	 * Sets the Accomplishment to always display, even if the Pilot has achieved an accomplishment
	 * with the same Unit and a higher count.
	 * @param show TRUE if the accomplishment should always be displayed if achieved, otherwise FALSE
	 */
	public void setAlwaysDisplay(boolean show) {
		_alwaysDisplay = show;
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

	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	@Override
	public Object cacheKey() {
		return _owner.getDB() + "!!" + String.valueOf(getID());
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof Accomplishment) && (compareTo(o) == 0));
	}
	
	@Override
	public String getRowClassName() {
		return _active ? null : "warn";
	}
	
	@Override
	public int compareTo(Object o) {
		Accomplishment a2 = (Accomplishment) o;
		int tmpResult = _unit.compareTo(a2._unit);
		if (tmpResult == 0)
			tmpResult = Integer.compare(_value, a2._value);
			
		return (tmpResult == 0) ? _name.compareTo(a2._name) : tmpResult;
	}

	@Override
	public String getAuditID() {
		return Integer.toHexString(hashCode());
	}
}