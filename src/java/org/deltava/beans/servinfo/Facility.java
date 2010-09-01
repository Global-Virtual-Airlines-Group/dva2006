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
	
	OBS("Observer", WHITE), FSS("Flight Service Station", PURPLE), DEL("Clearance Delivery", BLUE),
	GND("Ground", ORANGE), TWR("Tower", YELLOW), APP("Approach", GREEN), CTR("Center", RED),
	ATIS("ATIS", WHITE);
	
	private String _name;
	private String _color;

	Facility(String name, String color) {
		_name = name;
		_color = color;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getColor() {
		return _color;
	}
}