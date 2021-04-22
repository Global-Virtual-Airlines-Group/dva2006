// Copyright 2005, 2006, 2009, 2010, 2011, 2015, 2016, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.beans.OnlineNetwork;

import org.deltava.util.StringUtils;

/**
 * A bean to store online Controller information.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class Controller extends ConnectedUser {
	
	/**
	 * Observer frequency.
	 */
	public static final String OBS_FREQ = "199.998";
   
   private Facility _type = Facility.OBS;
   private int _range;
   
   private final SortedSet<RadioPosition> _freqs = new TreeSet<RadioPosition>();

    /**
     * Initializes the bean with a particular user ID.
     * @param id the user ID
     * @param net the OnlineNetwork
     */
    public Controller(int id, OnlineNetwork net) {
        super(id, net);
    }

    @Override
    public final Type getType() {
        return Type.ATC;
    }
    
    /**
     * Returns the Controller's communication frequency.
     * @return the frequency
     * @see Controller#addPosition(RadioPosition)
     */
    public String getFrequency() {
    	return _freqs.isEmpty() ? OBS_FREQ : _freqs.first().getFrequency();
    }
    
    /**
     * Returns the Controller's facility type code.
     * @return the facility code
     * @see Controller#setFacility(Facility)
     */
    public Facility getFacility() {
       return _type;
    }
    
    /**
     * Returns the Controller's visibility range.
     * @return the range in miles
     * @see Controller#setRange(int)
     */
    public int getRange() {
    	return (_range <= 0) ? _type.getRange() : _range;
    }
    
    public Collection<RadioPosition> getRadios() {
    	return _freqs;
    }
    
    public void addPosition(RadioPosition rp) {
    	if (_freqs.isEmpty())
    		super.setPosition(rp.getLatitude(), rp.getLongitude());
    	_freqs.add(rp);
    }
    
    @Override
    public final void setName(String name) {
    	String n = name.trim();
    	int pos = n.lastIndexOf(' ');
    	if (pos == -1) {
    		setLastName(n);
    		setFirstName("??");
    	} else {
    		setLastName(n.substring(pos + 1));
    		setFirstName(n.substring(0, pos));
    	}
    }
    
    @Override
    public void setCallsign(String cs) {
    	super.setCallsign(cs);
    	if (getCallsign().endsWith("_ATIS"))
    		setFacility(Facility.ATIS);
    }
    
    /**
     * Sets the Controller's facility type.
     * @param ft the Facility
     * @see Controller#getFacility()
     */
    public void setFacility(Facility ft) {
    	_type = ft;
    }
    
    /**
     * Updates the Controller's visibility range.
     * @param rng the range in miles
     * @see Controller#getRange()
     */
    public void setRange(int rng) {
    	_range = rng;
    }
    
    /**
     * Returns whether the controller is an Observer.
     * @return TRUE if the Controller has an Observer rating or _OBS callsign
     */
    public boolean isObserver() {
    	return (getRating() == Rating.OBS) || getCallsign().endsWith("_OBS");
    }
    
    /**
     * Returns whether the Conrtoller has set a primary frequency.
     * @return TRUE if a frequency has been set, otherwise FALSE
     * @see Controller#getFrequency()
     */
    public boolean hasFrequency() {
    	return !OBS_FREQ.equals(getFrequency());
    }
    
    @Override
    public String getIconColor() {
    	return _type.getColor();
    }
    
    /**
     * Compares this user to another Controller by comparing the Network IDs and callsigns.
     * @param c2 the Controller
     * @return  TRUE if the IDs and Callsigns are equal, otherwise FALSE
     */
    public int compareTo(Controller c2) {
    	int tmpResult = super.compareTo(c2);
    	return (tmpResult == 0) ? getCallsign().compareTo(c2.getCallsign()) : tmpResult;
    }
    
    @Override
    public final boolean equals(Object o2) {
    	if (o2 instanceof Controller)
    		return (compareTo((Controller) o2) == 0);
    	
   		return super.equals(o2);
    }
    
    @Override
    public int hashCode() {
    	return getCallsign().hashCode();
    }
    
    @Override
    public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox onlineATC\"><span class=\"bld\">");
		buf.append(getCallsign());
		buf.append("</span> (");
		buf.append(StringUtils.stripInlineHTML(getName()));
		buf.append(")<span class=\"small\"><br /><br />Network ID: ");
		buf.append(String.valueOf(getID()));
		buf.append("<br />Controller rating: ");
		Rating r = getRating();
		buf.append(r.getName());
		buf.append(" (");
		buf.append(r.toString());
		buf.append(")<br /><br />Facility Type: ");
		buf.append(_type.getName());
		buf.append("</span></div>");
		return buf.toString();
    }
}