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

import com.log4ic.compressor.utils.FileUtils;
import com.log4ic.compressor.utils.template.JavascriptTemplateEngine;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-09-12
 */
public class JavascriptTemplateEngineTest {
    @Test
    public void parseJavascriptTemplate() throws Exception {
        InputStream stream = JavascriptTemplateEngineTest.class.getResourceAsStream("test.tpl");

        String html = FileUtils.InputStream2String(stream);

        String compressedHtml = JavascriptTemplateEngine.compress("app", html, JavascriptTemplateEngine.Mode.AMD);

        System.out.println(compressedHtml);
    }
}
