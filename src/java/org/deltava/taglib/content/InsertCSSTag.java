package org.deltava.taglib.content;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;

import org.deltava.beans.Person;
import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a Cascading Style Sheet.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InsertCSSTag extends InsertContentTag {

   private static final String DEFAULT_SCHEME = "legacy";

   private String _scheme;
   private boolean _browserSpecific;

   /**
    * Sets wether to include a brower-specific Cascading Style Sheet.
    * @param isSpecific TRUE if the CSS is browser-specific, otherwise FALSE
    */
   public void setBrowserSpecific(boolean isSpecific) {
      _browserSpecific = isSpecific;
   }

   /**
    * Sets the CSS scheme to display.
    * @param name the scheme name
    * @see InsertCSSTag#getScheme()
    */
   public void setScheme(String name) {
      _scheme = name;
   }

   /**
    * Gets the scheme in use, or DEFAULT_SCHEME if none specified
    * @return the scheme name
    * @see InsertCSSTag#setScheme(String)
    */
   protected String getScheme() {
      return (_scheme == null) ? DEFAULT_SCHEME : _scheme;
   }

   /**
    * Loads the UI scheme name from the user object, if present.
    * @param ctxt the JSP page context
    */
   public final void setPageContext(PageContext ctxt) {
      super.setPageContext(ctxt);
      HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
      Principal user = req.getUserPrincipal();
      if (user instanceof Person) {
         Person p = (Person) user;
         setScheme(p.getUIScheme());
      }
   }

   /**
    * Renders the tag.
    * @return TagSupport.EVAL_PAGE
    * @throws JspException if an error occurs
    */
   public int doEndTag() throws JspException {

      // Check if the content has already been added
      if (ContentHelper.containsContent(pageContext, "CSS", _resourceName) && (!_forceInclude)) 
         return EVAL_PAGE;

      JspWriter out = pageContext.getOut();
      try {
         out.print("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"");
         out.print(SystemData.get("path.css"));
         out.print('/');
         out.print(getScheme());
         out.print('/');
         out.print(_resourceName);

         // Append browser-specific extension
         if (_browserSpecific) {
            if (isFirefox()) {
               out.print("_ff");
            } else if (isIE()) {
               out.print("_ie");
            }
         }

         out.print(".css\" />");
      } catch (Exception e) {
         throw new JspException(e);
      }

      // Mark the content as added and return
      ContentHelper.addContent(pageContext, "CSS", _resourceName);
      return EVAL_PAGE;
   }

   /**
    * Release's the tag's state.
    */
   public void release() {
      super.release();
      _scheme = null;
   }
}