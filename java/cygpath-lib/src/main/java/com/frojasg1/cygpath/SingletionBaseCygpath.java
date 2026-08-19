
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

package com.frojasg1.cygpath;

import com.frojasg1.cygpath.cygwin.CygpathBuilder;

public class SingletionBaseCygpath implements BaseCygpath {

    protected SingletionBaseCygpath() {
        // intentionally empty
    }

    protected static class DeferredInstantiation {
        protected static BaseCygpath INSTANCE = new CygpathBuilder()
                .createCygpath(true);
    }

    public static BaseCygpath instance() {
        return DeferredInstantiation.INSTANCE;
    }

    protected void throwUnsupportedOperationException() {
        throw new UnsupportedOperationException(String.format("Cannot call this object functions, call functions of %s.instance() object instead", getClass().getName()));
    }

    @Override
    public String posix2win(String path, boolean absolute) {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public String win2posix(String path, boolean absolute) {
        throwUnsupportedOperationException();
        return null;
    }
}
