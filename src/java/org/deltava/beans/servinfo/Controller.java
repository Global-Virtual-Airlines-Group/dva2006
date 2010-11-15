// Copyright 2005, 2006, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.util.StringUtils;

/**
 * A bean to store online Controller information.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class Controller extends NetworkUser {
   
   private Facility _type;
   private String _freq;

    /**
     * Initializes the bean with a particular user ID.
     * @param id the user ID
     */
    public Controller(int id) {
        super(id);
    }

    /**
     * Returns the user type.
     * @return NetworkUser.Type.ATC
     */
    public final Type getType() {
        return Type.ATC;
    }
    
    /**
     * Returns the Controller's communication frequency.
     * @return the frequency
     * @see Controller#setFrequency(String)
     */
    public String getFrequency() {
    	return _freq;
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
     * Sets the user name.
     * @param name the controller name
     */
    public final void setName(String name) {
    	int pos = name.lastIndexOf(' ');
    	if (pos == -1) {
    		setLastName(name);
    		setFirstName("??");
    	} else {
    		setLastName(name.substring(pos + 1));
    		setFirstName(name.substring(0, pos));
    	}
    }
    
    /**
     * Updates the Controller's callsign.
     * @param cs the callsign
     * @throws NullPointerException if cs is null
     * @see NetworkUser#getCallsign()
     */
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
     * Updates the Controller's communication frequency.
     * @param freq the frequency
     * @see Controller#getFrequency()
     */
    public void setFrequency(String freq) {
    	_freq = freq;
    }
    
    /**
     * Returns the Google Maps icon color.
     * @return the color as defined by COLORS and faclity type
     * @see Controller#getFacility()
     */
    public String getIconColor() {
    	return _type.getColor();
    }
    
    /**
	 * Returns the Google Map Infobox text.
	 * @return HTML text
	 */
    public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><span class=\"bld\">");
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