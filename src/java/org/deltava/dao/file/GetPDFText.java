// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.deltava.dao.DAOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * A Data Access Object to extract text from a PDF document.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class GetPDFText extends DAO {
	
	private boolean _sortByPosition;
	private int _startPage;
	private int _endPage; 

	/**
	 * Initializes the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetPDFText(InputStream is) {
		super(is);
	}
	
	/**
	 * Updates the first page to extract.
	 * @param page the page number
	 */
	public void setStartPage(int page) {
		_startPage = page;
	}
	
	/**
	 * Updates the last page to extract.
	 * @param page the page number
	 */
	public void setEndPage(int page) {
		_endPage = page;
	}
	
	public void setSortByPosition(boolean sbp) {
		_sortByPosition = sbp;
	}

	/**
	 * Extracts the PDF document text.
	 * @return the PDF text
	 * @throws DAOException if an I/O error occurs
	 */
	public String getText() throws DAOException {
		try (PDDocument doc = PDDocument.load(getStream())) {
			AccessPermission ap = doc.getCurrentAccessPermission();
			if (!ap.canExtractContent())
				throw new IOException("You do not have permission to extract text");

			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(_sortByPosition);
			if (_startPage > 0)
				stripper.setStartPage(_startPage);
			if (_endPage > 0)
				stripper.setEndPage(_endPage);
			
			return stripper.getText(doc);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}