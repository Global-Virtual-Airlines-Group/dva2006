package org.deltava.taglib.content;

import org.deltava.taglib.AbstractTagTestCase;

public class TestInsertCSSTag extends AbstractTagTestCase {

    private InsertCSSTag _tag;
    
    private static final String CSS_START = "<link rel=\"STYLESHEET\" type=\"text/css\" href=\"";
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new InsertCSSTag();
        _tag.setPageContext(_ctx);
    }
    
    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }
    
    public void testOutput() throws Exception {
        _tag.setName("dva");
        assertEquals("legacy", _tag.getScheme());
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals(CSS_START + "/css/legacy/dva.css\" />", _jspOut.toString());

        _jspOut.clearBuffer();
        
        _tag.setName("deltava");
        _tag.setScheme("modern");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals(CSS_START + "/css/modern/deltava.css\" />", _jspOut.toString());
    }
}