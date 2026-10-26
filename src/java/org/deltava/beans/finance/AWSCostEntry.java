// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.finance;

import java.time.Instant;

/**
 * A bean to tractk AWS cost entries by service and date. 
 * @author Luke
 * @version 13.1
 * @since 13.1	
 */

public class AWSCostEntry extends CostEntry {
	
	private String _svcName;

	/**
	 * Creates the bean. 
	 * @param dt the date incurred
	 * @param amt the amount
	 */
	public AWSCostEntry(Instant dt, int amt) {
		super("AWS", dt, amt);
	}
	
	/**
	 * Returns the AWS service name.
	 * @return the service name
	 */
	public String getService() {
		return _svcName;
	}
	
	/**
	 * Updates the AWS service name.
	 * @param svc the service name
	 */
	public void setService(String svc) {
		_svcName = svc;
	}
	
	@Override
	public int compareTo(CostEntry c2) {
		int tmpResult = getDate().compareTo(c2.getDate());
		if (tmpResult == 0) tmpResult = getCategory().compareTo(c2.getCategory());
		if ((tmpResult == 0) && (c2 instanceof AWSCostEntry ac2)) tmpResult = _svcName.compareTo(ac2._svcName);
		return (tmpResult == 0) ? Integer.compare(getAmount(), c2.getAmount()) : tmpResult;
	}
}