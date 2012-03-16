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

package com.log4ic.compressor.cache.impl.memcached.logger;

import net.spy.memcached.compat.log.AbstractLogger;
import net.spy.memcached.compat.log.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-14
 */
public class Slf4JLogger extends AbstractLogger {

    private final Logger log;

    public Slf4JLogger(String name) {
        super(name);
        log = LoggerFactory.getLogger(name);
    }

    public boolean isDebugEnabled() {
        return (log.isDebugEnabled());
    }

    public boolean isInfoEnabled() {
        return (log.isInfoEnabled());
    }

    public void log(Level level, Object message, Throwable e) {
        String msg = message + "";
        switch (level == null ? Level.FATAL : level) {
            case DEBUG:
                log.debug(msg, e);
                break;
            case INFO:
                log.info(msg, e);
                break;
            case WARN:
                log.warn(msg, e);
            case ERROR:
            case FATAL:
                log.error(msg, e);
                break;
        }
    }
}
