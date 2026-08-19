
import os
import sys

import sys_path_wrapper as sys_w


def get_env_var(env_var_name: str, default_value: str=None, do_raise: bool=False) -> str:
    assert not do_raise or env_var_name in os.environ, \
        f"env variable not assigned: '{env_var_name}'"

    return os.environ.get(env_var_name, default_value)


def paint(text: str="", my_file=sys.stdout):
    print(text, file=my_file)


def run_function(my_path: str, my_fun, my_fun_name: str):
    result = my_fun(my_path)
    paint(f"      ------> {my_fun_name} : '{result}'")


def run_all_path_functions(my_path: str):
    paint()
    paint()
    paint(f"with: '{my_path}'")



    run_function(my_path, lambda x: sys_w.win2posix(x, absolute=False), 'win2posix(absolute=False)')
    run_function(my_path, lambda x: sys_w.win2posix(x, absolute=True), 'win2posix(absolute=True)')

    run_function(my_path, lambda x: sys_w.posix2win(x, absolute=False), 'posix2win(absolute=False)')
    run_function(my_path, lambda x: sys_w.posix2win(x, absolute=True), 'posix2win(absolute=True)')

    run_function(my_path, lambda x: sys_w.host_to_sys_path(x, absolute=False), 'host_to_sys_path(absolute=False)')
    run_function(my_path, lambda x: sys_w.host_to_sys_path(x, absolute=True), 'host_to_sys_path(absolute=True)')

    run_function(my_path, lambda x: sys_w.sys_to_host_path(x, absolute=False), 'sys_to_host_path(absolute=False)')
    run_function(my_path, lambda x: sys_w.sys_to_host_path(x, absolute=True), 'sys_to_host_path(absolute=True)')

    run_function(my_path, lambda x: sys_w.python_to_sys_path(x, absolute=False), 'python_to_sys_path(absolute=False)')
    run_function(my_path, lambda x: sys_w.python_to_sys_path(x, absolute=True), 'python_to_sys_path(absolute=True)')

    run_function(my_path, lambda x: sys_w.sys_to_python_path(x, absolute=False), 'sys_to_python_path(absolute=False)')
    run_function(my_path, lambda x: sys_w.sys_to_python_path(x, absolute=True), 'sys_to_python_path(absolute=True)')



def main():
    run_all_path_functions('/bin')
    run_all_path_functions('/')
    run_all_path_functions('.')
    run_all_path_functions('scripts/public/frojasg1/python/../bash/../../../private/')
    run_all_path_functions('.bash_profile')
    run_all_path_functions('./.bash_profile')
    run_all_path_functions(r'\bin')
    run_all_path_functions(r'D:\bin')
    run_all_path_functions(r'.\bin')
    run_all_path_functions(r'.\.bash_profile')
    run_all_path_functions('scripts\\public\\frojasg1\\python\\..\\bash\\..\\..\\..\\private\\')

    winlike_unix_root_path = get_env_var("ROOT_PATH_WINLIKE", do_raise=True)
    run_all_path_functions(winlike_unix_root_path)
    run_all_path_functions(winlike_unix_root_path + '\\..\\cygwin64\\')
    
    

if __name__ == "__main__":
    main()

