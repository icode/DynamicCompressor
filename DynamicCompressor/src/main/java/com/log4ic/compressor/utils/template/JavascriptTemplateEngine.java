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

package com.log4ic.compressor.utils.template;

import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.javascript.jscomp.CompilationLevel;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.Compressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-09-11
 */
public class JavascriptTemplateEngine {
    public enum Mode {
        STRING,
        COMMON,
        AMD
    }


    public static String parse(String name, String source, Mode mode) {
        StringBuffer buffer = new StringBuffer();
        switch (mode) {
            case AMD:
                buffer.append("define('").append(name).append("',function(){return ");
                break;
            case COMMON:
                buffer.append(name).append("=");
                break;
            case STRING:
            default:
        }

        if (mode != Mode.STRING) {
            source = source.replaceAll("'", "\\\\'");
            buffer.append(source.replaceAll("(?m)^(\\s*)(.*?)\\s*$", "$1'$2'+"));
            buffer.delete(buffer.length() - 1, buffer.length());
        } else {
            buffer.append(source);
        }

        switch (mode) {
            case AMD:
                buffer.append("})");
                break;
            case COMMON:
            case STRING:
            default:
        }
        return buffer.toString();
    }

    public static String compress(String name, String source, Mode mode) {
        HtmlCompressor compressor = new HtmlCompressor();

        compressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
        compressor.setRemoveQuotes(true);              //removes unnecessary tag attribute quotes
        compressor.setSimpleDoctype(true);             //simplify existing doctype
        compressor.setRemoveScriptAttributes(true);    //remove optional attributes from script tags
        compressor.setRemoveStyleAttributes(true);     //remove optional attributes from style tags
        compressor.setRemoveLinkAttributes(true);      //remove optional attributes from link tags
        compressor.setRemoveFormAttributes(true);      //remove optional attributes from form tags
        compressor.setRemoveInputAttributes(true);     //remove optional attributes from input tags
        compressor.setSimpleBooleanAttributes(true);   //remove values from boolean tag attributes
        compressor.setRemoveJavaScriptProtocol(true);  //remove "javascript:" from inline event handlers
        compressor.setRemoveHttpProtocol(true);        //replace "http://" with "//" inside tag attributes
//        compressor.setRemoveHttpsProtocol(true);       //replace "https://" with "//" inside tag attributes
        compressor.setPreserveLineBreaks(false);        //preserves original line breaks
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.ALL_TAGS); //remove spaces around provided tags

        compressor.setCompressCss(true);               //compress inline css
        compressor.setCompressJavaScript(true);        //compress inline javascript

        //use Google Closure Compiler for javascript compression
        compressor.setJavaScriptCompressor(new ClosureJavaScriptCompressor(CompilationLevel.SIMPLE_OPTIMIZATIONS));

        //use your own implementation of css comressor
        compressor.setCssCompressor(new Compressor() {
            @Override
            public String compress(String source) {
                try {
                    return com.log4ic.compressor.utils.Compressor.compressGss(new SourceCode("inner-style", source));
                } catch (GssParserException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        String compressedHtml = compressor.compress(source);

        return parse(name, compressedHtml, mode);
    }
}
