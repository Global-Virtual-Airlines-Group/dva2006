// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.List;

import com.vividsolutions.jts.geom.*;

import org.deltava.beans.*;
import org.deltava.util.*;

/**
 * A bean to store Runway information.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class Runway extends NavigationFrequencyBean implements ComboAlias {

	private int _length;
	private int _width = 175;
	private int _heading;
	private int _threshold;
	
	private Simulator _sim = Simulator.UNKNOWN;
	private Surface _sfc = Surface.UNKNOWN;
	private double _magVar;
	
	private String _altCode;
	private boolean _isAltNew;
	
	private transient Geometry _geo;

	/**
	 * Creates a new Runway bean.
	 * @param lat the latitude of the start of the runway
	 * @param lon the longitude of the start of the runway
	 */
	public Runway(double lat, double lon) {
		super(Navaid.RUNWAY, lat, lon);
	}
	
	@Override
	public String getLabel() {
		return getName();
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
	 * Returns the width of the runway.
	 * @return the width in feet
	 * @see Runway#setWidth(int)
	 */
	public int getWidth() {
		return _width;
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
	 * Returns the length of the runway threshold.
	 * @return the length in feet
	 * @see Runway#setThresholdLength(int)
	 */
	public int getThresholdLength() {
		return _threshold;
	}
	
	private GeoLocation getThresholdLocation() {
		if (_threshold == 0) return this;
		double thresholdM = ((double)_threshold) / GeoLocation.FEET_MILES;
		return GeoUtils.bearingPointS(this, thresholdM, _heading - _magVar);
	}
	
	/**
	 * Returns the position of the displaced runway threshold, if any. 
	 * @return the GeoLocation of the threshold
	 */
	public MapEntry getThreshold() {
		if (_threshold == 0) return this;
		return new RunwayThreshold(getThresholdLocation(), getName(), _threshold);
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
	 * Returns the alternate code for this Runway.
	 * @return the alternate code, or null if none
	 * @see Runway#setAlternateCode(String, boolean)
	 */
	public String getAlternateCode() {
		return _altCode;
	}
	
	/**
	 * Returns if the alternate code is the newer code for the Runway.
	 * @return TRUE if the newer code, otherwise FALSE
	 */
	public boolean isAltNew() {
		return _isAltNew;
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
	 * Updates the width of the runway.
	 * @param w the width in feet
	 * @see Runway#getWidth()
	 */
	public void setWidth(int w) {
		_width = Math.max(1, w);
	}
	
	/**
	 * Updates the length of the runway threshold. 
	 * @param len the length in feet
	 * @see Runway#getThresholdLength()
	 */
	public void setThresholdLength(int len) {
		_threshold = Math.max(0,  len);
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
	 * If this runway has been renumbered, the other runway code.
	 * @param altCode the previous runway code, or null if none
	 * @param isNew TRUE if a newer code for the runway, otherwise FALSE
	 */
	public void setAlternateCode(String altCode, boolean isNew) {
		_altCode = altCode;
		_isAltNew = isNew && (altCode != null);
	}
	
	/**
	 * Returns if a particular point is on this runway.
	 * @param loc a GeoLocation
	 * @return TRUE if on the runway, otherwise FALSE
	 */
	public boolean contains(GeoLocation loc) {
		calculateGeo();
		GeometryFactory gf = new GeometryFactory();
		Point pt = gf.createPoint(GeoUtils.toCoordinate(loc));
		return _geo.contains(pt);
	}
	
	private void calculateGeo() {
		if (_geo != null) return;
		
		double rcpHdg = GeoUtils.normalize(_heading + 180);
		GeoLocation end = GeoUtils.bearingPointS(this, (_length * 1.0d / GeoLocation.FEET_MILES), _heading);
		
        // Calculate runway box
		double rwyOffsetSize = (450.0d / GeoLocation.FEET_MILES);
		double rwyWidthSize = (_width * 1.0d / GeoLocation.FEET_MILES / 2);
		
        GeoLocation ofsRwy = GeoUtils.bearingPointS(this, rwyOffsetSize, rcpHdg); GeoLocation ofsEnd = GeoUtils.bearingPointS(end, rwyOffsetSize, _heading);
        double lHdg = GeoUtils.normalize(_heading - 90); double rHdg = GeoUtils.normalize(_heading + 90);
        GeoLocation sl = GeoUtils.bearingPointS(ofsRwy, rwyWidthSize, lHdg);
        GeoLocation sr = GeoUtils.bearingPointS(ofsRwy, rwyWidthSize, rHdg);
        GeoLocation el = GeoUtils.bearingPointS(ofsEnd, rwyWidthSize, lHdg);
        GeoLocation er = GeoUtils.bearingPointS(ofsEnd, rwyWidthSize, rHdg);
		_geo = GeoUtils.toGeometry(List.of(sl, sr, er, el));
	}
 
	@Override
	public String getIconColor() {
		return YELLOW;
	}
	
	@Override
	public int getPaletteCode() {
		return 3;
	}
	
	@Override
	public int getIconCode() {
		return (_threshold == 0) ? 60 : 40;
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
	 * Returns whether a runway code matches the current or alternate codes used for this Runway.
	 * @param rwCode the runway code
	 * @return TRUE if the code matches the current code, alternate code or &quot;ALL&quot;, otherwise FALSE
	 */
	public boolean matches(String rwCode) {
		if (rwCode ==  null) return false;
		String c = rwCode.toUpperCase();
		if (c.startsWith("RW"))
			c = c.substring(2);
		
		return c.equals(getName()) || c.equals(_altCode) || "ALL".equals(c);
	}
	
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\">");
		buf.append(getHTMLTitle());
		if (_altCode != null) {
			buf.append("<span class=\"ita\">");
			buf.append(_isAltNew ? "Now" : "Previously");
			buf.append(" <span class=\"sec bld\">");
			buf.append(_altCode);
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

		buf.append("<br /><br />");
		buf.append(getHTMLPosition());
		buf.append("</div>");
		return buf.toString();
	}
	
	@Override
	public String getComboName() {
		StringBuilder buf = new StringBuilder("Runway ");
		buf.append(getName());
		if (_altCode != null) {
			buf.append(" [");
			buf.append(_isAltNew ? "now " : "was ");
			buf.append(_altCode);
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