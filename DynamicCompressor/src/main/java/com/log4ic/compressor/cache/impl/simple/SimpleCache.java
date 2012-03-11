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
import com.log4ic.compressor.cache.CacheContent;
import com.log4ic.compressor.cache.CacheType;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 张立鑫 IntelligentCode
 */
public class SimpleCache implements Serializable, Cache {

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
    /**
     * 缓存内容
     */
    protected CacheContent content;
    /**
     * 缓存类型
     */
    protected CacheType cacheType = CacheType.MEMORY;

    /**
     * self key
     */
    protected String key;

    /**
     * 缓存是否过期     */
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

    public CacheContent getContent() {
        return content;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public String getKey() {
        return key;
    }

    public void remove() {
        if (this.content != null) {
            this.getContent().remove();
        }
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
