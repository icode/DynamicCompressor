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
import com.log4ic.compressor.cache.Cache;
import com.log4ic.compressor.cache.CacheFile;
import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.cache.impl.memcached.protobuf.MemcachedCacheProtobuf;
import com.log4ic.compressor.cache.impl.simple.SimpleCache;
import com.log4ic.compressor.utils.Compressor;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-09
 */
public class MemcachedCache implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(MemcachedCache.class);

    private MemcachedCacheProtobuf.MemcachedCache memcachedCache;
    /**
     * 命中次数
     */
    protected CacheType cacheType;
    protected CacheFile cacheFile;
    protected String cacheDir;
    protected String key;

    private MemcachedCache(String key, CacheType type, String dir) {
        this.key = key;
        this.cacheType = type;
        this.cacheDir = dir;
    }

    public MemcachedCache(String key, final String content, CacheType type, final Compressor.FileType fileType, String dir) throws CacheException {
        this(key, type, dir);
        //将缓存写入文件
        buildProtobuf(content, fileType);
        try {
            writeContent();
        } catch (CacheException e) {
            logger.error("写入缓存文件失败!", e);
        }
    }

    public MemcachedCache(String key, CacheFile file, CacheType type, String dir) throws CacheException {
        this(key, type, dir);
        buildProtobuf(file.readContent(), file.getFileType());
    }

    public MemcachedCache(String key, String dir, CacheType type, byte[] bytes) throws InvalidProtocolBufferException, CacheException {
        this(key, type, dir);
        buildProtobuf(bytes);
        try {
            writeContent();
        } catch (CacheException e) {
            logger.error("写入缓存文件失败!", e);
        }
    }

    private void writeContent() throws CacheException {
        this.cacheFile = SimpleCache.writeContent(this.getKey(), this.getContent(),
                Compressor.FileType.valueOf(this.memcachedCache.getFileType().name()),
                this.cacheDir, this.getMemcachedCacheProtobuf().getCreateDate());
    }


    protected void buildProtobuf(String value, Compressor.FileType type) throws CacheException {
        MemcachedCacheProtobuf.MemcachedCache.Builder cacheBuilder = MemcachedCacheProtobuf.MemcachedCache.newBuilder();
        cacheBuilder.setCacheFilePath(SimpleCache.findCacheFile(this.key, type, this.cacheDir).getPath());
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

    @Override
    public Date getLastVisitDate() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return new Date(this.getMemcachedCacheProtobuf().getCreateDate());
    }

    @Override
    public int getHitTimes() {
        return 0;
    }

    @Override
    public CacheType getCacheType() {
        return this.cacheType;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void removeContent() {
        this.cacheFile.delete();
        this.cacheFile = null;
        try {
            MemcachedUtils.delete(key);
        } catch (InterruptedException e) {
            logger.error("删除缓存失败", e);
        } catch (TimeoutException e) {
            logger.error("删除缓存失败", e);
        } catch (MemcachedException e) {
            logger.error("删除缓存失败", e);
        } /*finally {
            try {
                MemcachedUtils.shutdown();
            } catch (IOException e) {
                logger.error("close memcached client error", e);
            }
        }*/
    }

    @Override
    public boolean isExpired() {
        return this.getMemcachedCacheProtobuf().getCreateDate() == 0;
    }

    @Override
    public void setExpired(boolean expired) {
        if (this.isExpired() != expired) {
            MemcachedCacheProtobuf.MemcachedCache cache = this.getMemcachedCacheProtobuf();
            cache = cache.toBuilder().setCreateDate(0).build();
            this.memcachedCache = cache;
            try {
                MemcachedUtils.set(this.getKey(), 0, cache.toByteArray());
            } catch (InterruptedException e) {
                logger.error("标记缓存过期失败!", e);
            } catch (TimeoutException e) {
                logger.error("标记缓存过期失败!", e);
            } catch (MemcachedException e) {
                logger.error("标记缓存过期失败!", e);
            } /*finally {
                try {
                    MemcachedUtils.shutdown();
                } catch (IOException e) {
                    logger.error("close memcached client error", e);
                }
            }*/
        }
    }

    @Override
    public CacheFile getCacheFile() {
        return this.cacheFile;
    }

    @Override
    public File getCacheDir() {
        return new File(this.cacheDir);
    }

    public byte[] toByteArray() {
        return this.getMemcachedCacheProtobuf().toByteArray();
    }
}
