// Copyright 2019, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.awt.Rectangle;
import java.io.*;

import org.deltava.dao.DAOException;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripperByArea;

/**
 * A Data Access Object to extract text from a PDF document.
 * @author Luke
 * @version 10.2
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
		try (PDDocument doc = PDDocument.load(getStream(), MemoryUsageSetting.setupTempFileOnly())) {
			AccessPermission ap = doc.getCurrentAccessPermission();
			if (!ap.canExtractContent())
				throw new IOException("You do not have permission to extract text");

			// Left box is 11,50 top left, size is 282 x 675
			// right box is 300, 50 top left, size is 282 x 675
			StringBuilder buf = new StringBuilder();
			PDFTextStripperByArea stripper = new PDFTextStripperByArea();
			stripper.setSortByPosition(_sortByPosition);
			Rectangle lr = new Rectangle(11, 50, 282, 675);
			Rectangle rr = new Rectangle(300, 50, 282, 675);
			stripper.addRegion("lc", lr);
			stripper.addRegion("rc", rr);
			
			StringBuilder lb = new StringBuilder(); StringBuilder rb = new StringBuilder();
			int endPage = (_endPage <= 0) ? doc.getNumberOfPages() : _endPage;
			for (int p = _startPage; p < endPage; p++) {
				PDPage pg = doc.getPage(p);
				stripper.extractRegions(pg);
				String ltxt = stripper.getTextForRegion("lc");
				String rtxt = stripper.getTextForRegion("rc");
				boolean isHeader = ltxt.startsWith("FROM:") || rtxt.startsWith("FROM:");
				if (isHeader) {
					buf.append(lb);
					buf.append("\n\n");
					buf.append(rb);
					buf.append("\n\n");
					lb.setLength(0);
					rb.setLength(0);
				}
				
				lb.append(ltxt);
				rb.append(rtxt);
			}
			
			buf.append(lb);
			buf.append("\n\n");
			buf.append(rb);
			buf.append("\n\n");
			
			return buf.toString();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}