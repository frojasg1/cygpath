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

import com.frojasg1.cygpath.cygwin.windowslike.CygpathStraightWindowsImpl;
import com.frojasg1.cygpath.cygwin.windowslike.CygpathStraightWindowsImplTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CygpathStraightUnixImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CygpathStraightWindowsImplTest.class);


    protected CygpathStraightWindowsImpl instance;

    @Before
    public void setup() {
        instance = createInstance();
    }

    protected CygpathStraightWindowsImpl createInstance() {
        String rootPathWinlike = "D:\\cygwin64";
        String cRootPathUnixlike = "/c/";

        return new CygpathStraightWindowsImpl(rootPathWinlike, cRootPathUnixlike)
                .init();
    }
    @Test
    public void relativePathWithParentsPosixToAbsoluteWin() {
        String relativePath = "scripts/public/frojasg1/python/../bash/../../../private/";

        boolean absolute = true;
        String absUnixPath = instance.posix2win(relativePath, absolute);

        LOGGER.info("   posix2win(relative=false) '{}' -----> '{}'", relativePath, absUnixPath);
    }
}