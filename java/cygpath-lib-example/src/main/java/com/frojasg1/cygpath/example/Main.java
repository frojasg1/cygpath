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

package com.frojasg1.cygpath.example;

import com.frojasg1.cygpath.facade.CygpathLibFacade;
import com.frojasg1.cygpath.facade.SysPathWrapper;
import com.frojasg1.cygpath.helpers.SystemGuesser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (Exception ex) {
            LOGGER.error("An error has been produced", ex);
            System.exit(1);
        }
    }

    protected void paint(String text) {
        System.out.println(text);
    }

    protected void paint() {
        paint("");
    }

    protected void runFunction(String myPath, Function<String, String> pathTransformer, String pathTransformerName) {
        String result = pathTransformer.apply(myPath);

        String text = String.format("      ------> %s : '%s'", pathTransformerName, result);
        paint(text);
    }

    protected SysPathWrapper getCygpathLibFacade() {
        return CygpathLibFacade.getFacade();
    }

    protected void runAllPathFunctions(String myPath) {
        paint();
        paint();
        String text = String.format("with: '%s'", myPath);
        paint(text);

        runFunction(myPath, path -> getCygpathLibFacade().win2posix(path, false), "win2posix(absolute=false)");
        runFunction(myPath, path -> getCygpathLibFacade().win2posix(path, true), "win2posix(absolute=true)");

        runFunction(myPath, path -> getCygpathLibFacade().posix2win(path, false), "posix2win(absolute=false)");
        runFunction(myPath, path -> getCygpathLibFacade().posix2win(path, true), "posix2win(absolute=true)");

        runFunction(myPath, path -> getCygpathLibFacade().hostToSysPath(path, false), "hostToSysPath(absolute=false)");
        runFunction(myPath, path -> getCygpathLibFacade().hostToSysPath(path, true), "hostToSysPath(absolute=true)");

        runFunction(myPath, path -> getCygpathLibFacade().sysToHostPath(path, false), "sysToHostPath(absolute=false)");
        runFunction(myPath, path -> getCygpathLibFacade().sysToHostPath(path, true), "sysToHostPath(absolute=true)");

        runFunction(myPath, path -> getCygpathLibFacade().javaToSysPath(path, false), "javaToSysPath(absolute=false)");
        runFunction(myPath, path -> getCygpathLibFacade().javaToSysPath(path, true), "javaToSysPath(absolute=true)");

        runFunction(myPath, path -> getCygpathLibFacade().sysToJavaPath(path, false), "sysToJavaPath(absolute=false)");
        runFunction(myPath, path -> getCygpathLibFacade().sysToJavaPath(path, true), "sysToJavaPath(absolute=true)");
    }

    protected SystemGuesser getSystemGuesser() {
        return SystemGuesser.instance();
    }

    protected String getEnvVariable(String label) {
        return getSystemGuesser().getEnvVariable(label);
    }

    public void run() {
        runAllPathFunctions("/bin");
        runAllPathFunctions("/");
        runAllPathFunctions(".");
        runAllPathFunctions("scripts/public/frojasg1/python/../bash/../../../private/");
        runAllPathFunctions(".bash_profile");
        runAllPathFunctions("./.bash_profile");
        runAllPathFunctions("\\bin");
        runAllPathFunctions("D:\\bin");
        runAllPathFunctions(".\\bin");
        runAllPathFunctions(".\\.bash_profile");
        runAllPathFunctions("scripts\\public\\frojasg1\\python\\..\\bash\\..\\..\\..\\private\\");

        String winlikeUnixRootPath = getEnvVariable("ROOT_PATH_WINLIKE");
        runAllPathFunctions(winlikeUnixRootPath);
        runAllPathFunctions(winlikeUnixRootPath + "\\..\\cygwin64\\");
    }
}
