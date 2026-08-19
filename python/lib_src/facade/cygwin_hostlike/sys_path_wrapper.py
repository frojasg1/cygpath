#!/usr/bin/env frojasg1Python3Wrapper.sh

import cygpath_wrapper as cygpath_w
import cygpath_wrapper_builder as cygpath_w_b



def get_cygpath_inst() -> cygpath_w.BaseCygpath:
    return cygpath_w_b.get_cygpath_inst()


def win2posix(my_path: str, absolute: bool=False):
    return get_cygpath_inst().win2posix(my_path, absolute)


def posix2win(my_path: str, absolute: bool=False):
    return get_cygpath_inst().posix2win(my_path, absolute)


def host_to_sys_path(python_like_path: str, absolute: bool=False) -> str:
    return win2posix(python_like_path, absolute)


def sys_to_host_path(system_like_path: str, absolute: bool=False) -> str:
    return posix2win(system_like_path, absolute)


# in this implementation python like paths are Windows like and system like paths are unix like
def python_to_sys_path(python_like_path: str, absolute: bool=False) -> str:
    return host_to_sys_path(python_like_path, absolute)


def sys_to_python_path(system_like_path: str, absolute: bool=False) -> str:
    return sys_to_host_path(system_like_path, absolute)


