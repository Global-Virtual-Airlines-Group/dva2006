// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.DatabaseBean;

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
    * @param id the Database ID of the Pilot <i>or Applicant</i>
    * @param addr the new e-mail address
    */
   public AddressValidation(int id, String addr) {
      super();
      setID(id);
      _addr = addr.trim();
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
    * Updates the hash code sent to the user to validate the address.
    * @param hash the new hash code
    * @see AddressValidation#getHash()
    */
   public void setHash(String hash) {
      _hash = hash;
   }
}