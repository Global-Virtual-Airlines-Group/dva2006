// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.text.*;
import java.util.*;

import org.jdom.*;

import org.deltava.beans.ACARSFlightReport;
import org.deltava.beans.FlightReport;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.system.SystemData;

/**
 * A utility class to convert XML request data into an ACARS Flight Report.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ACARSHelper {

   private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");
   private static final DateFormat _dtf = new SimpleDateFormat("MM/dd/yyyy hh:mm");

   // singleton constructor
   private ACARSHelper() {
   }

   /**
    * Generates an ACARS Flight Report from an existing Flight Report.
    * @param fr the Flight Report
    * @return the ACARS Flight Report
    * @see ACARSHelper#create(String)
    */
   public static ACARSFlightReport create(FlightReport fr) {
      if (fr instanceof ACARSFlightReport) return (ACARSFlightReport) fr;

      // Create the new bean
      ACARSFlightReport afr = new ACARSFlightReport(fr.getAirline(), fr.getFlightNumber(), fr.getLeg());
      afr.setEquipmentType(fr.getEquipmentType());
      afr.setDate(fr.getDate());
      afr.setCreatedOn(fr.getCreatedOn());
      afr.setAirportD(fr.getAirportD());
      afr.setAirportA(fr.getAirportA());
      afr.setAttributes(fr.getAttributes());
      afr.setDatabaseID(FlightReport.DBID_PILOT, fr.getDatabaseID(FlightReport.DBID_PILOT));
      afr.setDatabaseID(FlightReport.DBID_ASSIGN, fr.getDatabaseID(FlightReport.DBID_ASSIGN));
      afr.setDatabaseID(FlightReport.DBID_EVENT, fr.getDatabaseID(FlightReport.DBID_EVENT));

      // Return the bean
      return afr;
   }

   /**
    * Creates a new ACARS Flight Report from a Flight code.
    * @param flightCode the flight Code
    * @return the ACARS Flight Report
    * @see ACARSHelper#create(FlightReport)
    */
   public static ACARSFlightReport create(String flightCode) {

      StringBuffer aCode = new StringBuffer();
      StringBuffer fCode = new StringBuffer();
      for (int x = 0; x < flightCode.length(); x++) {
         char c = flightCode.charAt(x);
         if (Character.isDigit(c)) {
            fCode.append(c);
         } else if (Character.isLetter(c)) {
            aCode.append(c);
         }
      }

      // Check the flight code
      if (fCode.length() == 0) fCode.append('1');

      // Get the airline
      Airline a = SystemData.getAirline(aCode.toString());
      if (a == null)
         a = SystemData.getAirline(SystemData.get("airline.code"));

      return new ACARSFlightReport(a, Integer.parseInt(fCode.toString()), 1);
   }
   
   /**
    * Helper method to return a new date/time, given a starting date/time and an offset in seconds. 
    */
   private static Date getDate(Date startTime, int offset) {
      return new Date(startTime.getTime() + (offset * 1000));
   }
   
   /**
    * Helper method to convert mm:ss values into seconds
    */
   private static int processTime(String timeStr) {
      StringTokenizer tkns = new StringTokenizer(timeStr, ":");
      if (tkns.countTokens() == 2) {
         return (Integer.parseInt(tkns.nextToken()) * 60) + Integer.parseInt(tkns.nextToken());
      } else if (tkns.countTokens() == 3) {
         return (Integer.parseInt(tkns.nextToken()) * 3600) + (Integer.parseInt(tkns.nextToken()) * 60) +
               Integer.parseInt(tkns.nextToken());
      }
      
      return 0;
   }

   public static void build(ACARSFlightReport afr, Properties p) {

      // Build the PIREP
      afr.setAttribute(FlightReport.ATTR_ACARS, true);
      afr.setDatabaseID(FlightReport.DBID_ACARS, Integer.parseInt(p.getProperty("flight_id")));
      afr.setStatus(FlightReport.SUBMITTED);
      afr.setEquipmentType(p.getProperty("equipment"));
      afr.setDate(new Date());
      afr.setAirportD(SystemData.getAirport(p.getProperty("dep_apt")));
      afr.setAirportA(SystemData.getAirport(p.getProperty("arr_apt")));

      // Get the online network
      String network = p.getProperty("network");
      if ("VATSIM".equals(network)) {
         afr.setAttribute(FlightReport.ATTR_VATSIM, true);
      } else if ("IVAO".equals(network)) {
         afr.setAttribute(FlightReport.ATTR_IVAO, true);
      } else if ("FPI".equals(network)) {
         afr.setAttribute(FlightReport.ATTR_FPI, true);
      }

      // Get the Flight Simulator version
      switch (Integer.parseInt(p.getProperty("fs_ver"))) {
         case 2:
            afr.setFSVersion(2000);
            break;

         case 6:
            afr.setFSVersion(2002);
            break;

            default:
         case 7:
            afr.setFSVersion(2004);
            break;
      }

      // Get the remarks and the route
      StringBuffer buf = new StringBuffer("Route: ");
      buf.append(p.getProperty("route"));
      buf.append("\nRemarks: ");
      buf.append(p.getProperty("remarks"));
      afr.setRemarks(buf.toString());

      // Create the start time
      String startTime = _df.format(afr.getDate()) + " " + p.getProperty("start_time");
      try {
         afr.setStartTime(_dtf.parse(startTime));
      } catch (ParseException pe) {
         throw new IllegalArgumentException("Invalid Start Time - " + startTime);
      }
      
      // Set the times
      try {
         afr.setEngineStartTime(getDate(afr.getStartTime(), Integer.parseInt(p.getProperty("time_out", "0"))));
      	afr.setTaxiTime(getDate(afr.getStartTime(), Integer.parseInt(p.getProperty("taxi_out_time", "0"))));
      	afr.setTakeoffTime(getDate(afr.getStartTime(), Integer.parseInt(p.getProperty("takeoff_time", "0"))));
      	afr.setLandingTime(getDate(afr.getStartTime(), Integer.parseInt(p.getProperty("time_on", "0"))));
      	afr.setEndTime(getDate(afr.getStartTime(), Integer.parseInt(p.getProperty("shutdown_time", "0"))));
      } catch (NumberFormatException nfe) {
         throw new IllegalArgumentException("Invalid Time - " + nfe.getMessage());
      }
      
      // Set the weights/speeds
      try {
         afr.setTaxiFuel(Integer.parseInt(p.getProperty("fuel_at_taxi")));
         afr.setTaxiWeight(Integer.parseInt(p.getProperty("weight_at_taxi")));
         afr.setTakeoffFuel(Integer.parseInt(p.getProperty("fuel_at_takeoff")));
         afr.setTakeoffWeight(Integer.parseInt(p.getProperty("weight_at_takeoff")));
         afr.setTakeoffN1(Double.parseDouble(p.getProperty("n1_at_takeoff")));
         afr.setTakeoffSpeed(Integer.parseInt(p.getProperty("takeoff_speed")));
         afr.setLandingFuel(Integer.parseInt(p.getProperty("fuel_at_landing")));
         afr.setLandingWeight(Integer.parseInt(p.getProperty("weight_at_landing")));
         afr.setLandingN1(Double.parseDouble(p.getProperty("n1_at_landing")));
         afr.setLandingSpeed(Integer.parseInt(p.getProperty("landing_speed")));
         afr.setLandingVSpeed(Integer.parseInt(p.getProperty("landing_vspeed")) * -1);
         afr.setGateFuel(Integer.parseInt(p.getProperty("fuel_at_gate")));
         afr.setGateWeight(Integer.parseInt(p.getProperty("weight_at_gate")));
      } catch (NumberFormatException nfe) {
         throw new IllegalArgumentException("Invalid Weight/Speed - " + nfe.getMessage());
      }
      
      // Set time at 1X/2X/4X
      try {
         afr.setTime1X(processTime(p.getProperty("time_at_1x", "0:00")));
         afr.setTime2X(processTime(p.getProperty("time_at_2x", "0:00")));
         afr.setTime4X(processTime(p.getProperty("time_at_4x", "0:00")));
      } catch (NumberFormatException nfe) {
         throw new IllegalArgumentException("Invalid Time - " + nfe.getMessage());
      }
      
      // Set the distances from the airport on takeoff/landing
      try {
         GeoPosition dgp = new GeoPosition(Double.parseDouble(p.getProperty("takeoff_lat")), 
               Double.parseDouble(p.getProperty("takeoff_lon")));
         GeoPosition agp = new GeoPosition(Double.parseDouble(p.getProperty("landing_lat")), 
               Double.parseDouble(p.getProperty("landing_lon")));
         
         afr.setTakeoffDistance(dgp.distanceTo(afr.getAirportD().getPosition()));
         afr.setLandingDistance(agp.distanceTo(afr.getAirportA().getPosition()));
      } catch (NumberFormatException nfe) {
         throw new IllegalArgumentException("Invalid Latitude/Longitude - " + nfe.getMessage());
      }
      
      // Set the submission date/time
      afr.setSubmittedOn(new Date());
   }
   
   public static Properties parse(Document xmldoc) throws JDOMException {
      
      // Get the command type
      Element cmdE = xmldoc.getRootElement().getChild("CMD");
      if (!"pirep".equals(cmdE.getAttributeValue("type")))
         throw new JDOMException("Invalid CMD type - " + cmdE.getAttributeValue("type"));
      
      // Parse through the elements
      Properties props = new Properties();
      for (Iterator i = cmdE.getChildren().iterator(); i.hasNext(); ) {
         Element e = (Element) i.next();
         props.setProperty(e.getName(), e.getTextTrim());
      }
      
      // Return the property set
      return props;
   }
}