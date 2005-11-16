// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * An HTTP servlet response wrapper to support GZIP compression of an output stream.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GZIPResponseWrapper extends HttpServletResponseWrapper {

	private PrintWriter _pw;
	private ServletOutputStream _out;
	
	class GZOutputStream extends ServletOutputStream {

		private ByteArrayOutputStream _buf;
		private GZIPOutputStream _gz;
		private OutputStream _raw;

		GZOutputStream(OutputStream out) throws IOException {
			super();
			_raw = out;
			_buf = new ByteArrayOutputStream(4096);
			_gz = new GZIPOutputStream(_buf);
		}

		public void write(int i) throws IOException {
			_gz.write(i);
		}

		public void write(byte b[]) throws IOException {
			_gz.write(b, 0, b.length);
		}

		public void write(byte b[], int ofs, int len) throws IOException {
			_gz.write(b, ofs, len);
		}

		public void flush() throws IOException {
			_gz.flush();	
		}

		public void close() throws IOException {
			_raw.write(_buf.toByteArray());
			_raw.flush();
			_raw.close();
		}
	}

	/**
	 * Creates a new response wrapper.
	 * @param rsp the original HTTP servlet response
	 */
	public GZIPResponseWrapper(HttpServletResponse rsp) throws IOException {
		super(rsp);
		rsp.setHeader("Content-Encoding", "gzip");
		_out = new GZOutputStream(rsp.getOutputStream());
		_pw = new PrintWriter(_out);
	}

	public PrintWriter getWriter() {
		return _pw;
	}
	
	public ServletOutputStream getOutputStream() {
		return _out;
	}
	
	public void flushBuffer() throws IOException {
		_out.flush();
	}
}