// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.acars.Capabilities;

/**
 * A JSP tag to display aircraft/simulator Capabilities bitmaps.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class CapabilitiesFormatTag extends TagSupport {

	private Collection<Capabilities> _caps = new TreeSet<Capabilities>();
	
	/**
	 * Sets the aircraft/simulator capabilities bitmap.
	 * @param capMap the bitmap
	 */
	public void setMap(long capMap) {
		for (Capabilities c : Capabilities.values()) {
			if (c.has(capMap) && c.isVisible())
				_caps.add(c);
		}
	}
	
	/**
	 * Prints the list of visible capabilities to the JSP output stream
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			for (Iterator<Capabilities> i = _caps.iterator(); i.hasNext(); ) {
				Capabilities c = i.next();
				out.print(c.getDescription());
				if (i.hasNext())
					out.print(' ');
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		_caps.clear();
	}
}