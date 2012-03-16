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

import com.log4ic.compressor.utils.Compressor;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 缓存管理器接口
 *
 * @author 张立鑫 IntelligentCode
 */
public interface CacheManager extends Serializable {
    public void put(String key, String value, Compressor.FileType fileType);


    /**
     * 移除一个缓存
     *
     * @param key
     * @return
     */

    public void remove(String key);


//    /**
//     * 移除命中率最低的一个缓存 最近最少算法（Least Recently Used，LRU）
//     *
//     * @return
//     */
//    public Cache removeLowCache();


//    /**
//     * 按照命中次数移除缓存,低于等于此标准的缓存会被移除
//     *
//     * @param hitTimes 命中次数
//     * @return List<Cache>
//     */
//    public List<Cache> removeLowCache(int hitTimes);

//    /**
//     * 按照命中次数和最后命中时间移除缓存,低于等于此标准的缓存会被移除
//     *
//     * @param hitTimes 命中次数
//     * @param date     时间
//     * @return List<Cache>
//     */
//    public List<Cache> removeLowCache(int hitTimes, Date date);


    /**
     * 获取一个缓存内容，该方法会增加缓存命中次数
     *
     * @param key 键
     * @return Cache
     */
    public Cache get(String key);

    /**
     * 缓存内是否包含此键
     *
     * @param key 键
     * @return
     */
    public boolean containsKey(String key);

//    /**
//     * 缓存内是否包含此值
//     *
//     * @param value 值
//     * @return
//     */
//    public boolean containsValue(Cache value);

    /**
     * 获取缓存类型
     *
     * @return
     */
    public CacheType getCacheType();

    /**
     * 获取最大缓存数目
     *
     * @return
     */
    public Integer getMaxCacheCount();

    /**
     * 获取缓存文件目录
     *
     * @return
     */
    public String getCacheDir();

    /**
     * 获取缓存管理器创建时间
     *
     * @return
     */
    public Date getCreateDate();

    /**
     * 获取缓存数目
     *
     * @return int
     */
    public int getCacheSize();

    /**
     * 根据正则匹配key,标记过期缓存
     *
     * @param pattern
     */
    public void markExpiredCache(Pattern pattern);
}
