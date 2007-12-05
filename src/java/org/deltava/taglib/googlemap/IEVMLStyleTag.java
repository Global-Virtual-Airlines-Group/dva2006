// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to render the Internet Explorer VML behavior style for Google Maps.
 * @author Luke
 * @version 2.0
 * @since 1.0
 */

public class IEVMLStyleTag extends TagSupport {

   public int doEndTag() throws JspException {
      
      // Do nothing if not IE
      if (!ContentHelper.isIE6(pageContext) && !ContentHelper.isIE7(pageContext))
         return EVAL_PAGE;
      
      JspWriter out = pageContext.getOut();
      try {
         out.println("<style type=\"text/css\">");
         out.println("v\\:* {");
         out.println("\tbehavior:url(#default#VML);");
         out.println("}");
         out.println("</style>");
      } catch (Exception e) {
         throw new JspException(e);
      }
      
      return EVAL_PAGE;
   }
}