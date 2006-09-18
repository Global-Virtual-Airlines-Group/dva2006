package org.deltava.taglib;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.tagext.Tag;

import org.jdom.*;
import org.jdom.input.*;

import junit.framework.TestCase;
import com.kizna.servletunit.*;

import org.deltava.beans.Person;
import org.deltava.commands.CommandContext;
import org.deltava.servlet.filter.CustomRequestWrapper;

import org.deltava.util.system.*;

public class AbstractTagTestCase extends TestCase {

   protected SystemData _sysData;
   private Element _htmlE;

   protected MockPageContext _ctx;
   protected JspTestWriter _jspOut;

   protected HttpServletRequest _req;
   protected HttpServletResponse _rsp;
   protected HttpServletRequestSimulatorHelper _rootReq;

   // Quick helper class since ServletUnit does not support getParameterMap()
   protected class HttpServletRequestSimulatorHelper extends HttpServletRequestSimulator {

      public Map<String, String[]> getParameterMap() {
         Map<String, String[]> results = new HashMap<String, String[]>();
         Enumeration pNames = getParameterNames();
         while (pNames.hasMoreElements()) {
            String pName = (String) pNames.nextElement();
            results.put(pName, getParameterValues(pName));
         }

         return results;
      }
   }

   protected static final String CRLF = System.getProperty("line.separator");

   protected void setUp() throws Exception {
      super.setUp();

      SystemData.init("org.deltava.util.system.TagTestSystemDataLoader", true);

      _rootReq = new HttpServletRequestSimulatorHelper();
      _req = new CustomRequestWrapper(_rootReq);
      _rsp = new HttpServletResponseSimulator();

      _jspOut = new JspTestWriter();
      _ctx = new MockPageContext(_jspOut);

      _ctx.initialize(null, _req, _rsp, "", false, 8192, false);
   }

   protected void tearDown() throws Exception {
      _jspOut.close();
      _jspOut = null;
      _ctx = null;
      super.tearDown();
   }

   protected void setUser(Person p) {
      HttpSession s = _req.getSession(true);
      s.setAttribute(CommandContext.USER_ATTR_NAME, p);
   }

   protected void assertSkipBody(int resultCode) {
      assertEquals(Tag.SKIP_BODY, resultCode);
   }

   protected void assertEvalBody(int resultCode) {
      assertEquals(Tag.EVAL_BODY_INCLUDE, resultCode);
   }

   protected void assertEvalPage(int resultCode) {
      assertEquals(Tag.EVAL_PAGE, resultCode);
   }

   protected void parseOutput() throws IOException, JDOMException {

      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(new StringReader(_jspOut.toString()));
      _htmlE = doc.getRootElement();
   }
   
   protected String getElementName() {
      assertNotNull(_htmlE);
      return _htmlE.getName();
   }
   
   protected int getAttrCount() {
      assertNotNull(_htmlE);
      return _htmlE.getAttributes().size();
   }
   
   protected void assertAttr(String attrValue, String attrName) {
      assertNotNull(_htmlE.getAttribute(attrName));
      assertEquals(attrValue, _htmlE.getAttributeValue(attrName));
   }
}