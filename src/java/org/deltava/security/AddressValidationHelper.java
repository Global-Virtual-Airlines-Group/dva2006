// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.zip.CRC32;

import org.deltava.crypt.*;

import org.deltava.util.Base64;
import org.deltava.util.system.SystemData;

/**
 * A helper class to calculate hash code values for e-mail address validation.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public final class AddressValidationHelper {

   // We're a singleton, alone and lonely.
   private AddressValidationHelper() {
	   super();
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
    * Calculates the validate hash code for an e-mail address using CRC32, converted to hexadecimal.
    * @param addr the e-mail address
    * @return the hash code
    * @throws NullPointerException if addr is null
    */
   public static String calculateCRC32(String addr) {
	   CRC32 crc = new CRC32();
	   crc.update(SystemData.get("security.hash.salt").getBytes());
	   crc.update(addr.getBytes());
	   
	   // Return the CRC32 plus an equals to match the Base64 encoded hashcode
	   StringBuilder buf = new StringBuilder(Long.toHexString(crc.getValue()));
	   buf.append('=');
	   return buf.toString();
   }
   
   /**
    * Restores a submitted hashcode even after IE has converted the plus signs to spaces.
    * @param rawHash the raw hash code
    * @return the restored hash code with spaces converted to plus signs
    */
   public static String formatHash(CharSequence rawHash) {
	   if (rawHash == null)
		   return null;
	   
	   StringBuilder buf = new StringBuilder();
	   for (int x = 0; x < rawHash.length(); x++) {
		   char c = rawHash.charAt(x);
		   buf.append((c == ' ') ? '+' : c);
	   }
	   
	   // If we've got data but no equals at the end, append it
	   if ((buf.length() > 0) && (buf.lastIndexOf("=") != (buf.length() - 1)))
		   buf.append('=');
	   
	   return buf.toString();
   }
}