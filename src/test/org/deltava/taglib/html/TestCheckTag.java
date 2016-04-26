package org.deltava.taglib.html;

import java.util.ArrayList;
import java.util.List;

import org.deltava.taglib.AbstractFormTagTestCase;

import org.deltava.beans.schedule.Airline;

public class TestCheckTag extends AbstractFormTagTestCase {

    private CheckTag _tag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new CheckTag();
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
        _tag.setName("RADIO1");
        _tag.setType("radio");
        _tag.setIdx("2");
        
        List<String> options = new ArrayList<String>();
        options.add("OPT1");
        options.add("OPT2");
        _tag.setOptions(options);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"radio\" class=\"radio\" name=\"RADIO1\" tabindex=\"2\" value=\"OPT1\" />OPT1" + 
                "<input type=\"radio\" class=\"radio\" name=\"RADIO1\" tabindex=\"2\" value=\"OPT2\" />OPT2", _jspOut.toString());

        _jspOut.clearBuffer();
        _tag.setName("RADIO1");
        _tag.setType("radio");
        _tag.setOptions(options);
        _tag.setValue("OPT1");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"radio\" class=\"radio\" name=\"RADIO1\" checked=\"checked\" value=\"OPT1\" />OPT1" + 
                "<input type=\"radio\" class=\"radio\" name=\"RADIO1\" value=\"OPT2\" />OPT2", _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("CHECK1");
        _tag.setOptions(options);
        _tag.setDelimValues("OPT1,OPT2");

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"checkbox\" class=\"check\" name=\"CHECK1\" checked=\"checked\" value=\"OPT1\" />OPT1" + 
                "<input type=\"checkbox\" class=\"check\" name=\"CHECK1\" checked=\"checked\" value=\"OPT2\" />OPT2", _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("CHECK1");
        _tag.setOptions(options);
        _tag.setValue(options);

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"checkbox\" class=\"check\" name=\"CHECK1\" checked=\"checked\" value=\"OPT1\" />OPT1" + 
                "<input type=\"checkbox\" class=\"check\" name=\"CHECK1\" checked=\"checked\" value=\"OPT2\" />OPT2", _jspOut.toString());
    }
    
    public void testComboAlias() throws Exception {
        _tag.setName("RADIO1");
        _tag.setType("radio");
        List<Airline> options = new ArrayList<Airline>();
        options.add(new Airline("DVA", "Delta Virtual"));
        options.add(new Airline("AF", "Air France"));
        _tag.setOptions(options);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"radio\" class=\"radio\" name=\"RADIO1\" value=\"DVA\" />Delta Virtual" + 
                "<input type=\"radio\" class=\"radio\" name=\"RADIO1\" value=\"AF\" />Air France", _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("RADIO1");
        _tag.setType("radio");
        _tag.setOptions(options);
        _tag.setValue("DVA");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"radio\" class=\"radio\" name=\"RADIO1\" checked=\"checked\" value=\"DVA\" />Delta Virtual" + 
                "<input type=\"radio\" class=\"radio\" name=\"RADIO1\" value=\"AF\" />Air France", _jspOut.toString());
    }

    public void testNumericValidation() throws Exception {
        _tag.setName("CHECK2");
        _tag.setIdx("0");
        List<String> options = new ArrayList<String>();
        options.add("OPT1");
        _tag.setOptions(options);

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        assertEquals("<input type=\"checkbox\" class=\"check\" name=\"CHECK2\" value=\"OPT1\" />OPT1", _jspOut.toString());

        _jspOut.clearBuffer();
        _tag.setName("CHECK2");
        _tag.setIdx("asgagfsdf");
        _tag.setOptions(options);

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"checkbox\" class=\"check\" name=\"CHECK2\" value=\"OPT1\" />OPT1", _jspOut.toString());
        
        _jspOut.clearBuffer();
        _tag.setName("CHECK2");
        _tag.setIdx(null);
        _tag.setOptions(options);

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<input type=\"checkbox\" class=\"check\" name=\"CHECK2\" value=\"OPT1\" />OPT1", _jspOut.toString());
    }

    public void testNameException() {
        _tag.setClassName("CLASSNAME");
        testNameValidation(_tag);
    }
}