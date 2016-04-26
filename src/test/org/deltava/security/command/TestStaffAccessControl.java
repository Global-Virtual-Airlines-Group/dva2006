// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.Staff;

public class TestStaffAccessControl extends AccessControlTestCase {

   private StaffAccessControl _ac;
   private Staff _s;
   
   public static Test suite() {
      return new CoverageDecorator(TestStaffAccessControl.class, new Class[] { AccessControl.class,
            StaffAccessControl.class });
   }

   @Override
protected void setUp() throws Exception {
      super.setUp();
      _s = new Staff(_user.getFirstName(), _user.getLastName());
      _ac = new StaffAccessControl(_ctxt, _s);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      _s = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
      assertFalse(_s.getID() == _user.getID());
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
      
      _user.addRole("HR");
      _ac.validate();
      
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanCreate());
      assertTrue(_ac.getCanDelete());

      _user.removeRole("HR");
      _s.setID(_user.getID());
      _ac.validate();
      
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testNullStaffProfile() throws Exception {
      _ac = new StaffAccessControl(_ctxt, null);
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _user.addRole("HR");
      _ac.validate();

      assertFalse(_ac.getCanEdit());
      assertTrue(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
      
      _ctxt.logoff();
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testContextValidation() {
      doContextValidation(new StaffAccessControl(null, _s));
   }
}