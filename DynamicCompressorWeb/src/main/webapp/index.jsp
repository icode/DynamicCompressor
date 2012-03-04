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
    <title>静态文件动态压缩器</title>
</head>
<body>
<div>

    <p>动态压缩器能帮我做什么?</p>

    <p>合并压缩JS CSS代码,编译GSS模版,压缩后的版本会被缓存,之后进来的新版本将替换之前的缓存.</p>

    <p>压缩器有较为完善的内存缓存与本地缓存相结合的缓存方式,将服务器响应时间做到更高.</p>

    <p>将CSS内的URL() 替换为绝对路径.减少静态文件对服务器的压力开销.</p>

    <p>无需过多人工干涉,代码管理方便,不会破坏原有代码.</p>

    <p>JS CSS版本变动后只需在URL后面再加一个参数,如代码变动之前为:</p>

    <p>&lt;script src="/compress.js?v=0.001&xx.js&xxx.js"&gt;&lt;/script&gt;</p>

    <p>代码变动后改为:</p>

    <p>&lt;script src="/compress.js?v=0.002&xx.js&xxx.js"&gt;&lt;/script&gt;</p>

    <p>这样压缩器就会去应用新的代码合并压缩.</p>

    <p>值得一提的是 压缩器还支持将远程跨域文件与本地文件合并压缩.</p>

    <p>如何使用压缩器来压缩JS CSS GSS?</p>

    <p>和普通的JS CSS引用相同 唯一不同是以 /compress.*? 开头 （* 为文件类型后缀，目前支持css，gss，js） 所有原来的JS CSS文件作为参数传入以&符号连接.</p>

    <p>如果你熟悉YUI Compressor这款工具对此方式应该并不陌生,这正是YAHOO所有服务器调用JS CSS的方法.</p>

    <p>如果你熟悉YUI框架对此也应该不会陌生 YUI 的 use 方法可以访问YAHOO的在线服务做到合并压缩,其格式也是这样的.</p>

    <p>如:&lt;script src="/compress.js?xx.js&xxx.js"&gt;&lt;/script&gt;</p>

    <p>压缩器只会选取/compress的后缀作为压缩的类型，如上例中的compress.js.</p>

    <p>压缩器目前只支持三种文件格式 JS CSS GSS.</p>

    <p>如果想把CSS和GSS混合压缩使用 请将GSS作为后缀,如:</p>

    <p>&lt;link rel="stylesheet" href="/compress.gss?style.gss&style1.css&style2.css"&gt;</p>

    <p>什么是GSS?</p>

    <p>GSS是被Google Closure Stylesheets处理的特殊文件后缀, 而Stylesheets 这种工具属于Closure Tools包之内，在处理CSS的时候很有用.</p>

    <p>Closure Stylesheets是一个Java程序，它向CSS中添加了变量、函数、条件语句以及混合类型,使得我们更易于处理大型的CSS文件.</p>

    <p>开发者可以使用Google stylesheets (GSS)这种工具来生成web应用程序或者网站所使用的真正的CSS文件.</p>

    <p>参考地址:</p>

    <p><a href="http://www.infoq.com/cn/news/2011/11/Google-Closure-Stylesheets">Google Closure
        Stylesheets让我们更易于使用CSS</a></p>

    <p><a href="http://code.google.com/p/closure-stylesheets/">Closure Stylesheets</a></p>

    <p>压缩器原理:</p>

    <p>压缩CSS时候使用的是Google Closure Stylesheets 压缩JS的时候使用的是Google Closure Compiler</p>

    <p>公共参数(gss css js 压缩通用):</p>

    <p>调试版本加debug参数,只合并不压缩加入nocompress参数</p>

    <p>JS独有参数:</p>

    <p>需要控制JS压缩级别加入level参数,0为只去空格和换行 1为普通压缩 2为高级压缩(慎用,会破坏接口,具体参考Closure Compiler官网),省略level参数默认为1</p>

    <p>CSS/GSS独有参数:</p>

    <p>需要对CSS美化而不压缩采用pretty参数</p>

    <p>参数如何加入:</p>

    <p>在/compress.css/gss?后面直接加入debug或其他参数即可</p>

    <p>如:/compress.css/gss?debug 或 /compress.css/gss?debug=true</p>

    <p>/compress.css/gss?level=0</p>

</div>
</body>
</html>
