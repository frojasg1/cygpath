
/*
 *  MIT License
 *
 * Copyright (c) 2026. Francisco Javier Rojas Garrido <frojasg1@hotmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 *  You may obtain a copy of the License at
 *
 *       https://opensource.org/license/mit
 *
 *
 */

package com.frojasg1.cygpath.cygwin.windowslike;

import com.frojasg1.cygpath.cygwin.BaseStraightCygpathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation to be used under windows like Java environments.
 * Ported from Python to Java.
 *
 + based on a conversion by:
 * https://app.codeconvert.ai/code-converter
 */
public class CygpathStraightWindowsImpl extends BaseStraightCygpathImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(CygpathStraightWindowsImpl.class);

    // absolute windows path for unix: '/'
    // rootPathWinlike  ---> 'C:\\cygwin64' for cygwin
    // rootPathWinlike  ---> 'C:\\Program Files\\Git' for git bash
    public CygpathStraightWindowsImpl(String rootPathWinlike, String cRootPathUnixlike) {
        super(rootPathWinlike, cRootPathUnixlike);
    }

    @Override
    public CygpathStraightWindowsImpl init() {
        super.init();
        return this;
    }

    @Override
    protected String abspath(String myPath) {
        return getFileFunctions().abspath(myPath);
    }

    protected String simplifyAbsUnixPath(String absUnixPath) {
        String[] unixDirs = splitUnixPath(absUnixPath);
        List<String> resultPath = new ArrayList<>();

        for (int ind = 0; ind < unixDirs.length; ind++) {
            String unixDir = unixDirs[ind];
            if (unixDir.equals("..")) {
                if (resultPath.isEmpty()) {
                    throw new AssertionError("cannot go to parent at root: \"" + absUnixPath + "\"");
                }
                resultPath.remove(resultPath.size() - 1);
            } else if (unixDir.length() == 0) {
                if (ind == 0) {
                    resultPath.add("");
                }
            } else if (unixDir.equals(".")) {
                // no action
            } else {
                resultPath.add(unixDir);
            }
        }

        String result = String.join("/", resultPath);

        if (result.length() == 0) {
            result = "/";
        }

        return result;
    }

    protected String absUnixPathFromRelativeUnixPath(String relativeUnixPath) {
        String[] unixDirs = relativeUnixPath.split("/");
        String winPwd = this.obtainAbsWinPath(".");

        Object[] lookResult = this.lookForWindowsLetterUnitRootPathOrWindowsUnixAbsoluteRoot(winPwd);
        boolean isAbsoluteRoot = (boolean) lookResult[0];
        String rootPathUnitLetter = (String) lookResult[1];
        int pos = (int) lookResult[2];

        String result;
        if (isAbsoluteRoot) {
            String subPath = winPwd.substring(pos).replaceAll("^[\\\\/]+", "");
            result = "/" + String.join("/", splitWinPath(subPath)) + "/" + String.join("/", unixDirs);
        } else if (rootPathUnitLetter != null && !rootPathUnitLetter.isEmpty()) {
            String subPath = winPwd.substring(pos).replaceAll("^[\\\\/]+", "");
            result = this.getLetterUnitUnixRootPath(rootPathUnitLetter) + "/"
                    + String.join("/", splitWinPath(subPath)) + "/" + String.join("/", unixDirs);
        } else {
            throw new RuntimeException("Impossible: is_absolute_root: " + isAbsoluteRoot + ", root_path_unit_letter: '" + rootPathUnitLetter + "', relative_unix_path:'" + relativeUnixPath + "'");
        }

        return result;
    }

    @Override
    protected String obtainAbsUnixPath(String winPath) {
        String result = null;
        if (!this.isAbsUnixPath(winPath)) {
            result = this.absUnixPathFromRelativeUnixPath(winPath);
        } else {
            result = winPath;
        }
        result = this.simplifyAbsUnixPath(result);
        return result;
    }

    @Override
    protected String obtainAbsWinPath(String myPath) {
        return abspath(myPath);
    }

    @Override
    public String posix2win(String path, boolean absolute) {
        String result = path;
        if (path != null && !path.isEmpty()) {
            if (!this.mightBeWinPath(path)) {
                result = this.winPath(path);
            }

            if (absolute) {
                result = this.obtainAbsWinPath(result);
            }
        }
        return result;
    }

    @Override
    public String win2posix(String path, boolean absolute) {
        String unixPath = path;
        if (path != null && !path.isEmpty()) {
            if (this.mightBeWinPath(path)) {
                unixPath = this.unixPath(path);
            }

            if (absolute) {
                unixPath = this.obtainAbsUnixPath(unixPath);
            }
        }
        return unixPath;
    }
}
