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

import java.io.Serializable;
import java.util.Date;

/**
 * 缓存项
 *
 * @author 张立鑫 IntelligentCode
 */
public interface Cache extends Serializable {
    /**
     * 创建时间
     */
    public Date getLastVisitDate();

    /**
     * 最后访问时间
     */
    public Date getCreateDate();

    /**
     * 命中次数
     */
    public int getHitTimes();

    /**
     * 缓存内容
     */
    public CacheContent getContent();

    /**
     * 缓存类型
     */
    public CacheType getCacheType();

    /**
     * 获取自身键
     *
     * @return
     */
    public String getKey();

    /**
     * 移除该缓存内容
     */
    public void removeContent();

    /**
     * 获取缓存是否过期
     *
     * @return boolean
     */
    public boolean isExpired();

    /**
     * 设置缓存是否过期
     *
     * @param expired
     */
    public void setExpired(boolean expired);
}
