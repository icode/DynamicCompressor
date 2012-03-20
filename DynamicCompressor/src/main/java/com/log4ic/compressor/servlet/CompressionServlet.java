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

package com.log4ic.compressor.servlet;

import com.google.common.css.compiler.ast.GssParserException;
import com.log4ic.compressor.cache.CacheManager;
import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.impl.simple.SimpleCacheManager;
import com.log4ic.compressor.exception.CompressionException;
import com.log4ic.compressor.exception.QueryStringEmptyException;
import com.log4ic.compressor.exception.UnsupportedFileTypeException;
import com.log4ic.compressor.utils.Compressor;
import com.log4ic.compressor.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * JS CSS 压缩器Servlet
 *
 * @author 张立鑫 IntelligentCode
 */
public class CompressionServlet extends HttpServlet {

    private static CacheManager cacheManager = null;
    private static boolean initialized = false;
    private static String fileDomain = null;
    private static Logger logger = LoggerFactory.getLogger(CompressionServlet.class);


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf8");
        PrintWriter writer = resp.getWriter();
        //TODO 错误处理,调整显示页面
        try {
            Compressor.compress(req, resp, cacheManager, fileDomain);
        } catch (QueryStringEmptyException e) {
            logger.error("参数错误.", e);
            //501 - Not Implemented 服务器不支持实现请求所需要的功能，页眉值指定了未实现的配置
            resp.setStatus(501);
            writer.write("参数错误." + e.getMessage());
        } catch (UnsupportedFileTypeException e) {
            logger.error("不支持的文件处理类型.", e);
            //501 - Not Implemented 服务器不支持实现请求所需要的功能，页眉值指定了未实现的配置
            resp.setStatus(501);
            writer.write("不支持的文件处理类型." + e.getMessage());
        } catch (CompressionException e) {
            logger.error("内容处理错误.", e);
            //501 - Not Implemented 服务器不支持实现请求所需要的功能，页眉值指定了未实现的配置
            resp.setStatus(501);
            writer.write("内容处理错误." + e.getMessage());
        } catch (GssParserException e) {
            logger.error("内容处理错误,CSS语法错误.", e);
            //501 - Not Implemented 服务器不支持实现请求所需要的功能，页眉值指定了未实现的配置
            resp.setStatus(501);
            writer.write("内容处理错误,CSS语法错误." + e.getMessage());
        }
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        if (!initialized) {
            String cacheDir;
            CacheType cacheType = null;
            Integer cacheCount = null;
            boolean autoClean = true;
            int cleanHourAgo = 4;
            int lowHit = 3;
            if (config != null) {
                //压缩后的文件储存目录
                cacheDir = config.getInitParameter("cacheDir");
                if (StringUtils.isNotBlank(cacheDir)) {
                    if (cacheDir.startsWith("{contextPath}")) {
                        cacheDir = config.getServletContext().getRealPath(cacheDir.replace("{contextPath}", ""));
                    }
                    cacheDir = FileUtils.appendSeparator(cacheDir);
                } else {
                    cacheDir = config.getServletContext().getRealPath("/static/compressed/");
                }
                //缓存类型
                String cacheTypeStr = config.getInitParameter("cacheType");
                if (StringUtils.isNotBlank(cacheTypeStr)) {
                    try {
                        cacheType = CacheType.valueOf(cacheTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                    }
                }
                if ("none".equals(cacheTypeStr)) {
                    cacheType = null;
                }
                //缓存数目
                String cacheCountStr = config.getInitParameter("cacheCount");
                if (StringUtils.isNotBlank(cacheCountStr)) {
                    try {
                        cacheCount = Integer.parseInt(cacheCountStr);
                    } catch (Exception e) {
                    }
                }
                if (cacheCount == null) {
                    cacheCount = 5000;
                }

                String autoCleanStr = config.getInitParameter("autoClean");

                if (StringUtils.isNotBlank(autoCleanStr)) {
                    try {
                        autoClean = Boolean.parseBoolean(autoCleanStr);
                    } catch (Exception e) {
                    }
                }


                String fileDomain = config.getInitParameter("fileDomain");

                if (StringUtils.isNotBlank(fileDomain)) {
                    CompressionServlet.fileDomain = fileDomain;
                }

                Class cacheManagerClass = null;
                String cacheManagerClassStr = config.getInitParameter("cacheManager");
                if (StringUtils.isNotBlank(cacheManagerClassStr)) {
                    try {
                        cacheManagerClass = CompressionServlet.class.getClassLoader().loadClass(cacheManagerClassStr);
                    } catch (ClassNotFoundException e) {
                        logger.error("缓存管理器配置错误!", e);
                    }
                } else {
                    cacheManagerClass = SimpleCacheManager.class;
                }

                if (autoClean) {
                    String cleanIntervalStr = config.getInitParameter("cleanInterval");
                    long cleanInterval = 1000 * 60 * 60 * 5L;
                    if (StringUtils.isNotBlank(cleanIntervalStr)) {
                        try {
                            cleanInterval = Long.parseLong(cleanIntervalStr);
                        } catch (Exception e) {
                        }
                    }
                    String lowHitStr = config.getInitParameter("cleanLowHit");

                    if (StringUtils.isNotBlank(lowHitStr)) {
                        try {
                            lowHit = Integer.parseInt(autoCleanStr);
                        } catch (Exception e) {
                        }
                    }
                    String cleanHourAgoStr = config.getInitParameter("cleanHourAgo");

                    if (StringUtils.isNotBlank(cleanHourAgoStr)) {
                        try {
                            cleanHourAgo = Integer.parseInt(cleanHourAgoStr);
                        } catch (Exception e) {
                        }
                    }
                    if (cacheType != null) {
                        try {
                            Constructor constructor = cacheManagerClass.getConstructor(CacheType.class, int.class, int.class, int.class, long.class, String.class);
                            cacheManager = (CacheManager) constructor.newInstance(cacheType, cacheCount, lowHit, cleanHourAgo, cleanInterval, cacheDir);
                        } catch (NoSuchMethodException e) {
                            logger.error("初始化缓存管理器错误", e);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            logger.error("初始化缓存管理器错误", e);
                        } catch (IllegalAccessException e) {
                            logger.error("初始化缓存管理器错误", e);
                        } catch (Exception e) {
                            logger.error("初始化缓存管理器错误", e);
                        }
                    }
                } else {
                    if (cacheType != null) {
                        try {
                            Constructor constructor = cacheManagerClass.getConstructor(CacheType.class, int.class, String.class);
                            cacheManager = (CacheManager) constructor.newInstance(cacheType, cacheCount, cacheDir);
                        } catch (NoSuchMethodException e) {
                            logger.error("初始化缓存管理器错误", e);
                        } catch (InvocationTargetException e) {
                            logger.error("初始化缓存管理器错误", e);
                        } catch (InstantiationException e) {
                            logger.error("初始化缓存管理器错误", e);
                        } catch (IllegalAccessException e) {
                            logger.error("初始化缓存管理器错误", e);
                        } catch (Exception e) {
                            logger.error("初始化缓存管理器错误", e);
                        }
                    }
                }
            }
            initialized = true;
        }
    }
}
