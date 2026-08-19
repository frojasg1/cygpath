
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

package com.frojasg1.cygpath.cygwin;

import com.frojasg1.cygpath.BaseCygpath;
import com.frojasg1.cygpath.cygwin.empty.EmptyCygpathImpl;
import com.frojasg1.cygpath.cygwin.unixlike.CygpathStraightUnixImpl;
import com.frojasg1.cygpath.cygwin.windowslike.CygpathStraightWindowsImpl;
import com.frojasg1.cygpath.helpers.SystemGuesser;

public class CygpathBuilder {

    protected static final String ROOT_PATH_WINLIKE = "ROOT_PATH_WINLIKE";
    protected static final String C_ROOT_PATH_UNIXLIKE = "C_ROOT_PATH_UNIXLIKE";


    protected SystemGuesser getSystemGuesser() {
        return SystemGuesser.instance();
    }

    protected boolean isWindowsJava() {
        return getSystemGuesser().isWindowsJava();
    }

    protected boolean isUnixLikeJava() {
        return getSystemGuesser().isUnixLikeJava();
    }

    protected String getEnvVariable(String label) {
        return getSystemGuesser().getEnvVariable(label);
    }

    protected String getRootPathWinlikeValue() {
        return getEnvVariable(ROOT_PATH_WINLIKE);
    }

    protected String getCRootPathUnixlikeValue() {
        return getEnvVariable(C_ROOT_PATH_UNIXLIKE);
    }

    public BaseCygpath createCygpath(boolean throwException) {
        BaseCygpath result = null;
        if (isUnixLikeJava()) {
            result = createUnixLikeImplementation();
        } else if (isWindowsJava()) {
            result = createWindowsLikeImplementation();
        } else if (!throwException) {
            result = createEmptyImplementation();
        } else {
            throw new IllegalStateException("Running under a not expected environment type");
        }
        return result;
    }

    protected BaseCygpath createUnixLikeImplementation() {
        return new CygpathStraightUnixImpl(getRootPathWinlikeValue(), getCRootPathUnixlikeValue())
                .init();
    }

    protected BaseCygpath createWindowsLikeImplementation() {
        return new CygpathStraightWindowsImpl(getRootPathWinlikeValue(), getCRootPathUnixlikeValue())
                .init();
    }

    protected BaseCygpath createEmptyImplementation() {
        return new EmptyCygpathImpl();
    }
}
