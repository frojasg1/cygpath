
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

package com.frojasg1.cygpath.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SystemGuesser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemGuesser.class);

    protected static final String UNAME = "UNAME";

    protected static class LazySingleton {
        protected static SystemGuesser INSTANCE = new SystemGuesser().init();
    }

    public static SystemGuesser instance() {
        return LazySingleton.INSTANCE;
    }

    protected SystemType systemType;
    protected SystemType javaSystemType;

    public SystemGuesser init() {
        javaSystemType = obtainJavaSystemType();
        LOGGER.info("Java system found: '{}'", javaSystemType);
        systemType = obtainSystemType();
        LOGGER.info("System found: '{}'", systemType);

        return this;
    }

    protected String getUname() {
        return getEnvVariable(UNAME, null, NotificationTypeEnum.LOG);
    }

    protected SystemType obtainJavaSystemType() {
        SystemType result = null;

        if (obtainIsUnixLikeJava()) {
            result = SystemTypeEnum.UNIX_LIKE;
        } else if (obtainIsWindowsLikeJava()) {
            result = SystemTypeEnum.WINDOWS;
        } else {
            result = SystemTypeEnum.UNKONWN;
        }

        return result;
    }

    protected SystemType obtainSystemTypeFromUname(String uname) {
        SystemType result = SystemTypeEnum.UNIX_LIKE;
        if (uname.trim().toLowerCase().startsWith("win")) {
            result = SystemTypeEnum.WINDOWS;
        }
        return result;
    }

    protected SystemType obtainSystemType() {
        SystemType result = null;
        String uname = getUname();
        if (uname == null) {
            LOGGER.warn("UNAME env var not set. Expected to have the output of the command 'uname -a', or an empty value set to blank for a Windows system.");
            LOGGER.warn("We will do the presumption of the system being the same as the kind of java used, as if it was used neither with cygwin nor with git-bash");
            result = obtainJavaSystemType();
        } else {
            result = obtainSystemTypeFromUname(uname);
        }
        return result;
    }

    protected SystemType getSystemType() {
        return systemType;
    }

    protected SystemType getJavaSystemType() {
        return javaSystemType;
    }

    public boolean isWindowsSystem() {
        return getSystemType().isWindows();
    }

    public boolean isUnixlikeSystem() {
        return getSystemType().isUnixLike();
    }

    protected boolean obtainIsWindowsLikeJava() {
        // TODO: improve this
        return File.separator.equals("\\");
    }

    protected boolean obtainIsUnixLikeJava() {
        // TODO: improve this
        return File.separator.equals("/");
    }

    public boolean isJavaSystemTheSameAsSystem() {
        return getJavaSystemType() == getSystemType();
    }

    public boolean isUnixLikeJava() {
        return getJavaSystemType().isUnixLike();
    }

    public boolean isWindowsJava() {
        return getJavaSystemType().isWindows();
    }

    public String getEnvVariable(String label) {
        return getEnvVariable(label, null, NotificationTypeEnum.THROW);
    }

    public String getEnvVariable(String label, String defaultValue, NotificationTypeEnum notificationType) {
        String result = System.getenv(label);
        if (result == null) {
            if (notificationType != null) {
                String errorText = String.format("Environment label not found: '%s'", label);
                if (notificationType.isLog()) {
                    LOGGER.warn("{}", errorText);
                }

                if (notificationType.isThrow()) {
                    throw new RuntimeException(String.format("Environment label not found: '%s'", label));
                }
            }
            result = defaultValue;
        }
        return result;
    }
}
