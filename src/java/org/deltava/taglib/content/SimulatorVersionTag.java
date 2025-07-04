// Copyright 2016, 2017, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Simulator;

/**
 * A JSP tag to display Simulator service pack information.  
 * @author Luke
 * @version 10.3
 * @since 7.0
 */

public class SimulatorVersionTag extends TagSupport {
	
	private static final String[] FSX_NAMES = new String[] { "RTM", "SP1", "SP2", "Acceleration", "Steam Edition"};

	private Simulator _sim;
	private int _major;
	private int _minor;
	
	/**
	 * Sets the Simulator used.
	 * @param sim the Simulator
	 */
	public void setSim(Simulator sim) {
		_sim = sim;
	}
	
	/**
	 * Sets the simulator major version number.
	 * @param major the major version
	 */
	public void setMajor(int major) {
		_major = major;
	}
	
	/**
	 * Sets the simulator minor version number.
	 * @param minor the minor version
	 */
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
				if (_minor == 1) out.print("SP1");
				break;
				
			case XP10:
			case XP11:
			case XP12:
				out.print(_major);
				out.print('.');
				if (_minor < 10)
					out.print('0');

				out.print(_minor);
				break;
			
			default:
				out.print(_major);
				out.print('.');
				out.print(_minor);
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