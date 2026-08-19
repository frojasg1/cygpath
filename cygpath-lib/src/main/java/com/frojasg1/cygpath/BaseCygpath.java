package com.frojasg1.cygpath;

public interface BaseCygpapth {

    String posix2win(String path, boolean relative);
    String win2posix(String path, boolean relative);
}
