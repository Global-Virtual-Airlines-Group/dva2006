package org.deltava.taglib.content;

import org.deltava.beans.Applicant;
import org.deltava.beans.Pilot;

import org.deltava.taglib.AbstractTagTestCase;

public class TestRoleFilterTag extends AbstractTagTestCase {

    private RoleFilterTag _tag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new RoleFilterTag();
        _tag.setPageContext(_ctx);
    }

    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testPilot() throws Exception {
        Pilot p = new Pilot("John", "Smith");
        setUser(p);
        
        // test null handling
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        // test empty role handling
        _tag.setRoles("");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        // test wildcard handling
        _tag.setRoles("*");
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        // test single role handling when we don't belong
        _tag.setRoles("role1");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());

        // test single role handling when we do belong
        _tag.setRoles("role1");
        p.addRole("role1");
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
   
        // test multi-role handling when we don't belong
        _tag.setRoles("role2,role3,role4");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());

        // test multi-role handling when we do belong
        _tag.setRoles("role2,role3,role4");
        p.addRole("role3");
        p.addRole("role4");
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
    }
    
    public void testApplicant() throws Exception {
        Applicant a = new Applicant("John", "Smith");
        setUser(a);

        // test null handling
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());

        // test wildcard handling
        _tag.setRoles("*");
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        // test applicant handling
        _tag.setRoles("Applicant");
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
    }
    
    public void testAnonymous() throws Exception {
        // test null handling
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        // test wildcard handling
        _tag.setRoles("*");
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
    }
}