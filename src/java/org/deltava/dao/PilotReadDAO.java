package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.UserData;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A DAO to support reading Pilot object(s) from the database. This class contains methods to read an individual Pilot
 * from the database; implementing subclasses typically add methods to retrieve Lists of pilots based on particular
 * crtieria.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

abstract class PilotReadDAO extends PilotDAO {

   private static final Logger log = Logger.getLogger(PilotReadDAO.class);

   /**
    * Creates the DAO from a JDBC connection.
    * @param c the JDBC connection to use
    */
   protected PilotReadDAO(Connection c) {
      super(c);
   }

   /**
    * Gets a pilot object based on a database ID. <i>This uses a cached query, and populates ratings and roles. </i>
    * @param id the database ID of the Pilot object
    * @return the Pilot object, or null if the ID was not found
    * @throws DAOException if a JDBC error occured
    */
   public final Pilot get(int id) throws DAOException {

      // Check if we're in the cache
      Pilot p = (Pilot) _cache.get(new Integer(id));
      if (p != null)
         return p;

      try {
         prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
               + "MAX(F.DATE), S.ID FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN "
               + "SIGNATURES S ON (P.ID=S.ID) WHERE (P.ID=?) GROUP BY P.ID");
         _ps.setInt(1, FlightReport.OK);
         _ps.setInt(2, id);

         // Execute the query and get the result
         List results = execute();
         p = (results.size() == 0) ? null : (Pilot) results.get(0);
         if (p == null)
            return null;
         
         // Add roles/ratings
         addRatings(p, SystemData.get("airline.db"));
         addRoles(p, SystemData.get("airline.db"));
      } catch (SQLException se) {
         throw new DAOException(se);
      }
      
      // Add to the cache and return
      _cache.add(p);
      return p;
   }

   /**
    * Returns a Pilot based on a given full name. This method does not use first/last name splitting since this can be
    * unpredictable.
    * @param fullName the Full Name of the Pilot
    * @param dbName the database name to search
    * @return the Pilot, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public final Pilot getByName(String fullName, String dbName) throws DAOException {
      
      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), "
            + "ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.ID FROM ");
      sqlBuf.append(dbName.toLowerCase());
      sqlBuf.append(".PILOTS P LEFT JOIN ");
      sqlBuf.append(dbName.toLowerCase());
      sqlBuf.append(".PIREPS F ON (P.ID=F.PILOT_ID) LEFT JOIN ");
      sqlBuf.append(dbName.toLowerCase());
      sqlBuf.append(".SIGNATURES S ON (P.ID=S.ID) WHERE (UPPER(CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME))=?) "
            + "AND (F.STATUS=?) GROUP BY P.ID");
      
      try {
         prepareStatement(sqlBuf.toString());
         _ps.setString(1, fullName.toUpperCase());
         _ps.setInt(2, FlightReport.OK);

         // Execute the query and get the result
         List results = execute();
         Pilot result = (results.size() == 0) ? null : (Pilot) results.get(0);
         if (result == null)
            return null;

         // Add roles/ratings
         addRatings(result, dbName);
         addRoles(result, dbName);

         // Add the result to the cache and return
         _cache.add(result);
         return result;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns a Pilot object which may be in another Airline's database.
    * @param ud the UserData bean containg the Pilot location
    * @return the Pilot bean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public Pilot get(UserData ud) throws DAOException {

      // Check for null
      if (ud == null)
         return null;

      // Convert the ID into a set
      Set idSet = new HashSet();
      idSet.add(new Integer(ud.getID()));

      // Get a map from the table, and get the first value
      Map pilots = getByID(idSet, ud.getDB() + "." + ud.getTable());
      Iterator i = pilots.values().iterator();
      return i.hasNext() ? (Pilot) i.next() : null;
   }

   /**
    * Returns a Map of pilots based on a Set of pilot IDs. This is typically called by a Water Cooler thread/channel
    * list command.
    * @param ids a Collection of pilot IDs. This can either be a Collection of Integers, a Collection of
    *             {@link DatabaseBean}beans
    * @param tableName the table to read from, in <i>DATABASE.TABLE </i> format for a remote database, or <i>TABLE </i>
    *             for a table in the current airline's database.
    * @return a Map of Pilots, indexed by the pilot code
    * @throws DAOException if a JDBC error occurs
    */
   public Map getByID(Collection ids, String tableName) throws DAOException {

      // Get the datbaase - if we haven't specified one, use the current database
      String dbName = (tableName.indexOf('.') == -1) ? SystemData.get("airline.db") : tableName.substring(0, tableName
            .indexOf('.'));
      dbName = dbName.toLowerCase();

      List results = new ArrayList();
      log.debug("Raw set size = " + ids.size());

      // Init the prepared statement
      StringBuffer sqlBuf = new StringBuffer("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), "
            + "ROUND(SUM(F.FLIGHT_TIME), 1), MAX(F.DATE), S.ID FROM ");
      sqlBuf.append(tableName);
      sqlBuf.append(" P LEFT JOIN ");
      sqlBuf.append(dbName);
      sqlBuf.append(".PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN ");
      sqlBuf.append(dbName);
      sqlBuf.append(".SIGNATURES S ON (P.ID=S.ID) WHERE (P.ID IN (");

      // Add the pilot IDs to the set
      int querySize = 0;
      for (Iterator i = ids.iterator(); i.hasNext();) {
         Object rawID = i.next();
         assert ((rawID instanceof Integer) || (rawID instanceof DatabaseBean)) : String.valueOf(rawID) + " "
               + rawID.getClass().getName();
         Integer id = (rawID instanceof Integer) ? (Integer) rawID : new Integer(((DatabaseBean) rawID).getID());

         // Pull from the cache if at all possible; this is an evil query
         Pilot p = (Pilot) _cache.get(id);
         if (p != null) {
            results.add(p);
         } else {
            querySize++;
            sqlBuf.append(id.toString());
            if (i.hasNext())
               sqlBuf.append(',');
         }
      }

      // Only execute the prepared statement if we haven't gotten anything from the cache
      log.debug("Uncached set size = " + querySize);
      if (querySize > 0) {
         if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
            sqlBuf.setLength(sqlBuf.length() - 1);

         sqlBuf.append(")) GROUP BY P.ID");
         List uncached = null;
         try {
            prepareStatementWithoutLimits(sqlBuf.toString());
            _ps.setInt(1, FlightReport.OK);
            uncached = execute();

            // Convert to a map and load ratings/roles
            Map ucMap = CollectionUtils.createMap(uncached, "ID");
            loadRatings(ucMap, dbName);
            loadRoles(ucMap, dbName);
         } catch (SQLException se) {
            log.error("Query = " + sqlBuf.toString());
            throw new DAOException(se);
         }

         // If the database does not equal the current airline code, refresh all of the Pilot IDs with
         // the pilot number, but use the database name as the airline code.
         if (!dbName.equals(SystemData.get("airline.db"))) {
            Map apps = (Map) SystemData.getObject("apps");
            for (Iterator i = apps.values().iterator(); i.hasNext();) {
               AirlineInformation info = (AirlineInformation) i.next();
               if (dbName.equals(info.getDB())) {
                  for (Iterator uci = uncached.iterator(); uci.hasNext();) {
                     Pilot p = (Pilot) uci.next();
                     p.setPilotCode(info.getCode() + String.valueOf(p.getPilotNumber()));
                  }

                  break;
               }
            }
         }

         // Add to results and the cache
         results.addAll(uncached);
         _cache.addAll(uncached);
      }

      // Convert to a Map for easy searching
      return CollectionUtils.createMap(results, "ID");
   }

   /**
    * Query pilot objects from the database, assuming a pre-prepared statement.
    * @return a List of Pilot objects
    * @throws SQLException if a JDBC error occurs
    */
   protected final List execute() throws SQLException {
      List results = new ArrayList();

      // Get the pilot info from the list
      ResultSet rs = _ps.executeQuery();
      int columnCount = rs.getMetaData().getColumnCount();
      String airlineCode = SystemData.get("airline.code");

      while (rs.next()) {
         Pilot p = new Pilot(rs.getString(3), rs.getString(4));
         p.setID(rs.getInt(1));
         p.setPilotCode(airlineCode + String.valueOf(rs.getInt(2)));
         p.setStatus(rs.getInt(5));
         p.setDN(rs.getString(6));
         p.setEmail(rs.getString(7));
         p.setLocation(rs.getString(8));
         p.setIMHandle(rs.getString(9));
         p.setLegacyHours(rs.getDouble(10));
         p.setHomeAirport(rs.getString(11));
         p.setEquipmentType(rs.getString(12));
         p.setRank(rs.getString(13));
         p.setNetworkID("VATSIM", rs.getString(14));
         p.setNetworkID("IVAO", rs.getString(15));
         p.setCreatedOn(rs.getTimestamp(16));
         p.setLoginCount(rs.getInt(17));
         p.setLastLogin(rs.getTimestamp(18));
         p.setLastLogoff(rs.getTimestamp(19));
         p.setTZ(TZInfo.get(rs.getString(20)));
         p.setNotifyOption(Person.FLEET, rs.getBoolean(21));
         p.setNotifyOption(Person.EVENT, rs.getBoolean(22));
         p.setNotifyOption(Person.NEWS, rs.getBoolean(23));
         p.setEmailAccess(rs.getInt(24));
         p.setShowSignatures(rs.getBoolean(25));
         p.setShowSSThreads(rs.getBoolean(26));
         // FIXME Uncomment this and renumber
         p.setHasDefaultSignature(rs.getBoolean(27));
         p.setUIScheme(rs.getString(28));
         p.setLoginHost(rs.getString(29));
         p.setDateFormat(rs.getString(30));
         p.setTimeFormat(rs.getString(31));
         p.setNumberFormat(rs.getString(32));
         p.setAirportCodeType(rs.getInt(33));
         p.setMapType(rs.getInt(34));

         // Check if this result set has a column 34-37, which is the PIREP totals
         if (columnCount > 37) {
            p.setLegs(rs.getInt(35));
            p.setMiles(rs.getLong(36));
            p.setHours(rs.getDouble(37));
            p.setLastFlight(expandDate(rs.getDate(38)));
         }

         // Check if this result set has a column 38, which is the signature ID
         if (columnCount > 38)
            p.setHasSignature((rs.getInt(39) != 0));

         // CHeck if this result set has a column 39/40, which are online legs/hours
         if (columnCount > 40) {
            p.setOnlineLegs(rs.getInt(40));
            p.setOnlineHours(rs.getDouble(41));
         }

         // Add the pilot
         results.add(p);
      }

      // Close everything down
      rs.close();
      _ps.close();
      return results;
   }

   /**
    * Load the ratings for a Pilot.
    * @param p the Pilot bean
    * @param dbName the database Name
    * @throws SQLException if a JDBC error occurs
    */
   protected final void addRatings(Pilot p, String dbName) throws SQLException {
      Map tmpMap = new HashMap();
      tmpMap.put(new Integer(p.getID()), p);
      loadRatings(tmpMap, dbName);
   }

   /**
    * Load the security roles for a Pilot.
    * @param p the Pilot bean
    * @param dbName the database Name
    * @throws SQLException if a JDBC error occurs
    */
   protected final void addRoles(Pilot p, String dbName) throws SQLException {
      Map tmpMap = new HashMap();
      tmpMap.put(new Integer(p.getID()), p);
      loadRoles(tmpMap, dbName);
   }

   /**
    * Load the security roles for a group of Pilots.
    * @param pilots the Map of Pilots, indexed by database ID
    * @param dbName the database Name
    * @throws SQLException if a JDBC error occurs
    */
   protected final void loadRoles(Map pilots, String dbName) throws SQLException {

      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT ID, ROLE FROM ");
      sqlBuf.append(dbName.toLowerCase());
      sqlBuf.append(".ROLES WHERE (ID IN (");
      for (Iterator i = pilots.keySet().iterator(); i.hasNext();) {
         Integer id = (Integer) i.next();
         sqlBuf.append(id.toString());
         if (i.hasNext())
            sqlBuf.append(',');
      }

      sqlBuf.append("))");

      // Prepare the statement
      prepareStatementWithoutLimits(sqlBuf.toString());

      // Exeute the query
      ResultSet rs = _ps.executeQuery();
      while (rs.next()) {
         Pilot p = (Pilot) pilots.get(new Integer(rs.getInt(1)));
         if (p != null)
            p.addRole(rs.getString(2));
      }

      // Clean up and return
      rs.close();
      _ps.close();
   }

   /**
    * Load the equipment ratings for a group of Pilots.
    * @param pilots the Map of Pilots, indexed by database ID
    * @param dbName the database Name
    * @throws SQLException if a JDBC error occurs
    */
   protected final void loadRatings(Map pilots, String dbName) throws SQLException {

      // Build the SQL statement
      StringBuffer sqlBuf = new StringBuffer("SELECT ID, RATING FROM ");
      sqlBuf.append(dbName.toLowerCase());
      sqlBuf.append(".RATINGS WHERE (ID IN (");
      for (Iterator i = pilots.keySet().iterator(); i.hasNext();) {
         Integer id = (Integer) i.next();
         sqlBuf.append(id.toString());
         if (i.hasNext())
            sqlBuf.append(',');
      }

      sqlBuf.append("))");

      // Prepare the statement
      prepareStatementWithoutLimits(sqlBuf.toString());

      // Exeute the query
      ResultSet rs = _ps.executeQuery();
      while (rs.next()) {
         Pilot p = (Pilot) pilots.get(new Integer(rs.getInt(1)));
         if (p != null)
            p.addRating(rs.getString(2));
      }

      // Clean up and return
      rs.close();
      _ps.close();
   }
}