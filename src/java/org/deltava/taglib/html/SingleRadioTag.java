// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.Collection;

/**
 * A JSP tag to support the generation of HTML single-option radio buttons.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SingleRadioTag extends CheckTag {

	/**
	 * Initializes the tag.
	 */
	public SingleRadioTag() {
		super();
		setType("radio");
	}

	/**
	 * Sets the &lt;CHECK&gt; element type. <i>NOT SUPPORTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final void setType(String checkType) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Sets the radio button options. <i>NOT SUPPORTED</i>
	 * @throws UnsupportedOperationException always
	 * @see SingleRadioTag#setOption(Object)
	 */
	public final void setOptions(Collection opts) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Sets the radio button columns. <i>NOT SUPPORTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final void setCols(int columns) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Marks the radio button as being selected.
	 * @param isChecked TRUE if selected, otherwise FALSE
	 * @see FormElementTag#setValue(Object)
	 */
	public void setChecked(boolean isChecked) {
		super.setValue(isChecked ? _firstEntry : null);
	}
	
	/**
	 * Sets the radio button option.
	 * @param opt the option
	 * @see CheckTag#setFirstEntry(Object)
	 */
	public void setOption(Object opt) {
		setFirstEntry(opt);
	}
	
	/**
	 * Releases the tag's state data.
	 */
	public void release() {
		super.release();
		setType("radio");
	}
}