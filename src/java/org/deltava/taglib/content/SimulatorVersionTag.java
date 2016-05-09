// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Simulator;

/**
 * A JSP tag to display Simulator service pack information.  
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class SimulatorVersionTag extends TagSupport {
	
	private static final String[] FSX_NAMES = new String[] { "RTM", "SP1", "SP2", "Acceleration", "Steam Edition"};

	private Simulator _sim;
	private int _major;
	private int _minor;
	
	public void setSim(Simulator sim) {
		_sim = sim;
	}
	
	public void setMajor(int major) {
		_major = major;
	}
	
	public void setMinor(int minor) {
		_minor = minor;
	}
	
	/**
	 * Prints a human-readable simulator version to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs 
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			switch (_sim) {
			case FSX:
				if ((_major == 10) && (_minor > 0) && (_minor <= 5))
					out.print(FSX_NAMES[_minor - 1]);
				break;
				
			case FS9:
				if (_minor == 1) out.print(" SP1");
				break;
				
			case P3D:
			case XP9:
			case XP10:
				out.print(_major);
				out.print('.');
				out.print(_minor);
				break;
			
			default:
				break;
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}