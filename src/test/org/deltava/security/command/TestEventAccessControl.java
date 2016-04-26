// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import java.time.Instant;
import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.event.Event;

public class TestEventAccessControl extends AccessControlTestCase {

   private EventAccessControl _ac;
   private Event _ev;
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _ev = new Event("Event Name");
      _ac = new EventAccessControl(_ctxt, _ev);
      
      long now = System.currentTimeMillis();
      _ev.setStartTime(Instant.ofEpochMilli(now + 350));
      _ev.setSignupDeadline(Instant.ofEpochMilli(now + 250));
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      _ev = null;
      super.tearDown();
   }

   public static Test suite() {
      return new CoverageDecorator(TestEventAccessControl.class, new Class[] { AccessControl.class,
            EventAccessControl.class });
   }
  
   public void testProperties() throws Exception {
      _ac.validate();
      
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanAssignFlights());
      assertFalse(_ac.getCanSignup());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCancel());
      
      _user.addRole("Pilot");
      _ac.validate();
      
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanAssignFlights());
      assertTrue(_ac.getCanSignup());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCancel());
   }
   
   public void testNullEvent() throws Exception {
      _user.addRole("Pilot");
      _ac = new EventAccessControl(_ctxt, null);
      _ac.validate();
      
      assertFalse(_ac.getCanCreate());
      
      _user.addRole("Event");
      _ac.validate();
      assertTrue(_ac.getCanCreate());
   }
   
   public void testContextValidation() {
      doContextValidation(new EventAccessControl(null, _ev));
   }
}
