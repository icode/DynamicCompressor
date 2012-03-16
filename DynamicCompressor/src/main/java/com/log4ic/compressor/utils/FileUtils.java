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

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * @author 张立鑫 IntelligentCode
 */
public class FileUtils {
    private FileUtils() {
    }

    /**
     * 将数据写入文件
     *
     * @param content
     * @param filePath
     * @return
     */
    public static File writeFile(byte[] content, String filePath) {
        FileOutputStream out = null;
        FileChannel outChannel = null;
        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
        }

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        ByteBuffer outBuffer = ByteBuffer.allocate(content.length);
        outBuffer.put(content);
        outBuffer.flip();
        try {
            out = new FileOutputStream(file);

            outChannel = out.getChannel();

            outChannel.write(outBuffer);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file.exists() ? file : null;
    }


    /**
     * 将字符串写入文件
     *
     * @param content
     * @param filePath
     * @return
     */
    public static File writeFile(String content, String filePath) {
        byte[] contentByte = new byte[0];
        if (StringUtils.isNotBlank(content)) {
            try {
                contentByte = content.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return writeFile(contentByte, filePath);
    }

    /**
     * 读取文件
     *
     * @param file
     * @return
     */
    public static String readFile(File file) {
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            return null;
        }
        FileInputStream fileInputStream = null;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuffer contentBuffer = new StringBuffer();
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            fileInputStream = new FileInputStream(file);
            FileChannel channel = fileInputStream.getChannel();
            while (true) {
                buffer.clear();
                int pos = channel.read(buffer);
                if (pos == -1) {
                    break;
                }
                buffer.flip();
                charset = Charset.forName("UTF-8");
                decoder = charset.newDecoder();
                charBuffer = decoder.decode(buffer);
                contentBuffer.append(charBuffer.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return contentBuffer.toString();
    }


    public static String appendSeparator(String path) {
        return path + (path.endsWith(File.separator) ? "" : File.separator);
    }
}
