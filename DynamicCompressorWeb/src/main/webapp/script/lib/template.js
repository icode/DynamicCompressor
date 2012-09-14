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

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-08-20
 */
define('template', ['underscore'], function (_) {
    'use strict';
    var cxt = require.s.contexts['_'];
    var onScriptLoad = cxt.onScriptLoad;
    var onScriptError = cxt.onScriptError;
    var templateHis = {};
    cxt.onScriptLoad = function (e) {
        if (templateHis) {
            for (var n in templateHis) {
                require([n], templateHis[n]);
            }
        }
        onScriptLoad.apply(cxt, arguments);
    };
    cxt.onScriptError = function (e) {
        if (templateHis) {
            for (var n in templateHis) {
                templateHis[n].error();
            }
        }
        onScriptError.apply(cxt, arguments);
    };
    return {
        version:'0.0.1',
        load:function (name, req, load, config) {
            name = name.substring(name.indexOf('!'));
            var url = req.toUrl(name);
            var inx = url.lastIndexOf('?');
            if (inx == -1) {
                url += '?';
            } else {
                url += '&';
            }
            url += ('name=' + name + '&mode=amd');

            templateHis[name] = load;
            require.load(cxt, name, url);
        }
    };
});
define('tpl', ['template'], function (t) {
    'use strict';
    return t;
});