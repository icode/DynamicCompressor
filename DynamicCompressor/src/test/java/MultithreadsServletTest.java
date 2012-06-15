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

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import junit.framework.TestCase;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-06-15
 */
public class MultithreadsServletTest extends TestCase {
    Server server;

    protected void setUp() throws Exception {
        super.setUp();
       /* server = new Server();
        Connector connector = new SocketConnector();
        connector.setPort(8010);
        server.setConnectors(new Connector[]{connector});
        ServletHandler handler = new ServletHandler();
        ServletHolder holder = new ServletHolder("compressor", CompressionServlet.class);
        Map<String, String> compressorServletInitParameters = new HashMap<String, String>();
        compressorServletInitParameters.put("cacheType", "memory");
        compressorServletInitParameters.put("cacheManager",
                "com.log4ic.compressor.cache.impl.memcached.MemcachedCacheManager");
        compressorServletInitParameters.put("cacheDir", "{contextPath}/static/compressed/");
        compressorServletInitParameters.put("cacheCount", "20");
        compressorServletInitParameters.put("autoClean", "false");
        holder.setInitParameters(compressorServletInitParameters);
        handler.addServlet(holder);
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName("compressor");
        mapping.setPathSpecs(new String[]{"/compress.gss", "/compress.css", "/compress.js"});
        handler.addServletMapping(mapping);
        server.setHandler(handler);
        server.start();
        holder.doStart();*/
    }

    protected void tearDown() throws Exception {
        super.tearDown();
//        server.stop();
    }

    private void runThreadHttpRequest(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WebConversation wc = new WebConversation();
                    WebResponse web = wc.getResponse(url);
                    String result = web.getText();
                    System.out.println(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Test
    public void testMultithreadsTest() {
        for (int i = 0; i < 1000; i++) {
            //runThreadHttpRequest("http://127.0.0.1:8080/compress.css?/style/style.css&/style/style1.css&/style/style2.css&/style/style3.css&/style/style4.css&/style/substyle.css");
//            runThreadHttpRequest("http://127.0.0.1:8080/compress.gss?/style/style1.css&/style/style.gss");
//            runThreadHttpRequest("http://127.0.0.1:8080/compress.js?/script/test1.js&/script/test2.js&/script/test3.js&/script/test4.js&/script/test5.js&/script/test6.js&/script/test7.js&/script/test8.js&/script/test9.js&/script/test10.js");
        }
    }
}
