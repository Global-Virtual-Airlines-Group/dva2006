// Copyright 2004, 2005, 2006, 2012, 2015, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;

import org.deltava.util.ImageInfo;

/**
 * An abstract bean class to store common image code.
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public abstract class ImageBean extends DatabaseBlobBean {
	
	/**
	 * Supported image types.
	 */
	public enum ImageFormat {
		JPG, GIF, PNG, BMP
	}

    private int _imgSize;
    private int _imgX;
    private int _imgY;
    private ImageFormat _fmt;

    /**
     * Returns the type of image. This uses constants found in ImageInfo.
     * @return the image type code
     * @see ImageInfo#getFormat()
     * @see ImageBean#setFormat(ImageFormat)
     * @see ImageBean#load(InputStream)
     */
    public ImageFormat getFormat() {
        return _fmt;
    }
    
    /**
     * Returns the database image type.
     * @return an ImageType
     */
    public abstract ImageType getImageType();
    
    /**
     * Returns the width of the image.
     * @return the width of the image, in pixels
     * @see ImageBean#setWidth(int)
     * @see ImageBean#load(InputStream)
     */
    public int getWidth() {
        return _imgX;
    }
    
    /**
     * Returns the height of the image.
     * @return the height of the image, in pixels
     * @see ImageBean#setHeight(int)
     * @see ImageBean#load(InputStream)
     */
    public int getHeight() {
        return _imgY;
    }

    /**
     * Returns the size of the image.
     * @return the size of the image, in bytes
     * @see ImageBean#setSize(int)
     * @see ImageBean#load(InputStream)
     */
    @Override
    public int getSize() {
        return (_buffer == null) ? _imgSize : super.getSize();
    }
    
    /**
     * Helper method to read image metadata.
     */
    private void getImageData() {
       _imgSize = getSize();
       
       // Get the image dimensions and type. If it's not valid, throw an exception
       // and invalidate the buffer
       ImageInfo imgInfo = new ImageInfo(_buffer);
       if (!imgInfo.check()) {
           _buffer = null;
           throw new UnsupportedOperationException("Unknown Image Format");
       }

       _fmt = ImageFormat.values()[imgInfo.getFormat()];
       _imgX = imgInfo.getWidth();
       _imgY = imgInfo.getHeight();
    }
    
	/**
	 * Queries if the bean has an associated Image.
	 * @return TRUE if the bean has an image, otherwise FALSE
	 */
	public boolean getHasImage() {
		return (_fmt != null);
	}
    
    /**
     * Loads an image into the buffer. The image data should be available at the specified input stream, and the stream
     * is <u>not</u> closed when this method completes. This method populates the size, X, Y and type properties
     * if the stream is loaded successfully.
     * @param is the stream containing the image data
     * @throws IOException if an error occurs loading the data
     * @throws UnsupportedOperationException if ImageInfo cannot identify the image format
     */
    @Override
    public final void load(InputStream is) throws IOException {
        super.load(is);
        getImageData();
    }
    
    /**
     * Updates the image buffer. This method populates the size, X, Y and type properties.
     * @param buffer an array containing the image data
     * @throws UnsupportedOperationException if ImageInfo cannot identify the image format
     */
    @Override
    public final void load(byte[] buffer) {
       super.load(buffer);
       getImageData();
    }
    
    /**
     * Helper method to check for negative numeric parameters.
     * @param param the parameter value
     * @param msg the field description
     * @throws IllegalArgumentException if param is negative 
     */
    protected static void checkParam(int param, String msg) throws IllegalArgumentException {
        if (param < 0)
            throw new IllegalArgumentException(msg + " cannot be negative");
    }

    /**
     * Updates the width of this image. Note that this method does not change the image date (ie. resize the image)
     * @param x the new width of the image, in pixels
     * @throws IllegalArgumentException if the image width has already been set
     * @see ImageBean#getWidth()
     * @see ImageBean#load(InputStream)
     */
    public void setWidth(int x) {
        checkParam(x, "Image Width");
        _imgX = x;
    }
    
    /**
     * Updates the height of this image. Note that this method does not change the image date (ie. resize the image)
     * @param y the new height of the image, in pixels
     * @throws IllegalArgumentException if the image height has already been set
     * @see ImageBean#getHeight()
     * @see ImageBean#load(InputStream)
     */
    public void setHeight(int y) {
        checkParam(y, "Image Height");
        _imgY = y;
    }
    
    /**
     * Updates the size of this image.
     * @param newSize the size of the image, in bytes
     * @throws IllegalStateException if the size of the image has already been set
     * @throws IllegalArgumentException if the image size is negative
     * @see ImageBean#getSize()
     */
    public void setSize(int newSize) {
        if (_imgSize != 0)
            throw new IllegalStateException("Image Size already set");
        
        checkParam(newSize, "Image Size");
        _imgSize = newSize;
    }
    
    /**
     * Updates the image format. Note that this method does not change the image data (ie. convert formats).
     * @param t an ImageFormat
     * @throws IllegalStateException if the image type has already been set
     * @see ImageBean#getFormat()
     * @see ImageBean#load(InputStream)
     */
    public void setFormat(ImageFormat t) {
        if (_fmt != null)
            throw new IllegalStateException("Image Type already set");
        
        _fmt = t;
    }
}