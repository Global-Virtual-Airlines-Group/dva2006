package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import java.io.File;

import org.deltava.beans.fleet.*;

public class TestFleetEntryAccessControl extends AccessControlTestCase {

   private FleetEntryAccessControl _ac;
   private Manual _m;
   
   public static Test suite() {
      return new CoverageDecorator(TestFleetEntryAccessControl.class, new Class[] { AccessControl.class,
            FleetEntryAccessControl.class });
   }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _m = new Manual(new File("manual.pdf"));
      _ac = new FleetEntryAccessControl(_ctxt, _m);
   }

   @Override
protected void tearDown() throws Exception {
      _m = null;
      _ac = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
      assertEquals(Security.PUBLIC, _m.getSecurity());
      _ac.validate();
      
      assertTrue(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _m.setSecurity(Security.AUTH);
      _ac.validate();

      assertTrue(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _m.setSecurity(Security.STAFF);
      _ac.validate();

      assertFalse(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      assertEquals(Security.PUBLIC, _m.getSecurity());
      _ac.validate();
      
      assertTrue(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
      
      _m.setSecurity(Security.AUTH);
      _ac.validate();
      
      assertFalse(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _m.setSecurity(Security.STAFF);
      _ac.validate();

      assertFalse(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testStaffAccess() throws Exception {
      _m.setSecurity(Security.STAFF);
      _ac.validate();
      
      assertFalse(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _user.addRole("Staff");
      _ac.validate();
      
      assertTrue(_ac.getCanView());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _user.addRole("Fleet");
      _ac.validate();
      
      assertTrue(_ac.getCanView());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
      
      _user.addRole("Admin");
      _ac.validate();

      assertTrue(_ac.getCanView());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanCreate());
      assertTrue(_ac.getCanDelete());
   }
   
   public void testNullEntry() throws Exception {
      _ac.setEntry(null);
      _ac.validate();
      
      assertFalse(_ac.getCanCreate());
      
      _user.addRole("Fleet");
      _ac.validate();
      
      assertTrue(_ac.getCanCreate());
      
      _ctxt.logoff();
      _ac.validate();
      
      assertFalse(_ac.getCanCreate());
   }
   
   public void testContextValidation() {
      doContextValidation(new FleetEntryAccessControl(null, _m));
   }
}