// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import javax.xml.rpc.ServiceException;

import com.flightaware.directflight.soap.DirectFlight.*;

/**
 * An abstract class to describe FlightAware WSDL Data Access Objects. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

abstract class FlightAwareDAO {
	
	private String _userID;
	private String _password;

	/**
	 * Sets the User ID to use.
	 * @param usr the user ID
	 */
	public final void setUser(String usr) {
		_userID = usr;
	}
	
	/**
	 * Sets the password to use.
	 * @param password the password
	 */
	public final void setPassword(String password) {
		_password = password;
	}
	
	/**
	 * Retrieves an AXIS SOAP stub for calling FlightAware SOAP functions. The
	 * credentials will have already been set.
	 * @return a SOAP stub object
	 * @throws ServiceException if something bad happens
	 */
	protected DirectFlightSoap getStub() throws ServiceException {
        DirectFlightLocator locator = new DirectFlightLocator();
        DirectFlightSoapStub df = (DirectFlightSoapStub) locator.getDirectFlightSoap();
        df.setUsername(_userID);
        df.setPassword(_password);
        return df;
	}
}