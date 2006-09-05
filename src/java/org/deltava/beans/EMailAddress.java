// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to mark objects that can be used as the recipient of an e-mail message.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface EMailAddress {
	
	/**
	 * Invalid e-mail address constant.
	 */
	public static final String INVALID_ADDR = "-";
	
	/**
	 * E-mail address validation regular expression.
	 */
	public static final String VALID_ADDR_REGEXP = "^[\\w](([_\\.\\-\\+]?[\\w]+)*)@([\\w]+)(([\\.-]?[\\w]+)*)\\.([A-Za-z]{2,})$";

   /**
    * Returns the message recipient's name.
    * @return the user name
    */
   public String getName();
   
   /**
    * Returns the message recipient's e-mail address.
    * @return an RFC822-compliant e-mail address
    */
   public String getEmail();
}