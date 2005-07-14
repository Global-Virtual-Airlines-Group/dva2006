// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.cooler.Channel;
import org.deltava.beans.cooler.MessageThread;

public class TestCoolerThreadAccessControl extends AccessControlTestCase {

   private MessageThread _mt;
   private Channel _c;
   
   private CoolerThreadAccessControl _ac;
   
   public static Test suite() {
      return new CoverageDecorator(TestCoolerThreadAccessControl.class, 
            new Class[] { AccessControl.class, CoolerThreadAccessControl.class } );
  }
   
   protected void setUp() throws Exception {
      super.setUp();
      _c = new Channel("Channel");
      _mt = new MessageThread("Thread Subject");
      _ac = new CoolerThreadAccessControl(_ctxt);
   }

   protected void tearDown() throws Exception {
      _mt = null;
      _c = null;
      _ac = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
      _ac.updateContext(_mt, _c);
      _ac.validate();
      
      assertFalse(_ac.getCanRead());
      assertFalse(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());
      
      _c.addRole("*");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());
   }
   
   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      _c.addRole("*");
      _ac.updateContext(_mt, _c);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());
   }
   
   public void testLockingAccess() throws Exception {
      _c.addRole("*");
      _user.addRole("Moderator");
      _ac.updateContext(_mt, _c);
      _ac.validate();

      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanReply());
      assertTrue(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());

      _mt.setLocked(true);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertTrue(_ac.getCanUnlock());
   }
   
   public void testLockedAccess() throws Exception {
      _c.addRole("*");
      _mt.setHidden(true);
      _ac.updateContext(_mt, _c);
      _ac.validate();

      assertFalse(_ac.getCanRead());
      assertFalse(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());

      _mt.setHidden(false);
      _mt.setLocked(true);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());
      
      _user.addRole("Admin");
      _mt.setHidden(true);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertTrue(_ac.getCanUnlock());
   }
   
   public void testNullChannel() throws Exception {
      _ac.updateContext(_mt, null);
      _ac.validate();

      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());
   }
   
   public void testValidation() {
      _ac.updateContext(null, _c);
      try {
         _ac.validate();   
         fail("IllegalStateException expected");
      } catch (IllegalStateException ise) {
         return;
      } catch (Exception e) {
         fail("IllegalStateException expected");
      }
   }
   
   public void testContextValidation() {
      doContextValidation(new CoolerThreadAccessControl(null));
   }
}