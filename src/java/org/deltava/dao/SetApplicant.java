// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Applicants to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetApplicant extends PilotWriteDAO {

   /**
    * Initialize the Data Access Object.
    * @param c the JDBC connection to use
    */
   public SetApplicant(Connection c) {
      super(c);
   }
   
   /**
    * Marks an Applicant as Rejected.
    * @param a the Applicant object
    * @throws DAOException if a JDBC error occurs
    */
   public void reject(Applicant a) throws DAOException {
      try {
         prepareStatement("UPDATE APPLICANTS SET STATUS=? WHERE (ID=?)");
         _ps.setInt(1, a.getID());
         executeUpdate(1);
         
         // Update the cache
         GetApplicant._cache.add(a);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Writes an Applicant to the database.
    * @param a the Applicant object
    * @throws DAOException if a JDBC error occurs
    */
   public void write(Applicant a) throws DAOException {
      try {
         startTransaction();
         
         // Prepare an INSERT or UPDATE statement
         if (a.getID() == 0) {
            // Write the USERDATA Object
            prepareStatement("INSERT INTO common.USERDATA (AIRLINE, DBNAME, TABLENAME, DOMAIN) VALUES "
                  + "(?, ?, ?, ?)");
            _ps.setString(1, SystemData.get("airline.code"));
            _ps.setString(2, SystemData.get("airline.db"));
            _ps.setString(3, "APPLICANTS");
            _ps.setString(4, SystemData.get("airline.domain"));
            executeUpdate(1);
            
            // Get the new applicant ID
            a.setID(getNewID());
            
            prepareStatement("INSERT INTO APPLICANTS (STATUS, FIRSTNAME, LASTNAME, EMAIL, LOCATION, IMHANDLE, "
                  + "VATSIM_ID, IVAO_ID, LEGACY_HOURS, LEGACY_URL, LEGACY_OK, HOME_AIRPORT, FLEET_NOTIFY, "
                  + "EVENT_NOTIFY, NEWS_NOTIFY, SHOW_EMAIL, CREATED, REGHOSTNAME, DFORMAT, TFORMAT, NFORMAT, "
               	+ "AIRPORTCODE, TZ, UISCHEME, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            _ps.setInt(25, a.getID());
         } else {
            prepareStatement("UPDATE APPLICANTS SET STATUS=?, FIRSTNAME=?, LASTNAME=?, EMAIL=?, LOCATION=?, "
                  + "IMHANDLE=?, VATSIM_ID=?, IVAO_ID=?, LEGACY_HOURS=?, LEGACY_URL=?, LEGACY_OK=?, "
                  + "HOME_AIRPORT=?, FLEET_NOTIFY=?, EVENT_NOTIFY=?, NEWS_NOTIFY=?, SHOW_EMAIL=?, CREATED=?, "
                  + "REGHOSTNAME=?, DFORMAT=?, TFORMAT=?, NFORMAT=?, AIRPORTCODE=?, TZ=?, UISCHEME=?, EQTYPE=?, "
                  + "RANK=? WHERE (ID=?)");
            _ps.setString(25, a.getEquipmentType());
            _ps.setString(26, a.getRank());
            _ps.setInt(27, a.getID());
         }
         
         // Set the fields
         _ps.setInt(1, a.getStatus());
         _ps.setString(2, a.getFirstName());
         _ps.setString(3, a.getLastName());
         _ps.setString(4, a.getEmail());
         _ps.setString(5, a.getLocation());
         _ps.setString(6, a.getIMHandle());
         _ps.setString(7, (String) a.getNetworkIDs().get("VATSIM"));
         _ps.setString(8, (String) a.getNetworkIDs().get("IVAO"));
         _ps.setDouble(9, a.getLegacyHours());
         _ps.setString(10, a.getLegacyURL());
         _ps.setBoolean(11, a.getLegacyVerified());
         _ps.setString(12, a.getHomeAirport());
         _ps.setBoolean(13, a.getNotifyOption(Person.FLEET));
         _ps.setBoolean(14, a.getNotifyOption(Person.EVENT));
         _ps.setBoolean(15, a.getNotifyOption(Person.NEWS));
         _ps.setInt(16, a.getEmailAccess());
         _ps.setTimestamp(17, createTimestamp(a.getCreatedOn()));
         _ps.setString(18, a.getRegisterHostName());
         _ps.setString(19, a.getDateFormat());
         _ps.setString(20, a.getTimeFormat());
         _ps.setString(21, a.getNumberFormat());
         _ps.setInt(22, a.getAirportCodeType());
         _ps.setString(23, a.getTZ().getID());
         _ps.setString(24, a.getUIScheme());
         
         // Update the cache
         GetApplicant._cache.add(a);
         
         // Update the database and commit
         executeUpdate(1);
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
   
   /**
    * Marks an Applicant as hired, and updates the Pilot ID.
    * @param a the Applicant bean
    * @throws DAOException if a JDBC error occurs
    */
   public void hire(Applicant a) throws DAOException {
      try {
         startTransaction();
         
         // Write the USERDATA Object
         prepareStatement("INSERT INTO common.USERDATA (AIRLINE, DBNAME, TABLENAME, DOMAIN) VALUES "
               + "(?, ?, ?, ?)");
         _ps.setString(1, SystemData.get("airline.code"));
         _ps.setString(2, SystemData.get("airline.db"));
         _ps.setString(3, "PILOTS");
         _ps.setString(4, SystemData.get("airline.domain"));
         executeUpdate(1);
         
         // Get the new Pilot's database ID
         a.setPilotID(getNewID());
         
         // Write the new Pilot object
         prepareStatement("INSERT INTO PILOTS (FIRSTNAME, LASTNAME, STATUS, LDAP_DN, EMAIL, LOCATION, "
               + "IMHANDLE, LEGACY_HOURS, HOME_AIRPORT, EQTYPE, RANK, VATSIM_ID, IVAO_ID, CREATED, LOGINS, "
               + "LAST_LOGIN, LAST_LOGOFF, TZ, FILE_NOTIFY, EVENT_NOTIFY, NEWS_NOTIFY, SHOW_EMAIL, "
               + "UISCHEME, LOGINHOSTNAME, DFORMAT, TFORMAT, NFORMAT, AIRPORTCODE, ID) VALUES (?, ?, ?, ?, ?, ?, "
               + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ID)");
         _ps.setString(1, a.getFirstName());
         _ps.setString(2, a.getLastName());
         _ps.setInt(3, Pilot.ACTIVE);
         _ps.setString(4, a.getDN());
         _ps.setString(5, a.getEmail());
         _ps.setString(6, a.getLocation());
         _ps.setString(7, a.getIMHandle());
         _ps.setDouble(8, a.getLegacyHours());
         _ps.setString(9, a.getHomeAirport());
         _ps.setString(10, a.getEquipmentType());
         _ps.setString(11, a.getRank());
         _ps.setString(12, (String) a.getNetworkIDs().get("VATSIM"));
         _ps.setString(13, (String) a.getNetworkIDs().get("IVAO"));
         _ps.setTimestamp(14, createTimestamp(a.getCreatedOn()));
         _ps.setInt(15, a.getLoginCount());
         _ps.setTimestamp(16, createTimestamp(a.getLastLogin()));
         _ps.setTimestamp(17, createTimestamp(a.getLastLogoff()));
         _ps.setString(18, a.getTZ().getID());
         _ps.setBoolean(19, a.getNotifyOption(Person.FLEET));
         _ps.setBoolean(20, a.getNotifyOption(Person.EVENT));
         _ps.setBoolean(21, a.getNotifyOption(Person.NEWS));
         _ps.setInt(22, a.getEmailAccess());
         _ps.setString(23, a.getUIScheme());
         _ps.setString(24, a.getLoginHost());
         _ps.setString(25, a.getDateFormat());
         _ps.setString(26, a.getTimeFormat());
         _ps.setString(27, a.getNumberFormat());
         _ps.setInt(28, a.getAirportCodeType());
         _ps.setInt(29, a.getPilotID());
         
         // update the database
         executeUpdate(1);
         
         // Update the applicant status
         prepareStatement("UPDATE APPLICANTS SET STATUS=?, PILOT_ID=? WHERE (ID=?)");
         _ps.setInt(1, Applicant.APPROVED);
         _ps.setInt(2, a.getPilotID());
         _ps.setInt(3, a.getID());
         
         // Clear the cache
         GetApplicant._cache.remove(new Integer(a.getID()));
         
         // Update the database and commit
         executeUpdate(1);
         commitTransaction();
      } catch(SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
   
   /**
    * Deletes an Applicant from the database. Unlike Pilots, applicants can be deleted. This call updates the 
    * <i>APPLICANTS</i> and <i>common.USERDATA</i> tables.
    * @param id the Applicant database ID
    * @throws DAOException if a JDBC error occurs
    */
   public void delete(int id) throws DAOException {
      try {
         startTransaction();
         
         // Remove the applicant profile
         prepareStatement("DELETE FROM APPLICANTS WHERE (ID=?)");
         _ps.setInt(1, id);
         executeUpdate(1);
         
         // Remove the USERDATA object
         prepareStatement("DELETE FROM common.USERDATA WHERE (ID=?)");
         _ps.setInt(1, id);
         executeUpdate(1);
         
         // Clear the cache
         GetApplicant._cache.remove(new Integer(id));
         
         // Commit the transaction
         commitTransaction();
      } catch (SQLException se) {
         rollbackTransaction();
         throw new DAOException(se);
      }
   }
}