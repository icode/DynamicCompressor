<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  ~ Dynamic Compressor - Java Library
  ~ Copyright (c) 2011-2012, IntelligentCode ZhangLixin.
  ~ All rights reserved.
  ~ intelligentcodemail@gmail.com
  ~
  ~ GUN GPL 3.0 License
  ~
  ~ http://www.gnu.org/licenses/gpl.html
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<html>
<head>
    <title>JS压缩测试</title>
    <script>
        var now = new Date();
    </script>
    <script src="/compress.js?/script/test1.js&/script/test2.js&/script/test3.js&/script/test4.js&/script/test5.js&/script/test6.js&/script/test7.js&/script/test8.js&/script/test9.js&/script/test10.js"></script>
</head>
<body>
<h3>这是JS压缩测试的测试</h3>

<div>
    <label>用时:</label>
            <span>
                <script>
                    document.write((new Date().getTime()) - now.getTime());
                </script>
            </span>
    <label>ms</label>
</div>
</body>
</html>