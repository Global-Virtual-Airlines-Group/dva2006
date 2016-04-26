// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

public class TestEquipmentAccessControl extends AccessControlTestCase {

   private EquipmentAccessControl _ac;
   
   public static Test suite() {
      return new CoverageDecorator(TestEquipmentAccessControl.class, 
            new Class[] { AccessControl.class, EquipmentAccessControl.class } );
  }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _ac = new EquipmentAccessControl(_ctxt);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      super.tearDown();
   }
   
   public void testAccess() throws Exception {
      _ac.validate();
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      assertFalse(_ac.getCanRename());

      _user.addRole("HR");
      _ac.validate();
      
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      assertFalse(_ac.getCanRename());

      _user.addRole("Admin");
      _ac.validate();
      
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanDelete());
      assertTrue(_ac.getCanRename());
   }
   
   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      assertFalse(_ac.getCanRename());
   }

   @SuppressWarnings("static-method")
public void testContextValidation() {
      doContextValidation(new EquipmentAccessControl(null));
   }
}