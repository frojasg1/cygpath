
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

package com.frojasg1.cygpath.cygwin.unixlike;

import com.frojasg1.cygpath.cygwin.BaseStraightCygpathImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * implementation to be used under unix like Java interpreters
 * Ported from Python to Java.
 *
 + based on a conversion by:
 * https://app.codeconvert.ai/code-converter
 */
public class CygpathStraightUnixImpl extends BaseStraightCygpathImpl {

    // absolute windows path for unix: '/'
    // root_path_winlike  ---> 'C:\\cygwin64' for cygwin
    // root_path_winlike  ---> 'C:\\Program Files\\Git' for git bash
    public CygpathStraightUnixImpl(String rootPathWinlike, String cRootPathUnixlike) {
        super(rootPathWinlike, cRootPathUnixlike);
    }

    @Override
    public CygpathStraightUnixImpl init() {
        super.init();

        return this;
    }


    @Override
    protected String abspath(String myPath) {
        return getFileFunctions().abspath(myPath);
    }

    protected String simplifyAbsWinPath(String absWinPath) {
        String[] winDirs = this.splitWinPath(absWinPath);

        List<String> resultPath = new ArrayList<>();
        for (int ind = 0; ind < winDirs.length; ind++) {
            String winDir = winDirs[ind];
            if (winDir.equals("..")) { // go to parent (removing current)
                if (!(resultPath.size() > 0)) {
                    throw new AssertionError("cannot go to parent at root: \"" + absWinPath + "\"");
                }
                resultPath.remove(resultPath.size() - 1);
            } else if (winDir.length() == 0) { // the same dir
                if (ind == 0) { // current unit root
                    resultPath.add("");
                }
            } else if (winDir.equals(".")) { // the same dir (no action)
                // pass
            } else {
                resultPath.add(winDir);
            }
        }

        String result = String.join("\\", resultPath);

        if (result.length() == 0) {
            result = "\\";
        }

        return result;
    }

    protected String winAbsPathFromUnixPath(String unixPath) {
        String unixAbsPath = this.obtainAbsUnixPath(unixPath);
        Object[] lookResult = this.lookForUnixLetterUnitRootPath(unixAbsPath);
        String rootPathUnitLetter = (String) lookResult[0];
        int pos = (int) lookResult[1];

        boolean isAbsoluteRoot;
        if (rootPathUnitLetter == null && unixAbsPath.startsWith("/")) {
            isAbsoluteRoot = true;
        } else {
            isAbsoluteRoot = false;
        }

        String rootPath;
        if (isAbsoluteRoot) {
            rootPath = this.rootPathWinlike;
            pos = 1;
        } else if (rootPathUnitLetter != null && !rootPathUnitLetter.isEmpty()) {
            rootPath = this.getLetterUnitWinRootPath(rootPathUnitLetter);
        } else {
            throw new RuntimeException("Impossible: is_absolute_root: " + isAbsoluteRoot + ", root_path_unit_letter: '" + rootPathUnitLetter + "', unix_path:'" + unixPath + "'");
        }

        String subPath = unixAbsPath.substring(pos);
        // lstrip equivalent for \ and /
        int start = 0;
        while (start < subPath.length() && (subPath.charAt(start) == '\\' || subPath.charAt(start) == '/')) {
            start++;
        }
        String strippedSubPath = subPath.substring(start);

        String[] parts = this.splitUnixPath(strippedSubPath);
        String result = rootPath + "\\" + String.join("\\", parts);
        return result;
    }

    protected String absWinPathFromRelativeWinPath(String relativeWinPath) {
        String[] winDirs = this.splitWinPath(relativeWinPath);
        String winPwd = this.winAbsPathFromUnixPath(".");
        return winPwd + "\\" + String.join("\\", winDirs);
    }

    protected boolean isAbsWinPath(String winPath) {
        boolean result = false;
        if (winPath != null && !winPath.isEmpty()) {
            result = winPath.startsWith("\\") || this.getAbsPathWinUnitLetter(winPath) != null;
        }
        return result;
    }

    @Override
    protected String obtainAbsUnixPath(String unixPath) {
        return abspath(unixPath);
    }

    @Override
    protected String obtainAbsWinPath(String winPath) {
        String result = null;
        if (!this.isAbsWinPath(winPath)) {
            result = this.absWinPathFromRelativeWinPath(winPath);
        } else {
            result = winPath;
        }
        result = this.simplifyAbsWinPath(result);
        return result;
    }

    protected String getLetterUnitWinRootPath(String rootPathUnitLetter) {
        return this.rootPathWinlikePatternStr.replace(this.unitPatternWildcard, rootPathUnitLetter);
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
        if (result != null) {
            result = result.replace("/", "\\");
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

        String result = "";
        if (unixPath != null) {
            result = unixPath.replace("\\", "/");
        }

        return result;
    }
}
