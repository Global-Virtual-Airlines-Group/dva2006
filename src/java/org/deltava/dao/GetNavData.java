// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to read Navigation data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetNavData extends DAO {

   /**
    * Initializes the Data Access Object.
    * @param c the JDBC connection to use
    */
   public GetNavData(Connection c) {
      super(c);
   }

   /**
    * Returns a Navigation object.
    * @param code the object code
    * @return a NavigationDataBean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public NavigationDataBean get(String code) throws DAOException {

      try {
         prepareStatement("SELECT * FROM common.NAVDATA WHERE (UPPER(CODE)=?)");
         _ps.setString(1, code.toUpperCase());
         setQueryMax(1);

         // Execute the query
         List results = execute();
         return results.isEmpty() ? null : (NavigationDataBean) results.get(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns information about a particular airport Runway.
    * @param airportCode the airport ICAO code
    * @param rwyCode the runway name/number
    * @return a Runway bean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public Runway getRunway(String airportCode, String rwyCode) throws DAOException {
      try {
         prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?) AND (UPPER(CODE)=?) "
               + "AND (UPPER(NAME)=?)");
         _ps.setInt(1, NavigationDataBean.RUNWAY);
         _ps.setString(2, airportCode.toUpperCase());
         _ps.setString(3, rwyCode.toUpperCase());

         // Execute the query
         List results = execute();
         return results.isEmpty() ? null : (Runway) results.get(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }

   /**
    * Returns a group of Navigation objects.
    * @param ids a Collection of navigation object codes
    * @return a NavigationDataMap bean
    * @throws DAOException if a JDBC error occurs
    */
   public NavigationDataMap getByID(Collection ids) throws DAOException {

      // Check for empty id set
      NavigationDataMap results = new NavigationDataMap();;
      if (ids.isEmpty())
         return results;

      // Build the SQL Statement
      StringBuffer sqlBuf = new StringBuffer("SELECT * FROM common.NAVDATA WHERE CODE IN (");
      for (Iterator i = ids.iterator(); i.hasNext();) {
         String code = ((String) i.next()).toUpperCase();
         sqlBuf.append('\'');
         sqlBuf.append(code);
         sqlBuf.append("\',");
      }

      // Only execute the prepared statement if we haven't gotten anything from the cache
      if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
         sqlBuf.setLength(sqlBuf.length() - 1);

      sqlBuf.append(')');
      try {
         prepareStatementWithoutLimits(sqlBuf.toString());
         results.addAll(execute());
         
      } catch (SQLException se) {
         throw new DAOException(se);
      }

      return results;
   }

   /**
    * Returns all Intersections within a set number of miles from a point.
    * @param loc the central location
    * @param distance the distance in miles
    * @return a Map of Intersections, with the code as the key
    * @throws DAOException if a JDBC error occurs
    * @throws IllegalArgumentException if distance is negative or > 300
    */
   public Map getIntersections(GeoLocation loc, int distance) throws DAOException {
      if ((distance < 0) || (distance > 300))
         throw new IllegalArgumentException("Invalid distance -  " + distance);

      // Calculate the height/width of the square in degrees (use 70% of the value of a lon degree)
      double height = (distance / GeoLocation.DEGREE_MILES) / 2;
      double width = (height * 0.7);

      Collection results = null;
      try {
         prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE=?) AND ((LATITUDE > ?) AND (LATITUDE < ?)) "
               + "AND ((LONGITUDE > ?) AND (LONGITUDE < ?))");
         _ps.setInt(1, NavigationDataBean.INT);
         _ps.setDouble(2, loc.getLatitude() - height);
         _ps.setDouble(3, loc.getLatitude() + height);
         _ps.setDouble(4, loc.getLongitude() - width);
         _ps.setDouble(5, loc.getLongitude() + width);
         results = execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }

      // Ensure that we are within the correct distance and convert to a Map for easy lookup
      distanceFilter(results, loc, distance);
      return CollectionUtils.createMap(results, "code");
   }

   /**
    * Returns all Navigation objects (except Intersections/Runways) within a set number of miles from a point.
    * @param loc the central location
    * @param distance the distance in miles
    * @return a Map of NavigationDataBeans, with the code as the key
    * @throws DAOException if a JDBC error occurs
    * @throws IllegalArgumentException if distance is negative or > 300
    */
   public Map getObjects(GeoLocation loc, int distance) throws DAOException {
      if ((distance < 0) || (distance > 300))
         throw new IllegalArgumentException("Invalid distance -  " + distance);

      // Calculate the height/width of the square in degrees (use 70% of the value of a lon degree)
      double height = (distance / GeoLocation.DEGREE_MILES) / 2;
      double width = (height * 0.7);

      Collection results = null;
      try {
         prepareStatement("SELECT * FROM common.NAVDATA WHERE (ITEMTYPE <> ?) AND (ITEMTYPE <> ?) AND "
               + "((LATITUDE > ?) AND (LATITUDE < ?)) AND ((LONGITUDE > ?) AND (LONGITUDE < ?))");
         _ps.setInt(1, NavigationDataBean.INT);
         _ps.setInt(2, NavigationDataBean.RUNWAY);
         _ps.setDouble(3, loc.getLatitude() - height);
         _ps.setDouble(4, loc.getLatitude() + height);
         _ps.setDouble(5, loc.getLongitude() - width);
         _ps.setDouble(6, loc.getLongitude() + width);
         results = execute();
      } catch (SQLException se) {
         throw new DAOException(se);
      }

      // Ensure that we are within the correct distance and convert to a Map for easy lookup
      distanceFilter(results, loc, distance);
      return CollectionUtils.createMap(results, "code");
   }
   
   /**
    * Loads a SID/STAR from the navigation database.
    * @param name the name of the Terminal Route, as NAME.TRANSITION
    * @return a TerminalRoute bean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public TerminalRoute getRoute(String name) throws DAOException {
      
      // Split the name
      StringTokenizer tkns = new StringTokenizer(name, ".");
      if (tkns.countTokens() != 2)
         return null;
      
      try {
         prepareStatement("SELECT * FROM common.SID_STAR WHERE (NAME=?) AND (TRANSITION=?)");
         _ps.setString(1, tkns.nextToken().toUpperCase());
         _ps.setString(2, tkns.nextToken().toUpperCase());
         setQueryMax(1);
         
         // Execute the query
         List results = executeSIDSTAR();
         return results.isEmpty() ? null : (TerminalRoute) results.get(0);
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Loads all SIDs/STARs for a particular Airport.
    * @param code the Airport ICAO code
    * @param type a bit mask to filter SID/STR routes
    * @return a List of TerminalRoutes
    * @throws DAOException
    * @see TerminalRoute#SID
    * @see TerminalRoute#STAR
    */
   public List getRoutes(String code, int type) throws DAOException {
      try {
         prepareStatement("SELECT * FROM common.SID_STAR WHERE (ICAO=?) AND ((TYPE & ?) != 0) ORDER BY "
               + "NAME, TRANSITION");
         _ps.setString(1, code.toUpperCase());
         _ps.setInt(2, type);
         return executeSIDSTAR();
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Loads an Airway definition from the database.
    * @param name the airway code
    * @return an Airway bean, or null if not found
    * @throws DAOException if a JDBC error occurs
    */
   public Airway getAirway(String name) throws DAOException {
      try {
         prepareStatement("SELECT * FROM common.AIRWAYS WHERE (NAME=?)");
         _ps.setString(1, name.toUpperCase());
         setQueryMax(1);
         
         // Execute the query
         Airway result = null;
         ResultSet rs = _ps.executeQuery();
         
         // Populate the airway bean
         if (rs.next())
            result = new Airway(rs.getString(1), rs.getString(2));
         
         // Clean up and return
         rs.close();
         _ps.close();
         return result;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
   
   /**
    * Helper method to iterate through a SID_STAR result set.
    */
   private List executeSIDSTAR() throws SQLException {

      // Execute the Query
      ResultSet rs = _ps.executeQuery();

      // Iterate through the results
      List results = new ArrayList();
      while (rs.next()) {
         TerminalRoute tr = new TerminalRoute(rs.getString(1), rs.getString(3), rs.getInt(2));
         tr.setTransition(rs.getString(4));
         tr.setRunway(rs.getString(5));
         tr.setRoute(rs.getString(6));
         
         // Add to results
         results.add(tr);
      }
      
      // Clean up and return
      rs.close();
      _ps.close();
      return results;
   }

   /**
    * Helper method to iterate through a NAVDATA result set.
    */
   private List execute() throws SQLException {

      // Execute the Query
      ResultSet rs = _ps.executeQuery();

      // Iterate through the results
      List results = new ArrayList();
      while (rs.next()) {
         NavigationDataBean obj = null;
         switch (rs.getInt(1)) {
            case NavigationDataBean.AIRPORT:
               AirportLocation a = new AirportLocation(rs.getDouble(3), rs.getDouble(4));
               a.setCode(rs.getString(2));
               a.setAltitude(rs.getInt(6));
               a.setName(rs.getString(7));
               obj = a;
               break;

            case NavigationDataBean.INT:
               Intersection i = new Intersection(rs.getDouble(3), rs.getDouble(4));
               i.setCode(rs.getString(2));
               obj = i;
               break;

            case NavigationDataBean.VOR:
               VOR vor = new VOR(rs.getDouble(3), rs.getDouble(4));
               vor.setCode(rs.getString(2));
               vor.setFrequency(rs.getString(5));
               vor.setName(rs.getString(7));
               obj = vor;
               break;

            case NavigationDataBean.NDB:
               NDB ndb = new NDB(rs.getDouble(3), rs.getDouble(4));
               ndb.setCode(rs.getString(2));
               ndb.setFrequency(rs.getString(5));
               ndb.setName(rs.getString(7));
               obj = ndb;
               break;

            case NavigationDataBean.RUNWAY:
               Runway rwy = new Runway(rs.getDouble(3), rs.getDouble(4));
               rwy.setCode(rs.getString(2));
               rwy.setFrequency(rs.getString(5));
               rwy.setLength(rs.getInt(6));
               rwy.setName(rs.getString(7));
               rwy.setHeading(rs.getInt(8));
               obj = rwy;
               break;

            default:
         }

         // Add to results
         results.add(obj);
      }

      // Clean up and return
      rs.close();
      _ps.close();
      return results;
   }

   /**
    * Helper method to filter objects based on distance from a certain point
    */
   private void distanceFilter(Collection entries, GeoLocation loc, int distance) {
      for (Iterator i = entries.iterator(); i.hasNext();) {
         NavigationDataBean ndb = (NavigationDataBean) i.next();
         if (ndb.getPosition().distanceTo(loc) > distance)
            i.remove();
      }
   }
}