package org.deltava.taglib.html;

import org.deltava.taglib.AbstractTagTestCase;

public class TestButtonTag extends AbstractTagTestCase {

    private ButtonTag _tag;

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new ButtonTag();
        _tag.setPageContext(_ctx);
    }

    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testProperties() throws Exception {
        _tag.setID("SaveButton");
        _tag.setLabel("SAVE");
        _tag.setOnClick("return true;");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"button\" id=\"SaveButton\" value=\"SAVE\" onclick=\"return true;\" />", _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setLabel("CLEAR");

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertEquals(2, getAttrCount());
        assertAttr("button", "type");
        assertAttr("CLEAR", "value");
    }
    
    public void testEmptyButton() throws Exception {
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertEquals(1, getAttrCount());
        assertAttr("button", "type");
    }
}