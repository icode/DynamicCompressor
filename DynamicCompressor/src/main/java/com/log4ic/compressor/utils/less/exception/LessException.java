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

package com.log4ic.compressor.utils.less.exception;

import java.util.List;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-08-17
 */
public class LessException extends Exception {

    private static final long serialVersionUID = 662552833197468936L;

    private String type;
    private String filename;
    private int line;
    private int column;
    private List<String> extract;

    public LessException() {
        super();
    }

    public LessException(String message) {
        super(message);
    }

    public LessException(String message, Throwable e) {
        super(message, e);
    }

    public LessException(String message, String errorType, String filename, int line, int column, List<String> extract) {
        super(message);
        this.type = errorType != null ? errorType : "LESS Error";
        this.filename = filename;
        this.line = line;
        this.column = column;
        this.extract = extract;
    }

    public LessException(Throwable e) {
        super(e);
    }

    @Override
    public String getMessage() {
        if (type != null) {
            String msg = String.format("%s: %s (line %s, column %s)", type, super.getMessage(), line, column);
            if (!(extract == null) && !extract.isEmpty()) {
                msg += " near";
                for (String l : extract) {
                    msg += "\n" + l;
                }
            }
            return msg;
        }

        return super.getMessage();
    }


    public String getType() {
        return type;
    }

    public String getFilename() {
        return filename;
    }

    public int getLine() {
        return line;
    }


    public int getColumn() {
        return column;
    }


    public List<String> getExtract() {
        return extract;
    }

}