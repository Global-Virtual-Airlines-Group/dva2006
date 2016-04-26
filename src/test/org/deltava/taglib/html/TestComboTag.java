package org.deltava.taglib.html;

import java.util.ArrayList;
import java.util.List;

import org.deltava.beans.schedule.Airport;

import org.deltava.taglib.AbstractFormTagTestCase;

public class TestComboTag extends AbstractFormTagTestCase {

    private ComboTag _tag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new ComboTag();
        _tag.setPageContext(_ctx);
        _tag.setParent(_formTag);
    }

    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testProperties() throws Exception {
        _tag.setName("COMBO");
        _tag.setIdx("2");
        _tag.setMultiple(true);
        _tag.setOnChange("return true;");
        
        List<String> options = new ArrayList<String>();
        options.add("OPT1");
        options.add("OPT2");
        _tag.setOptions(options);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\" tabindex=\"2\" multiple onchange=\"return true;\">" + CRLF +
                " <option>OPT1</option>" + CRLF + " <option>OPT2</option>" + CRLF + "</select>" +
                CRLF, _jspOut.toString());

        _jspOut.clearBuffer();
        _tag.setName("COMBO");
        _tag.setOptions(options);
        _tag.setValue("OPT2");

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\">" + CRLF + " <option>OPT1</option>" + CRLF +
                " <option selected=\"selected\">OPT2</option>" + CRLF + "</select>" + CRLF, _jspOut.toString());

        _jspOut.clearBuffer();
        _tag.setName("COMBO");
        _tag.setMultiple(true);
        _tag.setOptions(options);
        _tag.setValue(options);

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\" multiple>" + CRLF + " <option selected=\"selected\">OPT1</option>" + 
                CRLF + " <option selected=\"selected\">OPT2</option>" + CRLF + "</select>" + CRLF, _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("COMBO");
        _tag.setMultiple(true);
        _tag.setOptions(options);
        _tag.setDelimValues("OPT2,OPT1");

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\" multiple>" + CRLF + " <option selected=\"selected\">OPT1</option>" + 
                CRLF + " <option selected=\"selected\">OPT2</option>" + CRLF + "</select>" + CRLF, _jspOut.toString());
    }
    
    public void testBooleanProperties() throws Exception {
        _tag.setName("COMBO");
        _tag.setSize(1);
        _tag.setMultiple(true);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\" size=\"1\" multiple>" + CRLF + "</select>" + CRLF, _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("COMBO");
        _tag.setMultiple(false);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\">" + CRLF + "</select>" + CRLF, _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("COMBO2");

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO2\">" + CRLF + "</select>" + CRLF, _jspOut.toString());
    }
    
    public void testComboAlias() throws Exception {
        _tag.setName("COMBO");
        
        List<Airport> options = new ArrayList<Airport>();
        options.add(new Airport("ATL", "KATL", "Atlanta GA"));
        options.add(new Airport("CLT", "KCLT", "Charlotte NC"));
        _tag.setOptions(options);

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\">" + CRLF + " <option value=\"ATL\">Atlanta GA (ATL)</option>" +
                CRLF + " <option value=\"CLT\">Charlotte NC (CLT)</option>" + CRLF + "</select>" + CRLF,
                _jspOut.toString());

        _jspOut.clearBuffer();
        _tag.setName("COMBO");
        _tag.setOptions(options);
        _tag.setValue(new Airport("ATL", "KATL", "Atlanta GA"));
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\">" + CRLF + " <option selected=\"selected\" value=\"ATL\">Atlanta GA (ATL)</option>" +
                CRLF + " <option value=\"CLT\">Charlotte NC (CLT)</option>" + CRLF + "</select>" + CRLF,
                _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("COMBO");
        _tag.setOptions(options);
        _tag.setValue(options);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\">" + CRLF + " <option selected=\"selected\" value=\"ATL\">Atlanta GA (ATL)</option>" +
                CRLF + " <option selected=\"selected\" value=\"CLT\">Charlotte NC (CLT)</option>" + CRLF + "</select>" + CRLF,
                _jspOut.toString());
    }
    
    public void testNumericValidation() throws Exception {
        _tag.setName("COMBO");
        _tag.setSize(0);
        _tag.setIdx("asdasds");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\">" + CRLF + "</select>" + CRLF, _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("COMBO");
        _tag.setSize(-1);
        _tag.setIdx(null);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<select name=\"COMBO\">" + CRLF + "</select>" + CRLF, _jspOut.toString());
    }
    
    public void testNameException() {
        _tag.setClassName("CLASSNAME");
        testNameValidation(_tag);
    }
}