// Copyright 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import org.deltava.beans.ImageBean;

/**
 * A JSP tag to display database-served images. 
 * @author Luke
 * @version 12.0
 * @since 10.6
 */

public class ImageBeanTag extends ImageTag {
	
	private ImageBean _img;
	private String _airlineCode;

	/**
	 * Sets the ImageBean to display.
	 * @param i the ImageBean
	 */
	public void setImg(ImageBean i) {
		_img = i;
		super.setX(_img.getWidth());
		super.setY(_img.getHeight());
	}
	
	/**
	 * Updates the airline code for this Image.
	 * @param aCode the airline code
	 */
	public void setAirline(String aCode) {
		_airlineCode = aCode;
	}
	
	@Override
	public void setX(int x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setY(int y) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setSrc(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int doStartTag() {
		
		StringBuilder buf = new StringBuilder("dbimg/");
		buf.append(_img.getImageType().getURLPart());
		buf.append('/');
		if (_airlineCode != null) {
			buf.append(_airlineCode.toLowerCase());
			buf.append('/');
		}
		
		buf.append(_img.getHexID());
		buf.append('.');
		buf.append(_img.getFormat().name().toLowerCase());
		_data.setAttribute("src", buf.toString());
		return SKIP_BODY;
	}
	
	@Override
	public void release() {
		super.release();
		_airlineCode = null;
	}
}