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
import com.log4ic.compressor.cache.CacheContent;
import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.cache.impl.memcached.protobuf.MemcachedCacheProtobuf;
import com.log4ic.compressor.utils.Compressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-09
 */
public class MemcachedCache implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(MemcachedCache.class);


    private MemcachedCacheContent cacheContent;

    public MemcachedCache(String key, String value, CacheType cacheType, Compressor.FileType fileType, String cacheDir) throws CacheException {
        this.cacheContent = new MemcachedCacheContent(key, value, cacheType, fileType, cacheDir);
    }

    public MemcachedCache(CacheContent cacheContent) throws CacheException {
        this.cacheContent = new MemcachedCacheContent(cacheContent.getKey(), cacheContent.getContent(),
                cacheContent.getCacheType(), cacheContent.getFileType(), cacheContent.getCacheDir().getPath());
    }

    public MemcachedCache(String key, byte[] data, CacheType cacheType, String cacheDir) throws CacheException, InvalidProtocolBufferException {
        this.cacheContent = new MemcachedCacheContent(key, cacheDir, cacheType, data);
    }


    @Override
    public Date getLastVisitDate() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return new Date(this.cacheContent.getMemcachedCacheProtobuf().getCreateDate());
    }

    @Override
    public int getHitTimes() {
        return 0;
    }

    @Override
    public CacheContent getContent() {
        return this.cacheContent;
    }

    @Override
    public CacheType getCacheType() {
        return this.getContent().getCacheType();
    }

    @Override
    public String getKey() {
        return this.getContent().getKey();
    }

    @Override
    public void removeContent() {
        if (this.getContent() != null) {
            this.getContent().remove();
        }
    }

    @Override
    public boolean isExpired() {
        return this.cacheContent.getMemcachedCacheProtobuf().getCreateDate() == 0;
    }

    @Override
    public void setExpired(boolean expired) {
        if (this.isExpired() != expired) {
            MemcachedCacheProtobuf.MemcachedCache cache = this.cacheContent.getMemcachedCacheProtobuf();
            cache = cache.toBuilder().setCreateDate(0).build();
            byte[] data = cache.toByteArray();
            try {
                this.cacheContent = new MemcachedCacheContent(this.getKey(), this.getContent().getCacheDir().getPath(),
                        this.getCacheType(), data);
            } catch (InvalidProtocolBufferException e) {
                logger.error("标记缓存过期失败!", e);
            } catch (CacheException e) {
                logger.error("标记缓存过期失败!", e);
            }
            try {
                MemcachedUtils.getMemcachedClient().set(this.getKey(), 0, data);
            } catch (IOException e) {
                logger.error("标记缓存过期失败!", e);
            }
        }
    }

    public byte[] toByteArray() {
        return this.cacheContent.getMemcachedCacheProtobuf().toByteArray();
    }
}
