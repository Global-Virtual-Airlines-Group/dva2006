// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.simbrief;

/**
 * An enumeration of SimBrief package formats.
 * @author Luke
 * @version 10.4
 * @since 10.3
 */

public enum PackageFormat implements org.deltava.beans.EnumDescription {
	LIDO("LIDO"), AFR("Air France 2012", "AFR '12"), AFR2017("Air France 2017", "AFR '17"), DAL("Delta Air Lines"), KLM("KLM Royal Dutch Airlines");
	
	private final String _desc;
	private final String _alt;
	
	PackageFormat(String desc) {
		this(desc, null);
	}
	
	PackageFormat(String desc, String alt) {
		_desc = desc;
		_alt = alt;
	}
	
	/**
	 * Returns the alternate format code.
	 * @return the code
	 */
	public String getAlternate() {
		return _alt;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Parses a SimBrief package type name, including alternates.
	 * @param v the value to parse
	 * @param defaultValue the default value if unknown
	 * @return a PackageFormat
	 */
	public static PackageFormat parse(String v, PackageFormat defaultValue) {
		if (v == null) return defaultValue;
		for (PackageFormat pkg : values()) {
			if (v.equalsIgnoreCase(pkg.name()) || v.equalsIgnoreCase(pkg._alt))
				return pkg;
		}
		
		return defaultValue; 
	}
}