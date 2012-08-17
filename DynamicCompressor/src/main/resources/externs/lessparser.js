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
 * @since 2012-08-17
 */

//对原始方法进行更正，
// 使其在断言的时候遇到undefined不中段构建
//为支持浏览器断言
(function (tree) {
    tree.Condition.prototype.eval = function (env) {
        //如果断言变量未定义默认为false
        var a = false,
            b = this.rvalue.eval(env);
        //执行原始方法，获取值，并拦截错误信息
        try {
            a = this.lvalue.eval(env);
        } catch (e) {
        }

        var i = this.index;

        var result = (function (op) {
            switch (op) {
                case 'and':
                    return a && b;
                case 'or':
                    return a || b;
                default:
                    if (a.compare) {
                        result = a.compare(b);
                    } else if (b.compare) {
                        result = b.compare(a);
                    } else {
                        throw { type: "Type",
                            message: "Unable to perform comparison",
                            index: i };
                    }
                    switch (result) {
                        case -1: return op === '<' || op === '=<';
                        case  0: return op === '=' || op === '>=' || op === '=<';
                        case  1: return op === '>' || op === '>=';
                    }
            }
        })(this.op);
        return this.negate ? !result : result;
    };

})(less.tree);
var dynamicCompressor = {
    parser:new less.Parser(),
    parseLess:function (less) {
        var res;
        dynamicCompressor.parser.parse(less, function (e, root) {
            if (e) {
                throw e;
            } else {
                res = root.toCSS()
            }
        });
        return res;
    }
};