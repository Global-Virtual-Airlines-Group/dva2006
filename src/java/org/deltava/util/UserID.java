// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

/**
 * A bean to parse user IDs.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class UserID {
	
	private String _airline;
	private int _id;

	/**
	 * Parses a pilot code to determine the user ID.
	 * @param code the pilot code
	 */
	public UserID(CharSequence code) {
		super();
		
        if (StringUtils.isEmpty(code))
            return;

        StringBuilder pBuf = new StringBuilder();
        StringBuilder cBuf = new StringBuilder();
        for (int x = 0; x < code.length(); x++) {
            char c = Character.toUpperCase(code.charAt(x));
            if (Character.isDigit(c))
                cBuf.append(c);
            else if (Character.isLetter(c))
                pBuf.append(c);
        }

        // Save the prefix and the code
        _airline = pBuf.toString();
        try {
        	if (cBuf.length() > 0)
        		_id = Integer.parseInt(cBuf.toString());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid Pilot Code - " + code);
        }
	}

	/**
	 * Returns the airline code for this user. 
	 * @return the airline code
	 */
	public String getAirlineCode() {
		return _airline;
	}
	
	/**
	 * Returns whether an airline code was submitted.
	 * @return TRUE if a letter was in the ID, otherwise FALSE
	 */
	public boolean hasAirlineCode() {
		return (_airline != null);
	}

	/**
	 * Returns the seniority number for this user.
	 * @return the pilot code
	 */
	public int getUserID() {
		return _id;
	}
	
	/**
	 * Returns the Pilot code.
	 */
	public String toString() {
		return _airline + StringUtils.format(_id, "#000");
	}
}