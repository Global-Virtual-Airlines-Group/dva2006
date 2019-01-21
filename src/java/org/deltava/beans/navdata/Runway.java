// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2014, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.Simulator;
import org.deltava.util.StringUtils;

/**
 * A bean to store Runway information.
 * @author Luke
 * @version 8.5
 * @since 1.0
 */

public class Runway extends NavigationFrequencyBean implements ComboAlias {

	private int _length;
	private int _heading;
	
	private Simulator _sim = Simulator.UNKNOWN;
	private Surface _sfc = Surface.UNKNOWN;
	private double _magVar;
	
	private String _newCode;

	/**
	 * Creates a new Runway bean.
	 * @param lat the latitude of the start of the runway
	 * @param lon the longitude of the start of the runway
	 */
	public Runway(double lat, double lon) {
		super(Navaid.RUNWAY, lat, lon);
	}
	
	/**
	 * Returns the runway code, or the new code if updated.
	 * @return the latest runway code
	 */
	public String getCurrentName() {
		return StringUtils.isEmpty(_newCode) ? super.getName() : _newCode;
	}

	/**
	 * Returns the length of the runway.
	 * @return the length in feet
	 * @see Runway#setLength(int)
	 */
	public int getLength() {
		return _length;
	}

	/**
	 * Returns the runway heading.
	 * @return the heading in degrees
	 * @see Runway#setHeading(int)
	 */
	public int getHeading() {
		return _heading;
	}
	
	/**
	 * Returns the runway surface type.
	 * @return the Surface
	 * @see Runway#setSurface(Surface)
	 */
	public Surface getSurface() {
		return _sfc;
	}
	
	/**
	 * Returns the magnetic variation at the runway location.
	 * @return the variation in degrees
	 * @see Runway#setMagVar(double)
	 */
	public double getMagVar() {
		return _magVar;
	}
	
	/**
	 * Returns the updated code for this Runway.
	 * @return the new code, or null if none
	 * @see Runway#setNewCode(String)
	 */
	public String getNewCode() {
		return _newCode;
	}
	
	/**
	 * Returns the simulator this runway exists in.
	 * @return a Simulator
	 * @see Runway#setSimulator(Simulator)
	 */
	public Simulator getSimulator() {
		return _sim;
	}

	/**
	 * Updates the length of the runway.
	 * @param len the length in feet
	 * @see Runway#getLength()
	 */
	public void setLength(int len) {
		_length = Math.max(1, len);
	}

	/**
	 * Updates the runway heading.
	 * @param hdg the heading in degrees
	 * @see Runway#getHeading()
	 */
	public void setHeading(int hdg) {
		int h = hdg;
		while (h > 360)
			h -= 360;
		while (h < 0)
			h += 360;

		_heading = h;
	}
	
	/**
	 * Updates the runway surface type.
	 * @param s the Surface
	 * @see Runway#getSurface()
	 */
	public void setSurface(Surface s) {
		_sfc = s;
	}
	
	/**
	 * Updates the Simulator which this runway exists in.
	 * @param sim a Simulator, or UNKNOWN for any
	 * @see Runway#getSimulator()
	 */
	public void setSimulator(Simulator sim) {
		_sim = sim;
	}
	
	/**
	 * Updates the magnetic variation at the runway location.
	 * @param mv the variation in degrees
	 * @see Runway#getMagVar()
	 */
	public void setMagVar(double mv) {
		_magVar = mv;
	}
	
	/**
	 * If this runway has been renumbered, the current runway code.
	 * @param newCode the new runway code, or null if none
	 */
	public void setNewCode(String newCode) {
		_newCode = newCode;
	}
 
	/**
	 * Return the default Google Maps icon color.
	 * @return org.deltava.beans.MapEntry.YELLOW
	 */
	@Override
	public String getIconColor() {
		return YELLOW;
	}
	
	/**
	 * Returns the Google Earth palette code.
	 * @return 3
	 */
	@Override
	public int getPaletteCode() {
		return 3;
	}
	
	/**
	 * Returns the Google Earth icon code.
	 * @return 60
	 */
	@Override
	public int getIconCode() {
		return 60;
	}
	
	/**
	 * Compares two Runways by comparing their airport and runway codes.
	 * @param r2 the Runway
	 * @return TRUE if the airport and runway codes match, otherwise FALSE
	 */
	public boolean equals(Runway r2) {
		return (r2 == null) ? false : getComboAlias().equals(r2.getComboAlias());
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\">");
		buf.append(getHTMLTitle());
		if (_newCode != null) {
			buf.append("<span class=\"ita\">Renumbered to <span class=\"sec bld\">");
			buf.append(_newCode);
			buf.append("</span></span><br />");
		}
		
		buf.append("Heading: ");
		buf.append(StringUtils.format(_heading, "000"));
		buf.append("<br />Length: ");
		buf.append(StringUtils.format(_length, "#,##0"));
		buf.append(" feet");

		// Add ILS frequency if found
		if (getFrequency() != null) {
			buf.append("<br />ILS Frequency: ");
			buf.append(getFrequency());
		}

		buf.append("<br />");
		buf.append(getHTMLPosition());
		buf.append("</div>");
		return buf.toString();
	}
	
	@Override
	public String getComboName() {
		StringBuilder buf = new StringBuilder("Runway ");
		buf.append(getName());
		if (_newCode != null) {
			buf.append(" [now ");
			buf.append(_newCode);
			buf.append(']');
		}
		
		buf.append(" (");
		buf.append(getLength());
		buf.append(" feet - ");
		buf.append(getHeading());
		buf.append(" degrees)");
		return buf.toString();
	}
	
	@Override
	public String getComboAlias() {
		StringBuilder buf = new StringBuilder(getCode());
		buf.append(" RW");
		buf.append(getName());
		return buf.toString();
	}
	
	@Override
	public int hashCode() {
		return (getName() == null) ? super.hashCode() : getName().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Runway) && (o.hashCode() == hashCode());
	}
}