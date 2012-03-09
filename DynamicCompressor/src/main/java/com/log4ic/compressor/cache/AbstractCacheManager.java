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

package com.log4ic.compressor.cache;

import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.utils.Compressor;
import com.log4ic.compressor.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-09
 */
public abstract class AbstractCacheManager implements CacheManager {
    protected CacheType cacheType;
    protected Integer maxCacheCount;
    protected String cacheDir;
    protected Date createDate;

    private static final Logger logger = LoggerFactory.getLogger(AbstractCacheManager.class);

    /**
     * 缓存管理器
     *
     * @param cacheType  缓存类型
     * @param cacheCount 缓存最大数目
     * @param dir        本地缓存文件存放位置
     * @throws com.log4ic.compressor.cache.exception.CacheException
     *
     */
    public AbstractCacheManager(CacheType cacheType, int cacheCount, String dir) throws CacheException {
        if (cacheCount <= 0) {
            throw new CacheException("最大缓存数目不能小于等于0");
        }
        this.cacheType = cacheType;
        this.maxCacheCount = cacheCount;
        this.cacheDir = FileUtils.appendSeparator(dir);
        this.createDate = new Date();
    }

    /**
     * @param cacheType         缓存类型
     * @param cacheCount        缓存最大数目
     * @param autoCleanHitTimes 自动清理命中低于多少的缓存
     * @param autoCleanHourAgo  自动清理多少小时前的缓存
     * @param autoCleanInterval 多久执行一次自动清理
     * @param dir               本地缓存目录
     * @throws com.log4ic.compressor.cache.exception.CacheException
     *
     */
    public AbstractCacheManager(CacheType cacheType, int cacheCount, final int autoCleanHitTimes, final int autoCleanHourAgo, long autoCleanInterval, String dir) throws CacheException {
        this(cacheType, cacheCount, dir);
        Timer clearCacheTimer = new Timer();
        final AbstractCacheManager manager = this;
        clearCacheTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (manager.getMaxCacheCount() > 0) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.HOUR, -autoCleanHourAgo);
                        removeLowCache(autoCleanHitTimes, calendar.getTime());
                        System.gc();
                    }
                } catch (Exception e) {
                    logger.error("处理缓存出错", e);
                }
            }
        }, 0, autoCleanInterval);
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
}
