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

import com.log4ic.compressor.cache.AbstractCacheManager;
import com.log4ic.compressor.cache.Cache;
import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.utils.Compressor;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-09
 */
public class MemcachedCacheManager extends AbstractCacheManager {
    public MemcachedCacheManager(CacheType cacheType, int cacheCount, String dir) throws CacheException {
        super(cacheType, cacheCount, dir);
    }

    public MemcachedCacheManager(CacheType cacheType, int cacheCount, final int autoCleanHitTimes, final int autoCleanHourAgo, long autoCleanInterval, String dir) throws CacheException {
        super(cacheType, cacheCount, autoCleanHitTimes, autoCleanHourAgo, autoCleanInterval, dir);
    }

    @Override
    public Date getCreateDate() {
        return super.getCreateDate();
    }

    @Override
    public int getCacheSize() {
        return 0;
    }

    @Override
    public void markExpiredCache(Pattern pattern) {

    }

    @Override
    public Cache put(String key, String value, Compressor.FileType fileType) {
        return null;
    }

    @Override
    public Cache remove(String key) {
        return null;
    }

    @Override
    public Cache removeLowCache() {
        return null;
    }

    @Override
    public List<Cache> removeLowCache(int hitTimes) {
        return super.removeLowCache(hitTimes);
    }

    @Override
    public List<Cache> removeLowCache(int hitTimes, Date date) {
        return null;
    }

    @Override
    public Cache get(String key) {
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public boolean containsValue(Cache value) {
        return false;
    }

}
