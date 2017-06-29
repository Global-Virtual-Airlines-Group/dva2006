// Copyright 2005, 2007, 2010, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.beans.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store information about other virtual airlines.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class AirlineInformation implements Comparable<AirlineInformation>, Auditable, ComboAlias, Cacheable {
   
   private final String _code;
   private final String _name;
   private String _dbName;
   private String _domain;
   private boolean _canTransfer;
   private boolean _histRestricted;
   private boolean _hasSSL;

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
    * Returns whether Pilots can be transferred into this Airline.
    * @return TRUE if Pilots can be transferred here, otherwise FALSE
    * @see AirlineInformation#setCanTransfer(boolean)
    */
   public boolean getCanTransfer() {
      return _canTransfer;
   }
   
   /**
    * Returns whether historic routes are restricted to historic equipment.
    * @return TRUE if routes restricted, otherwise FALSE
    * @see AirlineInformation#setHistoricRestricted(boolean)
    */
   public boolean getHistoricRestricted() {
	   return _histRestricted;
   }
   
   /**
    * Returns whether the Airline has an SSL-enabled web site.
    * @return TRUE if SSL enabled, otherwise FALSE
    * @see AirlineInformation#setSSL(boolean)
    */
   public boolean getSSL() {
	   return _hasSSL;
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
    * Updates whether Pilots can be transferred <i>into</i> this Airline.
    * @param doTransfer TRUE if Pilots can be transferred, otherwise FALSE
    * @see AirlineInformation#getCanTransfer()
    */
   public void setCanTransfer(boolean doTransfer) {
      _canTransfer = doTransfer;
   }
   
   /**
    * Updates whether historic routes are restricted to historic equipment.
    * @param isRestricted TRUE if routes restricted, otherwise FALSE
    * @see AirlineInformation#getHistoricRestricted()
    */
   public void setHistoricRestricted(boolean isRestricted) {
	   _histRestricted = isRestricted;
   }
   
   /**
    * Updates whether this Airline has an SSL-enabled web site. 
    * @param hasSSL TRUE if the Airline has SSL, otherwise FALSE
    * @see AirlineInformation#getSSL()
    */
   public void setSSL(boolean hasSSL) {
	   _hasSSL = hasSSL;
   }
   
   @Override
   public String getComboName() {
      return getName();
   }
   
   @Override
   public String getComboAlias() {
      return getCode();
   }
   
   @Override
   public Object cacheKey() {
      return _code;
   }
   
   /**
    * Compares two Airline Information beans by comparing their codes.
    */
   @Override
   public int compareTo(AirlineInformation ai2) {
      return _code.compareTo(ai2._code);
   }
   
   @Override
   public boolean equals(Object o) {
	   return (o instanceof AirlineInformation) && (compareTo((AirlineInformation) o) == 0);
   }
   
   @Override
   public String toString() {
	   return _code;
   }
   
   @Override
   public int hashCode() {
      return _code.hashCode();
   }

   @Override
   public String getAuditID() {
	   return _code;
   }
}