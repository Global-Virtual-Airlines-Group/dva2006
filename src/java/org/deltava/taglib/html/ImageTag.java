// Copyright 2005, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * An JSP Tag to generate an IMG element.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class ImageTag extends ElementTag {

	private int _w;
	private int _h;

	/**
	 * Creates a new Image element tag.
	 */
	public ImageTag() {
		super("img");
	}

	/**
	 * Generates this image's HTML.
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			_out.print(_data.open(true, true));
			StringBuilder buf = new StringBuilder(_data.get("src"));
			ContentHelper.pushContent(pageContext, buf.insert(0, '/').toString(), "image");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}

	/**
	 * Sets the width of ths image. This does nothing if a negative, zero or non-numeric value is passed.
	 * @param width the width of the image in pixels
	 */
	public void setX(int width) {
		_w = Math.max(0, width);
	}

	/**
	 * Sets the height of ths image. This does nothing if a negative, zero or non-numeric value is passed.
	 * @param height the height of the image in pixels
	 */
	public void setY(int height) {
		_h = Math.max(0, height);
	}

	/**
	 * Sets the source of this image.
	 * @param url the location of the image
	 */
	public void setSrc(String url) {
		StringBuilder buf = new StringBuilder(SystemData.get("path.img"));
		buf.append('/').append(url);
		_data.setAttribute("src", buf.toString());
	}

	/**
	 * Sets the alternate caption for this image.
	 * @param caption the caption for this mage
	 */
	public void setCaption(String caption) {
		_data.setAttribute("title", caption);
	}

	/**
	 * Resets the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_h = 0;
		_w = 0;
	}

	/**
	 * Executed post tag setup. Creates a STYLE element if dimensions specified.
	 * @return SKIP_BODY always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		// Set style if image size defined
		if ((_w > 0) || (_h > 0)) {
			StringBuilder buf = new StringBuilder();
			if (_data.has("style")) {
				String s = _data.get("style");
				buf.append(s);
				if (!s.endsWith(";"))
					buf.append(';');
			}

			if (_w > 0) {
				buf.append("width:");
				buf.append(_w);
				buf.append("px;");
			}

			if (_h > 0) {
				buf.append("height:");
				buf.append(_h);
				buf.append("px;");
			}

			_data.setAttribute("style", buf.toString());
		}

		return SKIP_BODY;
	}
}