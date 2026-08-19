
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
import com.frojasg1.cygpath.helpers.FileFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base implementation inferred of the Python unix like implementation converted into Java by https://app.codeconvert.ai/code-converter.
 *
 * Ported from Python to Java.
 *
 + based on a conversion by:
 * https://app.codeconvert.ai/code-converter
 */
public abstract class BaseStraightCygpathImpl implements BaseCygpath {

    protected String originalRootPathWinlike;
    protected String rootPathWinlike;
    protected String cRootPathUnixlike;
    protected String unitPatternWildcard;

    protected Pattern rootUnitUnixlikeRegexPattern;
    protected String rootPathUnixlikePatternStr;
    protected Function<String, String> unixUnitLetterCaseLambda;

    protected Pattern rootUnitWinlikeRegexPattern;
    protected Pattern absPathRootUnitWinlikeRegexPattern;

    protected Pattern pathSeparatorPattern;
    protected String unixlikeRootPathWinlike;
    protected String rootPathWinlikePatternStr;
    protected Pattern rootUnitWindowsLikeRegexPattern;

    // absolute windows path for unix: '/'
    // rootPathWinlike  ---> 'C:\\cygwin64' for cygwin
    // rootPathWinlike  ---> 'C:\\Program Files\\Git' for git bash
    public BaseStraightCygpathImpl(String rootPathWinlike, String cRootPathUnixlike) {
        this.originalRootPathWinlike = rootPathWinlike;
        this.cRootPathUnixlike = cRootPathUnixlike;
    }

    public BaseStraightCygpathImpl init() {
        this.rootPathWinlike = this.removeMultiSlashes(originalRootPathWinlike);
        this.unitPatternWildcard = "<unit>";

        Object[] rootUnitResults = this.calculateRootUnitUnixlikeRegexPattern(cRootPathUnixlike);
        this.rootUnitUnixlikeRegexPattern = (Pattern) rootUnitResults[0];
        this.rootPathUnixlikePatternStr = (String) rootUnitResults[1];
        this.unixUnitLetterCaseLambda = (Function<String, String>) rootUnitResults[2];

        this.rootUnitWinlikeRegexPattern = Pattern.compile("^([a-zA-Z]):$");
        this.absPathRootUnitWinlikeRegexPattern = Pattern.compile("^([a-zA-Z]):\\\\");

        this.rootUnitWindowsLikeRegexPattern = Pattern.compile("^\\s*([A-Za-z]):\\\\");

        this.pathSeparatorPattern = Pattern.compile("[/\\\\]");

        this.unixlikeRootPathWinlike = null;
        this.rootPathWinlikePatternStr = this.unitPatternWildcard + ":";
        this.unixlikeRootPathWinlike = this.win2posix(this.rootPathWinlike, false);

        return this;
    }

    protected void showError(String message) {
        System.err.println(message);
    }

    protected abstract String abspath(String myPath);

    protected String removeMultiSlashes(String myWinPath) {
        if (myWinPath == null) return null;
        String result = myWinPath.replaceAll("\\\\+", "\\\\");
        result = result.replaceAll("/+", "/");
        return result;
    }

    // absolute unix path for: windows 'C:\'
    // cRootPathUnixlike  ---> '/cygdrive/c'   for cygwin
    // cRootPathUnixlike  ---> '/c'            for git bash
    protected Object[] calculateRootUnitUnixlikeRegexPattern(String cRootPathUnixlike) {
        String unitWildcard = "[a-zA-Z]";

        Pattern lastSlashesRemovalRegexPattern = Pattern.compile("^(.*[^/])/+$");
        String tmp = cRootPathUnixlike.trim();
        Matcher m = lastSlashesRemovalRegexPattern.matcher(tmp);
        if (m.find()) {
            tmp = m.group(1);
        }

        if (!(tmp.length() > 0 && Character.toUpperCase(tmp.charAt(tmp.length() - 1)) == 'C')) {
            throw new AssertionError("cRootPathUnixlike, '" + cRootPathUnixlike + "' does not seem to be a C root path");
        }

        String rootPathUnixlikeWithoutUnit = tmp.substring(0, tmp.length() - 1);

        Pattern resultPattern = Pattern.compile("^" + Pattern.quote(rootPathUnixlikeWithoutUnit) + "(" + unitWildcard + ")/*$");

        String rootPathUnixlikePatternStr = rootPathUnixlikeWithoutUnit + this.unitPatternWildcard;

        Function<String, String> unixUnitLetterCaseLambda;
        char lastChar = tmp.charAt(tmp.length() - 1);
        if (lastChar == 'C') {
            unixUnitLetterCaseLambda = (x) -> x.toUpperCase();
        } else if (lastChar == 'c') {
            unixUnitLetterCaseLambda = (x) -> x.toLowerCase();
        } else {
            unixUnitLetterCaseLambda = (x) -> x;
        }

        return new Object[]{resultPattern, rootPathUnixlikePatternStr, unixUnitLetterCaseLambda};
    }

    protected boolean mightBeWinPath(String myPath) {
        boolean result = true;
        if (myPath != null && !myPath.isEmpty()) {
            result = myPath.contains("\\") || (getAbsPathWinUnitLetter(myPath) != null);
        }
        return result;
    }

    protected String[] splitGenPath(String path) {
        List<String> elems = new ArrayList<>();
        Matcher matcher = pathSeparatorPattern.matcher(path);
        int pos = 0;
        while (matcher.find()) {
            elems.add(path.substring(pos, matcher.start()));
            pos = matcher.end();
        }
        elems.add(path.substring(pos));

        return elems.toArray(new String[elems.size()]);
    }

    protected String[] splitWinPath(String path) {
        return splitGenPath(path);
    }

    protected String[] splitUnixPath(String path) {
        return splitGenPath(path);
    }

    protected boolean isAbsUnixPath(String unixPath) {
        boolean result = false;
        if (unixPath != null && !unixPath.isEmpty()) {
            result = unixPath.charAt(0) == '/';
        }
        return result;
    }

    protected Object[] lookForUnixLetterUnitRootPath(String unixPath) {
        int pos = -1;
        int resPos = -1;
        String rootPathUnitLetter = "";
        if (unixPath != null && !this.mightBeWinPath(unixPath)) {
            pos = unixPath.length();

            while (rootPathUnitLetter == null || rootPathUnitLetter.isEmpty()) {
                resPos = pos + 1;
                if (resPos > unixPath.length()) resPos = unixPath.length();
                String candidateToUnitRootPath = unixPath.substring(0, resPos);
                String unixAbsPath = this.obtainAbsUnixPath(candidateToUnitRootPath);
                rootPathUnitLetter = this.getUnixUnitLetterIfUnitRootPath(unixAbsPath);

                if (rootPathUnitLetter == null || rootPathUnitLetter.isEmpty()) {
                    resPos = pos;
                    candidateToUnitRootPath = unixPath.substring(0, resPos);
                    unixAbsPath = this.obtainAbsUnixPath(candidateToUnitRootPath);
                    rootPathUnitLetter = this.getUnixUnitLetterIfUnitRootPath(unixAbsPath);
                    if (rootPathUnitLetter == null || rootPathUnitLetter.isEmpty()) {
                        resPos = -1;
                    }
                }

                if (resPos >= 0) break;
                pos = unixPath.lastIndexOf('/', pos - 1);
                if (pos < 0) break;
            }
        }

        return new Object[]{rootPathUnitLetter, resPos};
    }

    protected boolean isWindowsUnixAbsoluteRoot(String absWinCandidate) {
        String modifiedRootPathWinlike = this.rootPathWinlike.replaceAll("[\\\\/]+$", "");
        String modifiedAbsWinCandidate = absWinCandidate.replaceAll("[\\\\/]+$", "");
        return modifiedRootPathWinlike.equals(modifiedAbsWinCandidate);
    }

    protected Object[] isWinUnitRootOrIsWinUnixAbsoluteRoot(String absWinCandidate) {
        String modifiedAbsWinCandidate = absWinCandidate.replaceAll("[\\\\/]+$", "");
        boolean isAbsoluteRoot = this.isWindowsUnixAbsoluteRoot(modifiedAbsWinCandidate);
        String rootPathUnitLetter = null;
        if (!isAbsoluteRoot) {
            rootPathUnitLetter = this.getWinUnitLetterIfUnitRootPath(modifiedAbsWinCandidate);
        }
        return new Object[]{isAbsoluteRoot, rootPathUnitLetter};
    }

    protected int rfind(String text, String chars, int start, int end) {
        int result = -1;
        if (text != null && !text.isEmpty()) {
            int pos = Math.min(end - 1, text.length() - 1);
            int startIdx = Math.max(0, start);

            while (pos >= startIdx && result == -1) {
                char myChar = text.charAt(pos);
                if (chars.indexOf(myChar) != -1) {
                    result = pos;
                }
                pos--;
            }
        }
        return result;
    }

    protected Object[] lookForWindowsLetterUnitRootPathOrWindowsUnixAbsoluteRoot(String winPath) {
        int pos = -1;
        int resPos = -1;
        boolean found = false;
        boolean isAbsoluteRoot = false;
        String rootPathUnitLetter = "";

        if (winPath != null && this.mightBeWinPath(winPath)) {
            pos = winPath.length();

            while (true) {
                resPos = pos + 1;
                if (resPos > winPath.length()) resPos = winPath.length();
                String candidateToUnitRootPath = winPath.substring(0, resPos);
                String absWinCandidate = this.obtainAbsWinPath(candidateToUnitRootPath);
                Object[] check = this.isWinUnitRootOrIsWinUnixAbsoluteRoot(absWinCandidate);
                isAbsoluteRoot = (boolean) check[0];
                rootPathUnitLetter = (String) check[1];

                found = isAbsoluteRoot || (rootPathUnitLetter != null && !rootPathUnitLetter.isEmpty());
                if (!found) {
                    resPos = pos;
                    candidateToUnitRootPath = winPath.substring(0, pos);
                    absWinCandidate = this.obtainAbsWinPath(candidateToUnitRootPath);
                    Object[] check2 = this.isWinUnitRootOrIsWinUnixAbsoluteRoot(absWinCandidate);
                    isAbsoluteRoot = (boolean) check2[0];
                    rootPathUnitLetter = (String) check2[1];
                    found = isAbsoluteRoot || (rootPathUnitLetter != null && !rootPathUnitLetter.isEmpty());
                }

                if (!found) {
                    resPos = -1;
                }

                if (found) break;
                pos = this.rfind(winPath, "/\\", 0, pos);
                if (pos < 0) break;
            }
        }

        return new Object[]{isAbsoluteRoot, rootPathUnitLetter, resPos};
    }

    /*
# Source - https://stackoverflow.com/a/50137718
# Posted by Baldrickk, modified by community. See post 'Timeline' for change history
# Retrieved 2026-07-06, License - CC BY-SA 4.0

#    def win_path(path):
#        match = re.match('(/(cygdrive/)?)(.*)', path)
#        if not match:
#            return path.replace('/', '\\')
#        dirs = match.group(3).split('/')
#        dirs[0] = f'{dirs[0].upper()}:'
#        return '\\'.join(dirs)
     */
    protected String winPath(String path) {
        String result = path;
        if (path != null && !this.mightBeWinPath(path)) {
            if (this.isAbsUnixPath(path)) {
                Object[] lookResult = this.lookForUnixLetterUnitRootPath(path);
                String rootPathUnitLetter = (String) lookResult[0];
                int pos = (int) lookResult[1];

                boolean isAbsoluteRoot = false;
                if (pos < 0 && path.startsWith("/")) {
                    isAbsoluteRoot = true;
                    pos = 0;
                }

                result = "";

                if (pos >= 0) {
                    if (isAbsoluteRoot) {
                        result = this.rootPathWinlike + "\\" + String.join("\\", splitUnixPath(path));
                    } else if (rootPathUnitLetter != null && !rootPathUnitLetter.isEmpty()) {
                        result = rootPathUnitLetter.toUpperCase() + ":\\" + String.join("\\", splitUnixPath(path.substring(pos)));
                    } else {
                        throw new RuntimeException("Impossible");
                    }
                } else {
                    result = String.join("\\", splitUnixPath(path));
                }
            } else {
                result = String.join("\\", splitUnixPath(path));
            }
        }

        result = this.removeMultiSlashes(result);
        return result;
    }

    protected String getLetterUnitUnixRootPath(String rootPathUnitLetter) {
        String unitLetterWithCase = this.unixUnitLetterCaseLambda.apply(rootPathUnitLetter);
        return this.rootPathUnixlikePatternStr.replace(this.unitPatternWildcard, unitLetterWithCase);
    }

    protected boolean isLetterUnitWinPath(String path) {
        return getAbsPathWinUnitLetter(path) != null;
    }

    protected String unixPath(String path) {
        String result = path;
        if (path != null && this.mightBeWinPath(path)) {
            if (path.startsWith("\\")) {
                String winPwd = this.obtainAbsWinPath(".");
                String rootPathUnitLetter = this.getAbsPathWinUnitLetter(winPwd);
                if (!(rootPathUnitLetter != null && !rootPathUnitLetter.isEmpty())) {
                    throw new AssertionError("unit letter not found for pwd: \"" + winPwd + "\"");
                }

                result = this.getLetterUnitUnixRootPath(rootPathUnitLetter) + "/" + String.join("/", this.splitWinPath(path.substring(1)));
            } else {
                if (this.isLetterUnitWinPath(path)) {
                    Object[] lookResult = this.lookForWindowsLetterUnitRootPathOrWindowsUnixAbsoluteRoot(path);
                    boolean isAbsoluteRoot = (boolean) lookResult[0];
                    String winRootPathUnitLetter = (String) lookResult[1];
                    int pos = (int) lookResult[2];

                    if (!(pos >= 0)) {
                        throw new AssertionError("path was supposed to be a windows absolute path: \"" + path + "\"");
                    }

                    if (isAbsoluteRoot) {
                        result = "/" + String.join("/", this.splitWinPath(path.substring(pos)));
                    } else {
                        if (!(winRootPathUnitLetter != null && !winRootPathUnitLetter.isEmpty())) {
                            throw new AssertionError("Impossible: is_absolute_root=" + isAbsoluteRoot + ", win_root_path_unit_letter=\"" + winRootPathUnitLetter + "\", pos=" + pos + ", path=\"" + path + "\"");
                        }
                        result = this.getLetterUnitUnixRootPath(winRootPathUnitLetter)
                                + "/" + String.join("/", this.splitWinPath(path.substring(pos)));
                    }
                } else {
                    result = String.join("/", this.splitWinPath(path));
                }
            }
        }

        return this.removeMultiSlashes(result);
    }

    protected String getPatternGroupIfMatch(Pattern myRePattern, int targetGroup, String text) {
        if (text == null) return null;
        Matcher m = myRePattern.matcher(text);
        String result = null;
        if (m.find()) {
            result = m.group(targetGroup);
        }
        return result;
    }

    protected String getUnixUnitLetterIfUnitRootPath(String myPath) {
        return this.getPatternGroupIfMatch(this.rootUnitUnixlikeRegexPattern, 1, myPath);
    }

    protected String getAbsPathWinUnitLetter(String myPath) {
        return this.getPatternGroupIfMatch(this.absPathRootUnitWinlikeRegexPattern, 1, myPath);
    }

    protected String getWinUnitLetterIfUnitRootPath(String myPath) {
        return this.getPatternGroupIfMatch(this.rootUnitWinlikeRegexPattern, 1, myPath);
    }

    protected abstract String obtainAbsUnixPath(String myPath);
    protected abstract String obtainAbsWinPath(String myPath);

    public abstract String posix2win(String path, boolean absolute);

    public abstract String win2posix(String path, boolean absolute);

    protected FileFunctions getFileFunctions() {
        return FileFunctions.instance();
    }
}
