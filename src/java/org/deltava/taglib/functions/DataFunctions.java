// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.*;

/**
 * A JSP function library to perform bean operations. 
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class DataFunctions {

	/**
	 * Returns if the bean is a DatabaseBean.
	 * @param o the object
	 * @return TRUE if o implements DatabaseBean, otherwise FALSE
	 * @see DatabaseBean
	 */
	public static boolean isDatabaseBean(Object o) {
		return (o instanceof DatabaseBean);
	}
	
	/**
	 * Returns if the bean is an AuthoredBean.
	 * @param o the object
	 * @return TRUE if o implements AuthoredBean, otherwise FALSE
	 * @see AuthoredBean
	 */
	public static boolean isAuthoredBean(Object o) {
		return (o instanceof AuthoredBean);
	}
}