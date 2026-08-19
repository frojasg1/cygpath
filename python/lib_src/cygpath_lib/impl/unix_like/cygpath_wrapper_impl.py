#!/usr/bin/env frojasg1Python3Wrapper.sh

# implementation for being used under unix like Python interpreters

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

    def _simplify_abs_win_path_(self, abs_win_path: str) -> str:

        win_dirs = self._split_win_path_(abs_win_path)

        result_path = []
        for ind, win_dir in enumerate(win_dirs):
            if win_dir == '..': # go to parent (removing current)
                assert len(result_path) > 0, f'cannot go to parent at root: "{abs_win_path}"'
                del result_path[-1]
            elif len(win_dir) == 0: # the same dir
                pass
                if ind == 0: # current unit root
                     result_path.append('')
            elif win_dir == '.': # the same dir (no action)
                pass
            else:
                result_path.append(win_dir)

        result = '\\'.join(result_path)

        if len(result) == 0:
            result = '\\'

        return result

    def _split_gen_path_(self, path: str) -> list:
        return re.split(self.path_separator_pattern, path)

    def _split_unix_path_(self, path: str) -> list:
        return self._split_gen_path_(path)

    def _split_win_path_(self, path: str) -> list:
        return self._split_gen_path_(path)

    def _win_abs_path_from_unix_path_(self, unix_path: str) -> str:

        unix_abs_path = self._obtain_abs_unix_path_(unix_path)
#        show_error(f'win_pwd="{win_pwd}"')
        root_path_unit_letter, pos = \
                self._look_for_unix_letter_unit_root_path_(unix_abs_path)

        if root_path_unit_letter is None and unix_abs_path.startswith('/'):
            is_absolute_root = True
        else:
            is_absolute_root = False

        if is_absolute_root:
            root_path = self.root_path_winlike
            pos = 1
        elif root_path_unit_letter:
            root_path = self._get_letter_unit_win_root_path_(root_path_unit_letter)
        else:
            raise RuntimeError(f"Impossible: is_absolute_root: {is_absolute_root}, root_path_unit_letter: '{root_path_unit_letter}', relative_unix_path:'{relative_unix_path}'")

        result = root_path + '\\' + '\\'.join(self._split_unix_path_(unix_abs_path[pos:].lstrip('\\/')))
        return result

    def _abs_win_path_from_relative_win_path_(self, relative_win_path: str) -> str:
        win_dirs = self._split_win_path_(relative_win_path)

        win_pwd = self._win_abs_path_from_unix_path_('.')

        result = win_pwd + '\\' + '\\'.join(win_dirs)
        return result

    def _is_abs_win_path_(self, win_path: str) -> bool:
        result = False
        if bool(win_path):
            result = win_path.startswith('\\') or self.get_abs_path_win_unit_letter(win_path) is not None

        return result

    def _obtain_abs_unix_path_(self, unix_path: str) -> str:
        return self.abspath(unix_path)

    def _obtain_abs_win_path_(self, win_path: str) -> str:
        result = None
#        show_error(f'unix_path -> "{unix_path}"')
        if not self._is_abs_win_path_(win_path):
            result = self._abs_win_path_from_relative_win_path_(win_path)
#            show_error(f'abs_win_path -> "{result}"')
        else:
            result = win_path

        result = self._simplify_abs_win_path_(result)
#        show_error(f'standardized_abs_unix_path -> "{result}"')

        return result

    def _get_letter_unit_win_root_path_(self, root_path_unit_letter: str) -> str:
        result = self.root_path_winlike_pattern_str.replace(self.unit_pattern_wildcard, root_path_unit_letter)

#        show_error(f' letter_unit_unix_root_path_pattern = "{self.root_path_unixlike_pattern_str}".    ----> result = "{result}"')
        return result

    def posix2win(self, path: str, absolute: bool=False) -> str:
        result = path
        if path:
            if not self.might_be_win_path(path):
                result = self._win_path_(path)

            if absolute:
                result = self._obtain_abs_win_path_(result)

        result = re.sub('/', r'\\', result)
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

        return result

