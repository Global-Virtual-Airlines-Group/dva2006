// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import static org.deltava.beans.MapEntry.*;

/**
 * An enumeration of VATSIM/IVAO ATC facility types. 
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public enum Facility {
	
	OBS("Observer", WHITE, 30), FSS("Flight Service Station", PURPLE, 1750), DEL("Clearance Delivery", BLUE, 25),
	GND("Ground", ORANGE, 35), TWR("Tower", YELLOW, 60), APP("Approach", GREEN, 150), CTR("Center", RED, 600),
	ATIS("ATIS", WHITE, 30);
	
	private String _name;
	private String _color;
	private int _range;

	Facility(String name, String color, int range) {
		_name = name;
		_color = color;
		_range = range;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getColor() {
		return _color;
	}
	
	public int getRange() {
		return _range;
	}
}