// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.Person;
import org.deltava.beans.Pilot;

public class TestPilotAccessControl extends AccessControlTestCase {

   private PilotAccessControl _ac;
   private Pilot _p;
   
   public static Test suite() {
      return new CoverageDecorator(TestPilotAccessControl.class, new Class[] { AccessControl.class,
            PilotAccessControl.class });
   }

   @Override
protected void setUp() throws Exception {
      super.setUp();
      _p = new Pilot("John", "Smith");
      _ac = new PilotAccessControl(_ctxt, _p);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      _p = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
      assertTrue(_p.getID() != _user.getID());
      assertEquals(Person.HIDE_EMAIL, _p.getEmailAccess());
      _ac.validate();
      
      assertFalse(_ac.getIsOurs());
      assertFalse(_ac.getCanViewEmail());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanTakeLeave());
      assertFalse(_ac.getCanPromote());
      assertFalse(_ac.getCanChangeStatus());
      assertFalse(_ac.getCanChangeRoles());
      assertFalse(_ac.getCanChangeStaffProfile());
      
      _p.setEmailAccess(Person.AUTH_EMAIL);
      _ac.validate();
      
      assertTrue(_ac.getCanViewEmail());
      
      _p.setEmailAccess(Person.SHOW_EMAIL);
      _ac.validate();

      assertTrue(_ac.getCanViewEmail());
   }
   
   public void testAdminAccess() throws Exception {
      assertTrue(_p.getID() != _user.getID());
      _request.setAttribute("staff", new Object());
      _user.addRole("PIREP");
      _ac.validate();
      
      assertFalse(_ac.getIsOurs());
      assertTrue(_ac.getCanViewEmail());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanTakeLeave());
      assertTrue(_ac.getCanPromote());
      assertFalse(_ac.getCanChangeStatus());
      assertFalse(_ac.getCanChangeRoles());
      assertFalse(_ac.getCanChangeStaffProfile());

      _user.addRole("HR");
      _user.removeRole("PIREP");
      _ac.validate();
      
      assertFalse(_ac.getIsOurs());
      assertTrue(_ac.getCanViewEmail());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanTakeLeave());
      assertTrue(_ac.getCanPromote());
      assertTrue(_ac.getCanChangeStatus());
      assertFalse(_ac.getCanChangeRoles());
      assertTrue(_ac.getCanChangeStaffProfile());
      
      _user.addRole("Admin");
      _ac.validate();
      
      assertFalse(_ac.getIsOurs());
      assertTrue(_ac.getCanViewEmail());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanTakeLeave());
      assertTrue(_ac.getCanPromote());
      assertTrue(_ac.getCanChangeStatus());
      assertTrue(_ac.getCanChangeRoles());
      assertTrue(_ac.getCanChangeStaffProfile());
   }
   
   public void testMyProfileAccess() throws Exception {
      assertTrue(_p.getStatus() != Pilot.ON_LEAVE);
      _p.setID(_user.getID());
      _ac.validate();
      
      assertTrue(_ac.getIsOurs());
      assertTrue(_ac.getCanViewEmail());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanTakeLeave());
      assertFalse(_ac.getCanPromote());
      assertFalse(_ac.getCanChangeStatus());
      assertFalse(_ac.getCanChangeRoles());
      assertFalse(_ac.getCanChangeStaffProfile());
      
      _request.setAttribute("staff", new Object());
      _ac.validate();
      
      assertTrue(_ac.getIsOurs());
      assertTrue(_ac.getCanViewEmail());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanTakeLeave());
      assertFalse(_ac.getCanPromote());
      assertFalse(_ac.getCanChangeStatus());
      assertFalse(_ac.getCanChangeRoles());
      assertTrue(_ac.getCanChangeStaffProfile());

      _p.setStatus(Pilot.ON_LEAVE);
      _ac.validate();
      
      assertTrue(_ac.getIsOurs());
      assertTrue(_ac.getCanViewEmail());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanTakeLeave());
      assertFalse(_ac.getCanPromote());
      assertFalse(_ac.getCanChangeStatus());
      assertFalse(_ac.getCanChangeRoles());
      assertTrue(_ac.getCanChangeStaffProfile());
   }
      
   public void testAnonymousAccess() throws Exception {
      assertEquals(Person.HIDE_EMAIL, _p.getEmailAccess());
      _ctxt.logoff();
      _ac.validate();
      
      assertFalse(_ac.getIsOurs());
      assertFalse(_ac.getCanViewEmail());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanTakeLeave());
      assertFalse(_ac.getCanPromote());
      assertFalse(_ac.getCanChangeStatus());
      assertFalse(_ac.getCanChangeRoles());
      assertFalse(_ac.getCanChangeStaffProfile());
      
      _p.setEmailAccess(Person.AUTH_EMAIL);
      _ac.validate();
      
      assertFalse(_ac.getCanViewEmail());
      
      _p.setEmailAccess(Person.SHOW_EMAIL);
      _ac.validate();
      
      assertTrue(_ac.getCanViewEmail());
   }
   
   public void testContextValidation() {
      doContextValidation(new PilotAccessControl(null, _p));
   }
}