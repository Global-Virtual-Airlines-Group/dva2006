package org.deltava.taglib.view;

import org.deltava.taglib.AbstractTagTestCase;
import org.deltava.util.system.SystemData;

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
        _tag.setCmd("commandName");
        _tag.setSize(60);
        
        assertEquals("commandname", _tag.getCmd());
        assertEquals(60, _tag.size());
        
        assertEvalBody(_tag.doStartTag());
        assertEquals("<table id=\"TableID\" class=\"tableClass\">", _jspOut.toString());

        _jspOut.clearBuffer();
        assertEvalPage(_tag.doEndTag());
        assertEquals("</table>", _jspOut.toString());
    }
    
    public void testDefaults() throws Exception {
        assertEquals(SystemData.getInt("html.table.viewSize"), _tag.size());
        assertEvalBody(_tag.doStartTag());
        assertEquals("<table>", _jspOut.toString());

        _jspOut.clearBuffer();
        assertEvalPage(_tag.doEndTag());
        assertEquals("</table>", _jspOut.toString());
    }
    
    public void testValidation() throws Exception {
        _tag.setID("idTable");
        
        assertEquals(SystemData.getInt("html.table.viewSize"), _tag.size());
        assertEvalBody(_tag.doStartTag());
        assertEquals("<table id=\"idTable\">", _jspOut.toString());

        _jspOut.clearBuffer();
        assertEvalPage(_tag.doEndTag());
        assertEquals("</table>", _jspOut.toString());
    }
}