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

import com.google.common.css.JobDescription;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.GssParser;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.passes.CompactPrinter;
import com.google.common.css.compiler.passes.PassRunner;
import com.google.common.css.compiler.passes.PrettyPrinter;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import com.log4ic.compressor.cache.Cache;
import com.log4ic.compressor.cache.CacheContent;
import com.log4ic.compressor.cache.CacheManager;
import com.log4ic.compressor.exception.CompressionException;
import com.log4ic.compressor.servlet.http.CompressionResponseWrapper;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JS CSS 压缩器
 *
 * @author 张立鑫 IntelligentCode
 */
public class Compressor {
    private Compressor() {
    }

    private static final Logger logger = LoggerFactory.getLogger(Compressor.class);

    private static final Map<String, Lock> progressCacheLock = new FastMap<String, Lock>();

    /**
     * 压缩JS
     *
     * @param code
     * @param options
     * @return
     */
    public static String compressJS(String code, CompilerOptions options, String jsOutputFile) {
        return compressJS(new JSSourceFile[]{JSSourceFile.fromCode("all.js", code)}, options, jsOutputFile);
    }

    /**
     * 压缩JS
     *
     * @param jsFiles
     * @param options
     * @return
     */
    public static String compressJS(JSSourceFile[] jsFiles, CompilerOptions options, String jsOutputFile) {
        return compressJS(new JSSourceFile[]{JSSourceFile.fromCode("lib.js", "")}, jsFiles, options, jsOutputFile);
    }

    /**
     * 压缩JS
     *
     * @param externFiles
     * @param jsFiles
     * @param options
     * @return
     */
    public static String compressJS(JSSourceFile[] externFiles, JSSourceFile[] jsFiles, CompilerOptions options, String jsOutputFile) {
        String compressed = null;
        Compiler compiler = new Compiler();
        logger.debug("压缩JS...");
        compiler.compile(externFiles, jsFiles, options);
        compressed = compiler.toSource();
        logger.debug("压缩JS完毕...");
        if (StringUtils.isNotBlank(jsOutputFile)) {
            logger.debug("将压缩的JS写入文件...");
            sourceToFile(compressed, new File(jsOutputFile));
            logger.debug("写入文件完毕...");
        }
        return compressed;
    }

    /**
     * 压缩JS
     *
     * @param codes
     * @param options
     * @return
     */
    public static String compressJS(List<String> codes, CompilerOptions options, String jsOutputFile) {
        StringBuilder allCode = new StringBuilder();

        for (String code : codes) {
            allCode.append(code);
        }

        return compressJS(allCode.toString(), options, jsOutputFile);
    }

    /**
     * 压缩JS
     *
     * @param code
     * @param level
     * @param outPath
     * @param isDebug
     * @return
     */
    public static String compressJS(String code, CompilationLevel level, String outPath, Boolean isDebug) {
        CompilerOptions options = new CompilerOptions();
        options.setCodingConvention(new ClosureCodingConvention());
        level.setOptionsForCompilationLevel(options);
        if (isDebug) {
            level.setDebugOptionsForCompilationLevel(options);
        }
        return compressJS(code, options, outPath);
    }

    /**
     * 压缩JS
     *
     * @param code
     * @param level
     * @param isDebug
     * @return
     */
    public static String compressJS(String code, CompilationLevel level, Boolean isDebug) {
        return compressJS(code, level, null, isDebug);
    }

    /**
     * 将代码写入文件
     *
     * @param code
     * @param file
     */
    public static void sourceToFile(String code, File file) {
        FileUtils.writeFile(code, file.getPath());
    }

//    private static final DefaultGssFunctionMapProvider gssFunctionMapProvider = new DefaultGssFunctionMapProvider();
//
//    static {
//        Map<String, GssFunction> gssFunctionMap = gssFunctionMapProvider.get();
//        gssFunctionMap.putAll(GssFunctions.getFunctionMap());
//    }


    /**
     * 压缩css
     *
     * @param code
     * @param format
     * @param fileType
     * @param conditions
     * @return
     * @throws GssParserException
     */
    public static String compressCSS(String code, JobDescription.OutputFormat format, FileType fileType, List<String> conditions) throws GssParserException {
        JobDescriptionBuilder builder = new JobDescriptionBuilder();
        builder.setAllowWebkitKeyframes(true);
        builder.setProcessDependencies(true);
        builder.setSimplifyCss(true);
        builder.setEliminateDeadStyles(true);
        builder.addInput(new SourceCode("all." + (fileType == FileType.GSS ? "gss" : "css"), code));
        builder.setOutputFormat(format);
        //设置内置方法
        //builder.setGssFunctionMapProvider(gssFunctionMapProvider);
        //设置浏览器断言
        if (conditions != null && conditions.size() > 0) {
            for (String con : conditions) {
                builder.addTrueConditionName(con);
            }
        }
        return compressCSS(builder);
    }

    /**
     * 压缩css
     *
     * @param code
     * @param format
     * @param fileType
     * @return
     * @throws GssParserException
     */
    public static String compressCSS(String code, JobDescription.OutputFormat format, FileType fileType) throws GssParserException {
        return compressCSS(code, format, fileType, null);
    }

    /**
     * 压缩css
     *
     * @param builder
     * @return
     * @throws GssParserException
     */
    public static String compressCSS(JobDescriptionBuilder builder) throws GssParserException {
        logger.debug("压缩CSS...");
        try {
            JobDescription job = builder.getJobDescription();
            GssParser parser = new GssParser(job.inputs);
            CssTree cssTree = parser.parse();
            CompilerErrorManager errorManager = new CompilerErrorManager();
            PassRunner passRunner = new PassRunner(job, errorManager);
            if (job.outputFormat != JobDescription.OutputFormat.DEBUG) {
                passRunner.runPasses(cssTree);
            }

            if (job.outputFormat == JobDescription.OutputFormat.COMPRESSED) {
                CompactPrinter compactPrinterPass = new CompactPrinter(cssTree);
                compactPrinterPass.runPass();
                return compactPrinterPass.getCompactPrintedString();
            } else {
                PrettyPrinter prettyPrinterPass = new PrettyPrinter(cssTree
                        .getVisitController());
                prettyPrinterPass.runPass();
                return prettyPrinterPass.getPrettyPrintedString();
            }
        } finally {
            logger.debug("压缩CSS完毕...");
        }
    }

    /**
     * 修正文件内的URL相对指向
     *
     * @param code
     * @param fileUrl
     * @param type
     * @return
     */
    public static String fixUrlPath(String code, String fileUrl, FileType type) {
        return fixUrlPath(code, fileUrl, type, null);
    }

    /**
     * 修正文件内的URL相对指向
     *
     * @param code
     * @param fileUrl
     * @param type
     * @param fileDomain
     * @return
     */
    public static String fixUrlPath(String code, String fileUrl, FileType type, String fileDomain) {
        logger.debug("修正文件内的URL相对指向...");
        StringBuilder codeBuffer = new StringBuilder();
        switch (type) {
            case GSS:
            case CSS:
                Pattern pattern = Pattern.compile("url\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(code);

                String[] codeFragments = pattern.split(code);

                fileUrl = fileUrl.substring(0, fileUrl.lastIndexOf("/"));

                int i = 0;
                while (matcher.find()) {
                    codeBuffer.append(codeFragments[i]);
                    codeBuffer.append("url(");
                    MatchResult result = matcher.toMatchResult();
                    String url = result.group(1).replaceAll("'|\"", "");
                    String cssUrl = fileUrl;
                    //绝对路径不处理
                    if (!HttpUtils.isHttpProtocol(url)) {
                        StringBuilder pathBuffer = new StringBuilder();
                        if (url.startsWith("..")) {//如果访问上级目录
                            String[] imgUrl = url.split("/");
                            for (int j = 0; j < imgUrl.length; j++) {
                                String imgUrlFragment = imgUrl[j];
                                if (imgUrlFragment.equals("..")) {//上级目录
                                    int index = cssUrl.lastIndexOf("/");
                                    if (index > 0) {
                                        cssUrl = cssUrl.substring(0, index);
                                    } else {
                                        cssUrl = "/";
                                    }

                                    imgUrl[j] = null;
                                } else {
                                    for (String u : imgUrl) {
                                        if (u != null) {
                                            pathBuffer.append("/");
                                            pathBuffer.append(u);
                                        }
                                    }
                                    pathBuffer.deleteCharAt(0);
                                    break;
                                }
                            }
                        } else {
                            pathBuffer.append("/");
                            pathBuffer.append(url);
                        }
                        //如果设置了静态文件路径，并且cssUrl不是http开头（视为其他服务加载的文件)，则将文件服务器域名指向设置的域名
                        if (StringUtils.isNotBlank(fileDomain) && !HttpUtils.isHttpProtocol(cssUrl)) {
                            if (!fileDomain.endsWith("/") && !cssUrl.startsWith("/")) {
                                fileDomain = fileDomain + "/";
                            } else if (fileDomain.endsWith("/") && cssUrl.startsWith("/")) {
                                cssUrl = cssUrl.substring(1);
                            }
                            if (!HttpUtils.isHttpProtocol(fileDomain)) {
                                fileDomain = "http://" + fileDomain;
                            }
                            cssUrl = fileDomain + cssUrl;
                        }
                        pathBuffer.insert(0, cssUrl);
                        url = pathBuffer.toString();
                    }
                    codeBuffer.append(url);
                    codeBuffer.append(")");
                    i++;
                }
                if (i == 0) {
                    return code;
                } else {
                    if (codeFragments[i] != null) {
                        codeBuffer.append(codeFragments[i]);
                    }
                }
                break;
            case JS:
                codeBuffer.append(code);
                break;
        }
        logger.debug("修正文件内的URL相对指向完毕...");
        return codeBuffer.toString();
    }

    private static class CompilerErrorManager extends com.google.common.css.compiler.ast.BasicErrorManager {
        public void print(String msg) {
            System.err.println(msg);
        }
    }

    public enum FileType {
        JS,
        CSS,
        GSS
    }


    /**
     * 获取参数的文件并合并
     *
     * @param fileUrlList
     * @param request
     * @param response
     * @param type
     * @param fileDomain
     * @return
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     */
    public static String mergeCode(String[] fileUrlList, HttpServletRequest request, HttpServletResponse response, FileType type, String fileDomain) throws CompressionException {
        StringBuilder code = new StringBuilder();
        CompressionResponseWrapper wrapperResponse = null;
        //获取参数的文件并合并
        for (String url : fileUrlList) {
            if (url.toLowerCase().endsWith(type.name().toLowerCase())) {
                try {
                    //如果是http/https 协议开头则视为跨域
                    if (HttpUtils.isHttpProtocol(url)) {
                        code.append(Compressor.fixUrlPath(HttpUtils.requestFile(url), url, type));
                    } else {
                        //否则视为同域
                        if (wrapperResponse == null) {
                            wrapperResponse = new CompressionResponseWrapper(response);
                        }
                        request.getRequestDispatcher(url).include(request, wrapperResponse);
                        wrapperResponse.flushBuffer();
                        String fragment = wrapperResponse.getContent();
                        fragment = Compressor.fixUrlPath(fragment, url, type, fileDomain);
                        code.append(fragment);
                        code.append("\n");
                        wrapperResponse.reset();
                    }
                } catch (ServletException e) {
                    throw new CompressionException("ServletException", e);
                } catch (IOException e) {
                    throw new CompressionException(e);
                }
            }
        }

        if (wrapperResponse != null) {
            try {
                wrapperResponse.close();
            } catch (IOException e) {
                throw new CompressionException("IO异常", e);
            } catch (Throwable throwable) {
                throw new CompressionException("销毁Response异常", throwable);
            }
        }
        return code.toString();
    }


    /**
     * 压缩
     *
     * @param code
     * @param request
     * @param type
     * @return
     * @throws com.google.common.css.compiler.ast.GssParserException
     *
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     */
    public static String compressCode(String code, HttpServletRequest request, FileType type) throws GssParserException, CompressionException {
        //压缩
        boolean isDebug = HttpUtils.getBooleanParam(request, "debug");
        switch (type) {
            case JS:
                CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
                String levelParam = request.getParameter("level");
                if (StringUtils.isNotBlank(levelParam)) {
                    try {
                        level = CompilationLevel.values()[Integer.parseInt(levelParam)];
                    } catch (Exception e) {
                        try {
                            level = CompilationLevel.valueOf(levelParam);
                        } catch (IllegalArgumentException ae) {
                            throw new CompressionException("ServletException", e);
                        }
                    }
                    if (level == null) {
                        level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
                    }
                }
                code = Compressor.compressJS(code, level, isDebug);
                break;
            case GSS:
            case CSS:
                JobDescription.OutputFormat format = JobDescription.OutputFormat.COMPRESSED;
                if (isDebug) {
                    format = JobDescription.OutputFormat.DEBUG;
                } else if (HttpUtils.getBooleanParam(request, "pretty")) {
                    format = JobDescription.OutputFormat.PRETTY_PRINTED;
                }

                //压缩代码并设置浏览器断言
                code = Compressor.compressCSS(code, format, type, buildTrueConditions(request));
                break;
        }
        return code;
    }

    private static List<String> buildTrueConditions(HttpServletRequest request) {
        List<String> conditions = new FastList<String>();

        //获取浏览器信息
        List<BrowserInfo> browserInfoList = HttpUtils.getRequestBrowserInfo(request);
        String prefix = "browser_";
        //添加浏览器判断断言
        for (BrowserInfo info : browserInfoList) {
            //设置直接的浏览器断言 如 BROWSER_IE
            conditions.add((prefix + info.getBrowserType()).toUpperCase());
            Double version = info.getBrowserVersion();
            if (version != null) {
                String versionStr = version.toString();
                //设置带子版本号的浏览器断言 如 BROWSER_IE6.2
                conditions.add((prefix + info.getBrowserType() + versionStr.replace(".", "_")).toUpperCase());
                versionStr = versionStr.substring(0, versionStr.indexOf("."));
                //设置带主版本号的浏览器断言 如 BROWSER_IE6
                conditions.add((prefix + info.getBrowserType() + versionStr).toUpperCase());
            }
        }

        List<String> platformList = HttpUtils.getRequestPlatform(request);

        for (String platform : platformList) {
            conditions.add(("platform_" + platform).toUpperCase());
        }

        return conditions;
    }

    /**
     * 压缩
     *
     * @param request
     * @param response
     * @return
     */
    public static boolean compress(HttpServletRequest request, HttpServletResponse response) throws CompressionException, GssParserException {
        return compress(request, response, null, null);
    }

    public static boolean compress(HttpServletRequest request, HttpServletResponse response, CacheManager cacheManager) throws CompressionException, GssParserException {
        return compress(request, response, cacheManager, null);
    }


    /**
     * 去掉查询字符串的重复参数
     *
     * @param queryString
     * @return
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     */
    private static String removeDuplicateParameters(String queryString) throws CompressionException {
        try {
            queryString = URLDecoder.decode(queryString, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new CompressionException(e);
        }

        String[] params = queryString.replace("=", "").split("&");

        FastList<String> noRepeatParams = new FastList<String>();
        //去掉重复参数
        for (String param : params) {
            if (noRepeatParams.indexOf(param) == -1) {
                noRepeatParams.add(param);
            }
        }

        return StringUtils.join(noRepeatParams, "&");
    }

    /**
     * 将代码和类型打印到客户端
     *
     * @param code
     * @param type
     * @param response
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     */
    private static void writeOutCode(String code, FileType type, HttpServletResponse response) throws CompressionException {
        //设置想对应的返回类型mine type
        try {
            switch (type) {
                case JS:
                    response.setContentType("text/javascript");
                    break;
                case CSS:
                case GSS:
                    response.setContentType("text/css");
                    break;
                default:
                    response.setContentType("text/html");
            }
            PrintWriter writer = response.getWriter();
            writer.write(code);
            writer.flush();
            response.flushBuffer();
        } catch (IOException e) {
            throw new CompressionException("IO异常", e);
        }
    }

    private static FileType getFileType(HttpServletRequest request) throws CompressionException {
        //分割请求地址
        String[] uris = HttpUtils.getRequestUri(request).split("\\.");
        if (uris.length == 0) {
            throw new CompressionException("Un support FileType Exception,URI Error");
        }
        FileType type;
        //根据请求后缀获取压缩内容类型
        try {
            type = FileType.valueOf(uris[uris.length - 1].toUpperCase());
        } catch (Exception e) {
            throw new CompressionException("Un support FileType Exception", e);
        }
        return type;
    }

    /**
     * 构建压缩代码及缓存
     *
     * @param type
     * @param queryString
     * @param cacheManager
     * @param request
     * @param response
     * @param fileDomain
     * @return
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     * @throws com.google.common.css.compiler.ast.GssParserException
     *
     */
    private static String buildCode(FileType type,
                                    String queryString, @Nullable CacheManager cacheManager,
                                    HttpServletRequest request, HttpServletResponse response,
                                    String fileDomain) throws CompressionException, GssParserException {
        Cache cache = null;
        try {
            //如果有缓存管理器则进行线程同步操作
            if (cacheManager != null) {
                Lock cacheLock;
                //获取该次任务的压缩线程锁
                synchronized (progressCacheLock) {
                    cacheLock = progressCacheLock.get(queryString);
                }
                if (cacheLock != null) {
                    //如果已经有在压缩的线程则等待该线程结束
                    cacheLock.lock();
                    //并直接返回缓存内容
                    cache = cacheManager.get(queryString);
                } else {
                    //如过没有转化中的线程则创建该任务（每个任务按照queryString划分）的压缩的线程锁
                    cacheLock = new ReentrantLock();
                    cacheLock.lock();
                    synchronized (progressCacheLock) {
                        progressCacheLock.put(queryString, cacheLock);
                    }
                }
            }
            String code = null;
            //再次验证cache，如果是等待中的线程则不会再压缩
            if (cache == null) {
                //获取参数的文件并合并
                code = mergeCode(queryString.split("&"), request, response, type, fileDomain);
                //压缩
                code = HttpUtils.getBooleanParam(request, "nocompress") ? code : compressCode(code, request, type);
                if (cacheManager != null) {
                    cacheManager.put(queryString, code, type);
                }
                return code;
            } else {
                return cache.getContent().getContent();
            }

        } finally {
            if (cacheManager != null) {
                Lock cacheLock;
                synchronized (progressCacheLock) {
                    cacheLock = progressCacheLock.get(queryString);
                    progressCacheLock.remove(queryString);
                }
                if (cacheLock != null) {
                    cacheLock.unlock();
                }
            }
        }
    }

    /**
     * 压缩
     *
     * @param request
     * @param response
     * @param cacheManager
     * @param fileDomain
     * @return
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     * @throws GssParserException
     */
    public static boolean compress(HttpServletRequest request, HttpServletResponse response, @Nullable CacheManager cacheManager, String fileDomain) throws CompressionException, GssParserException {

        String queryString = HttpUtils.getQueryString(request);

        if (StringUtils.isBlank(queryString)) {
            return false;
        }

        //去除重复参数
        queryString = removeDuplicateParameters(queryString);

        FileType type = getFileType(request);

        if (type == FileType.GSS) {
            List<BrowserInfo> browserInfoList = HttpUtils.getRequestBrowserInfo(request);
            List<String> platformList = HttpUtils.getRequestPlatform(request);
            if (browserInfoList.size() > 0) {
                BrowserInfo info = browserInfoList.get(0);
                String versionStr = info.getBrowserVersion().toString();
                queryString += "&" + info.getBrowserType() + versionStr.replace(".", "_");
            }
            if (platformList.size() > 0) {
                String platform = platformList.get(0);
                queryString += "&" + platform;
            }
        }

        String code = "";

        Cache cache = null;
        if (cacheManager != null) {
            //线程不安全，减少性能消耗
            if (!progressCacheLock.isEmpty() && progressCacheLock.containsKey(queryString)) {
                Lock lock = progressCacheLock.get(queryString);
                lock.lock();
                lock.unlock();
            }
            cache = cacheManager.get(queryString);
        }

        //这里是为了减少同步带来的性能消耗，将线程同步全部放在耗时操作内，在此块进行线程同步过滤
        if (cache == null) {
            //进行构建代码
            code = buildCode(type, queryString, cacheManager, request, response, fileDomain);
        }

        //从缓存中解析内容
        if (cacheManager != null && cache != null) {
            CacheContent cacheContent = cache.getContent();
            code = cacheContent.getContent();
        }

        //将内容输出
        writeOutCode(code, type, response);

        return true;
    }

}
