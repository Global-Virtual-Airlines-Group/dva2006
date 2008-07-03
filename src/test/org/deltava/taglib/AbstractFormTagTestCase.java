package org.deltava.taglib;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.html.FormTag;
import org.deltava.taglib.html.FormElementTag;

public abstract class AbstractFormTagTestCase extends AbstractTagTestCase {

    protected FormTag _formTag;
  
    protected void setUp() throws Exception {
        super.setUp();
        _formTag = new FormTag();
        _formTag.setPageContext(_ctx);
    }
    
    protected void tearDown() throws Exception {
        _formTag.release();
        _formTag = null;
        super.tearDown();
    }
    
    protected void testNameValidation(FormElementTag feTag) {
        try {
            assertSkipBody(feTag.doStartTag());
            assertEvalPage(feTag.doEndTag());
            fail("JspException expected");
        } catch (JspException je) {
            Throwable t = je.getCause();
            assertNotNull(t);
            assertEquals(IllegalStateException.class, t.getClass());
            assertEquals("Form Element must contain NAME", t.getMessage());
        }
    }
    
    protected void testParentValidation(FormElementTag feTag) {
        try {
            assertSkipBody(feTag.doStartTag());
            assertEvalPage(feTag.doEndTag());
            fail("JspException expected");
        } catch (JspException je) {
            Throwable t = je.getCause();
            assertNotNull(t);
            assertEquals(IllegalStateException.class, t.getClass());
            assertTrue(t.getMessage().endsWith(" must be contained within a FORM tag"));
        }
    }
}