// Copyright 2005, 2006, 2014, 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to mark objects that can be used as the recipient of an e-mail message.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public interface EMailAddress {
	
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
   
   /**
    * Returns whether this recipient has an invalid e-mail address.
    * @return TRUE if invalid, otherwise FALSE
    */
   public boolean isInvalid();
   
   /**
    * Null-safe utility method to check if address is non-null and valid.
    * @param addr an EMailAddress
    * @return TRUE if non-null and not invalid, otherwise false
    */
   static boolean isValid(EMailAddress addr) {
	   return (addr != null) && !addr.isInvalid();
   }
}