package org.deltava.taglib.html;

import org.deltava.taglib.AbstractFormTagTestCase;

public class TestFormTag extends AbstractFormTagTestCase {

   public void testProperties() throws Exception {
      _formTag.setAction("/delta/command.do");
      _formTag.setValidate("return true;");
      _formTag.setMethod("POST");
      _formTag.setID("Form1");

      assertEvalBody(_formTag.doStartTag());
      assertEvalPage(_formTag.doEndTag());
      
      parseOutput();
      assertEquals("form", getElementName());
      assertEquals(4, getAttrCount());
      assertAttr("/delta/command.do", "action");
      assertAttr("return true;", "onsubmit");
      assertAttr("post", "method");
      assertAttr("Form1", "id");
   }
}