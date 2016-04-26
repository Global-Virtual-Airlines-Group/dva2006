package org.deltava.taglib.html;

import org.deltava.taglib.AbstractTagTestCase;

public class TestTableTag extends AbstractTagTestCase {

    private TableTag _tag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new TableTag();
        _tag.setPageContext(_ctx);
    }
    
    @Override
	protected void tearDown() throws Exception {
        _tag = null;
        super.tearDown();
    }
    
    public void testAttributes() throws Exception {
        _tag.setID("TableID");
        _tag.setClassName("tableClass");
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("table", getElementName());
        assertEquals(4, getAttrCount());
        
        assertAttr("TableID", "id");
        assertAttr("tableClass", "class");
    }
    
    public void testDefaults() throws Exception {
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("table", getElementName());
        assertEquals(2, getAttrCount());

        assertAttr("3", "cellspacing");
        assertAttr("4", "cellpadding");
    }
    
    public void testValidation() throws Exception {
        _tag.setID("idTable");
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("table", getElementName());
        assertEquals(1, getAttrCount());

        assertAttr("idTable", "id");
    }
}