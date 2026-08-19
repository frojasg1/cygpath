#!/usr/bin/env frojasg1Python3Wrapper.sh

# base class implementation for being used at unix like and windows like implmementation classes

# https://stackoverflow.com/questions/10884268/convert-posix-win-path-in-cygwin-python-w-o-calling-cygpath

import os
import sys
import re

import errno
import ctypes
import enum
import sys

import re
from cygpath_wrapper import BaseCygpath


def show_error(message: str):
    print(message, file=sys.stderr)


class BaseStraightCygpath(BaseCygpath):

    # absolute windows path for unix: '/'
    # root_path_winlike  ---> 'C:\\cygwin64' for cygwin
    # root_path_winlike  ---> 'C:\\Program Files\\Git' for git bash
    def __init__(self, root_path_winlike: str, c_root_path_unixlike: str):
        self.root_path_winlike = self._remove_multi_slashes_(root_path_winlike)
        self.c_root_path_unixlike = c_root_path_unixlike
        self.unit_pattern_wildcard = '<unit>'

        self.root_path_winlike_pattern_str = f'{self.unit_pattern_wildcard}:'

        self.root_unit_unixlike_regex_pattern, self.root_path_unixlike_pattern_str, self.unix_unit_letter_case_lambda = \
            self._calculate_root_unit_unixlike_regex_pattern_(c_root_path_unixlike)

        self.root_unit_winlike_regex_pattern = re.compile(r'^([a-zA-Z]):$')
        self.abs_path_root_unit_winlike_regex_pattern = re.compile(r'^([a-zA-Z]):\\')

        self.path_separator_pattern = re.compile(r'[/\\]')

        self.unixlike_root_path_winlike = None
        self.root_path_winlike_pattern_str = self.unit_pattern_wildcard + ":"
        self.unixlike_root_path_winlike = self.win2posix(self.root_path_winlike)


    def abspath(self, my_path: str) -> str:
        pass

    def _remove_multi_slashes_(self, my_win_path: str) -> str:
        result = re.sub(r'\\+', r'\\',  my_win_path)
        result = re.sub(r'/+', r'/',  result)

        return result

    # absolute unix path for: windows 'C:\'
    # c_root_path_unixlike  ---> '/cygdrive/c'   for cygwin
    # c_root_path_unixlike  ---> '/c'            for git bash
    def _calculate_root_unit_unixlike_regex_pattern_(self, c_root_path_unixlike: str) -> tuple:
        unit_wildcard = '[a-zA-Z]'

        last_slashes_removal_regex_pattern = re.compile('^(.*[^/])/+$')
        tmp = re.sub( last_slashes_removal_regex_pattern, r'\1', c_root_path_unixlike.strip())

        assert len(tmp) > 0 and tmp[-1].upper() == 'C', \
               f"c_root_path_unixlike, '{c_root_path_unixlike}' does not seem to be a C root path"

        root_path_unixlike_without_unit = tmp[:-1]

        result = re.compile( '^' + root_path_unixlike_without_unit + "(" + unit_wildcard + ")/*$" )

        root_path_unixlike_pattern_str = root_path_unixlike_without_unit \
                                       + self.unit_pattern_wildcard

        unix_unit_letter_case_lambda = lambda x: x
        if tmp[-1] == 'C':
            unix_unit_letter_case_lambda = lambda x: x.upper()
        elif tmp[-1] == 'c':
            unix_unit_letter_case_lambda = lambda x: x.lower()

        return result, root_path_unixlike_pattern_str, unix_unit_letter_case_lambda

    def might_be_win_path(self, my_path: str) -> bool:
        result = True
        if my_path:
            result = '\\' in my_path or bool(self.get_abs_path_win_unit_letter(my_path))

        return result

    def _split_gen_path_(self, path: str) -> list:
        return re.split(self.path_separator_pattern, path)

    def _split_unix_path_(self, path: str) -> list:
        return self._split_gen_path_(path)

    def _split_win_path_(self, path: str) -> list:
        return self._split_gen_path_(path)

    def _is_abs_unix_path_(self, unix_path: str) -> bool:
        result = False
        if bool(unix_path):
            result = unix_path[0] == '/'

        return result

    def _look_for_unix_letter_unit_root_path_(self, unix_path: str) -> tuple:
        pos = -1
        res_pos = -1
        root_path_unit_letter = ''
        if unix_path is not None and not self.might_be_win_path(unix_path):
            pos = len(unix_path)

            # we keep the last unit_root_path visiting the parent directories
            while not root_path_unit_letter:
                res_pos = pos + 1
                candidate_to_unit_root_path = unix_path[:res_pos]
                unix_abs_path = self._obtain_abs_unix_path_(candidate_to_unit_root_path)
                root_path_unit_letter = self.get_unix_unit_letter_if_unit_root_path(unix_abs_path)
                if not root_path_unit_letter:
                    res_pos = pos
                    candidate_to_unit_root_path = unix_path[:res_pos]
                    unix_abs_path = self._obtain_abs_unix_path_(candidate_to_unit_root_path)
                    root_path_unit_letter = self.get_unix_unit_letter_if_unit_root_path(unix_abs_path)
                    if not root_path_unit_letter:
                        res_pos = -1

                if res_pos >= 0 or (pos := unix_path.rfind('/', 0, pos)) < 0:
                    break

        return root_path_unit_letter, res_pos


    def _is_windows_unix_absolute_root_(self, abs_win_candidate: str):
        modified_root_path_winlike = self.root_path_winlike.rstrip('\\/')
        modified_abs_win_candidate = abs_win_candidate.rstrip('\\/')
       
        result = modified_root_path_winlike == abs_win_candidate
        return result

    def _is_win_unit_root_or_is_win_unix_absolute_root_(self, abs_win_candidate: str) -> tuple:
        modified_abs_win_candidate = abs_win_candidate.rstrip('\\/')
        is_absolute_root = self._is_windows_unix_absolute_root_(modified_abs_win_candidate)
        root_path_unit_letter = None
        if not is_absolute_root:        
            root_path_unit_letter = self.get_win_unit_letter_if_unit_root_path(modified_abs_win_candidate)

        return is_absolute_root, root_path_unit_letter

    def _rfind_(self, text: str, chars: str, start: int, end: int) -> int:
        result = -1
        if text:
            pos = min(end - 1, len(text))
            start = max(0, start)

            while pos >= start and result == -1:
                my_char = text[pos]

                if my_char in chars:
                    result = pos

                pos -= 1

        return result

    def _look_for_windows_letter_unit_root_path_or_windows_unix_absolute_root_(self, win_path: str) -> tuple:
        pos = -1
        res_pos = -1
        found = False
        is_absolute_root = False
        root_path_unit_letter = ''
#        show_error(f'    win_path="{win_path}"')
        if win_path is not None and self.might_be_win_path(win_path):
            pos = len(win_path)

#            show_error(f'   ---> [{pos}]')
            # we keep the last unit_root_path visiting the parent directories
            while True:
#                show_error(f'   ---> [{pos}]')
                res_pos = pos + 1
                candidate_to_unit_root_path = win_path[:res_pos]
                abs_win_candidate = self._obtain_abs_win_path_(candidate_to_unit_root_path)
                is_absolute_root, root_path_unit_letter = \
                         self._is_win_unit_root_or_is_win_unix_absolute_root_(abs_win_candidate)

                found = is_absolute_root or root_path_unit_letter
                if not found:
                    res_pos = pos
                    candidate_to_unit_root_path = win_path[:pos]

                    abs_win_candidate = self._obtain_abs_win_path_(candidate_to_unit_root_path)
                    is_absolute_root, root_path_unit_letter = \
                             self._is_win_unit_root_or_is_win_unix_absolute_root_(abs_win_candidate)

                    found = is_absolute_root or root_path_unit_letter

                if not found:
                    res_pos = -1

                if found or (pos := self._rfind_(win_path, '/\\', 0, pos)) < 0:
                     break

#        show_error(f'is_absolute_root={is_absolute_root}, root_path_unit_letter="{root_path_unit_letter}", pos={pos}')
        return is_absolute_root, root_path_unit_letter, res_pos


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

    def _win_path_(self, path: str) -> str:
        result = path
        if path is not None and not self.might_be_win_path(path):
#            show_error(f'_win_path_. path="{path}"')
            if self._is_abs_unix_path_(path):
                root_path_unit_letter, pos = self._look_for_unix_letter_unit_root_path_(path)

                is_absolute_root = False
                if pos < 0 and path.startswith('/'):
                     is_absolute_root = True
                     pos = 0

                result = ''

#                show_error(f'_win_path_. is_absolute_root={is_absolute_root}, root_path_unit_letter="{root_path_unit_letter}", pos={pos}')
                if pos >= 0:
                    assert is_absolute_root or bool(root_path_unit_letter), 'impossible'
    
                    if is_absolute_root:
                        result = self.root_path_winlike + '\\' + '\\'.join(path[pos:].split('/'))
                    elif bool(root_path_unit_letter):
                        result = root_path_unit_letter.upper() + ':\\' + '\\'.join(path[pos:].split('/'))
                    else:
                        raise RuntimeError("Impossible")
                else:
                    result = '\\'.join(path.split('/'))
            else:
                result = '\\'.join(path.split('/'))

        result = self._remove_multi_slashes_(result)

#        show_error(f'_win_path_. result="{result}"')

        return result

    def _get_letter_unit_unix_root_path_(self, root_path_unit_letter: str) -> str:
        unit_letter_with_case = self.unix_unit_letter_case_lambda(root_path_unit_letter)
        result = self.root_path_unixlike_pattern_str.replace(self.unit_pattern_wildcard, unit_letter_with_case)

#        show_error(f' letter_unit_unix_root_path_pattern = "{self.root_path_unixlike_pattern_str}".    ----> result = "{result}"')
        return result

    def _is_letter_unit_win_path_(self, path: str) -> bool:
        return bool(self.get_abs_path_win_unit_letter(path))

    def _unix_path_(self, path: str) -> str:
        result = path
        if path is not None and self.might_be_win_path(path):
            if path[0] == '\\':
                win_pwd = self._obtain_abs_win_path_('.')
                root_path_unit_letter = self.get_abs_path_win_unit_letter(win_pwd)
                assert bool(root_path_unit_letter), f'unit letter not found for pwd: "{win_pwd}"'

                result = self._get_letter_unit_unix_root_path_(root_path_unit_letter) + '/' + '/'.join(self._split_win_path_(path[1:]))
            else:
                if self._is_letter_unit_win_path_(path):
                    is_absolute_root, win_root_path_unit_letter, pos = \
                            self._look_for_windows_letter_unit_root_path_or_windows_unix_absolute_root_(path)

#                    show_error(f'is_absolute_root={is_absolute_root}')

                    assert pos >= 0, f'path was supposed to be a windows absolute path: "{path}"'

                    if is_absolute_root: # absolute unit root path
                        result = '/' + '/'.join(self._split_win_path_(path[pos:]))
                    else:
                        assert bool(win_root_path_unit_letter), \
                               f'Impossible:   is_absolute_root={is_absolute_root}, win_root_path_unit_letter="{win_root_path_unit_letter}", pos={pos}, path="{path}"'
                        result = self._get_letter_unit_unix_root_path_(win_root_path_unit_letter) \
                               + '/' + '/'.join(self._split_win_path_(path[pos:]))
                else:
                    result = '/'.join(self._split_win_path_(path))

        return self._remove_multi_slashes_(result)

    def _get_pattern_group_if_match_(self, my_re_pattern, target_group: int, text: str) -> str:
        re_result = my_re_pattern.search(text)
        result = None
        if re_result is not None:
            result = re_result.group(target_group)

        return result

    def get_unix_unit_letter_if_unit_root_path(self, my_path: str) -> str:
        result = self._get_pattern_group_if_match_(self.root_unit_unixlike_regex_pattern, 1, my_path)

#        show_error(f'   get_unix_unit_letter_if_unit_root_path("{my_path}") ----> "{result}"')

        return result

    def get_abs_path_win_unit_letter(self, my_path: str) -> str:
        return self._get_pattern_group_if_match_(self.abs_path_root_unit_winlike_regex_pattern, 1, my_path)

    def get_win_unit_letter_if_unit_root_path(self, my_path: str) -> str:
        return self._get_pattern_group_if_match_(self.root_unit_winlike_regex_pattern, 1, my_path)

    def _obtain_abs_unix_path_(self, unix_path: str) -> str:
        pass

    def _obtain_abs_win_path_(self, unix_path: str) -> str:
        pass

    def posix2win(self, path: str, absolute: bool=False) -> str:
        pass

    def win2posix(self, path: str, absolute: bool=False) -> str:
        pass

