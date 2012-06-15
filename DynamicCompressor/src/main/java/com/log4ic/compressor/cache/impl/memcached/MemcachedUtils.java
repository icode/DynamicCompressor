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

import net.spy.memcached.*;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.transcoders.Transcoder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-14
 */
public class MemcachedUtils {
    private MemcachedUtils() throws IOException {
    }

    private static final Logger logger = LoggerFactory.getLogger(MemcachedUtils.class);

    private static MemcachedClient memcachedClient;

    private static final byte[] lock = new byte[0];

    private static StringBuilder serverStringBuilder = new StringBuilder();

    private static String CONFIG_FILE_PATH = "/conf/memcached-servers.xml";

    public static void setConfigFile(File file) throws FileNotFoundException {
        if (file.exists() && file.isFile()) {
            CONFIG_FILE_PATH = file.getPath();
        } else {
            throw new FileNotFoundException("无法找到配置文件！");
        }
    }

    //设置MemcachedClient的日志器
    static {
        logger.debug("读取Memcached配置文件:" + CONFIG_FILE_PATH);
        InputStream in = MemcachedCacheManager.class.getResourceAsStream(CONFIG_FILE_PATH);
        if (in == null) {
            logger.error(CONFIG_FILE_PATH + " file not exists!");
        }
        SAXReader reader = new SAXReader();
        Document doc = null;

        try {
            doc = reader.read(in);
        } catch (DocumentException e) {
            logger.error(CONFIG_FILE_PATH + " file DocumentException!", e);
        }

        if (doc != null) {
            logger.debug("读取服务器列表...");
            List<Node> nodeList = doc.selectNodes("/memcached/servers/server");
            if (nodeList.isEmpty()) {
                logger.error("conf/memcached-servers.xml file memcached.servers server element empty!");
            }

            for (Node node : nodeList) {
                serverStringBuilder.append(node.selectSingleNode("host").getText());
                serverStringBuilder.append(":");
                serverStringBuilder.append(node.selectSingleNode("port").getText());
                serverStringBuilder.append(" ");
            }

            serverStringBuilder.deleteCharAt(serverStringBuilder.length() - 1);

            logger.debug("读取服务器列表完成，共" + nodeList.size() + "个节点.");
        }
    }

    public static MemcachedClient getMemcachedClient() throws IOException {
        if (memcachedClient == null) {
            synchronized (lock) {
                if (memcachedClient == null) {
                    memcachedClient = new MemcachedClient(new BinaryConnectionFactory(),
                            AddrUtil.getAddresses(serverStringBuilder.toString()));
                }
            }
        }
        return memcachedClient;
    }

    /**
     * Get the addresses of available servers.
     * <p/>
     * <p>
     * This is based on a snapshot in time so shouldn't be considered completely
     * accurate, but is a useful for getting a feel for what's working and what's
     * not working.
     * </p>
     *
     * @return point-in-time view of currently available servers
     */
    public static Collection<SocketAddress> getAvailableServers() throws IOException {
        return getMemcachedClient().getAvailableServers();
    }

    /**
     * Get the addresses of unavailable servers.
     * <p/>
     * <p>
     * This is based on a snapshot in time so shouldn't be considered completely
     * accurate, but is a useful for getting a feel for what's working and what's
     * not working.
     * </p>
     *
     * @return point-in-time view of currently available servers
     */
    public static Collection<SocketAddress> getUnavailableServers() throws IOException {
        return getMemcachedClient().getUnavailableServers();
    }

    /**
     * Get a read-only wrapper around the node locator wrapping this instance.
     *
     * @return this instance's NodeLocator
     */
    public static NodeLocator getNodeLocator() throws IOException {
        return getMemcachedClient().getNodeLocator();
    }

    /**
     * Get the default transcoder that's in use.
     *
     * @return this instance's Transcoder
     */
    public static Transcoder<Object> getTranscoder() throws IOException {
        return getMemcachedClient().getTranscoder();
    }

    /**
     * Touch the given key to reset its expiration time with the default
     * transcoder.
     *
     * @param key the key to fetch
     * @param exp the new expiration to set for the given key
     * @return a future that will hold the return value of whether or not the
     *         fetch succeeded
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<Boolean> touch(final String key, final int exp) throws IOException {
        return getMemcachedClient().touch(key, exp);
    }

    /**
     * Touch the given key to reset its expiration time.
     *
     * @param key the key to fetch
     * @param exp the new expiration to set for the given key
     * @param tc  the transcoder to serialize and unserialize value
     * @return a future that will hold the return value of whether or not the
     *         fetch succeeded
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<Boolean> touch(final String key, final int exp,
                                                     final Transcoder<T> tc) throws IOException {
        return getMemcachedClient().touch(key, exp, tc);
    }

    /**
     * Append to an existing value in the cache.
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     *
     * @param cas cas identifier (ignored in the ascii protocol)
     * @param key the key to whose value will be appended
     * @param val the value to append
     * @return a future indicating success, false if there was no change to the
     *         value
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> append(long cas, String key, Object val) throws IOException {
        return getMemcachedClient().append(cas, key, val);
    }

    /**
     * Append to an existing value in the cache.
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     *
     * @param <T>
     * @param cas cas identifier (ignored in the ascii protocol)
     * @param key the key to whose value will be appended
     * @param val the value to append
     * @param tc  the transcoder to serialize and unserialize the value
     * @return a future indicating success
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<Boolean> append(long cas, String key, T val,
                                                      Transcoder<T> tc) throws IOException {
        return getMemcachedClient().append(cas, key, val, tc);
    }

    /**
     * Prepend to an existing value in the cache.
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     *
     * @param cas cas identifier (ignored in the ascii protocol)
     * @param key the key to whose value will be prepended
     * @param val the value to append
     * @return a future indicating success
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> prepend(long cas, String key, Object val) throws IOException {
        return getMemcachedClient().prepend(cas, key, val);
    }

    /**
     * Prepend to an existing value in the cache.
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     *
     * @param <T>
     * @param cas cas identifier (ignored in the ascii protocol)
     * @param key the key to whose value will be prepended
     * @param val the value to append
     * @param tc  the transcoder to serialize and unserialize the value
     * @return a future indicating success
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<Boolean> prepend(long cas, String key, T val,
                                                       Transcoder<T> tc) throws IOException {
        return getMemcachedClient().prepend(cas, key, val, tc);
    }

    /**
     * Asynchronous CAS operation.
     *
     * @param <T>
     * @param key   the key
     * @param casId the CAS identifier (from a gets operation)
     * @param value the new value
     * @param tc    the transcoder to serialize and unserialize the value
     * @return a future that will indicate the status of the CAS
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> Future<CASResponse> asyncCAS(String key, long casId, T value,
                                                   Transcoder<T> tc) throws IOException {
        return getMemcachedClient().asyncCAS(key, casId, value, tc);
    }

    /**
     * Asynchronous CAS operation.
     *
     * @param <T>
     * @param key   the key
     * @param casId the CAS identifier (from a gets operation)
     * @param exp   the expiration of this object
     * @param value the new value
     * @param tc    the transcoder to serialize and unserialize the value
     * @return a future that will indicate the status of the CAS
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> Future<CASResponse> asyncCAS(String key, long casId, int exp,
                                                   T value, Transcoder<T> tc) throws IOException {
        return getMemcachedClient().asyncCAS(key, casId, exp, value, tc);
    }

    /**
     * Asynchronous CAS operation using the default transcoder.
     *
     * @param key   the key
     * @param casId the CAS identifier (from a gets operation)
     * @param value the new value
     * @return a future that will indicate the status of the CAS
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static Future<CASResponse> asyncCAS(String key, long casId, Object value) throws IOException {
        return getMemcachedClient().asyncCAS(key, casId, value);
    }

    /**
     * Perform a synchronous CAS operation.
     *
     * @param <T>
     * @param key   the key
     * @param casId the CAS identifier (from a gets operation)
     * @param value the new value
     * @param tc    the transcoder to serialize and unserialize the value
     * @return a CASResponse
     * @throws OperationTimeoutException if global operation timeout is exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> CASResponse cas(String key, long casId, T value,
                                      Transcoder<T> tc) throws IOException {
        return getMemcachedClient().cas(key, casId, value, tc);
    }

    /**
     * Perform a synchronous CAS operation.
     *
     * @param <T>
     * @param key   the key
     * @param casId the CAS identifier (from a gets operation)
     * @param exp   the expiration of this object
     * @param value the new value
     * @param tc    the transcoder to serialize and unserialize the value
     * @return a CASResponse
     * @throws OperationTimeoutException if global operation timeout is exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> CASResponse cas(String key, long casId, int exp, T value,
                                      Transcoder<T> tc) throws IOException {
        return getMemcachedClient().cas(key, casId, exp, value, tc);
    }

    /**
     * Perform a synchronous CAS operation with the default transcoder.
     *
     * @param key   the key
     * @param casId the CAS identifier (from a gets operation)
     * @param value the new value
     * @return a CASResponse
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static CASResponse cas(String key, long casId, Object value) throws IOException {
        return getMemcachedClient().cas(key, casId, value);
    }

    /**
     * Add an object to the cache iff it does not exist already.
     * <p/>
     * <p>
     * The <code>exp</code> value is passed along to memcached exactly as given,
     * and will be processed per the memcached protocol specification:
     * </p>
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     * <p/>
     * <blockquote>
     * <p>
     * The actual value sent may either be Unix time (number of seconds since
     * January 1, 1970, as a 32-bit value), or a number of seconds starting from
     * current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client
     * is larger than that, the server will consider it to be real Unix time value
     * rather than an offset from current time.
     * </p>
     * </blockquote>
     *
     * @param <T>
     * @param key the key under which this object should be added.
     * @param exp the expiration of this object
     * @param o   the object to store
     * @param tc  the transcoder to serialize and unserialize the value
     * @return a future representing the processing of this operation
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<Boolean> add(String key, int exp, T o,
                                                   Transcoder<T> tc) throws IOException {
        return getMemcachedClient().add(key, exp, o, tc);
    }

    /**
     * Add an object to the cache (using the default transcoder) iff it does not
     * exist already.
     * <p/>
     * <p>
     * The <code>exp</code> value is passed along to memcached exactly as given,
     * and will be processed per the memcached protocol specification:
     * </p>
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     * <p/>
     * <blockquote>
     * <p>
     * The actual value sent may either be Unix time (number of seconds since
     * January 1, 1970, as a 32-bit value), or a number of seconds starting from
     * current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client
     * is larger than that, the server will consider it to be real Unix time value
     * rather than an offset from current time.
     * </p>
     * </blockquote>
     *
     * @param key the key under which this object should be added.
     * @param exp the expiration of this object
     * @param o   the object to store
     * @return a future representing the processing of this operation
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> add(String key, int exp, Object o) throws IOException {
        return getMemcachedClient().add(key, exp, o);
    }

    /**
     * Set an object in the cache regardless of any existing value.
     * <p/>
     * <p>
     * The <code>exp</code> value is passed along to memcached exactly as given,
     * and will be processed per the memcached protocol specification:
     * </p>
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     * <p/>
     * <blockquote>
     * <p>
     * The actual value sent may either be Unix time (number of seconds since
     * January 1, 1970, as a 32-bit value), or a number of seconds starting from
     * current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client
     * is larger than that, the server will consider it to be real Unix time value
     * rather than an offset from current time.
     * </p>
     * </blockquote>
     *
     * @param <T>
     * @param key the key under which this object should be added.
     * @param exp the expiration of this object
     * @param o   the object to store
     * @param tc  the transcoder to serialize and unserialize the value
     * @return a future representing the processing of this operation
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<Boolean> set(String key, int exp, T o,
                                                   Transcoder<T> tc) throws IOException {
        return getMemcachedClient().set(key, exp, o, tc);
    }

    /**
     * Set an object in the cache (using the default transcoder) regardless of any
     * existing value.
     * <p/>
     * <p>
     * The <code>exp</code> value is passed along to memcached exactly as given,
     * and will be processed per the memcached protocol specification:
     * </p>
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     * <p/>
     * <blockquote>
     * <p>
     * The actual value sent may either be Unix time (number of seconds since
     * January 1, 1970, as a 32-bit value), or a number of seconds starting from
     * current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client
     * is larger than that, the server will consider it to be real Unix time value
     * rather than an offset from current time.
     * </p>
     * </blockquote>
     *
     * @param key the key under which this object should be added.
     * @param exp the expiration of this object
     * @param o   the object to store
     * @return a future representing the processing of this operation
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> set(String key, int exp, Object o) throws IOException {
        return getMemcachedClient().set(key, exp, o);
    }

    /**
     * Replace an object with the given value iff there is already a value for the
     * given key.
     * <p/>
     * <p>
     * The <code>exp</code> value is passed along to memcached exactly as given,
     * and will be processed per the memcached protocol specification:
     * </p>
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     * <p/>
     * <blockquote>
     * <p>
     * The actual value sent may either be Unix time (number of seconds since
     * January 1, 1970, as a 32-bit value), or a number of seconds starting from
     * current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client
     * is larger than that, the server will consider it to be real Unix time value
     * rather than an offset from current time.
     * </p>
     * </blockquote>
     *
     * @param <T>
     * @param key the key under which this object should be added.
     * @param exp the expiration of this object
     * @param o   the object to store
     * @param tc  the transcoder to serialize and unserialize the value
     * @return a future representing the processing of this operation
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<Boolean> replace(String key, int exp, T o,
                                                       Transcoder<T> tc) throws IOException {
        return getMemcachedClient().replace(key, exp, o, tc);
    }

    /**
     * Replace an object with the given value (transcoded with the default
     * transcoder) iff there is already a value for the given key.
     * <p/>
     * <p>
     * The <code>exp</code> value is passed along to memcached exactly as given,
     * and will be processed per the memcached protocol specification:
     * </p>
     * <p/>
     * <p>
     * Note that the return will be false any time a mutation has not occurred.
     * </p>
     * <p/>
     * <blockquote>
     * <p>
     * The actual value sent may either be Unix time (number of seconds since
     * January 1, 1970, as a 32-bit value), or a number of seconds starting from
     * current time. In the latter case, this number of seconds may not exceed
     * 60*60*24*30 (number of seconds in 30 days); if the number sent by a client
     * is larger than that, the server will consider it to be real Unix time value
     * rather than an offset from current time.
     * </p>
     * </blockquote>
     *
     * @param key the key under which this object should be added.
     * @param exp the expiration of this object
     * @param o   the object to store
     * @return a future representing the processing of this operation
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> replace(String key, int exp, Object o) throws IOException {
        return getMemcachedClient().replace(key, exp, o);
    }

    /**
     * Get the given key asynchronously.
     *
     * @param <T>
     * @param key the key to fetch
     * @param tc  the transcoder to serialize and unserialize value
     * @return a future that will hold the return value of the fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> GetFuture<T> asyncGet(final String key, final Transcoder<T> tc) throws IOException {
        return getMemcachedClient().asyncGet(key, tc);
    }

    /**
     * Get the given key asynchronously and decode with the default transcoder.
     *
     * @param key the key to fetch
     * @return a future that will hold the return value of the fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static GetFuture<Object> asyncGet(final String key) throws IOException {
        return getMemcachedClient().asyncGet(key);
    }

    /**
     * Gets (with CAS support) the given key asynchronously.
     *
     * @param <T>
     * @param key the key to fetch
     * @param tc  the transcoder to serialize and unserialize value
     * @return a future that will hold the return value of the fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<CASValue<T>> asyncGets(final String key,
                                                             final Transcoder<T> tc) throws IOException {
        return getMemcachedClient().asyncGets(key, tc);
    }

    /**
     * Gets (with CAS support) the given key asynchronously and decode using the
     * default transcoder.
     *
     * @param key the key to fetch
     * @return a future that will hold the return value of the fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<CASValue<Object>> asyncGets(final String key) throws IOException {
        return getMemcachedClient().asyncGets(key);
    }

    /**
     * Gets (with CAS support) with a single key.
     *
     * @param <T>
     * @param key the key to get
     * @param tc  the transcoder to serialize and unserialize value
     * @return the result from the cache and CAS id (null if there is none)
     * @throws OperationTimeoutException if global operation timeout is exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> CASValue<T> gets(String key, Transcoder<T> tc) throws IOException {
        return getMemcachedClient().gets(key, tc);
    }

    /**
     * Get with a single key and reset its expiration.
     *
     * @param <T>
     * @param key the key to get
     * @param exp the new expiration for the key
     * @param tc  the transcoder to serialize and unserialize value
     * @return the result from the cache (null if there is none)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> CASValue<T> getAndTouch(String key, int exp, Transcoder<T> tc) throws IOException {
        return getMemcachedClient().getAndTouch(key, exp, tc);
    }

    /**
     * Get a single key and reset its expiration using the default transcoder.
     *
     * @param key the key to get
     * @param exp the new expiration for the key
     * @return the result from the cache and CAS id (null if there is none)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static CASValue<Object> getAndTouch(String key, int exp) throws IOException {
        return getMemcachedClient().getAndTouch(key, exp);
    }

    /**
     * Gets (with CAS support) with a single key using the default transcoder.
     *
     * @param key the key to get
     * @return the result from the cache and CAS id (null if there is none)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static CASValue<Object> gets(String key) throws IOException {
        return getMemcachedClient().gets(key);
    }

    /**
     * Get with a single key.
     *
     * @param <T>
     * @param key the key to get
     * @param tc  the transcoder to serialize and unserialize value
     * @return the result from the cache (null if there is none)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> T get(String key, Transcoder<T> tc) throws IOException {
        return getMemcachedClient().get(key, tc);
    }

    /**
     * Get with a single key and decode using the default transcoder.
     *
     * @param key the key to get
     * @return the result from the cache (null if there is none)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static Object get(String key) throws IOException {
        return getMemcachedClient().get(key);
    }

    /**
     * Asynchronously get a bunch of objects from the cache.
     *
     * @param <T>
     * @param keyIter Iterator that produces keys.
     * @param tcIter  an iterator of transcoders to serialize and unserialize
     *                values; the transcoders are matched with the keys in the same
     *                order. The minimum of the key collection length and number of
     *                transcoders is used and no exception is thrown if they do not
     *                match
     * @return a Future result of that fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Iterator<String> keyIter,
                                                       Iterator<Transcoder<T>> tcIter) throws IOException {
        return getMemcachedClient().asyncGetBulk(keyIter, tcIter);
    }

    /**
     * Asynchronously get a bunch of objects from the cache.
     *
     * @param <T>
     * @param keys   the keys to request
     * @param tcIter an iterator of transcoders to serialize and unserialize
     *               values; the transcoders are matched with the keys in the same
     *               order. The minimum of the key collection length and number of
     *               transcoders is used and no exception is thrown if they do not
     *               match
     * @return a Future result of that fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> BulkFuture<Map<String, T>> asyncGetBulk(Collection<String> keys,
                                                              Iterator<Transcoder<T>> tcIter) throws IOException {
        return getMemcachedClient().asyncGetBulk(keys, tcIter);
    }

    /**
     * Asynchronously get a bunch of objects from the cache.
     *
     * @param <T>
     * @param keyIter Iterator for the keys to request
     * @param tc      the transcoder to serialize and unserialize values
     * @return a Future result of that fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> BulkFuture<Map<String, T>> asyncGetBulk(Iterator<String> keyIter,
                                                              Transcoder<T> tc) throws IOException {
        return getMemcachedClient().asyncGetBulk(keyIter, tc);
    }

    /**
     * Asynchronously get a bunch of objects from the cache.
     *
     * @param <T>
     * @param keys the keys to request
     * @param tc   the transcoder to serialize and unserialize values
     * @return a Future result of that fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> BulkFuture<Map<String, T>> asyncGetBulk(Collection<String> keys,
                                                              Transcoder<T> tc) throws IOException {
        return getMemcachedClient().asyncGetBulk(keys, tc);
    }

    /**
     * Asynchronously get a bunch of objects from the cache and decode them with
     * the given transcoder.
     *
     * @param keyIter Iterator that produces the keys to request
     * @return a Future result of that fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static BulkFuture<Map<String, Object>> asyncGetBulk(
            Iterator<String> keyIter) throws IOException {
        return getMemcachedClient().asyncGetBulk(keyIter);
    }

    /**
     * Asynchronously get a bunch of objects from the cache and decode them with
     * the given transcoder.
     *
     * @param keys the keys to request
     * @return a Future result of that fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static BulkFuture<Map<String, Object>> asyncGetBulk(Collection<String> keys) throws IOException {
        return getMemcachedClient().asyncGetBulk(keys);
    }

    /**
     * Varargs wrapper for asynchronous bulk gets.
     *
     * @param <T>
     * @param tc   the transcoder to serialize and unserialize value
     * @param keys one more more keys to get
     * @return the future values of those keys
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> BulkFuture<Map<String, T>> asyncGetBulk(Transcoder<T> tc,
                                                              String... keys) throws IOException {
        return getMemcachedClient().asyncGetBulk(tc, keys);
    }

    /**
     * Varargs wrapper for asynchronous bulk gets with the default transcoder.
     *
     * @param keys one more more keys to get
     * @return the future values of those keys
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static BulkFuture<Map<String, Object>> asyncGetBulk(String... keys) throws IOException {
        return getMemcachedClient().asyncGetBulk(keys);
    }

    /**
     * Get the given key to reset its expiration time.
     *
     * @param key the key to fetch
     * @param exp the new expiration to set for the given key
     * @return a future that will hold the return value of the fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<CASValue<Object>> asyncGetAndTouch(final String key,
                                                                     final int exp) throws IOException {
        return getMemcachedClient().asyncGetAndTouch(key, exp);
    }

    /**
     * Get the given key to reset its expiration time.
     *
     * @param key the key to fetch
     * @param exp the new expiration to set for the given key
     * @param tc  the transcoder to serialize and unserialize value
     * @return a future that will hold the return value of the fetch
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static <T> OperationFuture<CASValue<T>> asyncGetAndTouch(final String key,
                                                                    final int exp, final Transcoder<T> tc) throws IOException {
        return getMemcachedClient().asyncGetAndTouch(key, exp, tc);
    }

    /**
     * Get the values for multiple keys from the cache.
     *
     * @param <T>
     * @param keyIter Iterator that produces the keys
     * @param tc      the transcoder to serialize and unserialize value
     * @return a map of the values (for each value that exists)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> Map<String, T> getBulk(Iterator<String> keyIter,
                                             Transcoder<T> tc) throws IOException {
        return getMemcachedClient().getBulk(keyIter, tc);
    }

    /**
     * Get the values for multiple keys from the cache.
     *
     * @param keyIter Iterator that produces the keys
     * @return a map of the values (for each value that exists)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static Map<String, Object> getBulk(Iterator<String> keyIter) throws IOException {
        return getMemcachedClient().getBulk(keyIter);
    }

    /**
     * Get the values for multiple keys from the cache.
     *
     * @param <T>
     * @param keys the keys
     * @param tc   the transcoder to serialize and unserialize value
     * @return a map of the values (for each value that exists)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> Map<String, T> getBulk(Collection<String> keys,
                                             Transcoder<T> tc) throws IOException {
        return getMemcachedClient().getBulk(keys, tc);
    }

    /**
     * Get the values for multiple keys from the cache.
     *
     * @param keys the keys
     * @return a map of the values (for each value that exists)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static Map<String, Object> getBulk(Collection<String> keys) throws IOException {
        return getMemcachedClient().getBulk(keys);
    }

    /**
     * Get the values for multiple keys from the cache.
     *
     * @param <T>
     * @param tc   the transcoder to serialize and unserialize value
     * @param keys the keys
     * @return a map of the values (for each value that exists)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static <T> Map<String, T> getBulk(Transcoder<T> tc, String... keys) throws IOException {
        return getMemcachedClient().getBulk(tc, keys);
    }

    /**
     * Get the values for multiple keys from the cache.
     *
     * @param keys the keys
     * @return a map of the values (for each value that exists)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static Map<String, Object> getBulk(String... keys) throws IOException {
        return getMemcachedClient().getBulk(keys);
    }

    /**
     * Get the versions of all of the connected memcacheds.
     *
     * @return a Map of SocketAddress to String for connected servers
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static Map<SocketAddress, String> getVersions() throws IOException {
        return getMemcachedClient().getVersions();
    }

    /**
     * Get all of the stats from all of the connections.
     *
     * @return a Map of a Map of stats replies by SocketAddress
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static Map<SocketAddress, Map<String, String>> getStats() throws IOException {
        return getMemcachedClient().getStats();
    }

    /**
     * Get a set of stats from all connections.
     *
     * @param arg which stats to get
     * @return a Map of the server SocketAddress to a map of String stat keys to
     *         String stat values.
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static Map<SocketAddress, Map<String, String>> getStats(final String arg) throws IOException {
        return getMemcachedClient().getStats(arg);
    }

    /**
     * Increment the given key by the given amount.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the amount to increment
     * @return the new value (-1 if the key doesn't exist)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long incr(String key, long by) throws IOException {
        return getMemcachedClient().incr(key, by);
    }

    /**
     * Increment the given key by the given amount.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the amount to increment
     * @return the new value (-1 if the key doesn't exist)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long incr(String key, int by) throws IOException {
        return getMemcachedClient().incr(key, by);
    }

    /**
     * Decrement the given key by the given value.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the value
     * @return the new value (-1 if the key doesn't exist)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long decr(String key, long by) throws IOException {
        return getMemcachedClient().decr(key, by);
    }

    /**
     * Decrement the given key by the given value.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the value
     * @return the new value (-1 if the key doesn't exist)
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long decr(String key, int by) throws IOException {
        return getMemcachedClient().decr(key, by);
    }

    /**
     * Increment the given counter, returning the new value.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the amount to increment
     * @param def the default value (if the counter does not exist)
     * @param exp the expiration of this object
     * @return the new value, or -1 if we were unable to increment or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long incr(String key, long by, long def, int exp) throws IOException {
        return getMemcachedClient().incr(key, by, def, exp);
    }

    /**
     * Increment the given counter, returning the new value.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the amount to increment
     * @param def the default value (if the counter does not exist)
     * @param exp the expiration of this object
     * @return the new value, or -1 if we were unable to increment or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long incr(String key, int by, long def, int exp) throws IOException {
        return getMemcachedClient().incr(key, by, def, exp);
    }

    /**
     * Decrement the given counter, returning the new value.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the amount to decrement
     * @param def the default value (if the counter does not exist)
     * @param exp the expiration of this object
     * @return the new value, or -1 if we were unable to decrement or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long decr(String key, long by, long def, int exp) throws IOException {
        return getMemcachedClient().decr(key, by, def, exp);
    }

    /**
     * Decrement the given counter, returning the new value.
     * <p/>
     * Due to the way the memcached server operates on items, incremented and
     * decremented items will be returned as Strings with any operations that
     * return a value.
     *
     * @param key the key
     * @param by  the amount to decrement
     * @param def the default value (if the counter does not exist)
     * @param exp the expiration of this object
     * @return the new value, or -1 if we were unable to decrement or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long decr(String key, int by, long def, int exp) throws IOException {
        return getMemcachedClient().decr(key, by, def, exp);
    }

    /**
     * Asychronous increment.
     *
     * @param key key to increment
     * @param by  the amount to increment the value by
     * @return a future with the incremented value, or -1 if the increment failed.
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Long> asyncIncr(String key, long by) throws IOException {
        return getMemcachedClient().asyncIncr(key, by);
    }

    /**
     * Asychronous increment.
     *
     * @param key key to increment
     * @param by  the amount to increment the value by
     * @return a future with the incremented value, or -1 if the increment failed.
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Long> asyncIncr(String key, int by) throws IOException {
        return getMemcachedClient().asyncIncr(key, by);
    }

    /**
     * Asynchronous decrement.
     *
     * @param key key to increment
     * @param by  the amount to increment the value by
     * @return a future with the decremented value, or -1 if the increment failed.
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Long> asyncDecr(String key, long by) throws IOException {
        return getMemcachedClient().asyncDecr(key, by);
    }

    /**
     * Asynchronous decrement.
     *
     * @param key key to increment
     * @param by  the amount to increment the value by
     * @return a future with the decremented value, or -1 if the increment failed.
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Long> asyncDecr(String key, int by) throws IOException {
        return getMemcachedClient().asyncDecr(key, by);
    }

    /**
     * Increment the given counter, returning the new value.
     *
     * @param key the key
     * @param by  the amount to increment
     * @param def the default value (if the counter does not exist)
     * @return the new value, or -1 if we were unable to increment or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long incr(String key, long by, long def) throws IOException {
        return getMemcachedClient().incr(key, by, def);
    }

    /**
     * Increment the given counter, returning the new value.
     *
     * @param key the key
     * @param by  the amount to increment
     * @param def the default value (if the counter does not exist)
     * @return the new value, or -1 if we were unable to increment or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long incr(String key, int by, long def) throws IOException {
        return getMemcachedClient().incr(key, by, def);
    }

    /**
     * Decrement the given counter, returning the new value.
     *
     * @param key the key
     * @param by  the amount to decrement
     * @param def the default value (if the counter does not exist)
     * @return the new value, or -1 if we were unable to decrement or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long decr(String key, long by, long def) throws IOException {
        return getMemcachedClient().decr(key, by, def);
    }

    /**
     * Decrement the given counter, returning the new value.
     *
     * @param key the key
     * @param by  the amount to decrement
     * @param def the default value (if the counter does not exist)
     * @return the new value, or -1 if we were unable to decrement or add
     * @throws OperationTimeoutException if the global operation timeout is
     *                                   exceeded
     * @throws IllegalStateException     in the rare circumstance where queue is too
     *                                   full to accept any more requests
     */
    public static long decr(String key, int by, long def) throws IOException {
        return getMemcachedClient().decr(key, by, def);
    }

    /**
     * Delete the given key from the cache.
     * <p/>
     * <p>
     * The hold argument specifies the amount of time in seconds (or Unix time
     * until which) the client wishes the server to refuse "add" and "replace"
     * commands with this key. For this amount of item, the item is put into a
     * delete queue, which means that it won't possible to retrieve it by the
     * "get" command, but "add" and "replace" command with this key will also fail
     * (the "set" command will succeed, however). After the time passes, the item
     * is finally deleted from server memory.
     * </p>
     *
     * @param key  the key to delete
     * @param hold how long the key should be unavailable to add commands
     * @return whether or not the operation was performed
     * @deprecated Hold values are no longer honored.
     */
    @Deprecated
    public static OperationFuture<Boolean> delete(String key, int hold) throws IOException {
        return getMemcachedClient().delete(key, hold);
    }

    /**
     * Delete the given key from the cache.
     *
     * @param key the key to delete
     * @return whether or not the operation was performed
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> delete(String key) throws IOException {
        return getMemcachedClient().delete(key);
    }

    /**
     * Flush all caches from all servers with a delay of application.
     *
     * @param delay the period of time to delay, in seconds
     * @return whether or not the operation was accepted
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> flush(final int delay) throws IOException {
        return getMemcachedClient().flush(delay);
    }

    /**
     * Flush all caches from all servers immediately.
     *
     * @return whether or not the operation was performed
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static OperationFuture<Boolean> flush() throws IOException {
        return getMemcachedClient().flush();
    }

    public static Set<String> listSaslMechanisms() throws IOException {
        return getMemcachedClient().listSaslMechanisms();
    }

    /**
     * Shut down immediately.
     */
    public static void shutdown() throws IOException {
        getMemcachedClient().shutdown();
    }

    /**
     * Shut down this client gracefully.
     *
     * @param timeout the amount of time time for shutdown
     * @param unit    the TimeUnit for the timeout
     * @return result of the shutdown request
     */
    public static boolean shutdown(long timeout, TimeUnit unit) throws IOException {
        return shutdown(timeout, unit);
    }

    /**
     * Wait for the queues to die down.
     *
     * @param timeout the amount of time time for shutdown
     * @param unit    the TimeUnit for the timeout
     * @return result of the request for the wait
     * @throws IllegalStateException in the rare circumstance where queue is too
     *                               full to accept any more requests
     */
    public static boolean waitForQueues(long timeout, TimeUnit unit) throws IOException {
        return getMemcachedClient().waitForQueues(timeout, unit);
    }

    /**
     * Add a connection observer.
     * <p/>
     * If connections are already established, your observer will be called with
     * the address and -1.
     *
     * @param obs the ConnectionObserver you wish to add
     * @return true if the observer was added.
     */
    public static boolean addObserver(ConnectionObserver obs) throws IOException {
        return addObserver(obs);
    }

    /**
     * Remove a connection observer.
     *
     * @param obs the ConnectionObserver you wish to add
     * @return true if the observer existed, but no longer does
     */
    public static boolean removeObserver(ConnectionObserver obs) throws IOException {
        return getMemcachedClient().removeObserver(obs);
    }

    public static void connectionEstablished(SocketAddress sa, int reconnectCount) throws IOException {
        getMemcachedClient().connectionEstablished(sa, reconnectCount);
    }

    public static void connectionLost(SocketAddress sa) throws IOException {
        getMemcachedClient().connectionLost(sa);
    }
}
