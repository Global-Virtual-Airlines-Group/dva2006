package org.deltava.security.command;

import org.deltava.beans.cooler.*;
import org.deltava.beans.cooler.Channel.InfoType;

public class TestCoolerThreadAccessControl extends AccessControlTestCase {

   private MessageThread _mt;
   private Channel _c;
   
   private CoolerThreadAccessControl _ac;
   
   @Override
   protected void setUp() throws Exception {
      super.setUp();
      _c = new Channel("Channel");
      _c.addAirline("DVA");
      _c.addRole(InfoType.READ, "Dummy");
      _mt = new MessageThread("Thread Subject");
      _ac = new CoolerThreadAccessControl(_ctxt);
   }

   @Override
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
      
      _c.addRole(InfoType.READ, "*");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());
   }
   
   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      _c.addRole(InfoType.READ, "*");
      _ac.updateContext(_mt, _c);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanReply());
      assertFalse(_ac.getCanLock());
      assertFalse(_ac.getCanUnlock());
   }
   
   public void testLockingAccess() throws Exception {
      _c.addRole(InfoType.READ, "*");
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
      _c.addRole(InfoType.READ, "*");
      _c.addRole(InfoType.WRITE, "*");
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
   
   @SuppressWarnings("static-method")
public void testContextValidation() {
      doContextValidation(new CoolerThreadAccessControl(null));
   }
}