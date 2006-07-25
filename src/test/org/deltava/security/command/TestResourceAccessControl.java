package org.deltava.security.command;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

import org.deltava.beans.fleet.Resource;

public class TestResourceAccessControl extends AccessControlTestCase {

	private ResourceAccessControl _ac;
	private Resource _r;

	public static Test suite() {
		return new CoverageDecorator(TestResourceAccessControl.class, new Class[] { AccessControl.class,
				ResourceAccessControl.class });
	}

	protected void setUp() throws Exception {
		super.setUp();
		_r = new Resource("http://localhost/test");
	}

	protected void tearDown() throws Exception {
		_r = null;
		_ac = null;
		super.tearDown();
	}

	public void testPilotAccess() {
		assertFalse(_ctxt.isUserInRole("Fleet") || _ctxt.isUserInRole("HR"));
		assertTrue(_ctxt.isUserInRole("Pilot"));
		assertFalse(_r.getAuthorID() == _user.getID());
		_ac = new ResourceAccessControl(_ctxt, _r);
		_ac.validate();
		
		assertTrue(_ac.getCanCreate());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanDelete());
		
		// Check access to our own private resources 
		_r.setAuthorID(_user.getID());
		assertFalse(_r.getPublic());
		assertTrue(_r.getAuthorID() == _user.getID());
		_ac.validate();
		
		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanEdit());
		assertTrue(_ac.getCanDelete());

		// Check access to our own public resources
		_r.setPublic(true);
		assertTrue(_r.getPublic());
		_ac.validate();
		
		assertTrue(_ac.getCanCreate());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanDelete());
	}
	
	public void testCreateAccess() {
		assertTrue(_ctxt.isUserInRole("Pilot"));
		_ac = new ResourceAccessControl(_ctxt, null);
		_ac.validate();
		
		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanEdit());
		assertFalse(_ac.getCanDelete());
		
		// Validate non-create access
		_ctxt.getRoles().remove("Pilot");
		assertFalse(_ctxt.isUserInRole("Pilot"));
		_ac.validate();
		
		assertFalse(_ac.getCanCreate());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanDelete());
	}
	
	public void testValidation() {
		_ac = new ResourceAccessControl(null, null);
		doContextValidation(_ac);
	}
	
	public void testHRAccess() {
		_user.addRole("HR");
		assertTrue(_ctxt.isUserInRole("HR"));
		assertFalse(_r.getAuthorID() == _user.getID());
		
		_ac = new ResourceAccessControl(_ctxt, _r);
		_ac.validate();
		
		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanEdit());
		assertTrue(_ac.getCanDelete());
	}
	
	public void testFleetAccess() {
		_user.addRole("Fleet");
		assertTrue(_ctxt.isUserInRole("Fleet"));
		assertFalse(_r.getAuthorID() == _user.getID());

		_ac = new ResourceAccessControl(_ctxt, _r);
		_ac.validate();
		
		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanEdit());
		assertTrue(_ac.getCanDelete());
	}
}