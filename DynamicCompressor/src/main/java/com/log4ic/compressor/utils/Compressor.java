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

import com.google.common.collect.Lists;
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
import com.log4ic.compressor.cache.CacheManager;
import com.log4ic.compressor.exception.CompressionException;
import com.log4ic.compressor.exception.QueryStringEmptyException;
import com.log4ic.compressor.exception.UnsupportedFileTypeException;
import com.log4ic.compressor.servlet.http.ContentResponseWrapper;
import com.log4ic.compressor.servlet.http.stream.ContentResponseStream;
import com.log4ic.compressor.utils.gss.passes.ExtendedPassRunner;
import com.log4ic.compressor.utils.less.LessEngine;
import com.log4ic.compressor.utils.less.exception.LessException;
import com.log4ic.compressor.utils.template.JavascriptTemplateEngine;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
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

    private static final Map<String, byte[]> progressCacheLock = new FastMap<String, byte[]>();

    /**
     * 压缩JS
     *
     * @param code
     * @param options
     * @param jsOutputFile
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
     * @param jsOutputFile
     * @return
     */
    public static String compressJS(JSSourceFile[] jsFiles, CompilerOptions options, String jsOutputFile) {
        return compressJS(new JSSourceFile[]{JSSourceFile.fromCode("lib.js", "")}, jsFiles, options, jsOutputFile);
    }


    /**
     * 压缩LESS
     *
     * @param codeList
     * @param conditions
     * @return
     */
    public static String compressLess(List<SourceCode> codeList, JobDescription.OutputFormat format, List<String> conditions, JobDescription.OptimizeStrategy level) throws GssParserException, LessException, CompressionException {
        return compressGss(LessEngine.parseLess(codeList, conditions), format, conditions, level);
    }


    /**
     * 压缩JS
     *
     * @param externFiles
     * @param jsFiles
     * @param options
     * @param jsOutputFile
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
     * @param jsSourceFiles
     * @param level
     * @param isDebug
     * @return
     */
    public static String compressJS(JSSourceFile[] jsSourceFiles, CompilationLevel level, Boolean isDebug) {
        CompilerOptions options = new CompilerOptions();
        options.setCodingConvention(new ClosureCodingConvention());
        level.setOptionsForCompilationLevel(options);
        if (isDebug) {
            level.setDebugOptionsForCompilationLevel(options);
        }
        return compressJS(jsSourceFiles, options, null);
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

    public static String compressGss(SourceCode sourceCode) throws GssParserException {
        return compressGss(Lists.<SourceCode>newArrayList(sourceCode), null, null, null);
    }

    public static String compressGss(List<SourceCode> codeList) throws GssParserException {
        return compressGss(codeList, null, null, null);
    }

    public static String compressGss(List<SourceCode> codeList, JobDescription.OutputFormat format, JobDescription.OptimizeStrategy level) throws GssParserException {
        return compressGss(codeList, format, null, level);
    }

    /**
     * 压缩css
     *
     * @param codeList
     * @param format
     * @param conditions
     * @return
     * @throws GssParserException
     */
    public static String compressGss(List<SourceCode> codeList, JobDescription.OutputFormat format, List<String> conditions) throws GssParserException {
        return compressGss(buildJobDesBuilder(codeList, format, conditions).getJobDescription());
    }

    public static String compressGss(List<SourceCode> codeList, JobDescription.OutputFormat format, List<String> conditions, JobDescription.OptimizeStrategy level) throws GssParserException {
        return compressGss(buildJobDesBuilder(codeList, format, conditions, level).getJobDescription());
    }

    private static JobDescriptionBuilder buildJobDesBuilder(List<SourceCode> codeList, JobDescription.OutputFormat format, List<String> conditions) {
        return buildJobDesBuilder(codeList, format, conditions, null);
    }

    private static JobDescriptionBuilder buildJobDesBuilder(List<SourceCode> codeList, JobDescription.OutputFormat format, List<String> conditions, JobDescription.OptimizeStrategy level) {
        JobDescriptionBuilder builder = new JobDescriptionBuilder();
        builder.setAllowWebkitKeyframes(true);
        builder.setAllowKeyframes(true);
        builder.setAllowUnrecognizedFunctions(true);
        builder.setAllowUnrecognizedProperties(true);
        builder.setProcessDependencies(true);
        builder.setSimplifyCss(true);
        builder.setEliminateDeadStyles(true);
        builder.setOptimizeStrategy(level == null ? JobDescription.OptimizeStrategy.SAFE : level);
        for (SourceCode code : codeList) {
            builder.addInput(new SourceCode(code.getFileName(), fixIE9Hack(code.getFileContents())));
        }
        if (format != null) {
            builder.setOutputFormat(format);
        }
        //设置内置方法
        //builder.setGssFunctionMapProvider(gssFunctionMapProvider);
        //设置浏览器断言
        if (conditions != null && conditions.size() > 0) {
            for (String con : conditions) {
                builder.addTrueConditionName(con);
            }
        }
        return builder;
    }

    public static String parseGss(List<SourceCode> codeList, List<String> conditions) throws GssParserException, CompressionException {
        List<SourceCode> codes = Lists.newArrayList();
        for (SourceCode s : codeList) {
            if (getFileType(s.getFileName()) == FileType.GSS) {
                codes.add(s);
            }
        }
        JobDescriptionBuilder builder = buildJobDesBuilder(codes, null, conditions);
        builder.setProcessDependencies(false);
        builder.setSimplifyCss(false);
        builder.setEliminateDeadStyles(false);
        builder.setOptimizeStrategy(JobDescription.OptimizeStrategy.NONE);
        return parseGss(builder.getJobDescription());
    }


    public static String parseGss(JobDescription job) throws GssParserException {
        logger.debug("解析GSS...");
        try {
            GssParser parser = new GssParser(job.inputs);
            CssTree cssTree = parser.parse();
            CompilerErrorManager errorManager = new CompilerErrorManager();
            PassRunner passRunner = new ExtendedPassRunner(job, errorManager);
            passRunner.runPasses(cssTree);
            PrettyPrinter prettyPrinterPass = new PrettyPrinter(cssTree.getVisitController());
            prettyPrinterPass.runPass();
            return prettyPrinterPass.getPrettyPrintedString();
        } finally {
            logger.debug("解析GSS完毕...");
        }
    }

    /**
     * 压缩css
     *
     * @param job
     * @return
     * @throws GssParserException
     */
    public static String compressGss(JobDescription job) throws GssParserException {
        logger.debug("压缩CSS...");
        try {
            GssParser parser = new GssParser(job.inputs);
            CssTree cssTree = parser.parse();
            if (job.outputFormat != JobDescription.OutputFormat.DEBUG) {
                CompilerErrorManager errorManager = new CompilerErrorManager();
                PassRunner passRunner = new ExtendedPassRunner(job, errorManager);
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
     * IE9 \9 HACK去掉\9前面空格
     *
     * @param fragment
     * @return
     */
    public static String fixIE9Hack(String fragment) {
        //IE9 \9 HACK去掉\9前面空格
        return fragment.replaceAll("\\s+\\\\", "\\9");
    }

    private static final String importPatternStr = "@import\\s+(?:url\\()?[\\s\\'\\\"]?([^\\'\\\";\\s\\n]+)[\\s\\'\\\"]?(?:\\))?;?";
    private static final Pattern importPattern = Pattern.compile(importPatternStr, Pattern.CASE_INSENSITIVE);

    public static String importCode(String code, String fileUrl, FileType type, HttpServletRequest request, HttpServletResponse response) throws CompressionException, LessException, GssParserException {
        StringBuilder codeBuilder = new StringBuilder();
        switch (type) {
            case GSS:
            case CSS:
            case LESS:
            case MSS:
                Matcher matcher = importPattern.matcher(code);
                String[] codeFragments = importPattern.split(code);
                String filePath = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);
                FileType fileType = getFileType(fileUrl);
                int i = 0;
                while (matcher.find()) {

                    String cssPath;
                    String cssFile = matcher.group(1);
                    if (!HttpUtils.isHttpProtocol(cssFile) && !cssFile.startsWith("/")) {
                        cssPath = filePath + cssFile;
                    } else {
                        cssPath = cssFile;
                    }

                    if (StringUtils.isNotBlank(codeFragments[i])) {
                        if (codeFragments[i].lastIndexOf("/*") > codeFragments[i].lastIndexOf("*/")
                                || codeFragments[i].lastIndexOf("//") > codeFragments[i].lastIndexOf("\n")) {
                            if (codeFragments.length > i + 1) {
                                codeFragments[i + 1] = codeFragments[i] + codeFragments[i + 1];
                                codeFragments[i] = null;
                                i++;
                            }
                            continue;
                        } else {
                            codeBuilder.append(codeFragments[i]);
                        }
                    }

                    if (request.getAttribute(cssPath) == null) {
                        if (fileType == FileType.LESS) {
                            try {
                                if (!type.contains(getFileType(cssPath))) {
                                    cssPath += ".less";
                                }
                            } catch (Exception e) {
                                cssPath += ".less";
                            }
                        }
                        logger.debug("导入[{}]文件", cssPath);
                        request.setAttribute(cssPath, true);
                        List<SourceCode> sourceCodes = mergeCode(new String[]{cssPath}, request, response, type);
                        for (SourceCode s : sourceCodes) {
                            FileType t = getFileType(s.getFileName());
                            if (fileType.equals(t)) {
                                codeBuilder.append(s.getFileContents());
                            } else {
                                codeBuilder.append(fixIE9Hack(mergeCode(Lists.<SourceCode>newArrayList(s), request, t)));
                            }
                        }
                    }
                    i++;
                }
                if (i != 0) {
                    if (codeFragments.length > i && StringUtils.isNotBlank(codeFragments[i])) {
                        codeBuilder.append(codeFragments[i]);
                    }
                    break;
                }
            default:
                return code;
        }
        return codeBuilder.toString();
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

        StringBuilder codeBuffer = new StringBuilder();
        switch (type) {
            case GSS:
            case CSS:
            case LESS:
            case MSS:
                logger.debug("修正文件内的URL相对指向...");
                Pattern pattern = Pattern.compile("url\\(\\s*(?!['\"]?(?:data:|about:|#|@))([^)]+)\\)", Pattern.CASE_INSENSITIVE);

                Matcher matcher = pattern.matcher(code);

                String[] codeFragments = pattern.split(code);

                fileUrl = fileUrl.substring(0, fileUrl.lastIndexOf("/") + 1);

                int i = 0;
                while (matcher.find()) {
                    codeBuffer.append(codeFragments[i]);
                    codeBuffer.append("url(");
                    MatchResult result = matcher.toMatchResult();
                    String url = result.group(1).replaceAll("'|\"", "");
                    //绝对路径不处理
                    if (!HttpUtils.isHttpProtocol(url) && !url.startsWith("/")) {
                        url = URI.create(fileUrl + url).normalize().toASCIIString();//转成URL
                    }

                    //如果设置了静态文件路径，并且url不是http开头（视为其他服务加载的文件)，则将文件服务器域名指向设置的域名
                    if (StringUtils.isNotBlank(fileDomain) && !HttpUtils.isHttpProtocol(url)) {
                        if (!fileDomain.endsWith("/") && !url.startsWith("/")) {
                            fileDomain = fileDomain + "/";
                        } else if (fileDomain.endsWith("/") && url.startsWith("/")) {
                            url = url.substring(1);
                        }
                        if (!HttpUtils.isHttpProtocol(fileDomain)) {
                            fileDomain = "http://" + fileDomain;
                        }
                        url = fileDomain + url;
                    }
                    codeBuffer.append(url);
                    codeBuffer.append(")");
                    i++;
                }
                if (i == 0) {
                    return code;
                } else {
                    if (codeFragments.length > i && StringUtils.isNotBlank(codeFragments[i])) {
                        codeBuffer.append(codeFragments[i]);
                    }
                }
                logger.debug("修正文件内的URL相对指向完毕...");
                break;
            default:
                return code;
        }
        return codeBuffer.toString();
    }

    private static class CompilerErrorManager extends com.google.common.css.compiler.ast.BasicErrorManager {
        public void print(String msg) {
            System.err.println(msg);
        }
    }

    public enum FileType {
        JS {
            @Override
            boolean contains(FileType type) {
                return equals(type) || TPL.contains(type);
            }

            @Override
            public boolean contains(String type) {
                return name().equals(type.toUpperCase()) || TPL.contains(type);
            }
        },
        TPL {
            @Override
            boolean contains(FileType type) {
                return equals(type) || ArrayUtils.indexOf(FileType.values(), type) == -1;
            }

            @Override
            public boolean contains(String type) {
                boolean is = name().equals(type.toUpperCase());
                if (!is) {
                    try {
                        FileType.valueOf(type.toUpperCase());
                    } catch (Exception e) {
                        is = true;
                    }
                }
                return is;
            }
        },
        CSS {
            @Override
            boolean contains(FileType type) {
                return equals(type);
            }

            @Override
            public boolean contains(String type) {
                return name().equals(type.toUpperCase());
            }
        },
        GSS {
            @Override
            boolean contains(FileType type) {
                return equals(type) || CSS.contains(type);
            }

            @Override
            public boolean contains(String type) {
                return CSS.contains(type) ||
                        name().equals(type.toUpperCase());
            }
        },
        LESS {
            @Override
            boolean contains(FileType type) {
                return equals(type) || CSS.contains(type);
            }

            @Override
            public boolean contains(String type) {
                return CSS.contains(type) ||
                        name().equals(type.toUpperCase());
            }
        },
        MSS {
            @Override
            boolean contains(FileType type) {
                return equals(type)
                        || CSS.contains(type)
                        || GSS.contains(type)
                        || LESS.contains(type);
            }

            @Override
            public boolean contains(String type) {
                return name().equals(type.toUpperCase())
                        || CSS.contains(type)
                        || GSS.contains(type)
                        || LESS.contains(type);
            }
        };

        abstract boolean contains(String type);

        abstract boolean contains(FileType type);
    }

    /**
     * 获取参数的文件并合并
     *
     * @param fileUrlList
     * @param request
     * @param response
     * @param type
     * @return
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     */
    public static List<SourceCode> mergeCode(String[] fileUrlList, HttpServletRequest request, HttpServletResponse response, FileType type) throws CompressionException {
        List<SourceCode> codeList = new FastList<SourceCode>();
        ContentResponseWrapper wrapperResponse = null;
        //获取参数的文件并合并
        for (String url : fileUrlList) {
            int index = url.lastIndexOf(".");
            if (index < 0) {
                continue;
            }
            if (type.contains(url.substring(index + 1))) {
                String fragment;
                try {
                    url = URLDecoder.decode(url, "utf8");
                } catch (UnsupportedEncodingException e) {
                    throw new CompressionException(e);
                }
                try {
                    //如果是http/https 协议开头则视为跨域
                    if (HttpUtils.isHttpProtocol(url)) {
                        fragment = importCode(HttpUtils.requestFile(url), url, type, request, response);
                    } else {
                        //否则视为同域
                        if (wrapperResponse == null) {
                            wrapperResponse = new ContentResponseWrapper(response);
                        }
                        request.getRequestDispatcher(url).include(request, wrapperResponse);
                        wrapperResponse.flushBuffer();
                        fragment = wrapperResponse.getContent();
                        fragment = importCode(fragment, url, type, request, response);
                        ((ContentResponseStream) wrapperResponse.getOutputStream()).reset();
                    }
                } catch (ServletException e) {
                    throw new CompressionException("ServletException", e);
                } catch (IOException e) {
                    throw new CompressionException(e);
                } catch (GssParserException e) {
                    throw new CompressionException(e);
                } catch (LessException e) {
                    throw new CompressionException(e);
                }
                if (StringUtils.isNotBlank(fragment)) {
                    codeList.add(new SourceCode(url, fragment));
                }
            }
        }

        if (wrapperResponse != null) {
            try {
                wrapperResponse.close();
            } catch (IOException e) {
                throw new CompressionException(e);
            } catch (Throwable throwable) {
                throw new CompressionException("Close Response error", throwable);
            }
        }
        return codeList;
    }


    private static JobDescription.OptimizeStrategy getCompressGssOptimizeStrategy(String levelStr) throws CompressionException {
        JobDescription.OptimizeStrategy level = JobDescription.OptimizeStrategy.SAFE;
        if (StringUtils.isNotBlank(levelStr)) {
            try {
                level = JobDescription.OptimizeStrategy.values()[Integer.parseInt(levelStr)];
            } catch (Exception e) {
                try {
                    level = JobDescription.OptimizeStrategy.valueOf(levelStr);
                } catch (IllegalArgumentException ae) {
                    //
                }
            }
        }
        return level;
    }

    /**
     * 压缩
     *
     * @param fileSourceList
     * @param request
     * @param type
     * @return
     * @throws com.google.common.css.compiler.ast.GssParserException
     *
     * @throws com.log4ic.compressor.exception.CompressionException
     *
     */
    public static String compressCode(List<SourceCode> fileSourceList, HttpServletRequest request, FileType type) throws GssParserException, CompressionException, LessException {
        //压缩
        boolean isDebug = HttpUtils.getBooleanParam(request, "debug");
        String code = "";
        if (fileSourceList.size() > 0) {
            String levelParam = request.getParameter("level");
            switch (type) {
                case JS:
                    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
                    if (StringUtils.isNotBlank(levelParam)) {
                        try {
                            level = CompilationLevel.values()[Integer.parseInt(levelParam)];
                        } catch (Exception e) {
                            try {
                                level = CompilationLevel.valueOf(levelParam);
                            } catch (IllegalArgumentException ae) {
                                //
                            }
                        }
                    }
                    JSSourceFile[] sourceFiles = new JSSourceFile[fileSourceList.size()];
                    for (int i = 0; i < fileSourceList.size(); i++) {
                        SourceCode source = fileSourceList.get(i);
                        if (FileType.TPL.contains(getFileTypeString(source.getFileName()))) {
                            String tpl = JavascriptTemplateEngine.compress(URI.create(source.getFileName()), source.getFileContents());
                            source = new SourceCode(source.getFileName(), tpl);
                        }
                        sourceFiles[i] = JSSourceFile.fromCode(source.getFileName(), source.getFileContents());
                    }
                    code = Compressor.compressJS(sourceFiles, level, isDebug);
                    break;
                case CSS:
                    code = Compressor.compressGss(fileSourceList,
                            getGssFormat(isDebug, request),
                            getCompressGssOptimizeStrategy(levelParam));
                    break;
                case GSS:
                    code = Compressor.compressGss(fileSourceList,
                            getGssFormat(isDebug, request),
                            buildTrueConditions(request),
                            getCompressGssOptimizeStrategy(levelParam));
                    break;
                case LESS:
                case MSS:
                    //压缩代码并设置浏览器断言
                    code = Compressor.compressLess(fileSourceList,
                            getGssFormat(isDebug, request),
                            buildTrueConditions(request),
                            getCompressGssOptimizeStrategy(levelParam));
                    break;
            }
        }
        return code;
    }

    private static JobDescription.OutputFormat getGssFormat(boolean isDebug, HttpServletRequest request) {
        JobDescription.OutputFormat format = JobDescription.OutputFormat.COMPRESSED;
        if (isDebug) {
            format = JobDescription.OutputFormat.DEBUG;
        } else if (HttpUtils.getBooleanParam(request, "pretty")) {
            format = JobDescription.OutputFormat.PRETTY_PRINTED;
        }
        return format;
    }

    private static List<String> buildTrueConditions(HttpServletRequest request) {
        List<String> conditions = new FastList<String>();
        if (!HttpUtils.getBooleanParam(request, "condition")) {
            return conditions;
        }
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
    public static void compress(HttpServletRequest request, HttpServletResponse response) throws CompressionException, GssParserException, LessException {
        compress(request, response, null, null);
    }

    public static void compress(HttpServletRequest request, HttpServletResponse response, CacheManager cacheManager) throws CompressionException, GssParserException, LessException {
        compress(request, response, cacheManager, null);
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
//        try {
//            queryString = URLDecoder.decode(queryString, "utf8");
//        } catch (UnsupportedEncodingException e) {
//            throw new CompressionException(e);
//        }

        String root = null;
        int rootIndex = queryString.indexOf("root=");
        if (rootIndex > -1) {
            root = queryString.substring(rootIndex + 5);
            int rootEnd = root.indexOf("&");
            root = root.substring(0, rootEnd);
            queryString = queryString.substring(0, rootIndex) + queryString.substring(rootEnd + 1);
        }

        String[] params = queryString.split("&");

        List<String> noRepeatParams = Lists.newArrayList();
        //去掉重复参数
        for (String param : params) {
            String[] p = param.split("=");
            if (StringUtils.isNotBlank(p[0])) {
                if (StringUtils.isNotBlank(root)) {
                    p[0] = root + (root.endsWith("/") || p[0].startsWith("/") ? "" : "/") + p[0];
                }
                if (!noRepeatParams.contains(p[0])) {
                    if (p.length > 1) {
                        StringBuffer buffer = new StringBuffer();
                        noRepeatParams.add(buffer.append(p[0]).append("=").append(p[1]).toString());
                    } else {
                        noRepeatParams.add(p[0]);
                    }
                }
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
                case LESS:
                case MSS:
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
            throw new CompressionException("Write code to client error.", e);
        }
    }

    public static FileType getFileType(HttpServletRequest request) throws CompressionException {
        return getFileType(HttpUtils.getRequestUri(request));
    }

    public static String getFileTypeString(String uri) throws CompressionException {
        //分割请求地址
        String[] uris = uri.split("\\.");
        if (uris.length == 0) {
            throw new UnsupportedFileTypeException();
        }
        FileType type;
        //根据请求后缀获取压缩内容类型
        String typeStr = uris[uris.length - 1].toUpperCase();
        int paramIndexTag = typeStr.indexOf("?");
        return typeStr.substring(0, paramIndexTag == -1 ? typeStr.length() : paramIndexTag);
    }

    public static FileType getFileType(String uri) throws CompressionException {
        FileType type;
        //根据请求后缀获取压缩内容类型
        try {
            type = FileType.valueOf(getFileTypeString(uri));
        } catch (Exception e) {
            throw new UnsupportedFileTypeException();
        }
        return type;
    }

    private static String mergeCode(List<SourceCode> codeList, HttpServletRequest request, FileType type) throws LessException, GssParserException, CompressionException {
        StringBuilder builder = new StringBuilder();
        List<String> con = buildTrueConditions(request);

        if (type == FileType.LESS || type == FileType.MSS) {
            codeList = LessEngine.parseLess(codeList, con);
            for (SourceCode code : codeList) {
                if (getFileType(code.getFileName()) == FileType.LESS) {
                    builder.append(code.getFileContents()).append("\n");
                }
            }
        }

        if (type == FileType.GSS || type == FileType.MSS) {
            builder.append(parseGss(codeList, con));
        }

        if (type == FileType.JS || type == FileType.CSS) {
            for (SourceCode code : codeList) {
                if (FileType.TPL.contains(getFileTypeString(code.getFileName()))) {
                    String tpl = JavascriptTemplateEngine.parse(URI.create(code.getFileName()), code.getFileContents());
                    builder.append(tpl).append("\n");
                } else {
                    builder.append(code.getFileContents()).append("\n");
                }
            }
        }

        return builder.toString();
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
    private static String buildCode(final FileType type,
                                    final String queryString, final CacheManager cacheManager,
                                    HttpServletRequest request, HttpServletResponse response,
                                    String fileDomain) throws CompressionException, GssParserException, LessException {
        Cache cache = null;
        try {
            //如果有缓存管理器则进行线程同步操作
            if (cacheManager != null) {
                byte[] cacheLock;
                //获取该次任务的压缩线程锁
                synchronized (progressCacheLock) {
                    cacheLock = progressCacheLock.get(queryString);
                }
                if (cacheLock != null) {
                    //如果已经有在压缩的线程则等待该线程结束
                    synchronized (cacheLock) {
                        logger.debug("已经有在压缩的线程，等待其他线程结果");
                        cacheLock.wait();
                    }
                    //并直接返回缓存内容
                    cache = cacheManager.get(queryString);
                } else {
                    //如过没有转化中的线程则创建该任务（每个任务按照queryString划分）的压缩的线程锁
                    cacheLock = new byte[0];
                    synchronized (progressCacheLock) {
                        progressCacheLock.put(queryString, cacheLock);
                    }
                }
            }
            String code;
            //再次验证cache，如果是等待中的线程则不会再压缩
            if (cache == null || cache.isExpired()) {
                //获取参数的文件并合并
                List<SourceCode> codeList = mergeCode(queryString.split("&"), request, response, type);
                List<SourceCode> sourceCodeList = Lists.newArrayList();
                //修正css里面的路径
                for (SourceCode source : codeList) {
                    SourceCode s = new SourceCode(source.getFileName(), fixUrlPath(source.getFileContents(),
                            source.getFileName(),
                            type,
                            fileDomain
                    ));
                    sourceCodeList.add(s);
                }
                //压缩
                code = HttpUtils.getBooleanParam(request, "nocompress") ? mergeCode(sourceCodeList, request, type) : compressCode(sourceCodeList, request, type);
                if (cacheManager != null) {
                    final String finalCode = code;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                cacheManager.put(queryString, finalCode, type);
                            } catch (Exception e) {
                                logger.error("", e);
                            } finally {
                                byte[] cacheLock;
                                synchronized (progressCacheLock) {
                                    cacheLock = progressCacheLock.remove(queryString);
                                }
                                synchronized (cacheLock) {
                                    cacheLock.notifyAll();
                                }
                            }
                        }
                    }).start();
                }
                return code;
            } else {
                return cache.getContent();
            }
        } catch (Exception e) {
            if (cacheManager != null) {
                byte[] cacheLock;
                synchronized (progressCacheLock) {
                    cacheLock = progressCacheLock.remove(queryString);
                }
                synchronized (cacheLock) {
                    cacheLock.notifyAll();
                }
            }
            throw new CompressionException(e);
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
    public static void compress(HttpServletRequest request, HttpServletResponse response, CacheManager cacheManager, String fileDomain) throws CompressionException, GssParserException, LessException {

        String queryString = HttpUtils.getQueryString(request);

        if (StringUtils.isBlank(queryString)) {
            throw new QueryStringEmptyException();
        }

        //去除重复参数
        queryString = removeDuplicateParameters(queryString);

        FileType type = getFileType(request);

        if ((type == FileType.GSS || type == FileType.MSS || type == FileType.LESS) && HttpUtils.getBooleanParam(request, "condition")) {
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

        String code;

        Cache cache = null;
        if (cacheManager != null) {
            //双保险锁，减少性能消耗
            byte[] lock = progressCacheLock.get(queryString);
            if (lock != null) {
                try {
                    synchronized (progressCacheLock) {
                        lock = progressCacheLock.get(queryString);
                    }
                    if (lock != null) {
                        logger.debug("等待其他线程结果");
                        synchronized (lock) {
                            lock.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
            }
            cache = cacheManager.get(queryString);
        }

        //这里是为了减少同步带来的性能消耗，将线程同步全部放在耗时操作内，在此块进行线程同步过滤
        if (cache == null || cache.isExpired()) {
            //进行构建代码
            if (cacheManager != null) {
                logger.debug("未找到缓存，进行构建代码");
            } else {
                logger.debug("进行构建代码");
            }
            code = buildCode(type, queryString, cacheManager, request, response, fileDomain);
            logger.debug("代码构建完毕");
        } else {
            logger.debug("找到缓存，返回数据流");
            code = cache.getContent();
        }

        logger.debug("输出内容流...");
        writeOutCode(code, type, response);
    }

}
