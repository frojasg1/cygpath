#!/usr/bin/env frojasg1Python3Wrapper.sh

# implementation to be used under windows like Python interpreters

# https://stackoverflow.com/questions/10884268/convert-posix-win-path-in-cygwin-python-w-o-calling-cygpath

import os
import sys
import re

import errno
import ctypes
import enum
import sys

import re
from cygpath_wrapper_base import BaseStraightCygpath


def show_error(message: str):
    print(message, file=sys.stderr)


class StraightCygpath(BaseStraightCygpath):

    # absolute windows path for unix: '/'
    # root_path_winlike  ---> 'C:\\cygwin64' for cygwin
    # root_path_winlike  ---> 'C:\\Program Files\\Git' for git bash
    def __init__(self, root_path_winlike: str, c_root_path_unixlike: str):
        super().__init__(root_path_winlike, c_root_path_unixlike)


    def abspath(self, my_path: str) -> str:
        result = my_path
        if my_path:
            result = os.path.abspath(my_path)

        return result

    def _simplify_abs_unix_path_(self, abs_unix_path: str) -> str:

        unix_dirs = abs_unix_path.split('/')

        result_path = []
        for ind, unix_dir in enumerate(unix_dirs):
            if unix_dir == '..': # go to parent (removing current)
                assert len(result_path) > 0, f'cannot go to parent at root: "{relative_win_path}"'
                del result_path[-1]
            elif len(unix_dir) == 0: # the same dir
                pass
                if ind == 0: # current unit root
                     result_path.append('')
            elif unix_dir == '.': # the same dir (no action)
                pass
            else:
                result_path.append(unix_dir)

        result = '/'.join(result_path)

        if len(result) == 0:
            result = '/'

        return result

    def _split_win_path_(self, path: str) -> list:
        return re.split(self.path_separator_pattern, path)

    def _abs_unix_path_from_relative_unix_path_(self, relative_unix_path: str) -> str:

        unix_dirs = relative_unix_path.split('/')

        win_pwd = self._obtain_abs_win_path_('.')
#        show_error(f'win_pwd="{win_pwd}"')
        is_absolute_root, root_path_unit_letter, pos = \
                self._look_for_windows_letter_unit_root_path_or_windows_unix_absolute_root_(win_pwd)

        if is_absolute_root:
            result = '/' + '/'.join(self._split_win_path_(win_pwd[pos:].lstrip('\\/'))) + '/' + '/'.join(unix_dirs)
        elif root_path_unit_letter:
            result = self._get_letter_unit_unix_root_path_(root_path_unit_letter) + '/' \
                        + '/'.join(self._split_win_path_(win_pwd[pos:].lstrip('\\/'))) + '/' + '/'.join(unix_dirs)
        else:
            raise RuntimeError(f"Impossible: is_absolute_root: {is_absolute_root}, root_path_unit_letter: '{root_path_unit_letter}', relative_unix_path:'{relative_unix_path}'")

        return result

    def _obtain_abs_unix_path_(self, unix_path: str) -> str:
        result = None
#        show_error(f'unix_path -> "{unix_path}"')
        if not self._is_abs_unix_path_(unix_path):
            result = self._abs_unix_path_from_relative_unix_path_(unix_path)
#            show_error(f'abs_unix_path -> "{result}"')
        else:
            result = unix_path

        result = self._simplify_abs_unix_path_(result)
#        show_error(f'standardized_abs_unix_path -> "{result}"')

        return result

    def _obtain_abs_win_path_(self, win_path: str) -> str:
        result = self.abspath(win_path)
#        show_error(f'   self.abspath("{win_path}")  ---> "{result}"')
        return result

    def posix2win(self, path: str, absolute: bool=False) -> str:
        result = path
        if path:
            if not self.might_be_win_path(path):
                result = self._win_path_(path)

            if absolute:
                result = self._obtain_abs_win_path_(result)

        return result

    def win2posix(self, path: str, absolute: bool=False) -> str:
        unix_path = path
        if path:
            if self.might_be_win_path(path):
                unix_path = self._unix_path_(path)

            if absolute:
                unix_path = self._obtain_abs_unix_path_(unix_path)

        result = re.sub(r'\\', '/', unix_path)

#        # replacing unix root
#        unix_root = self.unixlike_root_path_winlike
#        if unix_root is not None and result.startswith(unix_root):
#            result = "/" + result[len(unix_root):].lstrip('\\/')

        return unix_path

