package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Airline;

public class TestPIREPAccessControl extends AccessControlTestCase {

   private PIREPAccessControl _ac;
   private FlightReport _fr; 
   
   public static Test suite() {
      return new CoverageDecorator(TestPIREPAccessControl.class, new Class[] { AccessControl.class,
            PIREPAccessControl.class });
   }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _fr = new FlightReport(new Airline("DVA", "Delta Virtual"), 123, 1);
      _fr.setLength(10);
      _ac = new PIREPAccessControl(_ctxt, _fr);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      _fr = null;
      super.tearDown();
   }

   public void testPIREPAccess() throws Exception {
      assertEquals(FlightStatus.DRAFT, _fr.getStatus());
      _user.addRole("PIREP");
      _ac.validate();
      
      assertTrue(_ac.getCanSubmit());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
      
      _fr.setStatus(FlightStatus.SUBMITTED);
      _ac.validate();

      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertTrue(_ac.getCanHold());
      assertTrue(_ac.getCanApprove());
      assertTrue(_ac.getCanReject());
      assertTrue(_ac.getCanDispose());
      
      _fr.setStatus(FlightStatus.HOLD);
      _ac.validate();
      
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertTrue(_ac.getCanApprove());
      assertTrue(_ac.getCanReject());
      assertTrue(_ac.getCanDispose());

      _fr.setStatus(FlightStatus.OK);
      _ac.validate();

      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
      
      _fr.setStatus(FlightStatus.REJECTED);
      _ac.validate();

      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
   }
   
   public void testHRAccess() throws Exception {
      assertEquals(FlightStatus.DRAFT, _fr.getStatus());
      _user.addRole("HR");
      _ac.validate();

      assertTrue(_ac.getCanSubmit());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
      
      _fr.setStatus(FlightStatus.SUBMITTED);
      _ac.validate();

      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertTrue(_ac.getCanHold());
      assertTrue(_ac.getCanApprove());
      assertTrue(_ac.getCanReject());
      assertTrue(_ac.getCanDispose());
      
      _fr.setStatus(FlightStatus.HOLD);
      _ac.validate();
      
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertTrue(_ac.getCanApprove());
      assertTrue(_ac.getCanReject());
      assertTrue(_ac.getCanDispose());

      _fr.setStatus(FlightStatus.OK);
      _ac.validate();

      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertTrue(_ac.getCanReject());
      assertTrue(_ac.getCanDispose());

      _fr.setStatus(FlightStatus.REJECTED);
      _ac.validate();

      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertTrue(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertTrue(_ac.getCanDispose());
   }
   
   public void testOurAccess() throws Exception {
      _fr.setDatabaseID(DatabaseID.PILOT, _user.getID());
      assertEquals(FlightStatus.DRAFT, _fr.getStatus());
      _user.addRole("Pilot");
      _ac.validate();
      
      assertTrue(_ac.getOurFlight());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanCreate());
      assertTrue(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
      
      _fr.setStatus(FlightStatus.SUBMITTED);
      _ac.validate();
      
      assertTrue(_ac.getOurFlight());
      assertTrue(_ac.getCanCreate());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
   }
   
   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      _ac.validate();
      
      assertFalse(_ac.getOurFlight());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
   }
   
   public void testNullFlightReport() throws Exception {
	   _ctxt.getRoles().remove("Pilot");
	   assertFalse(_ctxt.isUserInRole("Pilot"));
      _ac = new PIREPAccessControl(_ctxt, null);
      _ac.validate();
      
      assertFalse(_ac.getOurFlight());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());

      _user.addRole("Pilot");
      _ac.validate();
      
      assertFalse(_ac.getOurFlight());
      assertTrue(_ac.getCanCreate());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanHold());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      assertFalse(_ac.getCanDispose());
   }
   
   public void testContextValidation() {
      doContextValidation(new PIREPAccessControl(null, _fr));
   }
}