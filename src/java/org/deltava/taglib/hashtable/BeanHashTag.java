package org.deltava.taglib.hashtable;

import java.util.Iterator;
import java.util.Set;

import javax.servlet.jsp.*;

import org.deltava.beans.ComboAlias;
import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to store bean OPTION data in a JavaScript hashtable of arrays.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

 public class BeanHashTag extends AbstractHashTag {

 	public int doStartTag() throws JspException {
 		
		// Check if we've included the airportRefresh JavaScript file
		if (!ContentHelper.containsContent(pageContext, "JS", "comboRefresh"))
			throw new IllegalStateException("comboRefresh.js must have been included");
		
		// Split the items into the arrays
		return super.doStartTag();
 	}
 	
 	public int doEndTag() throws JspException {
 	
 		JspWriter out = pageContext.getOut();
 		try {
 			openScriptBlock(out);
 			
			// Loop through the hashtable keys
			for (Iterator i = _sets.keySet().iterator(); i.hasNext(); ) {
				String keyCode = (String) i.next();
				Set arrayValues = (Set) _sets.get(keyCode);
				
				// Write the array definition
				renderArrayStart(out, keyCode, arrayValues.size());
				
				// Get all the values in the set, dump them to the output stream as OPTION elements
				int idx = 0;
				for (Iterator i2 = arrayValues.iterator(); i2.hasNext(); ) {
					Object entry = i2.next();
					out.print(ARRAY_NAME + "[" + idx + "] = new Option(\'");

					// Render ComboAlias elements properly
					if (entry instanceof ComboAlias) {
						ComboAlias ca = (ComboAlias) entry;
						out.print(ca.getComboName() + "\', \'" + ca.getComboAlias() + "\');");						
					} else {
						out.print(String.valueOf(entry) + "\');");
					}
				}
			}
 			
 			closeScriptBlock(out);
		} catch (Throwable t) {
	        throw new JspException("Error writing " + getClass().getName(), t);
 		}
		
		return EVAL_PAGE;
 	}
}