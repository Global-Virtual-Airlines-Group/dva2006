// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.util.StringUtils;

/**
 * A bean to store online Controller information.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class Controller extends NetworkUser {
   
   /**
    * Facility types.
    */
   private static final String[] FACILITIES = {"Observer", "Flight Service Station", "Clearance Delivery", "Ground", "Tower",
         "Approach/Departure", "Center", "ATIS"};
   
   public static final int OBSERVER = 0;
   public static final int FSS = 1;
   public static final int DEL = 2;
   public static final int GND = 3;
   public static final int TWR = 4;
   public static final int APP = 5;
   public static final int DEP = 5;
   public static final int CTR = 6;
   public static final int ATIS = 7;
   
   private static final String[] FAC_COLORS = {WHITE, PURPLE, BLUE, ORANGE, GREEN, YELLOW, RED};
   
   private int _facility;
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
     * @return NetworkUser.ATC;
     */
    public int getType() {
        return NetworkUser.ATC;
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
     * @see Controller#getFacilityType()
     * @see Controller#setFacilityType(int)
     */
    public int getFacility() {
       return _facility;
    }
    
    /**
     * Returns the Controller's facility type name.
     * @return the facility type
     * @see Controller#getFacility()
     * @see Controller#setFacilityType(int)
     */
    public String getFacilityType() {
       return FACILITIES[_facility];
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
    		setFacilityType(ATIS);
    }
    
    /**
     * Sets the Controller's facility type.
     * @param type the facility type code
     * @see Controller#getFacility()
     * @see Controller#getFacilityType()
     */
    public void setFacilityType(int type) {
       if ((type < 0) || (type >= FACILITIES.length))
          throw new IllegalArgumentException("Invalid Controller facility - " + type);
       
       _facility = type;
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
     * @see Controller#getFacilityType()
     */
    public String getIconColor() {
    	return FAC_COLORS[_facility];
    }
    
    /**
	 * Returns the Google Map Infobox text.
	 * @return HTML text
	 */
    public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><b>");
		buf.append(getCallsign());
		buf.append("</b> (");
		buf.append(StringUtils.stripInlineHTML(getName()));
		buf.append(")<span class=\"small\"><br /><br />Network ID: ");
		buf.append(String.valueOf(getID()));
		buf.append("<br />Controller rating: ");
		buf.append(getRatingName());
		buf.append("<br /><br />Facility Type: ");
		buf.append(getFacilityType());
		buf.append("</span></div>");
		return buf.toString();
    }
}