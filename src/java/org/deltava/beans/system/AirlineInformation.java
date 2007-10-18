// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.ComboAlias;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store information about other virtual airline databases.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirlineInformation implements Comparable<AirlineInformation>, ComboAlias, Cacheable {
   
   private String _code;
   private String _name;
   private String _dbName;
   private String _domain;
   private boolean _canTransfer;

   /**
    * Creates a new Airline Information bean.
    * @param code the Airline Code
    * @param name the Airline name
    * @throws NullPointerException if code or name are null
    * @see AirlineInformation#getCode()
    * @see AirlineInformation#getName()
    */
   public AirlineInformation(String code, String name) {
      super();
      _code = code.trim().toUpperCase();
      _name = name.trim();
   }
   
   /**
    * Returns the Airline code.
    * @return the airline code
    */
   public String getCode() {
      return _code;
   }
   
   /**
    * Returns the Airline domain name.
    * @return the domain
    * @see AirlineInformation#setDomain(String)
    */
   public String getDomain() {
      return _domain;
   }
   
   /**
    * Returns the Airline database name.
    * @return the database name
    * @see AirlineInformation#setDB(String)
    */
   public String getDB() {
      return _dbName;
   }
   
   /**
    * Returns the Airline name.
    * @return the airline name
    */
   public String getName() {
      return _name;
   }
   
   /**
    * Returns wether Pilots can be transferred into this Airline.
    * @return TRUE if Pilots can be transferred here, otherwise FALSE
    * @see AirlineInformation#setCanTransfer(boolean)
    */
   public boolean getCanTransfer() {
      return _canTransfer;
   }
   
   /**
    * Updates the Airline domain name. The domain will be converted to lowercase.
    * @param domain the domain name
    * @throws NullPointerException if domain is null
    * @see AirlineInformation#getDomain()
    */
   public void setDomain(String domain) {
      _domain = domain.trim().toLowerCase();
   }
   
   /**
    * Updates the Airline database name. The database will be converted to lowercase.
    * @param dbName the database name
    * @throws NullPointerException if dbName is null
    * @see AirlineInformation#getDB()
    */
   public void setDB(String dbName) {
      _dbName = dbName.trim().toLowerCase();
   }
   
   /**
    * Updates wether Pilots can be transferred <i>into</i> this Airline.
    * @param doTransfer TRUE if Pilots can be transferred, otherwise FALSE
    * @see AirlineInformation#getCanTransfer()
    */
   public void setCanTransfer(boolean doTransfer) {
      _canTransfer = doTransfer;
   }
   
   public String getComboName() {
      return getName();
   }
   
   public String getComboAlias() {
      return getCode();
   }
   
   public Object cacheKey() {
      return _code;
   }
   
   /**
    * Compares two Airline Information beans by comparing their codes.
    */
   public int compareTo(AirlineInformation ai2) {
      return _code.compareTo(ai2._code);
   }
   
   public boolean equals(Object o) {
	   if (o instanceof AirlineInformation)
		   return (compareTo((AirlineInformation) o) == 0);
	   else if (o instanceof String)
		   return _code.equals(o);
	   else
		   return false;
   }
   
   /**
    * Returns the airline code.
    */
   public String toString() {
	   return _code;
   }
   
   /**
    * Returns the airline code's hashcode.
    */
   public int hashCode() {
      return _code.hashCode();
   }
}