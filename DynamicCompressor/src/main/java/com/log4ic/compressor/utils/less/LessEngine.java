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

package com.log4ic.compressor.utils.less;

import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.GssParserException;
import com.log4ic.compressor.utils.less.exception.LessException;
import javolution.util.FastList;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-08-17
 */
public class LessEngine {
    private static final Logger logger = LoggerFactory.getLogger(LessEngine.class);

    private static Global global = new Global();
    private static final ContextFactory jsContextFactory = new ContextFactory();

    static {
        global.init(jsContextFactory);
        global.defineProperty("window", global, ScriptableObject.DONTENUM);

        final URL lessLib = LessEngine.class.getResource("/externs/less.js");
        InputStreamReader lessLibReader = null;
        try {
            lessLibReader = new InputStreamReader(lessLib.openStream());
        } catch (IOException e) {
            logger.error("read /externs/less.js error", e);
        }

        final URL lessParser = LessEngine.class.getResource("/externs/lessparser.js");
        InputStreamReader lessParserReader = null;
        try {
            lessParserReader = new InputStreamReader(lessParser.openStream());
        } catch (IOException e) {
            logger.error("read /externs/lessparser.js error", e);
        }

        final InputStreamReader finalLessLibReader = lessLibReader;
        final InputStreamReader finalLessParserReader = lessParserReader;
        jsContextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                cx.setLanguageVersion(Context.VERSION_1_8);
                cx.setOptimizationLevel(9);
                logger.debug("load file /externs/less.js");
                try {
                    cx.evaluateReader(global, finalLessLibReader, lessLib.getFile(), 1, null);
                } catch (IOException e) {
                    logger.error("evaluateReader /externs/less.js error", e);
                }
                logger.debug("load file /externs/lessparser.js");
                try {
                    cx.evaluateReader(global, finalLessParserReader, lessParser.getFile(), 1, null);
                } catch (IOException e) {
                    logger.error("evaluateReader /externs/lessparser.js error", e);
                }
                return null;
            }
        });
    }

    private static final NativeObject fnThisScopeObj = (NativeObject) global.get("dynamicCompressor", global);
    private static final Function parseFn = (Function) fnThisScopeObj.get("parseLess");

    private static void parseLessException(Exception root) throws LessException {
        if (root instanceof JavaScriptException) {
            Scriptable value = (Scriptable) ((JavaScriptException) root).getValue();
            String type = ScriptableObject.getProperty(value, "type").toString() + " Error";
            String message = ScriptableObject.getProperty(value, "message").toString();
            String filename = "";
            if (ScriptableObject.getProperty(value, "filename") != null) {
                filename = ScriptableObject.getProperty(value, "filename").toString();
            }
            int line = -1;
            if (ScriptableObject.getProperty(value, "line") != null) {
                line = ((Double) ScriptableObject.getProperty(value, "line")).intValue();
            }
            int column = -1;
            if (ScriptableObject.getProperty(value, "column") != null) {
                column = ((Double) ScriptableObject.getProperty(value, "column")).intValue();
            }
            List<String> extractList = new ArrayList<String>();
            if (ScriptableObject.getProperty(value, "extract") != null) {
                NativeArray extract = (NativeArray) ScriptableObject.getProperty(value, "extract");
                for (int i = 0; i < extract.getLength(); i++) {
                    if (extract.get(i, extract) instanceof String) {
                        extractList.add(((String) extract.get(i, extract)).replace("\t", " "));
                    }
                }
            }
            throw new LessException(message, type, filename, line, column, extractList);
        }
        throw new LessException(root);
    }

    /**
     * 解释less
     *
     * @param codeList
     * @param conditions
     * @return
     * @throws GssParserException
     */
    public static List<SourceCode> parseLess(List<SourceCode> codeList, List<String> conditions) throws LessException {
        final List<SourceCode> resultCodeList = new FastList<SourceCode>();
        StringBuilder conditionsBuilder = new StringBuilder();
        if (conditions != null) {
            for (String con : conditions) {
                conditionsBuilder.append("@").append(con).append(":true;");
            }
        }
        for (final SourceCode sourceCode : codeList) {
            if (!sourceCode.getFileName().endsWith(".less") && !sourceCode.getFileName().endsWith(".mss")) {
                resultCodeList.add(new SourceCode(sourceCode.getFileName(), sourceCode.getFileContents()));
                continue;
            }
            final Object[] functionArgs = new Object[]{conditionsBuilder.toString() + sourceCode.getFileContents()};
            try {
                jsContextFactory.call(new ContextAction() {
                    @Override
                    public Object run(Context cx) {
                        cx.setLanguageVersion(Context.VERSION_1_8);
                        Object result = parseFn.call(cx, global, fnThisScopeObj, functionArgs);
                        resultCodeList.add(new SourceCode(sourceCode.getFileName(), Context.toString(result)));
                        return null;
                    }
                });
            } catch (Exception e) {
                parseLessException(e);
            }
        }
        return resultCodeList;
    }
}
