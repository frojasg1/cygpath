#!frojasg1Python3Wrapper.sh

import os

import cygpath_wrapper as cygpath_w
import cygpath_wrapper_builder as cygpath_w_b


def abspath(my_path: str) -> str:
    return os.path.abspath(my_path)


def keep_path(my_path: str, absolute: bool) -> str:
    result = my_path
    if absolute:
        result = abspath(my_path)

    return result


def get_cygpath_inst() -> cygpath_w.BaseCygpath:
    return cygpath_w_b.get_cygpath_inst()


def win2posix(my_path: str, absolute: bool=False):
    return get_cygpath_inst().win2posix(my_path, absolute)


def posix2win(my_path: str, absolute: bool=False):
    return get_cygpath_inst().posix2win(my_path, absolute)


def host_to_sys_path(host_like_path: str, absolute: bool=False) -> str:
    return keep_path(host_like_path, absolute)


def sys_to_host_path(system_like_path: str, absolute: bool=False) -> str:
    return keep_path(system_like_path, absolute)


def python_to_sys_path(python_like_path: str, absolute: bool=False) -> str:
    return keep_path(python_like_path, absolute)


def sys_to_python_path(system_like_path: str, absolute: bool=False) -> str:
    return keep_path(system_like_path, absolute)


