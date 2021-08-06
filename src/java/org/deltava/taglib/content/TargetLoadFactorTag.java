// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.time.Instant;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.econ.*;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert the day's target load factor.
 * @author Luke
 * @version 10.1
 * @since 10.1
 */

public class TargetLoadFactorTag extends TagSupport {
	
	private String _varName;
	
	/**
	 * Sets the JSP attribute name to store the target load factor in.
	 * @param varName the attribute name
	 */
	public void setVar(String varName) {
		_varName = varName;
	}

	@Override
	public int doStartTag() {
		
		EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
		if (eInfo != null) {
			LoadFactor lf = new LoadFactor(eInfo);
			double targetLoad = lf.getTargetLoad(Instant.now());
			pageContext.setAttribute(_varName, Double.valueOf(targetLoad), PageContext.REQUEST_SCOPE);
		}
		
		return SKIP_BODY;
	}
}