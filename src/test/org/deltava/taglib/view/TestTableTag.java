package org.deltava.taglib.view;

import org.deltava.taglib.AbstractTagTestCase;
import org.deltava.util.system.SystemData;

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
        _tag.setCmd("commandName");
        _tag.setSize("60");
        
        assertEquals("commandname", _tag.getCmd());
        assertEquals(60, _tag.size());
        
        assertEvalBody(_tag.doStartTag());
        assertEquals("<table id=\"TableID\" class=\"tableClass\" cellspacing=\"2\" cellpadding=\"5\">", _jspOut.toString());

        _jspOut.clearBuffer();
        assertEvalPage(_tag.doEndTag());
        assertEquals("</table>", _jspOut.toString());
    }
    
    public void testDefaults() throws Exception {
        _tag.setSpace("default");
        _tag.setPad("default");
        
        assertEquals(SystemData.getInt("html.table.viewSize"), _tag.size());
        assertEvalBody(_tag.doStartTag());
        assertEquals("<table cellspacing=\"3\" cellpadding=\"4\">", _jspOut.toString());

        _jspOut.clearBuffer();
        assertEvalPage(_tag.doEndTag());
        assertEquals("</table>", _jspOut.toString());
    }
    
    public void testValidation() throws Exception {
        _tag.setID("idTable");
        _tag.setSize("XXXX");
        _tag.setSpace("-1");
        _tag.setPad("XXXX");
        
        assertEquals(SystemData.getInt("html.table.viewSize"), _tag.size());
        assertEvalBody(_tag.doStartTag());
        assertEquals("<table id=\"idTable\">", _jspOut.toString());

        _jspOut.clearBuffer();
        assertEvalPage(_tag.doEndTag());
        assertEquals("</table>", _jspOut.toString());
    }
}