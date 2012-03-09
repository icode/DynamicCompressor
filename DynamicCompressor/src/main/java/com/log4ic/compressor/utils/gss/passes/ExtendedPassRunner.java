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

package com.log4ic.compressor.utils.gss.passes;

import com.google.common.css.JobDescription;
import com.google.common.css.RecordingSubstitutionMap;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.passes.PassRunner;

import java.util.Map;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2012-03-06
 */
public class ExtendedPassRunner extends PassRunner {
    public ExtendedPassRunner(JobDescription job, ErrorManager errorManager) {
        super(job, errorManager);
    }

    public ExtendedPassRunner(JobDescription job, ErrorManager errorManager, RecordingSubstitutionMap recordingSubstitutionMap) {
        super(job, errorManager, recordingSubstitutionMap);
    }

    @Override
    public void runPasses(CssTree cssTree) {
        super.runPasses(cssTree);
    }
}
