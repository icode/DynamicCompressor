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

import com.log4ic.compressor.servlet.http.stream.ContentResponseStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * JS CSS 压缩器ServletResponseWrapper，用于合并当前服务器内文件
 *
 * @author 张立鑫 IntelligentCode
 */
public class ContentResponseWrapper extends HttpServletResponseWrapper {
    private PrintWriter printWriter;
    private ContentResponseStream servletOutput;

    public ContentResponseWrapper(HttpServletResponse httpServletResponse) throws IOException {
        super(httpServletResponse);
        this.servletOutput = new ContentResponseStream();
        this.printWriter = new PrintWriter(servletOutput);
    }

    public void finalize() throws Throwable {
        super.finalize();
        servletOutput.close();
        printWriter.close();
    }

    public String getContent() throws UnsupportedEncodingException {
        return servletOutput.getContent();
    }

    public String getContent(String charsetName) throws UnsupportedEncodingException {
        return servletOutput.getContent(charsetName);
    }

    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return servletOutput;
    }

    public void flushBuffer() throws IOException {
        printWriter.flush();
        servletOutput.flush();
    }

    public void reset() {
        super.reset();
        servletOutput.reset();
    }

    public void close() throws IOException {
        printWriter.close();
        servletOutput.close();
    }

}
