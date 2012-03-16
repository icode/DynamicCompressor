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

package com.log4ic.compressor.cache.impl.memcached;

import com.google.protobuf.InvalidProtocolBufferException;
import com.log4ic.compressor.cache.CacheFile;
import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.cache.impl.memcached.protobuf.MemcachedCacheProtobuf;
import com.log4ic.compressor.cache.impl.simple.SimpleCacheContent;
import com.log4ic.compressor.utils.Compressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-09
 */
public class MemcachedCacheContent extends SimpleCacheContent {
    private static Logger logger = LoggerFactory.getLogger(MemcachedCacheContent.class);

    public MemcachedCacheContent(String key, final String content, CacheType type, final Compressor.FileType fileType, String dir) throws CacheException {
        super(key, type, dir);
        //将缓存写入文件
        final MemcachedCacheContent co = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    co.cacheFile = co.writeContent(content, fileType);
                } catch (CacheException e) {
                    logger.error("写入缓存文件失败!", e);
                }
            }
        }).run();
        buildProtobuf(content, fileType);
    }

    public MemcachedCacheContent(String key, File file, CacheType type, Compressor.FileType fileType, String dir) throws CacheException {
        super(key, file, type, fileType, dir);
        buildProtobuf(this.cacheFile.readContent(), fileType);
    }

    public MemcachedCacheContent(String key, CacheFile file, CacheType type, String dir) throws CacheException {
        super(key, file, type, dir);
        buildProtobuf(file.readContent(), file.getFileType());
    }

    public MemcachedCacheContent(String key, String dir, CacheType type, byte[] bytes) throws InvalidProtocolBufferException, CacheException {
        buildProtobuf(bytes);
        this.key = key;
        this.cacheDir = dir;
        this.cacheType = type;
        final MemcachedCacheContent co = this;
        //异步写入文件
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    co.cacheFile = co.writeContent(co.getContent(), Compressor.FileType.valueOf(co.memcachedCache.getFileType().name()));
                } catch (CacheException e) {
                    logger.error("写入缓存文件失败!", e);
                }
            }
        }).run();
    }

    private MemcachedCacheProtobuf.MemcachedCache memcachedCache;

    protected void buildProtobuf(String value, Compressor.FileType type) throws CacheException {
        MemcachedCacheProtobuf.MemcachedCache.Builder cacheBuilder = MemcachedCacheProtobuf.MemcachedCache.newBuilder();
        cacheBuilder.setCacheFilePath(MemcachedCacheContent.findCacheFile(this.key, type, this.cacheDir).getPath());
        cacheBuilder.setContent(value);
        cacheBuilder.setCreateDate(System.currentTimeMillis());
        cacheBuilder.setFileType(MemcachedCacheProtobuf.MemcachedCache.FileType.valueOf(type.name()));
        this.memcachedCache = cacheBuilder.build();
    }

    protected void buildProtobuf(byte[] bytes) throws InvalidProtocolBufferException {
        this.memcachedCache = MemcachedCacheProtobuf.MemcachedCache.parseFrom(bytes);
    }

    public MemcachedCacheProtobuf.MemcachedCache getMemcachedCacheProtobuf() {
        return this.memcachedCache;
    }

    @Override
    public String getContent() {
        return this.memcachedCache.getContent();
    }

    @Override
    public Compressor.FileType getFileType() {
        return this.cacheFile.getFileType();
    }
}
