// Copyright 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

import javax.xml.rpc.ServiceException;

import com.flightaware.flightxml.soap.FlightXML2.*;

/**
 * An abstract class to describe FlightAware WSDL Data Access Objects. 
 * @author Luke
 * @version 3.3
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
	protected final FlightXML2Soap getStub() throws ServiceException {
		FlightXML2Locator locator = new FlightXML2Locator();
		FlightXML2SoapStub df = (FlightXML2SoapStub) locator.getFlightXML2Soap();
        df.setUsername(_userID);
        df.setPassword(_password);
        return df;
	}
}