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

(function () {
    "use strict";

    var doc = document,
        head = doc.head || doc.getElementsByTagName('head')[0],
        isOldWebKit = Number(navigator.userAgent.replace(/.*AppleWebKit\/(\d+)\..*/, '$1')) < 536,
        isOldFirefox = navigator.userAgent.indexOf('Firefox') > 0 && !('onload' in document.createElement('link'));

    function createLink(url) {
        var link = doc.createElement('link');

        link.rel = "stylesheet";
        link.type = "text/css";
        link.href = url;

        return link;
    }

    var timer, currentUrl, loads = [],
        loadAll = function () {
            for (var i = 0; i < loads.length; i++) {
                loads[i]();
            }
            loads = [];
        }, encodeUrl = function (url, config) {
            var compressorCfg = config.compressor;
            if (!currentUrl) {
                currentUrl = '/compress.mss?' + (compressorCfg.compress === true ? '' : 'pretty&') +
                    (compressorCfg.browserCondition === true ? 'condition&' : '') + encodeURIComponent(url);
            } else {
                currentUrl += '&' + encodeURIComponent(url);
            }
            return currentUrl;
        };

    /**
     * Load using the browsers built-in load event on link tags
     */
    function loadLink(url, load) {
        var link = createLink(url);

        styleOnload(link, load);

        head.appendChild(link);
    }

    function styleOnload(node, callback) {

        // for Old WebKit and Old Firefox
        if (isOldWebKit || isOldFirefox) {

            setTimeout(function () {
                poll(node, callback)
            }, 1); // Begin after node insertion
        }
        else {
            node.onload = node.onerror = function () {
                node.onload = node.onerror = null;
                node = undefined;
                callback()
            }
        }

    }

    function poll(node, callback) {
        var isLoaded;

        // for WebKit < 536
        if (isOldWebKit) {
            if (node['sheet']) {
                isLoaded = true
            }
        }
        // for Firefox < 9.0
        else if (node['sheet']) {
            try {
                if (node['sheet'].cssRules) {
                    isLoaded = true
                }
            } catch (ex) {
                // The value of `ex.name` is changed from
                // 'NS_ERROR_DOM_SECURITY_ERR' to 'SecurityError' since Firefox 13.0
                // But Firefox is less than 9.0 in here, So it is ok to just rely on
                // 'NS_ERROR_DOM_SECURITY_ERR'
                if (ex.name === 'NS_ERROR_DOM_SECURITY_ERR') {
                    isLoaded = true
                }
            }
        }

        setTimeout(function () {
            if (isLoaded) {
                // Place callback in here due to giving time for style rendering.
                callback()
            } else {
                poll(node, callback)
            }
        }, 1)
    }

    function loadSwitch(url, load, config) {
        var compressorCfg = config.compressor;
        if (compressorCfg && compressorCfg.enabled === true) {
            if (compressorCfg.merge === true) {
                loads[loads.length] = load;
                if (timer) {
                    clearTimeout(timer);
                }
                url = encodeUrl(url, config);
                timer = setTimeout(function () {
                    loadLink(url, loadAll);
                    timer = null;
                    currentUrl = null;
                }, 50);
                return;
            }
            var _url = '';
            if (compressorCfg.compress !== true) {
                _url = '/compress.mss?pretty&';
                url = _url + encodeURIComponent(url);
            }

            if (compressorCfg.browserCondition === true) {
                if (_url) {
                    url += '&condition';
                } else {
                    url = '/compress.mss?condition&' + encodeURIComponent(url);
                }
            }
        }
        loadLink(url, load);
    }

    define('css', function () {
        return {
            version:'0.3.1',

            load:function (name, req, load, config) {
                // convert name to actual url
                var url = req.toUrl(
                    // Append default extension
                    name.search(/\.(css|less|scss|mss|gss)$/i) === -1 ? name + '.css' : name
                );
                loadSwitch(url, load, config);
            }
        };
    });
}());