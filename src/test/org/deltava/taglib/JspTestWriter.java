package org.deltava.taglib;

import java.io.*;
import javax.servlet.jsp.JspWriter;

public class JspTestWriter extends JspWriter {
    
    private StringWriter _writer;

    public JspTestWriter() {
        super(8192, true);
        _writer = new StringWriter();
    }

    public void newLine() throws IOException {
        _writer.write(System.getProperty("line.separator"));
    }

    public void print(boolean arg0) throws IOException {
        _writer.write(String.valueOf(arg0));
    }

    public void print(char arg0) throws IOException {
        _writer.write(arg0);
    }

    public void print(int arg0) throws IOException {
        _writer.write(arg0);
    }

    public void print(long arg0) throws IOException {
        _writer.write(String.valueOf(arg0));
    }

    public void print(float arg0) throws IOException {
        _writer.write(String.valueOf(arg0));
    }

    public void print(double arg0) throws IOException {
        _writer.write(String.valueOf(arg0));
    }

    public void print(char[] arg0) throws IOException {
        _writer.write(arg0, 0, arg0.length);
    }

    public void print(String arg0) throws IOException {
        _writer.write(arg0);
    }

    public void print(Object arg0) throws IOException {
        _writer.write(arg0.toString());
    }

    public void println() throws IOException {
        newLine();
    }

    public void println(boolean arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(char arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(int arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(long arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(float arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(double arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(char[] arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(String arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void println(Object arg0) throws IOException {
        print(arg0);
        newLine();
    }

    public void clear() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void clearBuffer() throws IOException {
        _writer.getBuffer().setLength(0);
    }

    public void flush() throws IOException {
        _writer.flush();
    }

    public void close() throws IOException {
        _writer.close();
    }

    public int getRemaining() {
        throw new UnsupportedOperationException();
    }

    public void write(char[] arg0, int arg1, int arg2) throws IOException {
        _writer.write(arg0, arg1, arg2);
    }
    
    public String toString() {
        return _writer.toString();
    }
}