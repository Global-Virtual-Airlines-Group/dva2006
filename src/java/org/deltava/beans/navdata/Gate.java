// Copyright 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airline;

/**
 * A bean to store airport Gate information.
 * @author Luke
 * @version 10.0
 * @since 5.1
 */

public class Gate extends NavigationDataBean implements UseCount, ComboAlias {

	/**
	 * Gate types.
	 */
	public enum Type implements EnumDescription {
		GATE, PARKING, DOCK;
	}
	
	private int _heading;
	private int _number;
	private Type _type = Type.GATE;
	private int _useCount;
	private Simulator _sim = Simulator.UNKNOWN;
	
	private GateZone _zone = GateZone.DOMESTIC;
	private final Collection<Airline> _airlines = new TreeSet<Airline>();

	/**
	 * Creates the bean.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public Gate(double lat, double lon) {
		super(Navaid.GATE, lat, lon);
	}
	
	/**
	 * Clones an existing gate, with the option to override usage counts.
	 * @param g the Gate
	 * @param usage the new usage count
	 */
	public Gate(Gate g, int usage) {
		this(g.getLatitude(), g.getLongitude());
		setCode(g.getCode());
		setName(g.getName());
		setRegion(g.getRegion());
		_heading = g._heading;
		_number = g._number;
		_useCount = usage;
		_zone = g._zone;
		_airlines.addAll(g._airlines);
		_sim = g._sim;
	}
	
	/**
	 * Returns the gate heading.
	 * @return the heading in degrees
	 */
	public int getHeading() {
		return _heading;
	}
	
	/**
	 * Returns the gate type.
	 * @return the Type
	 */
	public Type getGateType() {
		return _type;
	}
	
	/**
	 * Returns the gate number.
	 * @return the gate number
	 */
	public int getGateNumber() {
		return _number;
	}
	
	/**
	 * Returns what Airlines use this Gate.
	 * @return a Collection of Airline beans
	 */
	public Collection<Airline> getAirlines() {
		return _airlines;
	}
	
	/**
	 * Returns whether this Gate is used for international flights or other special customs zones.
	 * @return a GateZone
	 */
	public GateZone getZone() {
		return _zone;
	}
	
	/**
	 * Returns the Simulator this Gate exists in.
	 * @return the Simulator
	 */
	public Simulator getSimulator() {
		return _sim;
	}
	
	@Override
	public int getUseCount() {
		return _useCount;
	}

	/**
	 * Returns whether a Gate serves a paticular Airline.
	 * @param a the Airline
	 * @return TRUE if a is null or the Airline uses this Gate, otherwise FALSE
	 */
	public boolean hasAirline(Airline a) {
		return (a == null) || _airlines.contains(a);
	}
	
	/**
	 * Updates the number of times this gate has been used.
	 * @param cnt the number of flights
	 */
	public void setUseCount(int cnt) {
		_useCount = Math.max(0, cnt);
	}
	
	/**
	 * Updates the gate heading.
	 * @param hdg the heading in degrees
	 */
	public void setHeading(int hdg) {
		int h = hdg;
		while (h > 360)
			h -= 360;
		while (h < 0)
			h += 360;

		_heading = h;
	}

	@Override
	public void setName(String name) {
		String n = name.toUpperCase();
		for (Type t : Type.values()) {
			if (n.contains(t.name())) {
				_type = t;
				break;
			}
		}
		
		StringBuilder buf = new StringBuilder();
		for (int x = 0; x < n.length(); x++) {
			char c = n.charAt(x);
			if (Character.isDigit(c))
				buf.append(c);
		}
		
		if (buf.length() > 0)
			_number = Integer.parseInt(buf.toString());
		super.setName(n);
	}

	/**
	 * Marks this gate as being used by an Airline.
	 * @param a the Airline bean
	 */
	public void addAirline(Airline a) {
		if (a != null)
			_airlines.add(a);
	}
	
	/**
	 * Clears all Airlines associated with this Gate.
	 */
	public void clearAirlines() {
		_airlines.clear();
	}
	
	/**
	 * Updates the customs zone used for this Gate.
	 * @param z a GateZone
	 */
	public void setZone(GateZone z) {
		_zone = z;
	}
	
	/**
	 * Updates the Simulator that this Gate exists in.
	 * @param s a Simulator
	 */
	public void setSimulator(Simulator s) {
		_sim = s;
	}
	
	@Override
	public String getComboAlias() {
		return getName();
	}

	@Override
	public String getComboName() {
		StringBuilder buf = new StringBuilder(getName());
		buf.append(" [").append(_zone.getDescription()).append("] (");
		buf.append(_useCount).append(" flights)");
		return buf.toString();
	}

	
	@Override
	public String getIconColor() {
		return GREY;
	}

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\">");
		buf.append(getHTMLTitle());
		buf.append(getHTMLPosition());
		if (_useCount > 0) {
			buf.append("<br />Used for ");
			buf.append(_useCount);
			buf.append(" Flight");
			if (_useCount > 1)
				buf.append('s');
		}
		
		if (_airlines.size() > 0) {
			buf.append("<br /><br />");
			buf.append("Airline");
			if (_airlines.size() > 1)
				buf.append('s');
			
			buf.append(":<br />");
			_airlines.forEach(a -> buf.append(a.getName()).append("<br />"));
		}
		
		if (_zone != GateZone.DOMESTIC) {
			buf.append("<br /><span class=\"sec bld ita caps\">");
			buf.append(_zone.getDescription());
			buf.append("</span>");
		}
		
		buf.append("</div>");
		return buf.toString();
	}

	@Override
	public int getPaletteCode() {
		return 3;
	}

	@Override
	public int getIconCode() {
		return 52;
	}

	@Override
	public int hashCode() {
		StringBuilder buf = new StringBuilder(getCode());
		buf.append('!').append(getName());
		return buf.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return ((o instanceof Gate) && (hashCode() == o.hashCode()));
	}
	
	@Override
	public int compareTo(NavigationDataBean ndb2) {
		if ((ndb2.getType() != Navaid.GATE) || (_useCount == 0))
			return super.compareTo(ndb2);
		
		Gate g2 = (Gate) ndb2;
		return Integer.compare(_useCount, g2._useCount);
	}
}