// Copyright 2012, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.UseCount;

/**
 * A bean to store airport Gate information.
 * @author Luke
 * @version 6.3
 * @since 5.1
 */

public class Gate extends NavigationDataBean implements UseCount {

	public enum Type {
		GATE, PARKING, DOCK;
	}
	
	private int _heading;
	private int _number;
	private Type _type = Type.GATE;
	private int _useCount;

	/**
	 * Creates the bean.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 */
	public Gate(double lat, double lon) {
		super(Navaid.GATE, lat, lon);
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
	 * Returns the number of times this Gate was used in a flight.
	 * @return the number of flights
	 */
	@Override
	public int getUseCount() {
		return _useCount;
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
		while (hdg > 360)
			hdg -= 360;
		while (hdg < 0)
			hdg += 360;

		_heading = hdg;
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
	 * Return the default Google Maps icon color.
	 * @return org.deltava.beans.MapEntry.GREY
	 */
	@Override
	public String getIconColor() {
		return GREY;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
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
		return Integer.valueOf(_useCount).compareTo(Integer.valueOf(g2._useCount));
	}
}