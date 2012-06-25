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

package com.log4ic.compressor.utils;

import com.google.code.yanf4j.core.SocketOption;
import net.rubyeye.xmemcached.CommandFactory;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.transcoders.Transcoder;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-14
 */
public class MemcachedUtils {
    private MemcachedUtils() throws IOException {
    }

    private static final Logger logger = LoggerFactory.getLogger(MemcachedUtils.class);

    private static String CONFIG_FILE_PATH = "/memcached.xml";

    private static MemcachedClient client;


    private static final byte[] lock = new byte[0];


    public static void setConfigFile(File file) throws FileNotFoundException {
        if (file.exists() && file.isFile()) {
            CONFIG_FILE_PATH = file.getPath();
        } else {
            throw new FileNotFoundException("无法找到配置文件！");
        }
    }


    private static class MemcachedConfig {
        private MemcachedClientBuilder builder;
        private boolean enableHeartBeat = true;
        private int mergeFactor = 150;
        private boolean optimizeMergeBuffer = true;
    }

    private static InetSocketAddress biludAddr(Node node) {
        Node hostNode = node.selectSingleNode("host");
        Node portNode = node.selectSingleNode("port");
        if (hostNode != null && StringUtils.isNotBlank(hostNode.getText())
                && portNode != null && StringUtils.isNotBlank(portNode.getText())) {
            return new InetSocketAddress(hostNode.getText(), Integer.parseInt(portNode.getText()));
        }
        return null;
    }

    private static Map<InetSocketAddress, InetSocketAddress> biludAddrMap(List<Node> nodeList) {
        Map<InetSocketAddress, InetSocketAddress> map = new LinkedHashMap<InetSocketAddress, InetSocketAddress>();
        for (Node node : nodeList) {
            Node masterNode = node.selectSingleNode("master");
            InetSocketAddress masterAddr;
            if (masterNode != null) {
                masterAddr = biludAddr(masterNode);
            } else {
                masterAddr = biludAddr(node);
            }
            Node standbyNode = node.selectSingleNode("standby");
            InetSocketAddress standbyAddr = null;
            if (standbyNode != null) {
                standbyAddr = biludAddr(standbyNode);
            }
            map.put(masterAddr, standbyAddr);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static MemcachedConfig getMemcachedConfig(InputStream in) throws DocumentException {
        SAXReader reader = new SAXReader();

        MemcachedConfig config = new MemcachedConfig();

        Document doc = reader.read(in);

        logger.debug("读取服务器列表...");
        List<Node> nodeList = doc.selectNodes("/memcached/servers/server");
        if (nodeList.isEmpty()) {
            throw new DocumentException(CONFIG_FILE_PATH + " file memcached.servers server element empty!");
        } else {
            logger.debug("读取服务器列表完成，共" + nodeList.size() + "个节点.");
        }

        config.builder = new XMemcachedClientBuilder(biludAddrMap(nodeList));

        Element el = (Element) doc.selectSingleNode("/memcached");
        logger.debug("读取连接池大小设置...");
        Attribute attr = el.attribute("connectionPoolSize");
        if (attr != null) {
            String connPoolSize = attr.getValue();
            if (StringUtils.isNotBlank(connPoolSize)) {
                try {
                    config.builder.setConnectionPoolSize(Integer.parseInt(connPoolSize));
                    logger.debug("连接池大小设置为：" + connPoolSize);
                } catch (Exception e) {
                    logger.error("连接池大小设置参数错误！", e);
                }
            } else {
                logger.error("连接池大小设置参数错误！");
            }
        } else {
            logger.warn("未找到连接池大小设置！");
        }
        logger.debug("启用空闲并发起心跳检测设置...");
        attr = el.attribute("enableHeartBeat");
        if (attr != null) {
            String enableHeartBeatS = attr.getValue();
            if (StringUtils.isNotBlank(enableHeartBeatS)) {
                try {
                    config.enableHeartBeat = Boolean.parseBoolean(enableHeartBeatS);
                    logger.debug("是否启用空闲并发起心跳检测设置为：" + enableHeartBeatS);
                } catch (Exception e) {
                    logger.error("是否启用空闲并发起心跳检测设置参数错误！", e);
                }
            } else {
                logger.error("是否启用空闲并发起心跳检测设置参数错误！");
            }
        } else {
            logger.warn("未找到是否启用空闲并发起心跳检测设置！");
        }
        logger.debug("空闲并发起心跳检测时间设置...");
        attr = el.attribute("sessionIdleTimeout");
        if (attr != null) {
            String sessionIdleTimeout = attr.getValue();
            if (StringUtils.isNotBlank(sessionIdleTimeout)) {
                try {
                    config.builder.getConfiguration().
                            setSessionIdleTimeout(Long.parseLong(sessionIdleTimeout));
                    logger.debug("空闲并发起心跳检测时间设置为：" + sessionIdleTimeout);
                } catch (Exception e) {
                    logger.error("空闲并发起心跳检测时间设置参数错误！", e);
                }
            } else {
                logger.error("空闲并发起心跳检测时间设置参数错误！");
            }
        } else {
            logger.warn("未找到空闲并发起心跳检测设置！");
        }
        //统计连接是否空闲
        logger.debug("统计连接是否空闲设置...");
        attr = el.attribute("statisticsServer");
        if (attr != null) {
            String statisticsServer = attr.getValue();
            if (StringUtils.isNotBlank(statisticsServer)) {
                try {
                    config.builder.getConfiguration().
                            setStatisticsServer(Boolean.parseBoolean(statisticsServer));
                    logger.debug("统计连接是否空闲设置为：" + statisticsServer);
                } catch (Exception e) {
                    logger.error("统计连接是否空闲设置参数错误！", e);
                }
            } else {
                logger.error("统计连接是否空闲设置参数错误！");
            }
        } else {
            logger.warn("未找到统计连接是否空闲设置！");
        }
        logger.debug("统计连接是否空闲间隔设置...");
        attr = el.attribute("statisticsInterval");
        if (attr != null) {
            String statisticsInterval = attr.getValue();
            if (StringUtils.isNotBlank(statisticsInterval)) {
                try {
                    config.builder.getConfiguration().
                            setStatisticsInterval(Long.parseLong(statisticsInterval));
                    logger.debug("统计连接是否空闲间隔设置为：" + statisticsInterval);
                } catch (Exception e) {
                    logger.error("统计连接是否空闲间隔设置参数错误！", e);
                }
            } else {
                logger.error("统计连接是否空闲间隔设置参数错误！");
            }
        } else {
            logger.warn("未找到统计连接是否空闲间隔设置！");
        }
        logger.debug("是否启用合并因子设置...");
        attr = el.attribute("optimizeMergeBuffer");
        if (attr != null) {
            String optimizeMergeBufferS = attr.getValue();
            if (StringUtils.isNotBlank(optimizeMergeBufferS)) {
                try {
                    config.optimizeMergeBuffer = Boolean.parseBoolean(optimizeMergeBufferS);
                    logger.debug("是否启用合并因子设置为：" + optimizeMergeBufferS);
                } catch (Exception e) {
                    logger.error("是否启用合并因子设置参数错误！", e);
                }
            } else {
                logger.error("是否启用合并因子设置参数错误！");
            }
        } else {
            logger.warn("是否启用合并因子设置！");
        }
        logger.debug("合并请求因子数目设置...");
        attr = el.attribute("mergeFactor");
        if (attr != null) {
            String mergeFactorS = attr.getValue();
            if (StringUtils.isNotBlank(mergeFactorS)) {
                try {
                    config.mergeFactor = Integer.parseInt(mergeFactorS);
                    logger.debug("统计连接是否空闲间隔设置为：" + mergeFactorS);
                } catch (Exception e) {
                    logger.error("统计连接是否空闲间隔设置参数错误！", e);
                }
            } else {
                logger.error("统计连接是否空闲间隔设置参数错误！");
            }
        } else {
            logger.warn("未找到统计连接是否空闲间隔设置！");
        }
        logger.debug("协议设置...");
        attr = el.attribute("commandFactory");
        if (attr != null) {
            String commandFactory = attr.getValue();
            if (StringUtils.isNotBlank(commandFactory)) {
                try {
                    config.builder.setCommandFactory((CommandFactory) Class.forName(commandFactory).newInstance());
                    logger.debug("协议设置为：" + commandFactory);
                } catch (Exception e) {
                    logger.error("协议设置参数错误！", e);
                }
            } else {
                logger.error("协议设置参数错误！");
            }
        } else {
            logger.warn("未找到协议设置！");
        }
        config.builder.setCommandFactory(new BinaryCommandFactory());
        logger.debug("网络设置...");
        nodeList = doc.selectNodes("/memcached/socketOption/*");
        if (!nodeList.isEmpty()) {
            for (Node n : nodeList) {
                try {
                    attr = ((Element) n).attribute("type");
                    if (attr == null) {
                        logger.error("type attribute undefined");
                    } else {
                        String type = attr.getValue();
                        if (StringUtils.isNotBlank(type)) {
                            String name = n.getName();
                            String value = n.getStringValue();
                            Class valueType = Class.forName(type);
                            Constructor constructor = SocketOption.class.getConstructor(String.class, Class.class);
                            SocketOption socketOption = (SocketOption) constructor.newInstance(name, valueType);
                            constructor = valueType.getConstructor(String.class);
                            config.builder.setSocketOption(socketOption, constructor.newInstance(value));
                            logger.debug("设置网络选项[" + name + "]为：" + value);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    logger.error("NoSuchMethodException", e);
                } catch (InvocationTargetException e) {
                    logger.error("InvocationTargetException", e);
                } catch (InstantiationException e) {
                    logger.error("InstantiationException", e);
                } catch (IllegalAccessException e) {
                    logger.error("IllegalAccessException", e);
                } catch (ClassNotFoundException e) {
                    logger.error("ClassNotFoundException", e);
                }
            }
        } else {
            logger.warn("未找网络设置！");
        }
        logger.debug("Failure模式...");
        attr = el.attribute("failureMode");
        if (attr != null) {
            String failureMode = attr.getValue();
            if (StringUtils.isNotBlank(failureMode)) {
                try {
                    config.builder.setFailureMode(Boolean.parseBoolean(failureMode));
                    logger.debug("Failure模式设置为：" + failureMode);
                } catch (Exception e) {
                    logger.error("Failure模式设置参数错误！", e);
                }
            } else {
                logger.error("Failure模式设置参数错误！");
            }
        } else {
            logger.warn("未找到Failure模式设置！");
        }
        return config;
    }

    public static MemcachedClient getMemcachedClient(InputStream in) throws DocumentException, IOException {
        MemcachedConfig config = getMemcachedConfig(in);
        MemcachedClient mc = config.builder.build();
        mc.setEnableHeartBeat(config.enableHeartBeat);
        mc.setOptimizeMergeBuffer(config.optimizeMergeBuffer);
        mc.setMergeFactor(config.mergeFactor);
        return mc;
    }


    public static MemcachedClient getSingleMemcachedClient() {
        if (client == null) {
            synchronized (lock) {
                if (client == null || client.isShutdown()) {
                    try {
                        logger.debug("读取Memcached配置文件:" + CONFIG_FILE_PATH);
                        InputStream in = FileUtils.getResourceAsStream(CONFIG_FILE_PATH);
                        if (in == null) {
                            CONFIG_FILE_PATH = "/conf" + CONFIG_FILE_PATH;
                            logger.debug("读取Memcached配置文件:" + CONFIG_FILE_PATH);
                            in = FileUtils.getResourceAsStream(CONFIG_FILE_PATH);
                        }
                        if (in == null) {
                            logger.error(CONFIG_FILE_PATH + " file not exists!");
                        } else {
                            try {
                                client = getMemcachedClient(in);
                            } catch (DocumentException e) {
                                logger.error("", e);
                            } finally {
                                in.close();
                            }
                        }
                    } catch (IOException e) {
                        logger.error("构建memcached客户端失败！", e);
                    }
                }
            }
        }
        return client;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(final String key, final long timeout, final Transcoder<T> transcoder) throws MemcachedException, TimeoutException, InterruptedException {
        return (T) getSingleMemcachedClient().get(key, timeout, transcoder);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(final String key, final Transcoder<T> transcoder) throws MemcachedException, TimeoutException, InterruptedException {
        return (T) getSingleMemcachedClient().get(key, transcoder);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(final String key) throws MemcachedException, TimeoutException, InterruptedException {
        return (T) getSingleMemcachedClient().get(key);
    }

    public static <T> Map<String, T> get(final Collection<String> keyCollections)
            throws TimeoutException, InterruptedException, MemcachedException {
        return getSingleMemcachedClient().get(keyCollections);
    }

    public static boolean set(final String key, final int exp, final Object value)
            throws TimeoutException, InterruptedException, MemcachedException {
        return getSingleMemcachedClient().set(key, exp, value);
    }

    public static void setWithNoReply(final String key, final int exp,
                                      final Object value) throws InterruptedException, MemcachedException {
        getSingleMemcachedClient().setWithNoReply(key, exp, value);
    }

    public static boolean delete(final String key) throws TimeoutException,
            InterruptedException, MemcachedException {
        return getSingleMemcachedClient().delete(key);
    }

    public static boolean touch(final String key, int exp) throws TimeoutException,
            InterruptedException, MemcachedException {
        return getSingleMemcachedClient().touch(key, exp);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAndTouch(final String key, int newExp)
            throws TimeoutException, InterruptedException, MemcachedException {
        return (T) getSingleMemcachedClient().getAndTouch(key, newExp);
    }

    public static void shutdown() throws IOException {
        getSingleMemcachedClient().shutdown();
    }

}
