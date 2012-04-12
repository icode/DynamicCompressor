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
import com.log4ic.compressor.cache.AbstractCacheManager;
import com.log4ic.compressor.cache.Cache;
import com.log4ic.compressor.cache.CacheFile;
import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.cache.impl.simple.SimpleCache;
import com.log4ic.compressor.utils.Compressor;
import javolution.util.FastList;
import net.spy.memcached.transcoders.LongTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-09
 */
public class MemcachedCacheManager extends AbstractCacheManager {

    private static final Logger logger = LoggerFactory.getLogger(MemcachedCacheManager.class);

    protected static final String MEMCACHED_KEY_LIST_NAME = "COMPRESSOR_CACHE_MANAGER_KEY";

    protected static final String MEMCACHED_KEY_LIST_SIZE_NAME = "COMPRESSOR_CACHE_MANAGER_KEY_LIST_SIZE";


    public MemcachedCacheManager(CacheType cacheType, int cacheCount, String dir) throws CacheException {
        super(cacheType, cacheCount, dir);
    }

    public MemcachedCacheManager(CacheType cacheType, int cacheCount, final int autoCleanHitTimes, final int autoCleanHourAgo, long autoCleanInterval, String dir) throws CacheException {
        //todo 暂不支持手动清理
        super(cacheType, cacheCount, dir);
    }

    @Override
    public List<Cache> removeLowCache(int hitTimes, Date date) {
        //todo 暂不支持手动清理
        return null;
    }

    @Override
    public Date getCreateDate() {
        return super.getCreateDate();
    }

    /**
     * 获取缓存内键列表
     *
     * @return
     * @throws CacheException
     * @throws InvalidProtocolBufferException
     */
    protected List<String> getKeyList() throws CacheException, InvalidProtocolBufferException {


        return null;
    }


    @Override
    public int getCacheSize() {
        try {
            long size = MemcachedUtils.get(MEMCACHED_KEY_LIST_SIZE_NAME, new LongTranscoder());
            return Integer.parseInt(size + "");
        } catch (InvalidProtocolBufferException e) {
            logger.error("获取键列表错误！", e);
            return 0;
        } catch (IOException e) {
            logger.error("获取键列表错误！", e);
            return 0;
        }
    }

    @Override
    public void markExpiredCache(Pattern pattern) {
        logger.debug("标记过期缓存...");
        List<String> list = new FastList<String>();
        try {
            for (String key : this.getKeyList()) {
                if (pattern.matcher(key).matches()) {
                    list.add(key);
                }
            }
        } catch (CacheException e) {
            logger.error("获取键列表失败", e);
        } catch (InvalidProtocolBufferException e) {
            logger.error("获取键列表失败", e);
        }
        if (list.size() > 0) {
            Map<String, Object> map = null;
            try {
                map = MemcachedUtils.getBulk(list);
            } catch (IOException e) {
                logger.error("IOException", e);
            }
            if (map != null) {
                for (String key : map.keySet()) {
                    byte[] bytes = (byte[]) map.get(key);

                    Cache cache = null;
                    try {
                        cache = new MemcachedCache(key, this.cacheDir, this.cacheType, bytes);
                    } catch (CacheException e) {
                        logger.error("转化缓存对象失败", e);
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("转化缓存对象失败", e);
                    }

                    if (cache != null && !cache.isExpired()) {
                        cache.setExpired(true);
                    }
                }
            }
        }

        logger.debug("标记了" + list.size() + "个过期缓存!");
    }


    @Override
    public void put(final String key, final String value, final Compressor.FileType fileType) {
        final CacheType cacheType = this.cacheType;
        final String cacheDir = this.cacheDir;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MemcachedUtils.set(key, 0, new MemcachedCache(key, value, cacheType, fileType, cacheDir).toByteArray());
                    //todo key list
                } catch (CacheException e) {
                    logger.error("put cache exception", e);
                } catch (InvalidProtocolBufferException e) {
                    logger.error("put cacheKeyList exception", e);
                } catch (IOException e) {
                    logger.error("put cacheKeyList exception", e);
                }
            }
        }).start();
    }

    @Override
    public void remove(String key) {
        try {
            MemcachedUtils.delete(key);
            //todo 设置键列表

            CacheFile file = SimpleCache.lookupCacheFile(key, this.cacheDir);
            if (file != null) {
                file.delete();
            }
        } catch (CacheException e) {
            logger.error("删除缓存出错", e);
        } catch (IOException e) {
            logger.error("删除缓存出错", e);
        }
    }

    @Override
    public Cache get(final String key) {
        try {
            logger.debug("尝试获取缓存...");
            Object data = MemcachedUtils.get(key);

            if (data != null) {
                logger.debug("发现缓存...");
                return new MemcachedCache(key, this.cacheDir, this.cacheType, (byte[]) data);
            } else {
                logger.debug("未发现缓存，查看缓存文件是否存在...");
                // 查看缓存文件是否存在
                Cache cache = null;
                try {
                    cache = SimpleCache.createFromCacheFile(key, this.cacheType, this.cacheDir);
                } catch (CacheException e) {
                    logger.error("从文件创建缓存内容失败", e);
                }
                if (cache != null) {
                    logger.debug("从缓存文件建立内容");
                    final MemcachedCacheManager manager = this;
                    final Cache finalCache = cache;
                    //异步的进行缓存设置
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            manager.put(key, finalCache.getContent(), finalCache.getFileType());
                        }
                    }).start();
                    return cache;
                }
            }
        } catch (CacheException e) {
            logger.error("获取key错误", e);
        } catch (InvalidProtocolBufferException e) {
            logger.error("获取key错误", e);
        } catch (IOException e) {
            logger.error("获取key错误", e);
        }
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return this.get(key) != null;
    }
}
