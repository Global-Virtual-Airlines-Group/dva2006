// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

/**
 * An enumeration to store weather conditions.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class Condition {
	
	private Type _type;
	private Qualifier _qual = Qualifier.NONE;
	private Intensity _int = Intensity.NONE;
	
	/**
	 * An enumeration for weather condition types. 
	 */
	public enum Type {
		
		DZ("drizzle"), RA("rain"), SN("snow"), SG("snow grains"), IC("ice crystals"), PL("ice pellets"), 
		GR("hail"), GS("snow pellets"), UP("unknown"), BR("mist"), FU("smoke"), VA("volcanic ash"),
		SA("sand"), HZ("haze"), PY("spray"), DU("dust"), SQ("squall"), SS("sandstorm"), DS("duststorm"),
		PO("dust whirls"), FC("funnel cloud"), NONE("");

		private String _name;

		Type(String name) {
			_name = name;
		}

		/**
		 * Returns the type name.
		 * @return the name
		 */
		public String getName() {
			return _name;
		}
	}
	
	/**
	 * An enumeration for weather condition qualifiers. 
	 */
	public enum Qualifier {
		
		MI("shallow"), BC("patchy"), SH("showers"), PR("partial"), TS("thunderstorm"), BL("blowing"),
		DR("drifting"), FZ("freezing"), NONE("");
		
		private String _name;
		
		Qualifier(String name) {
			_name = name;
		}
		
		/**
		 * Returns the qualifier name.
		 * @return the name
		 */
		public String getName() {
			return _name;
		}
	}

	/**
	 * An enumeration for weather condition intensity/proximity.
	 */
	public enum Intensity {
		HEAVY, LIGHT, VICINITY, NONE;
	}
	
	/**
	 * Checks if a code is a valid type or qualifier.
	 * @param code the code
	 * @return TRUE if it is a valid condition type or qualifier, otherwise FALSE
	 */
	public static boolean isValid(String code) {
		if (code == null) return false;
		try {
			Type.valueOf(code.toUpperCase());
			return true;
		} catch (IllegalArgumentException iae) {
			try {
				Qualifier.valueOf(code.toUpperCase());
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	/**
	 * Creates the weather condition bean.
	 * @param t the Type
	 */
	public Condition(Type t) {
		super();
		_type = t;
	}
	
	/**
	 * Returns the condition type.
	 * @return the type
	 */
	public Type getType() {
		return _type;
	}
	
	/**
	 * Returns the condition qualifier.
	 * @return the qualifier
	 */
	public Qualifier getQualifier() {
		return _qual;
	}
	
	/**
	 * Returns the condition intensity/proximity.
	 * @return the intensity
	 */
	public Intensity getIntensity() {
		return _int;
	}
	
	/**
	 * Sets the condition qualifier.
	 * @param q the qualifier
	 */
	public void setQualifier(Qualifier q) {
		_qual = q;
	}
	
	/**
	 * Sets the condition's intensity/proximity.
	 * @param i the intensity
	 */
	public void setIntensity(Intensity i) {
		_int = i;
	}
}