package org.deltava.util;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hansel.CoverageDecorator;

import org.deltava.beans.FlightReport;
import org.deltava.beans.ACARSFlightReport;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.system.SystemData;

public class TestACARSHelper extends TestCase {

   public static Test suite() {
      return new CoverageDecorator(TestACARSHelper.class, new Class[] { ACARSHelper.class });
   }

   protected void setUp() throws Exception {
      super.setUp();
      
      SystemData.init();
      
      // Add airlines
      Map airlines = new HashMap();
      airlines.put("DVA", new Airline("DVA", "Delta Virtual Airlines"));
      SystemData.add("airlines", airlines);
      
      // Build airports
      Airport jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
      jfk.setLocation(40.6397, -73.7789);
      Airport atl = new Airport("ATL", "KATL", "Atlanta GA");
      atl.setLocation(33.6404, -84.4269);
      
      // Add airports
      Map airports = new HashMap();
      airports.put("ATL", atl);
      airports.put("JFK", jfk);
      SystemData.add("airports", airports);
   }

   public void testCreation() {
      ACARSFlightReport afr1 = ACARSHelper.create("DVA043");
      assertNotNull(afr1.getAirline());
      assertEquals("DVA", afr1.getAirline().getCode());
      assertEquals(43, afr1.getFlightNumber());
      assertEquals(1, afr1.getLeg());
      
      ACARSFlightReport afr2 = ACARSHelper.create("143%");
      assertNotNull(afr2.getAirline());
      assertEquals(SystemData.get("airline.code"), afr1.getAirline().getCode());
      assertEquals(143, afr2.getFlightNumber());
      assertEquals(1, afr2.getLeg());
      
      ACARSFlightReport afr3 = ACARSHelper.create("DVA");
      assertNotNull(afr3.getAirline());
      assertEquals("DVA", afr3.getAirline().getCode());
      assertEquals(1, afr3.getFlightNumber());
      assertEquals(1, afr3.getLeg());
   }
   
   public void testConversion() {
      Date cd = new Date();
      Date pd = new Date();
      
      FlightReport fr = new FlightReport(SystemData.getAirline("DVA"), 123, 2);
      fr.setAirportD(SystemData.getAirport("ATL"));
      fr.setAirportA(SystemData.getAirport("JFK"));
      fr.setCreatedOn(cd);
      fr.setDate(pd);
      fr.setEquipmentType("CV-880");
      fr.setAttribute(FlightReport.ATTR_IVAO, true);
      fr.setAttribute(FlightReport.ATTR_TIMEWARN, true);
      fr.setDatabaseID(FlightReport.DBID_PILOT, 123);
      fr.setDatabaseID(FlightReport.DBID_EVENT, 234);
      
      // Create the ACARS PIREP
      ACARSFlightReport afr = ACARSHelper.create(fr);
      assertNotSame(afr, fr);
      assertEquals(afr.getAirline(), fr.getAirline());
      assertEquals(afr.getAirportA(), fr.getAirportA());
      assertEquals(afr.getAirportD(), fr.getAirportD());
      assertEquals(afr.getCreatedOn(), fr.getCreatedOn());
      assertEquals(afr.getDate(), fr.getDate());
      assertEquals(afr.getEquipmentType(), fr.getEquipmentType());
      assertEquals(afr.getAttributes(), fr.getAttributes());
      assertEquals(afr.getDatabaseID(FlightReport.DBID_PILOT), fr.getDatabaseID(FlightReport.DBID_PILOT));
      assertEquals(afr.getDatabaseID(FlightReport.DBID_EVENT), fr.getDatabaseID(FlightReport.DBID_EVENT));
      
      // Try and create a new ACARS PIREP
      ACARSFlightReport afr2 = ACARSHelper.create(afr);
      assertSame(afr, afr2);
   }
   
   public void testBuild() throws Exception {
      
      // Create the builder and load the file into an XML in-memory document
      InputStream xmlStream = ConfigLoader.getStream("/data/acars_pirep.xml");
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(xmlStream);
      xmlStream.close();
      
      // Build the properties
      Properties props = ACARSHelper.parse(doc);

      // Build the PIREP
      ACARSFlightReport afr = ACARSHelper.create(props.getProperty("flight_num", "1"));
      assertNotNull(afr);
      ACARSHelper.build(afr, props);
      
      assertNotNull(afr.getAirline());
      assertEquals("DVA", afr.getAirline().getCode());
      assertEquals(950, afr.getFlightNumber());
   }
   
   public void testInvalidXML() throws Exception{
      
      InputStream xmlStream = ConfigLoader.getStream("/data/acars_invalid_pirep.xml");

      // Create the builder and load the file into an XML in-memory document
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(xmlStream);
      xmlStream.close();
      
      try {
         ACARSHelper.parse(doc);
         fail("JDOMException expected");
      } catch (JDOMException je) { }
   }
}