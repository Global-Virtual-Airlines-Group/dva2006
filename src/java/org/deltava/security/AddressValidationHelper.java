// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security;

import javax.servlet.http.HttpSession;

import org.deltava.beans.system.AddressValidation;
import org.deltava.commands.CommandContext;
import org.deltava.crypt.*;

import org.deltava.util.Base64;
import org.deltava.util.system.SystemData;

/**
 * A helper class to calculate hash code values for e-mail address validation.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class AddressValidationHelper {

   // We're a singleton, alone and lonely.
   private AddressValidationHelper() {
   }

   /**
    * Calculates the validate hash code for an e-mail address, converted to Base64.
    * @param addr the e-mail address
    * @return the hash code
    * @throws NullPointerException if addr is null
    */
   public static String calculateHashCode(String addr) {
      
      // Build the digester and salt it
      MessageDigester md = new MessageDigester(SystemData.get("security.hash.algorithm"));
      md.salt(SystemData.get("security.hash.salt"));
      
      // Calculate the hash
      return Base64.encode(md.digest(addr.getBytes()));
   }
   
   /**
    * Calculates the hash code for an Address Validation bean with a populated e-mail address.
    * @param av the AddressValidation bean
    * @throws NullPointerException if av is null
    */
   public static void calculateHashCode(AddressValidation av) {
      av.setHash(calculateHashCode(av.getAddress()));
   }
   
   /**
    * Restores a submitted hashcode even after IE has converted the plus signs to spaces.
    * @param rawHash the raw hash code
    * @return the restored hash code with spaces converted to plus signs
    */
   public static String formatHash(CharSequence rawHash) {
	   if (rawHash == null)
		   return null;
	   
	   StringBuffer buf = new StringBuffer();
	   for (int x = 0; x < rawHash.length(); x++) {
		   char c = rawHash.charAt(x);
		   buf.append((c == ' ') ? '+' : c);
	   }
	   
	   // If we've got data but no equals at the end, append it
	   if ((buf.length() > 0) && (buf.lastIndexOf("=") != (buf.length() - 1)))
		   buf.append('=');
	   
	   return buf.toString();
   }
   
   /**
    * Removes the <i>address invalid</i> flag from an HTTP session.
    * @param s the HTTP session
    * @see CommandContext#ADDRINVALID_ATTR_NAME
    */
   public static void clearSessionFlag(HttpSession s) {
	   if (s == null)
		   return;
	   
	   s.removeAttribute(CommandContext.ADDRINVALID_ATTR_NAME);
   }
}