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

import com.log4ic.compressor.cache.CacheFile;
import com.log4ic.compressor.utils.Compressor;
import com.log4ic.compressor.utils.FileUtils;

import java.io.File;
import java.io.Serializable;

/**
 * 缓存文件
 *
 * @author 张立鑫 IntelligentCode
 */
public class SimpleCacheFile implements CacheFile, Serializable {
    private File cacheFile;
    private Compressor.FileType fileType;

    public SimpleCacheFile(File cacheFile, Compressor.FileType fileType) {
        this.cacheFile = cacheFile;
        this.fileType = fileType;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public Compressor.FileType getFileType() {
        return fileType;
    }

    public boolean exists() {
        return this.cacheFile.exists();
    }

    public boolean delete() {
        return this.cacheFile.delete();
    }

    public String getPath() {
        return this.cacheFile.getPath();
    }

    public String readContent() {
        if (this.cacheFile != null && this.cacheFile.exists()) {
            return FileUtils.readFile(this.cacheFile);
        }
        return null;
    }
}
