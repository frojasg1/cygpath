#!/usr/bin/env frojasg1Python3Wrapper.sh

# https://stackoverflow.com/questions/10884268/convert-posix-win-path-in-cygwin-python-w-o-calling-cygpath

import os
import sys
import re

import errno
import ctypes
import enum
import sys


def show_error(message: str):
    print(message, file=sys.stderr)


def get_env_value(env_label: str, mandatory: bool=False, do_raise: bool=False) -> str:
    result = None
    if env_label in os.environ:
        result = os.environ[env_label]
    elif mandatory:
        message = f'Env variable not found: "{env_label}"'
        if do_raise:
            raise RuntimeError(message)
        else:
            show_error(message)

    return result


class BaseCygpath(object):

    def __init__(self):
        pass

    def posix2win(self, path, absolute=False):
        pass

    def win2posix(self, path, absolute=False):
        pass


class ccp_what(enum.Enum):
    posix_to_win_a = 0 # from is char *posix, to is char *win32
    posix_to_win_w = 1 # from is char *posix, to is wchar_t *win32
    win_a_to_posix = 2 # from is char *win32, to is char *posix
    win_w_to_posix = 3 # from is wchar_t *win32, to is char *posix

    convtype_mask = 3

    absolute = 0          # Request absolute path (default).
    relative = 0x100      # Request to keep path relative.
    proc_cygdrive = 0x200 # Request to return /proc/cygdrive path (only with CCP_*_TO_POSIX)


class CygpathError(Exception):
    def __init__(self, errno, msg=""):
        self.errno = errno
        super(Exception, self).__init__(os.strerror(errno))


class Cygpath(BaseCygpath):
    bufsize = 512

    def __init__(self, cygwin1_dll_abspath: str):
        if 'cygwin' not in sys.platform:
            raise SystemError('Not running on cygwin')

#        self._dll = ctypes.cdll.LoadLibrary("cygwin1.dll")
        self._dll = ctypes.cdll.LoadLibrary(cygwin1_dll_abspath)

    def _cygwin_conv_path(self, what, path, size = None):
        if size is None:
            size = self.bufsize
        out = ctypes.create_string_buffer(size)
        ret = self._dll.cygwin_conv_path(what, path, out, size)
        if ret < 0:
            raise CygpathError(ctypes.get_errno())
        return out.value

    def posix2win(self, path, relative=False):
        out = ctypes.create_string_buffer(self.bufsize)
        t = ccp_what.relative.value if relative else ccp_what.absolute.value
        what = ccp_what.posix_to_win_a.value | t
        return self._cygwin_conv_path(what, path)

    def win2posix(self, path, relative=False):
        out = ctypes.create_string_buffer(self.bufsize)
        t = ccp_what.relative.value if relative else ccp_what.absolute.value
        what = ccp_what.win_a_to_posix.value | t
        return self._cygwin_conv_path(what, path)


