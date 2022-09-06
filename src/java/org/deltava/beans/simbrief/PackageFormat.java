// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

import org.deltava.beans.EnumDescription;

/**
 * An enumeration of SimBrief package formats.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public enum PackageFormat implements EnumDescription {
	LIDO("LIDO"), AFR("Air France 2012"), AFR17("Air France 2017"), DAL("Delta Air Lines"), KLM("KLM Royal Dutch Airlines");
	
	private final String _desc;
	
	PackageFormat(String desc) {
		_desc = desc;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
}