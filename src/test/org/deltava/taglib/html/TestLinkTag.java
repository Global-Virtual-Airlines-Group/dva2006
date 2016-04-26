package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.AbstractTagTestCase;

public class TestLinkTag extends AbstractTagTestCase {

    private LinkTag _tag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new LinkTag();
        _tag.setPageContext(_ctx);
    }

    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testProperties() throws Exception {
        _tag.setUrl("/delta/command.do");
        _tag.setTarget("_NEW");
        _tag.setOnClick("return true;");
        _tag.setLabel("x");
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("a", getElementName());
        assertEquals(5, getAttrCount());
        
        assertAttr("/delta/command.do", "href");
        assertAttr("_NEW", "target");
        assertAttr("return true;", "onclick");
        assertAttr("window.status=\'\';", "onmouseout");
        assertAttr("window.status=\'x\';", "onmouseover");
    }
    
    public void testNoLinkException() {
        _tag.setLabel("This will fail");
        try {
            assertEvalBody(_tag.doStartTag());
            fail("JspException Expected");
        } catch (JspException je) {
            Throwable t = je.getCause();
            assertEquals(IllegalStateException.class, t.getClass());
            assertEquals("HREF or onClick must be set", t.getMessage());
        }
    }
}