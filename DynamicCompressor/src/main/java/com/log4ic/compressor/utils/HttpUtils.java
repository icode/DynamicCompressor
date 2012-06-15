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

package com.log4ic.compressor.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.log4ic.compressor.utils.tester.BrowserTester;
import com.log4ic.compressor.utils.tester.PlatformTester;
import javolution.util.FastList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 张立鑫 IntelligentCode
 */
public class HttpUtils {
    private HttpUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(Compressor.class);

    public static boolean isHttpProtocol(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private static List<BrowserTester> browserTesterList = new FastList<BrowserTester>();
    private static List<PlatformTester> platformTesterList = new FastList<PlatformTester>();

    static {
        logger.debug("读取游览器匹配配置文件 /conf/browsers.js...");
        InputStream browserFormatsIn = FileUtils.getResourceAsStream("/conf/browsers.js");
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(browserFormatsIn);
        try {
            browserTesterList = gson.fromJson(reader, new TypeToken<List<BrowserTester>>() {
            }.getType());
        } catch (Exception e) {
            logger.error("读取游览器匹配配置文件 /conf/browsers.js 文件失败", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
        logger.debug("读取平台匹配配置文件 /conf/platform.js...");
        InputStream platFormatsIn = FileUtils.getResourceAsStream("/conf/platform.js");
        reader = new InputStreamReader(platFormatsIn);
        try {
            platformTesterList = gson.fromJson(reader, new TypeToken<List<PlatformTester>>() {
            }.getType());
        } catch (Exception e) {
            logger.error("读取平台匹配配置文件 /conf/platform.js 文件失败", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }

    }


    /**
     * 请求一个远程文件的内容
     *
     * @param httpUrl
     * @return
     * @throws java.io.IOException
     */
    public static String requestFile(String httpUrl) {
        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) {
            return null;
        }
        InputStream in = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try {
            in = url.openStream();
            streamReader = new InputStreamReader(in);
            reader = new BufferedReader(streamReader);
            String lineCode;
            StringBuilder pageCodeBuffer = new StringBuilder();
            while ((lineCode = reader.readLine()) != null) {
                pageCodeBuffer.append(lineCode);
                pageCodeBuffer.append("\n");
            }

            return pageCodeBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取bool类型的数据
     *
     * @param request
     * @param key
     * @return
     */
    public static Boolean getBooleanParam(HttpServletRequest request, String key) {
        String param = request.getParameter(key);
        boolean is = false;
        if (param != null) {
            if (StringUtils.isBlank(param)) {
                is = true;
            } else {
                try {
                    is = Boolean.parseBoolean(param);
                } catch (Exception e) {
                    is = true;
                }
            }
        }
        return is;
    }

    /**
     * 获取查询字符串
     *
     * @param request
     * @return
     */
    public static String getQueryString(HttpServletRequest request) {
        //根据JSP规范获取include方式的请求字符串
        Object queryString = request.getAttribute("javax.servlet.include.query_string");
        if (queryString == null) {
            //根据JSP规范获取forward方式的请求字符串
            queryString = request.getAttribute("javax.servlet.forward.query_string");
        }

        if (queryString != null) {
            return queryString.toString();
        }
        return request.getQueryString();
    }

    /**
     * 获取请求的资源路径
     *
     * @param request
     * @return
     */
    public static String getRequestUri(HttpServletRequest request) {
        //根据JSP规范获取include方式的请求的资源路径
        Object queryUri = request.getAttribute("javax.servlet.include.request_uri");
        if (queryUri == null) {
            //根据JSP规范获取forward方式的请求的资源路径
            queryUri = request.getAttribute("javax.servlet.forward.request_uri");
        }

        if (queryUri != null) {
            return queryUri.toString();
        }
        return request.getRequestURI();
    }

    /**
     * 正则匹配字符串，并返回匹配的字符串
     *
     * @param str
     * @param regex
     * @return
     */
    private static String findPattern(String str, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 获取浏览器相关信息
     *
     * @param request
     * @return
     */
    public static List<BrowserInfo> getRequestBrowserInfo(HttpServletRequest request) {
        List<BrowserInfo> list = new FastList<BrowserInfo>();
        String userAgent = request.getHeader("User-Agent");
        for (BrowserTester tester : browserTesterList) {
            //试图获取浏览器类型
            String browserType = findPattern(userAgent, tester.getRegex());
            if (browserType != null) {
                BrowserInfo info = new BrowserInfo();
                info.setBrowserType(tester.getIdentity());
                //试图获浏览器版本
                String browserVersion = findPattern(userAgent, tester.getVersionRegex());
                if (StringUtils.isNotBlank(browserVersion)) {
                    try {
                        browserVersion = findPattern(browserVersion, "\\d+\\.\\d+");
                        info.setBrowserVersion(Double.parseDouble(browserVersion));
                    } catch (Exception e) {
                    }

                    list.add(info);
                }
            }
        }
        return list;
    }


    /**
     * 获取浏览器平台
     *
     * @param request
     * @return
     */
    public static List<String> getRequestPlatform(HttpServletRequest request) {
        List<String> list = new FastList<String>();
        String userAgent = request.getHeader("User-Agent");
        for (PlatformTester tester : platformTesterList) {
            //试图获取浏览器类型
            String platform = findPattern(userAgent, tester.getRegex());
            if (platform != null) {
                list.add(tester.getIdentity());
            }
        }
        return list;
    }

}
