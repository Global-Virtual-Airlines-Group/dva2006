package org.deltava.taglib.html;

import org.deltava.taglib.AbstractTagTestCase;

public class TestTableTag extends AbstractTagTestCase {

    private TableTag _tag;
    
    protected void setUp() throws Exception {
        super.setUp();
        _tag = new TableTag();
        _tag.setPageContext(_ctx);
    }
    
    protected void tearDown() throws Exception {
        _tag = null;
        super.tearDown();
    }
    
    public void testAttributes() throws Exception {
        _tag.setID("TableID");
        _tag.setClassName("tableClass");
        _tag.setSpace("2");
        _tag.setPad("5");
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("table", getElementName());
        assertEquals(4, getAttrCount());
        
        assertAttr("TableID", "id");
        assertAttr("tableClass", "class");
        assertAttr("2", "cellspacing");
        assertAttr("5", "cellpadding");
    }
    
    public void testDefaults() throws Exception {
        _tag.setSpace("default");
        _tag.setPad("default");
        
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
        _tag.setSpace("-1");
        _tag.setPad("XXXX");
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("table", getElementName());
        assertEquals(1, getAttrCount());

        assertAttr("idTable", "id");
    }
}