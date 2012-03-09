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

import com.log4ic.compressor.cache.CacheType;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.cache.impl.simple.SimpleCacheContent;
import com.log4ic.compressor.cache.impl.simple.SimpleCacheFile;
import com.log4ic.compressor.utils.Compressor;

import java.io.File;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-09
 */
public class MemcachedCacheContent extends SimpleCacheContent{
    public MemcachedCacheContent(String key, String content, CacheType type, Compressor.FileType fileType, String dir) throws CacheException {
        super(key, content, type, fileType, dir);
    }

    public MemcachedCacheContent(String key, File file, CacheType type, Compressor.FileType fileType, String dir) {
        super(key, file, type, fileType, dir);
    }

    public MemcachedCacheContent(String key, SimpleCacheFile file, CacheType type, String dir) {
        super(key, file, type, dir);
    }
}
