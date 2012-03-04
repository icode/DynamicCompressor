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

import java.io.File;
import java.io.Serializable;

/**
 * 缓存内容接口
 *
 * @author 张立鑫 IntelligentCode
 */
public interface CacheContent extends Serializable {
    /**
     * 获取缓存内容
     *
     * @return
     */
    public String getContent();

    /**
     * 获取缓存类型
     *
     * @return
     */
    public CacheType getCacheType();

    /**
     * 获取缓存文件
     *
     * @return
     */
    public CacheFile getCacheFile();

    /**
     * 获取缓存目录
     *
     * @return
     */
    public File getCacheDir();

    /**
     * 获取缓存键
     *
     * @return
     */
    public String getKey();

    /**
     * 获取缓存的文件类型
     *
     * @return
     */
    public Compressor.FileType getFileType();

    /**
     * 移除所有缓存内容
     */
    public void remove();
}
