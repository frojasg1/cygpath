#!/usr/bin/env frojasg1Python3Wrapper.sh

import sys

import cygpath_wrapper as cygpath_w
import cygpath_wrapper_impl as cygpath_w_i


class RefWrapper(object):

    def __init__(self, value=None):
        self.__value__ = value

    def get(self):
        return self.__value__

    def set(self, value):
        self.__value__ = value


def show_error(text: str):
    print(text, file=sys.stderr)


def build_cygpath_wrapper(do_raise: bool=False) -> cygpath_w.BaseCygpath:
    try:
        mandatory = True
        do_raise = True
        root_path_winlike = cygpath_w.get_env_value('ROOT_PATH_WINLIKE', mandatory, do_raise)
        c_root_path_unixlike = cygpath_w.get_env_value('C_ROOT_PATH_UNIXLIKE', mandatory, do_raise)

        result = cygpath_w_i.StraightCygpath(root_path_winlike, c_root_path_unixlike)

        return result
    except:
        error_text = "Error trying to create BaseCygpath object"
        if do_raise:
            raise RuntimeError(error_text)
        else:
            show_error(error_text)

    return None


cygpath_ref = RefWrapper(build_cygpath_wrapper())


def get_cygpath_inst():
    result = cygpath_ref.get()
    if result is None:
        result = build_cygpath_wrapper(do_raise=True)
        cygpath_ref.set(result)

    return result


