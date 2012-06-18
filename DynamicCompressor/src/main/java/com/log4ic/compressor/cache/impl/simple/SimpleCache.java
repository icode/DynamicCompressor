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

import com.log4ic.compressor.cache.Cache;
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
import java.util.Date;

/**
 * @author 张立鑫 IntelligentCode
 */
public class SimpleCache implements Serializable, Cache {
    private static final Logger logger = LoggerFactory.getLogger(SimpleCache.class);
    /**
     * 创建时间
     */
    private Date createDate = new Date();
    /**
     * 最后访问时间
     */
    protected Date lastVisitDate = this.createDate;
    /**
     * 命中次数
     */
    protected int hitTimes = 0;
    protected CacheType cacheType;
    protected String content = null;
    protected CacheFile cacheFile;
    protected String cacheDir;
    protected String key;

    protected SimpleCache(String key, CacheType type, String dir) {
        this.cacheType = type;
        this.cacheDir = FileUtils.appendSeparator(dir);
        this.key = key;
    }

    public SimpleCache(String key, String content, CacheType type, Compressor.FileType fileType, String dir) throws CacheException {
        this(key, type, dir);
        //将缓存写入文件
        this.cacheFile = this.writeContent(content, fileType);
        //如果缓存类型为内存，则放入内存缓存
        if (type == CacheType.MEMORY) {
            this.content = content;
        }
    }

    public SimpleCache(String key, CacheFile file, CacheType type, String dir) {
        this(key, type, dir);
        this.cacheFile = file;
    }

    public static Cache createFromCacheFile(String key, CacheType cacheType, String dir) throws CacheException {
        CacheFile file = lookupCacheFile(key, dir);
        if (file != null) {
            return new SimpleCache(key, file, cacheType, dir);
        }
        return null;
    }

    public static Cache createFromCacheFile(String key, CacheType cacheType, Compressor.FileType fileType, String dir) throws CacheException {
        CacheFile file = findCacheFile(key, fileType, dir);
        if (file != null) {
            return new SimpleCache(key, file, cacheType, dir);
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
     * 将缓存写入文件，如果文件存并且未过期在则直接返回该文件
     *
     * @param content
     * @return
     * @throws com.log4ic.compressor.cache.exception.CacheException
     *
     */
    protected CacheFile writeContent(String content, Compressor.FileType fileType) throws CacheException {
        return writeContent(this.key, content, fileType, this.cacheDir, this.getCreateDate().getTime());
    }

    /**
     * 将缓存写入文件，如果文件存并且未过期在则直接返回该文件
     *
     * @param key
     * @param content
     * @param fileType
     * @param cacheDir
     * @param createDate
     * @return
     * @throws CacheException
     */
    public static CacheFile writeContent(String key, String content, Compressor.FileType fileType,
                                         String cacheDir, long createDate) throws CacheException {
        CacheFile file = findCacheFile(key, fileType, cacheDir);

        if (!file.getFile().exists() || file.getFile().lastModified() + 1000 < createDate) {
            logger.debug("尝试写入缓存文件....");
            File f = FileUtils.writeFile(content, file.getPath());
            logger.debug("写入缓存文件完毕!");
            return new SimpleCacheFile(f, fileType);
        }

        return file;
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


    public CacheFile getCacheFile() {
        return cacheFile;
    }

    public File getCacheDir() {
        return new File(cacheDir);
    }

    public Compressor.FileType getFileType() {
        return this.cacheFile.getFileType();
    }

    /**
     * 缓存是否过期
     */
    protected boolean isExpired = false;

    public Date getLastVisitDate() {
        return lastVisitDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public int getHitTimes() {
        return hitTimes;
    }


    public CacheType getCacheType() {
        return this.cacheType;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public void removeContent() {
        this.content = null;
        this.cacheFile.delete();
        this.cacheFile = null;
    }


    @Override
    public boolean isExpired() {
        return this.isExpired;
    }

    @Override
    public void setExpired(boolean expired) {
        this.isExpired = expired;
    }
}
