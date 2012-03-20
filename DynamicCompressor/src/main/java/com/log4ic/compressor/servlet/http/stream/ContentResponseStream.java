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

package com.log4ic.compressor.servlet.http.stream;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-20
 */
public class ContentResponseStream extends ServletOutputStream {

    private ByteArrayOutputStream stream = new ByteArrayOutputStream();


    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    public String getContent() throws UnsupportedEncodingException {
        return this.getContent("UTF-8");
    }

    public String getContent(String charsetName) throws UnsupportedEncodingException {
        return stream == null ? null : stream.toString(charsetName);
    }

    public void reset(){
        stream.reset();
    }
}
