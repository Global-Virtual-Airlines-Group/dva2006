// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.view;

import javax.servlet.jsp.JspException;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support database view tables.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TableTag extends org.deltava.taglib.html.TableTag {

   private String _viewCommandName;
   private int _viewSize;

   /**
    * Creates a new View Table tag.
    */
   public TableTag() {
      super();
      release();
   }

   /**
    * Sets the Web Site Command to execute when scrolling through the view.
    * @param name the command ID
    * @see TableTag#getCmd()
    */
   public void setCmd(String name) {
      _viewCommandName = name.toLowerCase();
   }

   /**
    * Sets the number of view rows to display.
    * @param size the number of rows
    * @see TableTag#size()
    */
   public void setSize(int size) {
      if (size > 0)
         _viewSize = size;
   }
   
   /**
    * Renders the tag to the JSP output stream. This ensures that state is released properly.
    * @return EVAL_PAGE
    * @throws JspException if an error occurs.
    */
   public int doEndTag() throws JspException {
      int result = super.doEndTag();
      release();
      return result;
   }
   
   /**
    * Releases the tag's state variables.
    */
   public void release() {
      super.release();
      _viewSize = SystemData.getInt("html.table.viewSize");
   }

   /**
    * Returns the view command ID (for unit tests).
    * @return the command ID
    * @see TableTag#setCmd(String)
    */
   protected String getCmd() {
      return _viewCommandName;
   }

   /**
    * Returns the view scroll window size (for unit tests)
    * @return the number of rows
    * @see TableTag#setSize(int)
    */
   protected int size() {
      return _viewSize;
   }
}