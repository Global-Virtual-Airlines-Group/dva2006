// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

/**
 * A bean to store cloud layer data.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class CloudLayer implements Comparable<CloudLayer> {
	
	/**
	 * An enumeration to store cloud types.
	 */
	public enum Type {

		C("cumulus"), CB("cumulonimbus"), TCU("towering cumulus");
		
		private String _type;

		Type(String type) {
			_type = type;
		}
		
		/**
		 * Returns the cloud type name.
		 * @return the type name
		 */
		public String getType() {
			return _type;
		}
	}
	
	/**
	 * An enumeration to store cloud thickness.
	 */
	public enum Amount {
		
		SKC("clear"), CLR("clear"), FEW("few"), SCT("scattered"), BKN("broken"), 
		OVC("overcast"), VV("vertical viz");
		
		private String _amt;
		
		Amount(String amt) {
			_amt = amt;
		}
		
		/**
		 * Returns the cloud thickness.
		 * @return the thickness
		 */
		public String getAmount() {
			return _amt;
		}
	}
	
	private Type _type = Type.C;
	private Amount _amt;
	private int _height;

	/**
	 * Initializes the bean.
	 * @param height the height of the cloud layer, in feet MSL
	 */
	public CloudLayer(int height) {
		super();
		_height = height;
	}
	
	/**
	 * Returns the height of the cloud layer.
	 * @return the height in feet MSL
	 */
	public int getHeight() {
		return _height;
	}
	
	/**
	 * Returns the cloud type.
	 * @return the type
	 */
	public Type getType() {
		return _type;
	}
	
	/**
	 * Returns the cloud layer thickness.
	 * @return the thickness
	 */
	public Amount getThickness() {
		return _amt;
	}
	
	/**
	 * Sets the cloud type.
	 * @param ct the cloud type
	 */
	public void setType(Type ct) {
		_type = ct;
	}
	
	/**
	 * Sets the cloud layer thickness.
	 * @param amt the thickness
	 */
	public void setThickness(Amount amt) {
		_amt = amt;
	}

	/**
	 * Compares two cloud layers by comparing their heights.
	 */
	public int compareTo(CloudLayer cl2) {
		return new Integer(_height).compareTo(new Integer(cl2._height));
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(_amt.toString());
		buf.append(_height / 100);
		if (_type != Type.C)
			buf.append(_type.toString());
		
		return buf.toString();
	}
}