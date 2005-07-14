// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A bean to store runway information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Runway extends NavigationDataBean {
	
	private int _length;
	private int _heading;
	private String _freq;

	/**
	 * Creates a new Runway bean.
	 * @param lat the latitude of the start of the runway
	 * @param lon the longitude of the start of the runway
	 */
	public Runway(double lat, double lon) {
		super(RUNWAY, lat, lon);
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
	 * Returns the frequency of the runway's ILS.
	 * @return the frequency
	 * @see Runway#setFrequency(String)
	 */
	public String getFrequency() {
		return _freq;
	}

	/**
	 * Updates the length of the runway.
	 * @param len the length in feet
	 * @throws IllegalArgumentException if len is zero, negative or > 25000
	 * @see Runway#getLength() 
	 */
	public void setLength(int len) {
		if ((len < 1) || (len > 25000))
			throw new IllegalArgumentException("Length cannot be < 1 or > 25000");
		
		_length = len;
	}
	
	/**
	 * Updates the runway heading.
	 * @param hdg the heading in degrees
	 * @throws IllegalArgumentException if hdg is negative or > 360
	 * @see Runway#getHeading()
	 */
	public void setHeading(int hdg) {
		if ((hdg < 0) || (hdg > 360))
			throw new IllegalArgumentException("Invalid Heading - " + hdg);
		
		_heading = hdg;
	}
	
	/**
	 * Updates the frequency of the runway's ILS.
	 * @param freq the frequency, or null if no ILS
	 * @see Runway#getFrequency()
	 */
	public void setFrequency(String freq) {
		_freq = freq;
	}
}