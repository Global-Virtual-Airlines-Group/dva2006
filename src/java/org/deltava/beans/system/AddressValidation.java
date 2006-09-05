// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store E-Mail Address validation data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AddressValidation extends DatabaseBean {
   
   private String _addr;
   private String _hash;

   /**
    * Creates a new Address validation entry.
    * @param id the Database ID of the Pilot <i>or Applicant</i>.
    * @param addr the new e-mail address
    * @throws NullPointerException if addr is null
    * @throws IllegalArgumentException if id is zero or negative
    */
   public AddressValidation(int id, String addr) {
      super();
      setID(id);
      setAddress(addr);
   }

   /**
    * Returns the new e-mail address.
    * @return the new address
    */
   public String getAddress() {
      return _addr;
   }
   
   /**
    * Returns the hashcode to be sent to the user to validate the address.
    * @return the hash code
    * @see AddressValidation#setHash(String)
    */
   public String getHash() {
      return _hash;
   }
   
   /**
    * Returns wether the address is valid or not.
    * @return TRUE if the address is valid, otherwise FALSE
    */
   public boolean getIsValid() {
	   return !StringUtils.isEmpty(_addr) && !EMailAddress.INVALID_ADDR.equals(_addr);
   }
   
   /**
    * Updates the hash code sent to the user to validate the address.
    * @param hash the new hash code
    * @see AddressValidation#getHash()
    */
   public void setHash(String hash) {
      _hash = hash;
   }
   
   /**
    * Updates the user's e-mail address.
    * @param addr the new e-mail address
    * @throws NullPointerException if addr is null
    * @see AddressValidation#getAddress()
    */
   public void setAddress(String addr) {
      _addr = addr.trim();
   }
}