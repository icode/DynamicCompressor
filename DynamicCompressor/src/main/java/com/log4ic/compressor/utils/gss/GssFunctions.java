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

package com.log4ic.compressor.utils.gss;

import com.google.common.collect.ImmutableMap;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;

import java.util.List;
import java.util.Map;

/**
 * 浏览器探测器
 *
 * @author 张立鑫 IntelligentCode
 */
public class GssFunctions {
    public static Map<String, GssFunction> getFunctionMap() {
        return ImmutableMap.<String, GssFunction>builder()
//                .put("getBrowserType", new GetBrowserType())
//                .put("getBrowserVersion", new GetBrowserVersion())
//                .put("getPlatform", new GetPlatform())
//                .put("import", new ImportStyleSheets())
                .build();
    }

    /**
     * 用于获取浏览器类型
     */
    private static class GetBrowserType implements GssFunction {
        @Override
        public Integer getNumExpectedArguments() {
            return null;
        }

        @Override
        public List<CssValueNode> getCallResultNodes(List<CssValueNode> args, ErrorManager errorManager) throws GssFunctionException {
            return null;
        }

        @Override
        public String getCallResultString(List<String> args) throws GssFunctionException {
            return null;
        }
    }

    /**
     * 用于获取浏览器版本
     */
    private static class GetBrowserVersion implements GssFunction {
        @Override
        public Integer getNumExpectedArguments() {
            return null;
        }

        @Override
        public List<CssValueNode> getCallResultNodes(List<CssValueNode> args, ErrorManager errorManager) throws GssFunctionException {
            return null;
        }

        @Override
        public String getCallResultString(List<String> args) throws GssFunctionException {
            return null;
        }
    }

    /**
     * 用于获取浏览器平台
     */
    private static class GetPlatform implements GssFunction {
        /**
         * 返回所需的参数数目，如果为null则参数不定
         *
         * @return integer
         */
        @Override
        public Integer getNumExpectedArguments() {
            return null;
        }

        @Override
        public List<CssValueNode> getCallResultNodes(List<CssValueNode> args, ErrorManager errorManager) throws GssFunctionException {
            return null;
        }

        @Override
        public String getCallResultString(List<String> args) throws GssFunctionException {
            return null;
        }
    }

    /**
     * 导入样式
     */
    private static class ImportStyleSheets implements GssFunction {

        @Override
        public Integer getNumExpectedArguments() {
            return null;
        }

        @Override
        public List<CssValueNode> getCallResultNodes(List<CssValueNode> args, ErrorManager errorManager) throws GssFunctionException {
            return null;
        }

        @Override
        public String getCallResultString(List<String> args) throws GssFunctionException {
            return null;
        }
    }
}
