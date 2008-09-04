package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.cooler.Channel;

import org.deltava.util.system.*;

public class TestCoolerChannelAccessControl extends AccessControlTestCase {

	private CoolerChannelAccessControl _ac;
	private Channel _c;

	public static Test suite() {
		return new CoverageDecorator(TestCoolerChannelAccessControl.class, new Class[] { AccessControl.class,
				CoolerChannelAccessControl.class });
	}

	protected void setUp() throws Exception {
		super.setUp();
		SystemData.init(MockSystemDataLoader.class.getName(), true);
		_c = new Channel("Name");
		_c.addAirline("DVA");
		_ac = new CoolerChannelAccessControl(_ctxt, _c);
	}

	protected void tearDown() throws Exception {
		_c = null;
		_ac = null;
		super.tearDown();
	}

	public void testAccess() throws Exception {
		_c.addRole(false, "X");
		assertFalse(_user.isInRole("X"));
		_ac.validate();

		assertFalse(_ac.getCanAccess());
		assertFalse(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());

		_user.addRole("X");
		_ac.validate();

		assertTrue(_ac.getCanAccess());
		assertFalse(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());
	}

	public void testWildcardAccess() throws Exception {
		_c.addRole(false, "*");
		_c.addRole(true, "*");
		_ac.validate();

		assertTrue(_ac.getCanAccess());
		assertTrue(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());
	}

	public void testAdminAccess() throws Exception {
		_user.addRole("Admin");
		_ac.validate();

		assertTrue(_ac.getCanAccess());
		assertTrue(_ac.getCanPost());
		assertTrue(_ac.getCanRead());
		assertTrue(_ac.getCanEdit());
	}

	public void testAnonymousAccess() throws Exception {
		_ctxt.logoff();
		_ac.validate();

		assertFalse(_ac.getCanAccess());
		assertFalse(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());

		_c.addRole(false, "Anonymous");
		_ac.validate();

		assertTrue(_ac.getCanAccess());
		assertFalse(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());
	}

	public void testNoChannelAccess() throws Exception {
		_ac = new CoolerChannelAccessControl(_ctxt, null);
		_ac.validate();

		assertTrue(_ac.getCanAccess());
		assertTrue(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());

		_user.addRole("Admin");
		_ac.validate();
		assertTrue(_ac.getCanAccess());
		assertTrue(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertTrue(_ac.getCanEdit());

		_ctxt.logoff();
		_ac.validate();

		assertTrue(_ac.getCanAccess());
		assertFalse(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());
	}

	public void testOtherAirlineAccess() throws Exception {
		_c.addRole(false, "Pilot");
		_c.addRole(true, "Pilot");
		_user.setPilotCode("AFV123");
		assertFalse(_c.getAirlines().contains("AFV"));
		_ac.validate();

		assertFalse(_ac.getCanAccess());
		assertFalse(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());

		_c.addAirline("AFV");
		_ac.validate();
		
		assertTrue(_ac.getCanAccess());
		assertTrue(_ac.getCanPost());
		assertFalse(_ac.getCanRead());
		assertFalse(_ac.getCanEdit());
	}

	public void testContextValidation() {
		doContextValidation(new CoolerChannelAccessControl(null, _c));
	}
}