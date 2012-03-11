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

import com.log4ic.compressor.cache.*;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.utils.Compressor;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 缓存管理器
 *
 * @author 张立鑫 IntelligentCode
 */
public class SimpleCacheManager extends AbstractCacheManager implements Serializable {
    private Map<String, Cache> cache = new FastMap<String, Cache>();

    private static final Logger logger = LoggerFactory.getLogger(SimpleCacheManager.class);

    public SimpleCacheManager(CacheType cacheType, int cacheCount, String dir) throws CacheException {
        super(cacheType, cacheCount, dir);
    }

    public SimpleCacheManager(CacheType cacheType, int cacheCount, final int autoCleanHitTimes, final int autoCleanHourAgo, long autoCleanInterval, String dir) throws CacheException {
        super(cacheType, cacheCount, autoCleanHitTimes, autoCleanHourAgo, autoCleanInterval, dir);
    }


    /**
     * 建立缓存
     *
     * @param key     键
     * @param content 值
     * @return
     */
    private PrivateSetCache createCache(String key, CacheContent content) {
        if (content == null) {
            return null;
        }
        PrivateSetCache cCache = new PrivateSetCache();
        cCache.setCacheType(this.getCacheType());
        //建立缓存内容
        cCache.setContent(content);
        cCache.setKey(key);
        return cCache;
    }

    /**
     * 建立缓存内容
     *
     * @param key      键
     * @param value    值
     * @param fileType 文件类型
     * @return CacheContent
     * @throws com.log4ic.compressor.cache.exception.CacheException
     *
     */
    private CacheContent createCacheContent(String key, String value, Compressor.FileType fileType) throws CacheException {
        return new SimpleCacheContent(key, value, this.cacheType, fileType, this.cacheDir);
    }

    /**
     * 建立缓存内容
     *
     * @param key      键
     * @param file     值
     * @param fileType 文件类型
     * @return CacheContent
     */
    private CacheContent createCacheContent(String key, File file, Compressor.FileType fileType) {
        return new SimpleCacheContent(key, file, this.cacheType, fileType, this.cacheDir);
    }

    private void putCache(String key, Cache cache) {
        //如果缓存超过预设最大限度则移除命中率最低的缓存
        if (this.cache.size() >= this.maxCacheCount) {
            this.removeLowCache();
        }
        //添加新缓存
        this.cache.put(key, cache);
    }

    /**
     * 加入缓存，存在则替换
     *
     * @param key      键
     * @param value    值
     * @param fileType 文件类型
     * @return
     */
    public Cache put(String key, String value, Compressor.FileType fileType) {
        //建立缓存内容
        CacheContent content = null;
        try {
            content = this.createCacheContent(key, value, fileType);
        } catch (CacheException e) {
            logger.error("创建缓存内容失败", e);
        }
        if (content != null) {
            //建立缓存
            Cache cCache = this.createCache(key, content);
            this.putCache(key, cCache);
            return cCache;
        } else {
            return null;
        }
    }


    /**
     * 移除一个缓存
     *
     * @param key
     * @return
     */

    public Cache remove(String key) {
        Cache cache = this.cache.remove(key);
        if (cache != null) {
            cache.remove();
        }
        return cache;
    }

    /**
     * 移除命中率最低的一个缓存 最近最少算法（Least Recently Used，LRU）
     *
     * @return
     */
    public Cache removeLowCache() {

        Collection<Cache> cacheCollection = this.cache.values();

        Cache[] values = new SimpleCache[cacheCollection.size()];

        cacheCollection.toArray(values);
        //按照命中率排序
        Arrays.sort(values, new Comparator<Cache>() {
            public int compare(Cache o1, Cache o2) {
                //取时间最老的
                if (o1.getLastVisitDate().after(o2.getLastVisitDate())) {
                    return 1;
                } else {
                    if (o1.getLastVisitDate().equals(o2.getLastVisitDate())) {
                        return o1.getHitTimes() - o2.getHitTimes();
                    }
                    return -1;
                }
            }
        });
        //移除命中率最低的缓存
        return this.remove(values[0].getKey());
    }


    /**
     * 按照命中次数移除缓存,低于等于此标准的缓存会被移除
     *
     * @param hitTimes 命中次数
     * @return List<SimpleCache>
     */
    public List<Cache> removeLowCache(int hitTimes) {
        return removeLowCache(hitTimes, new Date());
    }

    /**
     * 按照命中次数和最后命中时间移除缓存,低于等于此标准的缓存会被移除
     *
     * @param hitTimes 命中次数
     * @param date     时间
     * @return List<Cache>
     */
    public List<Cache> removeLowCache(int hitTimes, Date date) {

        List<Cache> cacheList = new FastList<Cache>();

        Collection<Cache> cacheCollection = this.cache.values();

        Cache[] values = new SimpleCache[cacheCollection.size()];

        cacheCollection.toArray(values);

        for (Cache c : values) {
            //移除命中率低的缓存
            if (c.getHitTimes() <= hitTimes && c.getLastVisitDate().before(date)) {
                this.remove(c.getKey());
                cacheList.add(c);
            }
        }

        return cacheList;
    }


    /**
     * 获取一个缓存内容，该方法会增加缓存命中次数
     *
     * @param key 键
     * @return Cache
     */
    public Cache get(final String key) {
        PrivateSetCache sCache = (PrivateSetCache) this.cache.get(key);
        if (sCache == null) {
            // 查看缓存文件是否存在
            CacheContent content = null;
            try {
                content = SimpleCacheContent.createFromCacheFile(key, this.cacheType, this.cacheDir);
            } catch (CacheException e) {
                logger.error("从文件创建缓存内容失败", e);
            }
            if (content != null) {
                final SimpleCacheManager manager = this;
                final CacheContent finalContent = content;
                //异步的进行缓存设置
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        manager.putCache(key, createCache(key, finalContent));
                    }
                });
            }
        }

        if (sCache != null) {
            final PrivateSetCache finalSCache = sCache;
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    synchronized (finalSCache) {
                    finalSCache.setHitTimes(finalSCache.getHitTimes() + 1);
                    finalSCache.setLastVisitDate(new Date());
//                    }
                }
            });
        }
        return sCache;
    }

    /**
     * 缓存内是否包含此键
     *
     * @param key 键
     * @return
     */
    public boolean containsKey(String key) {
        return this.cache.containsKey(key);
    }

    /**
     * 缓存内是否包含此值
     *
     * @param value 值
     * @return
     */
    public boolean containsValue(Cache value) {
        return this.cache.containsValue(value);
    }

    /**
     * 获取缓存类型
     *
     * @return
     */
    public CacheType getCacheType() {
        return cacheType;
    }

    /**
     * 获取最大缓存数目
     *
     * @return
     */
    public Integer getMaxCacheCount() {
        return maxCacheCount;
    }

    /**
     * 获取缓存文件目录
     *
     * @return
     */
    public String getCacheDir() {
        return cacheDir;
    }

    /**
     * 获取缓存管理器创建时间
     *
     * @return
     */
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public int getCacheSize() {
        return cache.size();
    }

    @Override
    public void markExpiredCache(Pattern pattern) {
        for (String key : this.cache.keySet()) {
            if (pattern.matcher(key).matches()) {
                this.get(key).setExpired(true);
            }
        }
    }

    /**
     * 重新继承Cache以设置值
     */
    private class PrivateSetCache extends SimpleCache {

        private void setLastVisitDate(Date lastVisitDate) {
            super.lastVisitDate = lastVisitDate;
        }

        private void setHitTimes(int hitTimes) {
            super.hitTimes = hitTimes;
        }

        private void setCacheType(CacheType cacheType) {
            super.cacheType = cacheType;
        }

        private void setContent(CacheContent content) {
            super.content = content;
        }

        private void setKey(String key) {
            super.key = key;
        }
    }
}
