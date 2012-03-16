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

import com.log4ic.compressor.cache.*;
import com.log4ic.compressor.cache.exception.CacheException;
import com.log4ic.compressor.cache.impl.simple.SimpleCacheManager;
import com.log4ic.compressor.utils.Compressor;
import org.junit.Test;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-12
 */
public class SimpleCacheManagerTest {
    @Test
    public void markExpiredCacheTest() throws CacheException {
        AbstractCacheManager cacheManager = new SimpleCacheManager(CacheType.MEMORY, 10, this.getClass().getResource("/").getPath());
        cacheManager.put("11", "1230", Compressor.FileType.JS);
        cacheManager.put("22&33&44", "1231", Compressor.FileType.GSS);
        cacheManager.put("33", "1232", Compressor.FileType.JS);
        cacheManager.put("/abc/d&&44&&/123/456", "1233", Compressor.FileType.CSS);
        printAllCache(cacheManager);
        System.out.println("\n标记过期缓存...\n");
        cacheManager.markExpiredCache(".*22.*");
        cacheManager.markExpiredCache(".*\\/abc\\/d.*");
        printAllCache(cacheManager);
    }

    private void printAllCache(AbstractCacheManager cacheManager) {
        Cache cache = cacheManager.get("11");
        printCache(cache);
        cache = cacheManager.get("22&33&44");
        printCache(cache);
        cache = cacheManager.get("33");
        printCache(cache);
        cache = cacheManager.get("/abc/d&&44&&/123/456");
        printCache(cache);
    }

    private void printCache(Cache cache) {
        if (cache == null) {
            //进行构建代码
            System.out.println("进行构建代码...");
        } else {
            CacheContent cacheContent = cache.getContent();
            String code = cacheContent.getContent();
            System.out.println(cache.getKey() + ":从缓存取得代码...：" + code);
        }
    }
}
