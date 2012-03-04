/*
 * Dynamic Compressor - Java Library
 * Copyright (c) 2011-2012, IntelligentCode ZhangLixin.
 * All rights reserved.
 * intelligentcodemail@gmail.com
 *
 * GUN GPL 3.0 License
 *
 * http://www.gnu.org/licenses/gpl.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.log4ic.compressor.servlet.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * JS CSS 压缩器ServletResponseWrapper，用于合并当前服务器内文件
 *
 * @author 张立鑫 IntelligentCode
 */
public class CompressionResponseWrapper extends HttpServletResponseWrapper {
    private PrintWriter tmpWriter;
    private ByteArrayOutputStream output;
    private ByteArrayServletOutputStream servletOutput;

    public CompressionResponseWrapper(HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
        output = new ByteArrayOutputStream();
        tmpWriter = new PrintWriter(output);
        servletOutput = new ByteArrayServletOutputStream(output);
    }

    public void finalize() throws Throwable {
        super.finalize();
        servletOutput.close();
        output.close();
        tmpWriter.close();
    }

    public String getContent() {
        try {
            return output.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "UnsupportedEncoding";
        }
    }

    public String getContent(String charsetName) {
        try {
            return output.toString(charsetName);
        } catch (UnsupportedEncodingException e) {
            return "UnsupportedEncoding";
        }
    }

    public PrintWriter getWriter() throws IOException {
        return tmpWriter;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return servletOutput;
    }

    public byte[] toByteArray() {
        return output.toByteArray();
    }

    public void flushBuffer() throws IOException {
        tmpWriter.flush();
        servletOutput.flush();
    }

    public void reset() {
        output.reset();
    }

    public void close() throws IOException {
        tmpWriter.close();
    }

    private static class ByteArrayServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream byteArrayOutputStream;

        public ByteArrayServletOutputStream(ByteArrayOutputStream baos) {
            this.byteArrayOutputStream = baos;
        }

        public void write(int i) throws IOException {
            byteArrayOutputStream.write(i);
        }
    }
}
