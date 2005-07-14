// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import org.deltava.crypt.*;

import org.deltava.util.Base64;
import org.deltava.util.system.SystemData;

/**
 * A helper class to calculate hash code values for e-mail address validation.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AddressValidationHelper {

   // We're a singleton, alone and lonely.
   private AddressValidationHelper() {
   }

   /**
    * Calculates the validate hash code for an e-mail address, converted to Base64.
    * @param addr the e-mail address
    * @return the hash code
    * @throws NullPointerException if addr is null
    */
   public static final String calculateHashCode(String addr) {
      
      // Build the digester and salt it
      MessageDigester md = new MessageDigester(SystemData.get("security.hash.algorithm"));
      md.salt(SystemData.get("security.hash.salt"));
      
      // Calculate the hash
      return Base64.encode(md.digest(addr.getBytes()));
   }
}