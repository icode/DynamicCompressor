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

package com.log4ic.compressor.cache.impl.simple;

import com.log4ic.compressor.cache.CacheContent;
import com.log4ic.compressor.cache.CacheFile;
import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.utils.ByteArrayUtils;
import com.log4ic.compressor.utils.Compressor;
import com.log4ic.compressor.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 缓存内容
 *
 * @author 张立鑫 IntelligentCode
 */
public class SimpleCacheContent implements Serializable, CacheContent {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheContent.class);

    protected CacheType cacheType;
    protected String content = null;
    protected CacheFile cacheFile;
    protected String cacheDir;
    protected String key;

    protected SimpleCacheContent(String key, CacheType type, String dir) {
        this.cacheType = type;
        this.cacheDir = FileUtils.appendSeparator(dir);
        this.key = key;
    }

    public SimpleCacheContent(String key, String content, CacheType type, Compressor.FileType fileType, String dir) throws CacheException {
        this(key, type, dir);
        //将缓存写入文件
        this.cacheFile = this.writeContent(content, fileType);
        //如果缓存类型为内存，则放入内存缓存
        if (type == CacheType.MEMORY) {
            this.content = content;
        }
    }

    public SimpleCacheContent(String key, File file, CacheType type, Compressor.FileType fileType, String dir) {
        this(key, type, dir);
        this.cacheFile = new SimpleCacheFile(file, fileType);
    }

    public SimpleCacheContent(String key, CacheFile file, CacheType type, String dir) {
        this(key, type, dir);
        this.cacheFile = file;
    }

    public SimpleCacheContent() {
    }

    public static CacheContent createFromCacheFile(String key, CacheType cacheType, String dir) throws CacheException {
        CacheFile file = lookupCacheFile(key, dir);
        if (file != null) {
            return new SimpleCacheContent(key, file, cacheType, dir);
        }
        return null;
    }

    public static CacheContent createFromCacheFile(String key, CacheType cacheType, Compressor.FileType fileType, String dir) throws CacheException {
        CacheFile file = findCacheFile(key, fileType, dir);
        if (file != null) {
            return new SimpleCacheContent(key, file, cacheType, dir);
        }
        return null;
    }

    protected String readContent() {
        return this.cacheFile.readContent();
    }

    protected static String encodeFileName(String name) throws CacheException {
        MessageDigest md = null;
        //用MD5编码参数作为缓存文件名称
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //MD5
        byte[] results = new byte[0];
        try {
            if (md != null) {
                results = md.digest(name.getBytes("UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new CacheException("编码格式转换失败", e);
        }
        return ByteArrayUtils.toStringHex(results);
    }


    /**
     * 将缓存写入文件，如果文件存在则直接返回该文件
     *
     * @param content
     * @param fileType
     * @return
     * @throws com.log4ic.compressor.cache.exception.CacheException
     *
     */
    protected CacheFile writeContent(String content, Compressor.FileType fileType) throws CacheException {

        logger.debug("尝试写入缓存文件....");
        CacheFile file = findCacheFile(this.key, fileType, this.cacheDir);

        if (file.exists()) {
            logger.debug("缓存文件已经存在!");
            return file;
        }

        File f = FileUtils.writeFile(content, file.getPath());
        logger.debug("写入缓存文件完毕!");
        return new SimpleCacheFile(f, fileType);
    }

    /**
     * 移除所有缓存内容
     */
    public void remove() {
        this.content = null;
        this.cacheFile.delete();
        this.cacheFile = null;
    }

    /**
     * 查找缓存文件
     *
     * @param key
     * @param fileType
     * @param dir
     * @return
     * @throws com.log4ic.compressor.cache.exception.CacheException
     *
     */
    public static CacheFile findCacheFile(String key, Compressor.FileType fileType, String dir) throws CacheException {
        String type = fileType.name().toLowerCase();

        String filePath = dir + type + File.separator + encodeFileName(key) + "." + type;

        File file = new File(filePath);

        return new SimpleCacheFile(file, fileType);
    }

    /**
     * 查找缓存文件
     *
     * @param key
     * @param dir
     * @return
     * @throws com.log4ic.compressor.cache.exception.CacheException
     *
     */
    public static CacheFile lookupCacheFile(String key, String dir) throws CacheException {
        Compressor.FileType[] types = Compressor.FileType.values();

        for (Compressor.FileType type : types) {
            CacheFile file = findCacheFile(key, type, dir);
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    /**
     * 获取缓存内容
     *
     * @return
     */
    public String getContent() {
        if (this.content == null) {
            if (this.cacheType == CacheType.MEMORY) {
                this.content = readContent();
                return this.content;
            } else if (this.cacheType == CacheType.FILE) {
                return readContent();
            }
        } else {
            return this.content;
        }
        return null;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public CacheFile getCacheFile() {
        return cacheFile;
    }

    public File getCacheDir() {
        return new File(cacheDir);
    }

    public String getKey() {
        return key;
    }

    public Compressor.FileType getFileType() {
        return this.cacheFile.getFileType();
    }
}
