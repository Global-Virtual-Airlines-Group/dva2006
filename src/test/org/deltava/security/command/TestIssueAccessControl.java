// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.system.Issue;

public class TestIssueAccessControl extends AccessControlTestCase {

	private IssueAccessControl _ac;
	private Issue _i;

	public static Test suite() {
		return new CoverageDecorator(TestIssueAccessControl.class, new Class[] { AccessControl.class,
				IssueAccessControl.class });
	}

	protected void setUp() throws Exception {
		super.setUp();
		_i = new Issue(1, "Issue");
		_ac = new IssueAccessControl(_ctxt, _i);
	}

	protected void tearDown() throws Exception {
		_ac = null;
		_i = null;
		super.tearDown();
	}

	public void testAccess() throws Exception {
		_user.removeRole("Pilot");
		assertFalse(_ctxt.isUserInRole("Pilot"));
		assertEquals(Issue.STATUS_OPEN, _i.getStatus());
		_ac.validate();

		assertFalse(_ac.getCanCreate());
		assertFalse(_ac.getCanComment());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());

		_user.addRole("Pilot");
		assertFalse(_i.getAssignedTo() == _user.getID());
		assertFalse(_i.getAuthorID() == _user.getID());
		_ac.validate();

		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanComment());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());
	}

	public void testAssignedToAccess() throws Exception {
		assertEquals(Issue.STATUS_OPEN, _i.getStatus());
		assertFalse(_i.getAuthorID() == _user.getID());
		_i.setAssignedTo(_user.getID());
		_user.addRole("Pilot");
		_ac.validate();

		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanComment());
		assertTrue(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());
	}

	public void testCreatedByAccess() throws Exception {
		assertEquals(Issue.STATUS_OPEN, _i.getStatus());
		assertFalse(_i.getAssignedTo() == _user.getID());
		_i.setAuthorID(_user.getID());
		_user.addRole("Pilot");
		_ac.validate();

		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanComment());
		assertTrue(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());

		_i.setStatus(Issue.STATUS_FIXED);
		_ac.validate();
		assertTrue(_ac.getCanCreate());
		assertFalse(_ac.getCanComment());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());
	}

	public void testResolutionAccess() throws Exception {
		assertEquals(Issue.STATUS_OPEN, _i.getStatus());
		assertFalse(_i.getAssignedTo() == _user.getID());
		assertFalse(_i.getAuthorID() == _user.getID());
		_user.addRole("Pilot");
		_user.addRole("Developer");
		_ac.validate();

		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanComment());
		assertTrue(_ac.getCanEdit());
		assertTrue(_ac.getCanReassign());
		assertTrue(_ac.getCanResolve());

		_i.setStatus(Issue.STATUS_WORKAROUND);
		_ac.validate();

		assertTrue(_ac.getCanCreate());
		assertTrue(_ac.getCanComment());
		assertTrue(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertTrue(_ac.getCanResolve());
	}

	public void testAnonymousAccess() throws Exception {
		_ctxt.logoff();
		_ac.validate();

		assertFalse(_ac.getCanCreate());
		assertFalse(_ac.getCanComment());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());
	}

	public void testNullIssue() throws Exception {
		_user.removeRole("Pilot");
		assertFalse(_ctxt.isUserInRole("Pilot"));
		_ac = new IssueAccessControl(_ctxt, null);
		_ac.validate();

		assertFalse(_ac.getCanCreate());
		assertFalse(_ac.getCanComment());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());

		_user.addRole("Pilot");
		_ac.validate();

		assertTrue(_ac.getCanCreate());
		assertFalse(_ac.getCanComment());
		assertFalse(_ac.getCanEdit());
		assertFalse(_ac.getCanReassign());
		assertFalse(_ac.getCanResolve());
	}
}