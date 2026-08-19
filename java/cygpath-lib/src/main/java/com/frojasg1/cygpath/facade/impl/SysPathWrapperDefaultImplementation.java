
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

package com.frojasg1.cygpath.facade.impl;

import com.frojasg1.cygpath.BaseCygpath;
import com.frojasg1.cygpath.SingletionBaseCygpath;
import com.frojasg1.cygpath.facade.SysPathWrapper;
import com.frojasg1.cygpath.helpers.KeepPathHelper;
import com.frojasg1.cygpath.helpers.SystemGuesser;

import java.util.function.BiFunction;

public class SysPathWrapperDefaultImplementation extends KeepPathHelper implements SysPathWrapper {

    // Let's make the presumption that Java system coincides with Host system.

    protected static class LazyHolder {
        protected static SysPathWrapperDefaultImplementation INSTANCE = new SysPathWrapperDefaultImplementation().init();
    }

    public static SysPathWrapperDefaultImplementation instance() {
        return LazyHolder.INSTANCE;
    }

    protected BiFunction<String, Boolean, String> toSysPathFunction;
    protected BiFunction<String, Boolean, String> toJavaPathFunction;

    public SysPathWrapperDefaultImplementation init() {
        this.toSysPathFunction = obtainToSysPathFunction();
        this.toJavaPathFunction = obtainToJavaPathFunction();

        return this;
    }

    public BiFunction<String, Boolean, String> getToSysPathFunction() {
        return toSysPathFunction;
    }

    public BiFunction<String, Boolean, String> getToJavaPathFunction() {
        return toJavaPathFunction;
    }

    protected SystemGuesser getSystemGuesser() {
        return SystemGuesser.instance();
    }

    protected boolean isJavaSystemTheSameAsSystem() {
        return getSystemGuesser().isJavaSystemTheSameAsSystem();
    }

    protected boolean isUnixLikeJava() {
        return getSystemGuesser().isUnixLikeJava();
    }

    protected boolean isWindowsJava() {
        return getSystemGuesser().isWindowsJava();
    }

    protected BaseCygpath getBaseCygpath() {
        return SingletionBaseCygpath.instance();
    }

    @Override
    public String win2posix(String myPath, boolean absolute) {
        return getBaseCygpath().win2posix(myPath, absolute);
    }

    @Override
    public String posix2win(String myPath, boolean absolute) {
        return getBaseCygpath().posix2win(myPath, absolute);
    }

    @Override
    public String hostToSysPath(String hostLikePath, boolean absolute) {
        return getToSysPathFunction().apply(hostLikePath, absolute);
    }

    @Override
    public String sysToHostPath(String systemLikePath, boolean absolute) {
        return getToJavaPathFunction().apply(systemLikePath, absolute);
    }

    @Override
    public String javaToSysPath(String javaLikePath, boolean absolute) {
        return getToSysPathFunction().apply(javaLikePath, absolute);
    }

    @Override
    public String sysToJavaPath(String systemLikePath, boolean absolute) {
        return getToJavaPathFunction().apply(systemLikePath, absolute);
    }

    protected BiFunction<String, Boolean, String> obtainToSysPathFunction() {
        BiFunction<String, Boolean, String> result = null;
        if (isJavaSystemTheSameAsSystem()) {
            result = this::keepPath;
        } else if (isWindowsJava()) {
            result = this::win2posix;
        } else if (isUnixLikeJava()) {
            result = this::keepPath;
        } else {
            result = this::keepPath;
        }

        return result;
    }

    protected BiFunction<String, Boolean, String> obtainToJavaPathFunction() {
        BiFunction<String, Boolean, String> result = null;
        if (isJavaSystemTheSameAsSystem()) {
            result = this::keepPath;
        } else if (isWindowsJava()) {
            result = this::posix2win;
        } else if (isUnixLikeJava()) {
            result = this::keepPath;
        } else {
            result = this::keepPath;
        }

        return result;
    }
}
