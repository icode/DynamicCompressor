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

import com.google.common.collect.Lists;
import com.google.common.css.SourceCode;
import com.log4ic.compressor.utils.FileUtils;
import com.log4ic.compressor.utils.less.LessEngine;
import com.log4ic.compressor.utils.less.exception.LessException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-08-29
 */
public class LessEngineTest {
    private static final Logger logger = LoggerFactory.getLogger(LessEngineTest.class);
    CountDownLatch latch = new CountDownLatch(100);

    @Test
    public void MultithreadingParseTest() throws IOException, InterruptedException {
        List<SourceCode> codeList = Lists.newArrayList();
        SourceCode code = new SourceCode("test.less", FileUtils.readFile(new File(this.getClass().getResource("test.less").getFile())));
        codeList.add(code);
        for (int i = 0; i < 100; i++) {
            logger.debug("runParseThread[{}]....", i);
            runParseThread(i, codeList);
        }
        latch.await();
    }

    private void runParseThread(final int index, final List<SourceCode> codeList) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LessEngine.parseLess(codeList, null);
                    logger.debug(index + " thread parse done.");

                } catch (LessException e) {
                    if (index == 0)
                        logger.debug(index + " thread parse error.", e);
                } finally {
                    latch.countDown();
                }
            }
        });
        t.start();
    }
}
