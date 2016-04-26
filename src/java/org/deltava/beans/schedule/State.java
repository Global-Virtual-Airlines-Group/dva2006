// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.ComboAlias;

/**
 * An Enumeration of US States.
 * @author Luke
 * @version 7.0
 * @since 3.2
 */

public enum State implements ComboAlias {
	
	AK("Alaska"), AL("Alabama"), AR("Arkansas"), AZ("Arizona"), CA("California"), CO("Colorado"),
	CT("Connecticut"), DE("Delaware"), FL("Florida"), GA("Georgia"), HI("Hawaii"), IA("Iowa"), 
	ID("Idaho"), IL("Illinois"), 	IN("Indiana"), KS("Kansas"), KY("Kentucky"), LA("Louisiana"), 
	MA("Massachusetts"), MD("Maryland"), ME("Maine"), MI("Michigan"), MN("Minnesota"), 
	MO("Missouri"), MS("Mississippi"), MT("Montana"), NC("North Carolina"), ND("North Dakota"), 
	NE("Nebraska"), NH("New Hampshire"), NJ("New Jersey"), NM("New Mexico"), NV("Nevada"), 
	NY("New York"), OH("Ohio"), OK("Oklahoma"), OR("Oregon"), PA("Pennsylvania"), 
	RI("Rhode Island"), SC("South Carolina"), SD("South Dakota"), TN("Tennessee"),
	TX("Texas"), UT("Utah"), VA("Virginia"), VT("Vermont"), WA("Washington"), WI("Wisconsin"),
	WV("West Virginia"), WY("Wyoming");

	private String _name;
	
	State(String name) {
		_name = name;
	}

	/**
	 * Returns the State name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	@Override
	public String getComboName() {
		return _name;
	}

	@Override
	public String getComboAlias() {
		return name();
	}
}