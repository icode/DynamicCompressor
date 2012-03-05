<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  ~ Dynamic Compressor - Java Library
  ~ Copyright (c) 2011-2012, Intelligent ZhangLixin.
  ~ All rights reserved.
  ~ intelligentmail@gmail.com
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

<h3>动态压缩器能帮我做什么?</h3>

<p>合并压缩JS CSS代码,编译GSS模版,压缩后的版本会被缓存,之后进来的新版本将替换之前的缓存.</p>

<p>压缩器有较为完善的内存缓存与本地缓存相结合的缓存方式,将服务器响应时间做到更高.</p>

<p>将CSS内的URL() 替换为绝对路径.亦可设置单独的静态文件服务器负载,减少静态文件对服务器的压力开销.</p>

<p>无需过多人工干涉,代码管理方便,不会破坏原有代码.</p>

<h3>JS CSS版本变动后只需在URL后面再加一个参数,如代码变动之前为:</h3>

<p>&lt;script src="/compress.js?v=0.001&xx.js&xxx.js"&gt;&lt;/script&gt;</p>

<h3>代码变动后改为:</h3>

<p>&lt;script src="/compress.js?v=0.002&xx.js&xxx.js"&gt;&lt;/script&gt;</p>

<p>这样压缩器就会去应用新的代码合并压缩.</p>

<p>值得一提的是 压缩器还支持将远程跨域文件与本地文件合并压缩.</p>

<h3>如何使用压缩器来压缩JS CSS GSS?</h3>

<p>和普通的JS CSS引用相同 唯一不同是以 /compress.*? 开头 （* 为文件类型后缀，目前支持css，gss，js） 所有原来的JS CSS文件作为参数传入以&符号连接.</p>

<p>如果你熟悉YUI Compressor这款工具对此方式应该并不陌生,这正是YAHOO所有服务器调用JS CSS的方法.</p>

<p>如果你熟悉YUI框架对此也应该不会陌生 YUI 的 use 方法可以访问YAHOO的在线服务做到合并压缩,其格式也是这样的.</p>

<p>如:&lt;script src="/compress.js?xx.js&xxx.js"&gt;&lt;/script&gt;</p>

<p>压缩器只会选取/compress的后缀作为压缩的类型，如上例中的compress.js.</p>

<p>压缩器目前只支持三种文件格式 JS CSS GSS.</p>

<p>如果想把CSS和GSS混合压缩使用 请将GSS作为后缀,如:</p>

<p>&lt;link rel="stylesheet" href="/compress.gss?style.gss&style1.css&style2.css"&gt;</p>

<h3>什么是GSS?</h3>

<p>GSS是一个Java程序，它向CSS中添加了变量、函数、条件语句以及混合类型,使得我们更易于处理大型的CSS文件.</p>

<p>开发者可以使用GSS这种工具来生成web应用程序或者网站所使用的真正的CSS文件.</p>

<h3>公共参数(gss css js 压缩通用):</h3>

<p>调试版本加debug参数,只合并不压缩加入nocompress参数</p>

<h3>JS独有参数:</h3>

<p>需要控制JS压缩级别加入level参数,0为只去空格和换行 1为普通压缩 2为高级压缩(慎用,会破坏接口,具体参考Closure Compiler官网),省略level参数默认为1</p>

<h3>CSS/GSS独有参数:</h3>

<p>需要对CSS美化而不压缩采用pretty参数</p>

<h3>参数如何加入:</h3>

<p>在/compress.css/gss?后面直接加入debug或其他参数即可</p>

<p>如:/compress.css/gss?debug 或 /compress.css/gss?debug=true</p>

<p>/compress.css/gss?level=0</p>

<h3>GSS浏览器/平台断言</h3>

<p>
    首先，建立GSS文件如style.gss。然后编写断言代码像这样：
</p>

<p>
    <pre>
        @if (BROWSER_IE) {
        @if (BROWSER_IE6) {
        @def GOOG_INLINE_BLOCK_DISPLAY inline;
        } @elseif (BROWSER_IE7) {
        @def GOOG_INLINE_BLOCK_DISPLAY inline;
        } @else {
        @def GOOG_INLINE_BLOCK_DISPLAY inline-block;
        }
        } @elseif (BROWSER_FF2) {
        @def GOOG_INLINE_BLOCK_DISPLAY -moz-inline-box;
        } @elseif (BROWSER_CHROME) {
        @def GOOG_INLINE_BLOCK_DISPLAY -webkit-inline-box;
        } @elseif(PLATFORM_MOBILE){
        @def GOOG_INLINE_BLOCK_DISPLAY -o-inline-box;
        } @elseif(BROWSER_WEBKIT){
        @if(PLATFORM_ANDROID){
        @def GOOG_INLINE_BLOCK_DISPLAY -webkit-box;
        }@else{
        @def GOOG_INLINE_BLOCK_DISPLAY -webkit-inline-box;
        }
        } @else {
        @def GOOG_INLINE_BLOCK_DISPLAY inline-block;
        }

        .goog-inline-block {
        position: relative;
        display: GOOG_INLINE_BLOCK_DISPLAY;
        }
    </pre>
</p>

<p>
    接着，加入到html中，像这样：
</p>

<p>
    <pre>
        &lt;link href="/compress.gss?/style/style.gss&/style/style1.css" rel="stylesheet"&gt;
    </pre>
</p>

<p>
    然后，我们就可以用各种浏览器来测试了。或者选择直接更改用户代理来测试。
</p>

<h3>GSS更多使用方法</h3>
<h4>变量</h4>

<p>变量是使用“@def”来定义的。下面的代码示例展示了如何使用变量：</p>
<pre>
    @def BG_COLOR rgb(235, 239, 249);
    @def DIALOG_BG_COLOR BG_COLOR;
    body {
    background-color: BG_COLOR;
    }

    .dialog {
    background-color: DIALOG_BG_COLOR;
    }
</pre>
得到的CSS如下：
<pre>
    body {
    background-color: #ebeff9;
    }
    .dialog {
    background-color: #ebeff9;
    }
</pre>
<h4>函数</h4>

<p>GSS引入了大量数学函数，使用它们你可以对数字型的值——比方说像素——进行以下操作： add()、 sub()、mult()、 div()、 min()以及max()。使用这些函数的示例如下：</p>
<pre>
    @def LEFT_WIDTH 100px;
    @def LEFT_PADDING 5px;
    @def RIGHT_PADDING 5px;
    .content {
    position: absolute;
    margin-left: add(LEFT_PADDING,
    LEFT_WIDTH,
    RIGHT_PADDING,
    px);
</pre>

<p>得到的CSS如下所示：</p>

<pre>
    .content {
    position: absolute;
    margin-left: 110px;
    }
</pre>

<h5>更多内置函数:</h5>
<ul>
    <li>add()</li>
    <li>sub()</li>
    <li>mult()</li>
    <li>divide()</li>
    <li>min()</li>
    <li>max()</li>
    <li>blendColorsHsb(startColor, endColor)</li>
    <li>blendColorsRgb(startColor, endColor)</li>
    <li>makeMutedColor(backgroundColor, foregroundColor [, saturationLoss])</li>
    <li>addHsbToCssColor(baseColor, hueToAdd, saturationToAdd, brightnessToAdd)</li>
    <li>makeContrastingColor(color, similarityIndex)</li>
    <li>adjustBrightness(color, brightness)</li>
</ul>

<h4>条件语句</h4>

<p>GSS让我们可以使用@if、@elseif和@else，从而基于某些变量的值来创建条件语句的分支。</p>

<h4>混合类型</h4>

<p>混合类型是为了重用带有参数的对结构体的声明，如下示例所示：</p>
<pre>
    @defmixin size(WIDTH, HEIGHT) {
    width: WIDTH;
    height: HEIGHT;
    }

    .image {
    @mixin size(200px, 300px);
    }
</pre>

<p>当解决跨浏览器的问题时，混合类型会更有用：</p>
<pre>
    @defmixin gradient(POS, HSL1, HSL2, HSL3, COLOR, FALLBACK_COLOR) {
    background-color: FALLBACK_COLOR; /* fallback color if gradients are not supported */
    background-image: -webkit-linear-gradient(POS, hsl(HSL1, HSL2, HSL3), COLOR); /* Chrome 10+,Safari 5.1+ */
    /* @alternate */ background-image: -moz-linear-gradient(POS, hsl(HSL1, HSL2, HSL3), COLOR); /* FF3.6+ */
    /* @alternate */ background-image: -ms-linear-gradient(POS, hsl(HSL1, HSL2, HSL3), COLOR); /* IE10 */
    /* @alternate */ background-image: -o-linear-gradient(POS, hsl(HSL1, HSL2, HSL3), COLOR); /* Opera 11.10+ */
    }

    .header {
    @mixin gradient(top, 0%, 50%, 70%, #cc0000, #f07575);
    }
</pre>

<p>结果如下：</p>
<pre>
    .header {
    background-color: #f07575;
    background-image: -webkit-linear-gradient(top,hsl(0%,50%,70%) ,#cc0000);
    background-image: -moz-linear-gradient(top,hsl(0%,50%,70%) ,#cc0000);
    background-image: -ms-linear-gradient(top,hsl(0%,50%,70%) ,#cc0000);
    background-image: -o-linear-gradient(top,hsl(0%,50%,70%) ,#cc0000);
    }
</pre>
<h4>/* @alternate */ 注解说明</h4>

<p>我们在上面代码中看到了注解alternate,这个注解是用来标记压缩时保留此CSS项,也不做错误检查.</p>
</div>
</body>
</html>
