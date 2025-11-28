package org.gvagroup.mockservlet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.*;
import java.util.*;

public class MockHttpServletResponse implements HttpServletResponse {

    private int status = 200;
    private final Map<String, List<String>> headers = new HashMap<>();
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));
    private final List<Cookie> cookies = new ArrayList<>();
    private String characterEncoding = "UTF-8";
    private String contentType;

    public String getBodyAsString() {
        writer.flush();
        try {
            return outputStream.toString(characterEncoding);
        } catch (UnsupportedEncodingException e) {
            return outputStream.toString();
        }
    }

    @Override public void addCookie(Cookie cookie) { cookies.add(cookie); }
    public List<Cookie> getCookiesList() { return cookies; }

    @Override public boolean containsHeader(String name) { return headers.containsKey(name.toLowerCase(Locale.ROOT)); }

    @Override public String encodeURL(String url) { return url; }

    @Override public String encodeRedirectURL(String url) { return url; }

    @Override public void sendError(int sc, String msg) { this.status = sc; }

    @Override public void sendError(int sc) { this.status = sc; }

    @Override public void sendRedirect(String location) { setHeader("Location", location); }

    @Override public void setDateHeader(String name, long date) { setHeader(name, Long.toString(date)); }

    @Override public void addDateHeader(String name, long date) { addHeader(name, Long.toString(date)); }

    @Override public void setHeader(String name, String value) {
        headers.put(name.toLowerCase(Locale.ROOT), new ArrayList<>(List.of(value)));
    }

    @Override public void addHeader(String name, String value) {
        headers.computeIfAbsent(name.toLowerCase(Locale.ROOT), _ -> new ArrayList<>()).add(value);
    }

    @Override public void setIntHeader(String name, int value) { setHeader(name, Integer.toString(value)); }

    @Override public void addIntHeader(String name, int value) { addHeader(name, Integer.toString(value)); }

    @Override public void setStatus(int sc) { this.status = sc; }

    @Override public int getStatus() { return status; }

    @Override public String getHeader(String name) {
        List<String> l = headers.get(name.toLowerCase(Locale.ROOT));
        return (l == null || l.isEmpty()) ? null : l.get(0);
    }

    @Override public Collection<String> getHeaders(String name) {
        List<String> l = headers.get(name.toLowerCase(Locale.ROOT));
        return (l == null) ? Collections.emptyList() : Collections.unmodifiableCollection(l);
    }

    @Override public Collection<String> getHeaderNames() { return Collections.unmodifiableSet(headers.keySet()); }

    @Override public String getCharacterEncoding() { return characterEncoding; }

    @Override public String getContentType() { return contentType; }

    @Override public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) { /* no-op */ }
            @Override public void write(int b) { outputStream.write(b); }
        };
    }

    @Override public PrintWriter getWriter() { return writer; }

    @Override public void setCharacterEncoding(String charset) { this.characterEncoding = charset; }

    @Override public void setContentLength(int len) { setIntHeader("Content-Length", len); }

    @Override public void setContentLengthLong(long len) { setHeader("Content-Length", Long.toString(len)); }

    @Override public void setContentType(String type) { this.contentType = type; }

    @Override public void setBufferSize(int size) { /* no-op */ }

    @Override public int getBufferSize() { return 0; }

    @Override public void flushBuffer() throws IOException { writer.flush(); }

    @Override public void resetBuffer() { outputStream.reset(); }

    @Override public boolean isCommitted() { return false; }

    @Override public void reset() { outputStream.reset(); headers.clear(); status = 200; }

    @Override public void setLocale(Locale loc) { /* no-op */ }

    @Override public Locale getLocale() { return Locale.getDefault(); }

	@Override
	public void sendRedirect(String arg0, int arg1, boolean arg2) throws IOException { /* empty */ }
}
